from flask import Flask, jsonify, request
import requests
import os
import torch
import numpy as np
import pandas as pd
import pickle
from model_utils import LSTMModel, LOOKBACK

app = Flask(__name__)


SPRING_BOOT_API = "http://backend:8080"
PRODUCTS_TO_PREDICT = ["Saizen", "Iliadin", "Lansor", "Glukofen", "Arveles", "Diclomec", "Parol", "Majezik", "Aspirin"]

def load_model_and_scaler(product_name):
    """Eğitilmiş modeli ve scaler'ı yükler."""
    try:
        model = LSTMModel(hidden_layer_size=64)
        model.load_state_dict(torch.load(f'pth_files/model_{product_name}.pth'))
        model.eval() # Tahmin moduna al

        with open(f'pkl_files/scaler_{product_name}.pkl', 'rb') as f:
            scaler = pickle.load(f)
            
        metrics = {}
        try:
            import json
            with open(f'json_files/metrics_{product_name}.json', 'r') as f:
                metrics = json.load(f)
        except Exception:
            print(f"UYARI: {product_name} için metrik dosyası bulunamadı.")

        return model, scaler, metrics
    except Exception as e:
        print(f"HATA: {product_name} modeli/scaler'ı yüklenemedi: {e}")
        return None, None, {}

def make_prediction(product_name, historical_data, model, scaler):
    """Yüklenen model ile bir sonraki ayın satışını tahmin eder."""

    try:
        df = pd.DataFrame(historical_data)
        product_df = df[df['product'] == product_name].sort_values(by='month')
        sales_data = product_df['sales'].values.astype(float).reshape(-1, 1)

        # Tahmin yapmak için **en son** LOOKBACK ayın verisini kullan
        last_lookback_data = sales_data[-LOOKBACK:]

        # Giriş verisini modelin scaler'ı ile normalize et
        scaled_input = scaler.transform(last_lookback_data)

        # PyTorch Tensor'a dönüştür: [batch_size=1, LOOKBACK, 1]
        input_tensor = torch.tensor(scaled_input, dtype=torch.float32).unsqueeze(0)

        # Tahmin yap
        with torch.no_grad():
            # Modelin gizli durumunu sıfırlamaya gerek yok çünkü batch_first=True kullandık
            predicted_scaled_value = model(input_tensor).item()

            # Ters normalizasyon (Gerçek satış miktarına dönüştür)
        predicted_sales = scaler.inverse_transform(np.array([[predicted_scaled_value]]))[0][0]

        # Satış miktarı sıfırdan küçük olamaz
        return max(0, predicted_sales)

    except Exception as e:
        print(f"HATA: {product_name} tahmini yapılırken bir sorun oluştu: {e}")
        return 0 # Hata durumunda sıfır dön

@app.route('/predict', methods=['GET', 'POST'])
def predict_sales():
    """
    Java backend'den geçmiş satış verilerini çeker ve PyTorch ile tahmin yapar.
    """
    historical_data = []

    try:
        # Adım 1: Java backend'den veriyi çek
        print("Java backend'den geçmiş satış verileri çekiliyor...")
        # Lütfen Java endpoint'inizin tüm geçmiş veriyi döndürdüğünden emin olun.
        response = requests.get(f"{SPRING_BOOT_API}/api/data/historical-sales")
        response.raise_for_status()

        historical_data = response.json()
        print(f"Çekilen veri adedi: {len(historical_data)}")

        if historical_data and isinstance(historical_data, list):
            df = pd.DataFrame(historical_data)
            
            
            if 'medicineName' in df.columns:
                df.rename(columns={'medicineName': 'product'}, inplace=True)
            if 'quantity' in df.columns:
                df.rename(columns={'quantity': 'sales'}, inplace=True)
                
            if 'date' in df.columns:
                 # Tarihi datetime'a çevir
                 df['date'] = pd.to_datetime(df['date'])
                 # Ayın ilk gününe yuvarla (YYYY-MM-01)
                 df['month'] = df['date'].dt.to_period('M').dt.to_timestamp().dt.strftime('%Y-%m-%d')
            
            # Aynı ay ve ürün için satışları topla (Aggregation)
            if {'product', 'sales', 'month'}.issubset(df.columns):
                df_agg = df.groupby(['product', 'month'])['sales'].sum().reset_index()
                historical_data = df_agg.to_dict('records')
                print(f"Agrege edilmiş veri adedi: {len(historical_data)}")

    except requests.exceptions.RequestException as e:
        print(f"HATA: Java backend'den veri çekilemedi: {e}. Tahmin servisi çalışmaya devam ediyor.")
        # Gerçek uygulamada bu durumda hata dönmek daha iyidir.
        return jsonify({"error": "Java backend'e ulaşılamadı veya veri çekilemedi."}, 503)

    # Adım 2: Veriyi işle ve ML tahminini yap
    predictions = {}
    for product in PRODUCTS_TO_PREDICT:
        model, scaler, metrics = load_model_and_scaler(product)

        if model and scaler:
            predicted_demand = make_prediction(product, historical_data, model, scaler)

            # RMSE Tabanlı Güven Skoru
            # RMSE, scaler (0-1) üzerinde hesaplandı.
            # Güven Skoru = 1 - RMSE (kabaca). RMSE arttıkça güven azalır.
            rmse = metrics.get("rmse") # Varsayılan 0.15
            confidence_score = max(0.0, 1.0 - rmse)

            predictions[product] = {
                "next_month_demand": int(round(predicted_demand)),
                "confidence": confidence_score
            }
        else:
            predictions[product] = {
                "next_month_demand": -1,
                "confidence": 0.0,
                "note": "Model yüklenemediği için tahmin yapılamadı."
            }


    # Adım 3: Tahmin sonuçlarını döndür
    return jsonify(predictions)

if __name__ == '__main__':
    # Flask, Docker network içinde 5000 portunda çalışır
    app.run(host='0.0.0.0', port=5000)