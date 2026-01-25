
import os
import time
import psycopg2
import pandas as pd
import numpy as np
import torch
import torch.nn as nn
import schedule
import threading
from sklearn.preprocessing import MinMaxScaler
from datetime import datetime, timedelta
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List

# FastAPI App
app = FastAPI()

# --- EXISTING SALES PREDICTION LOGIC (Condensed for brevity, kept functional) ---
# Configuration
DB_HOST = os.getenv("DB_HOST", "postgres_db")
DB_NAME = os.getenv("DB_NAME", "medicine_db")
DB_USER = os.getenv("DB_USER", "postgres")
DB_PASS = os.getenv("DB_PASS", "postgres")
EPOCHS = 100
LOOKBACK = 12

class LSTMModel(nn.Module):
    def __init__(self, input_size=1, hidden_size=50, output_size=1):
        super(LSTMModel, self).__init__()
        self.hidden_size = hidden_size
        self.lstm = nn.LSTM(input_size, hidden_size, batch_first=True)
        self.fc = nn.Linear(hidden_size, output_size)

    def forward(self, x):
        h0 = torch.zeros(1, x.size(0), self.hidden_size).to(x.device)
        c0 = torch.zeros(1, x.size(0), self.hidden_size).to(x.device)
        out, _ = self.lstm(x, (h0, c0))
        out = self.fc(out[:, -1, :])
        return out

def get_db_connection():
    try:
        conn = psycopg2.connect(host=DB_HOST, database=DB_NAME, user=DB_USER, password=DB_PASS)
        return conn
    except Exception as e:
        print(f"Error connecting to DB: {e}")
        return None

def fetch_data(conn):
    try:
        query = """
            SELECT m.id, m.name, p.date
            FROM medicine m
            JOIN prescription_medicine pm ON m.id = pm.medicine_id
            JOIN prescription p ON pm.prescription_id = p.id
            WHERE p.dispensed = true
        """
        return pd.read_sql_query(query, conn)
    except Exception as e:
        print(f"Error fetching data: {e}")
        return pd.DataFrame()

def save_predictions(conn, predictions):
    cursor = conn.cursor()
    try:
        cursor.execute("TRUNCATE TABLE medicine_forecast")
        insert_query = "INSERT INTO medicine_forecast (medicine_id, predicted_sales, forecast_date) VALUES (%s, %s, %s)"
        for pred in predictions:
            cursor.execute(insert_query, (pred['medicine_id'], pred['predicted_sales'], pred['forecast_date']))
        conn.commit()
    except Exception as e:
        print(f"Error saving: {e}")
        conn.rollback()
    finally:
        cursor.close()

def train_and_predict():
    print("Starting SALES prediction task...")
    conn = get_db_connection()
    if not conn: return
    try:
        df = fetch_data(conn)
        if df.empty: return
        df['date'] = pd.to_datetime(df['date'])
        df['month_year'] = df['date'].dt.to_period('M')
        monthly_sales = df.groupby(['id', 'month_year']).size().reset_index(name='count')
        medicine_ids = monthly_sales['id'].unique()
        predictions = []
        next_month = (datetime.now().replace(day=1) + timedelta(days=32)).replace(day=1)
        device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')

        for med_id in medicine_ids:
            med_data = monthly_sales[monthly_sales['id'] == med_id].sort_values('month_year')
            counts = med_data['count'].values.astype(float)
            pred_val = 0
            if len(counts) >= LOOKBACK:
                scaler = MinMaxScaler(feature_range=(0, 1))
                counts_normalized = scaler.fit_transform(counts.reshape(-1, 1))
                X, y = [], []
                for i in range(len(counts_normalized) - LOOKBACK):
                    X.append(counts_normalized[i:(i + LOOKBACK), 0])
                    y.append(counts_normalized[i + LOOKBACK, 0])
                if len(X) > 0:
                    model = LSTMModel().to(device)
                    criterion = nn.MSELoss()
                    optimizer = torch.optim.Adam(model.parameters(), lr=0.01)
                    X_train = torch.tensor(np.array(X), dtype=torch.float32).unsqueeze(2).to(device)
                    y_train = torch.tensor(np.array(y), dtype=torch.float32).unsqueeze(1).to(device)
                    for _ in range(EPOCHS):
                        model.train()
                        loss = criterion(model(X_train), y_train)
                        optimizer.zero_grad()
                        loss.backward()
                        optimizer.step()
                    model.eval()
                    last_seq = torch.tensor(counts_normalized[-LOOKBACK:].reshape(1, LOOKBACK, 1), dtype=torch.float32).to(device)
                    with torch.no_grad():
                        pred_val = scaler.inverse_transform(model(last_seq).cpu().numpy())[0][0]
                else: pred_val = np.mean(counts)
            else: pred_val = np.mean(counts) if len(counts) > 0 else 0
            
            predictions.append({'medicine_id': med_id, 'predicted_sales': int(round(max(0, pred_val))), 'forecast_date': next_month.date()})
        save_predictions(conn, predictions)
        print("Sales predictions saved.")
    except Exception as e:
        print(f"Error in training: {e}")
    finally:
        conn.close()

# --- DISEASE PREDICTION API ---

class SymptomRequest(BaseModel):
    symptoms: List[str]

# Simple rule-based mock for demonstration until a trained model is provided.
# You can load a PyTorch model here if you have one (e.g. disease_model.pth).
def predict_disease_rule_based(symptoms: List[str]):
    symptoms_set = set(s.lower().replace("_", " ") for s in symptoms)
    
    if "vomiting" in symptoms_set or "nausea" in symptoms_set:
        if "fever" in symptoms_set:
            return {"disease": "Gastroenteritis", "confidence": 0.85}
        return {"disease": "Gastritis", "confidence": 0.75}
    if "headache" in symptoms_set:
        if "sensitivity to light" in symptoms_set or "nausea" in symptoms_set:
            return {"disease": "Migraine", "confidence": 0.90}
        return {"disease": "Tension Headache", "confidence": 0.80}
    if "fever" in symptoms_set and "cough" in symptoms_set:
        return {"disease": "Flu (Influenza)", "confidence": 0.88}
    
    return {"disease": "General Viral Infection", "confidence": 0.60}

@app.post("/predict")
def predict_disease(request: SymptomRequest):
    print(f"Received symptoms: {request.symptoms}")
    result = predict_disease_rule_based(request.symptoms)
    return result

@app.get("/health")
def health_check():
    return {"status": "ok"}

# Background Scheduler
def run_scheduler():
    time.sleep(10) # Initial delay
    train_and_predict() # Run once on startup
    schedule.every().day.at("00:00").do(train_and_predict)
    while True:
        schedule.run_pending()
        time.sleep(60)

# Start scheduler in background
threading.Thread(target=run_scheduler, daemon=True).start()

