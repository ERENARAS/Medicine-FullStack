import torch
import torch.nn as nn
import pickle
import torch
import torch.nn as nn
import pickle
import random
import os
import pandas as pd
import numpy as np
from model_utils import LSTMModel, prepare_data_for_lstm, LOOKBACK


def train_and_save_model(product_name, historical_data):
    """Belirtilen ürün için modeli eğitir ve kaydeder."""

    X, Y, scaler = prepare_data_for_lstm(historical_data, product_name)

    if scaler is None:
        print(f"UYARI: {product_name} için hiç veri bulunamadı. Eğitim atlanıyor.")
        return

    # Veri kontrolü
    if len(X) == 0:
        print(f"UYARI: {product_name} için yeterli veri ({LOOKBACK} aydan az) bulunamadı. Eğitim atlanıyor.")
        return

    # Model, Optimizer ve Loss tanımı
    model = LSTMModel(hidden_layer_size=64) # Gizli katman boyutunu 64'e çıkarabiliriz
    loss_function = nn.MSELoss()
    optimizer = torch.optim.Adam(model.parameters(), lr=0.001)

    epochs = 200 # Daha iyi sonuçlar için epoch sayısını artırın
    batch_size = 1 # Zaman serisi için genellikle batch_size=1 kullanılır

    print(f"\n--- {product_name} Modeli Eğitiliyor (Epochs: {epochs}) ---")
    for epoch in range(epochs):
        total_loss = 0

        # Batch yerine basitçe her örneği tek tek işleyelim
        for i in range(len(X)):
            seq = X[i] # Şekil: [LOOKBACK, 1]
            label = Y[i] # Şekil: [1, 1]

            optimizer.zero_grad()

            # Model, [batch_size=1, LOOKBACK, 1] şeklinde giriş bekler
            y_pred = model(seq.unsqueeze(0))

            loss = loss_function(y_pred, label)
            loss.backward()
            optimizer.step()
            total_loss += loss.item()

        if (epoch + 1) % 50 == 0:
            print(f"Epoch {epoch+1}/{epochs}, Loss: {total_loss / len(X):.6f}")

    # Modeli kaydet
    model_path = f'pth_files/model_{product_name}.pth'
    torch.save(model.state_dict(), model_path)
    print(f"✅ {product_name} modeli '{model_path}' olarak kaydedildi.")

    # Scaler'ı kaydet
    scaler_path = f'pkl_files/scaler_{product_name}.pkl'
    with open(scaler_path, 'wb') as f:
        pickle.dump(scaler, f)
    print(f"✅ {product_name} scaler '{scaler_path}' olarak kaydedildi.")

    # RMSE Hesapla ve Kaydet
    model.eval()
    with torch.no_grad():
        predictions = []
        actuals = []
        for i in range(len(X)):
            seq = X[i].unsqueeze(0)
            y_pred = model(seq)
            predictions.append(y_pred.item())
            actuals.append(Y[i].item())
        
        mse = np.mean((np.array(predictions) - np.array(actuals))**2)
        rmse = np.sqrt(mse)
        print(f"📉 {product_name} Eğitim RMSE: {rmse:.4f}")
        
        import json
        metrics_path = f'json_files/metrics_{product_name}.json'
        with open(metrics_path, 'w') as f:
            json.dump({"rmse": rmse}, f)
        print(f"✅ {product_name} metrikleri '{metrics_path}' olarak kaydedildi.")


if __name__ == '__main__':
    import requests
    try:
        # Backend 8080 portunda çalışıyorsa ve bu script host makineden çalıştırılıyorsa localhost kullanın.
        # Docker içinden çalıştırılıyorsa 'http://backend:8080/api/data/historical-sales' olmalıdır.
        response = requests.get("http://localhost:8080/api/data/historical-sales")
        response.raise_for_status()
        all_historical_data = response.json()
        print("Gerçek veriler başarıyla çekildi.")

        if not all_historical_data or len(all_historical_data) == 0:
            print("UYARI: Hiç veri bulunamadı. Lütfen veritabanını kontrol edin veya önce seed scriptini çalıştırın.")
            exit(0)

        # Backend: medicineName, quantity, date
        # Model: product, sales, month
        if all_historical_data and isinstance(all_historical_data, list) and len(all_historical_data) > 0:
            # Sadece format farklıysa dönüşüm yap (medicineName kontrolü)
            if 'medicineName' in all_historical_data[0]:
                print("Veri formatı dönüştürülüyor...")
                df = pd.DataFrame(all_historical_data)
                
                if 'medicineName' in df.columns:
                    df.rename(columns={'medicineName': 'product'}, inplace=True)
                if 'quantity' in df.columns:
                    df.rename(columns={'quantity': 'sales'}, inplace=True)
                
                if 'date' in df.columns:
                    df['date'] = pd.to_datetime(df['date'])
                    # Ayın ilk gününe yuvarla string formatında (YYYY-MM-01)
                    df['month'] = df['date'].dt.to_period('M').dt.to_timestamp().dt.strftime('%Y-%m-%d')
                
                # Agregasyon (Aynı ay ve ürün satırlarını birleştir)
                if {'product', 'sales', 'month'}.issubset(df.columns):
                    df_agg = df.groupby(['product', 'month'])['sales'].sum().reset_index()
                    all_historical_data = df_agg.to_dict('records')
                    print(f"Dönüştürülen veri sayısı: {len(all_historical_data)}")

    except Exception as e:
        print(f"Veri çekme hatası: {e}.")
        exit(1)
       

    products_to_train = ["Saizen", "Iliadin", "Lansor", "Glukofen", "Arveles", "Diclomec", "Parol", "Majezik", "Aspirin"]

    for directory in ["pth_files", "pkl_files", "json_files"]:
        if not os.path.exists(directory):
            os.makedirs(directory)

    for product in products_to_train:
        train_and_save_model(product, all_historical_data)