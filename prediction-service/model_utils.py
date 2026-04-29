import pandas as pd
import numpy as np
import torch
import torch.nn as nn
from sklearn.preprocessing import MinMaxScaler


LOOKBACK = 12  # Son 12 ayın verisini kullanarak sonraki ayı tahmin et.

class LSTMModel(nn.Module):
    """
    Tek Katmanlı LSTM Modeli
    """
    def __init__(self, input_size=1, hidden_layer_size=50, output_size=1):
        super().__init__()
        self.hidden_layer_size = hidden_layer_size

        # input_size: Satış miktarı (tek özellik)
        self.lstm = nn.LSTM(input_size, hidden_layer_size, batch_first=True)

        # Çıktı katmanı
        self.linear = nn.Linear(hidden_layer_size, output_size)

    def forward(self, input_seq):
        # input_seq şekli: [batch_size, seq_len (LOOKBACK), input_size]

        # Gizli durumu ve hücre durumunu başlat (batch_size=1 varsayılır)
        h0 = torch.zeros(1, input_seq.size(0), self.hidden_layer_size).to(input_seq.device)
        c0 = torch.zeros(1, input_seq.size(0), self.hidden_layer_size).to(input_seq.device)

        # lstm_out şekli: [batch_size, seq_len, hidden_layer_size]
        lstm_out, _ = self.lstm(input_seq, (h0, c0))

        # Sadece son zaman adımının çıktısını kullan: lstm_out[:, -1, :]
        predictions = self.linear(lstm_out[:, -1, :])

        return predictions

def prepare_data_for_lstm(historical_data, product_name):
    """
    Ham veriyi alır, ilgili ürünü seçer, normalize eder ve LSTM için
    kayan pencere (sliding window) formatına dönüştürür.
    """
    df = pd.DataFrame(historical_data)

    # Tarih formatı YYYY-MM-DD olduğu için direkt sıralama yapar
    product_df = df[df['product'] == product_name].sort_values(by='month')

    # Satış verilerini numpy dizisine dönüştür
    sales_data = product_df['sales'].values.astype(float).reshape(-1, 1)

    if len(sales_data) == 0:
        return torch.tensor([]), torch.tensor([]), None

    # Normalizasyon
    scaler = MinMaxScaler(feature_range=(0, 1))
    scaled_data = scaler.fit_transform(sales_data)

    # Kayan pencere ile X (özellikler) ve Y (hedef) setlerini oluştur
    X, Y = [], []
    for i in range(len(scaled_data) - LOOKBACK):
        # LOOKBACK ayın verisi (X)
        X.append(scaled_data[i:(i + LOOKBACK), 0])
        # Bir sonraki ayın verisi (Y)
        Y.append(scaled_data[i + LOOKBACK, 0])

    # PyTorch Tensor'a dönüştür (Şekil: [örnek_sayısı, LOOKBACK, özellik_sayısı=1])
    X = torch.tensor(np.array(X), dtype=torch.float32).unsqueeze(-1)
    Y = torch.tensor(np.array(Y), dtype=torch.float32).unsqueeze(-1)

    return X, Y, scaler