
import os
import time
import psycopg2
import pandas as pd
import numpy as np
import torch
import torch.nn as nn
import schedule
from sklearn.preprocessing import MinMaxScaler
from datetime import datetime, timedelta

# Configuration
DB_HOST = os.getenv("DB_HOST", "postgres_db")
DB_NAME = os.getenv("DB_NAME", "medicine_db")
DB_USER = os.getenv("DB_USER", "postgres")
DB_PASS = os.getenv("DB_PASS", "postgres")
EPOCHS = 100
LOOKBACK = 12 # Number of past months to use for prediction

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
        conn = psycopg2.connect(
            host=DB_HOST,
            database=DB_NAME,
            user=DB_USER,
            password=DB_PASS
        )
        return conn
    except Exception as e:
        print(f"Error connecting to DB: {e}")
        return None

def fetch_data(conn):
    query = """
        SELECT m.id, m.name, p.date
        FROM medicine m
        JOIN prescription_medicine pm ON m.id = pm.medicine_id
        JOIN prescription p ON pm.prescription_id = p.id
        WHERE p.dispensed = true
    """
    df = pd.read_sql_query(query, conn)
    return df

def train_and_predict():
    print("Starting prediction task...")
    conn = get_db_connection()
    if not conn:
        print("Database connection failed. Retrying in 1 minute.")
        return

    try:
        df = fetch_data(conn)
        if df.empty:
            print("No data found for training.")
            return

        df['date'] = pd.to_datetime(df['date'])
        df['month_year'] = df['date'].dt.to_period('M')

        # Group by medicine and month to get sales count
        monthly_sales = df.groupby(['id', 'month_year']).size().reset_index(name='count')
        
        # Get unique medicines
        medicine_ids = monthly_sales['id'].unique()
        
        predictions = []
        today = datetime.now()
        next_month = today.replace(day=1) + timedelta(days=32)
        next_month = next_month.replace(day=1) # First day of next month
        
        device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')

        for med_id in medicine_ids:
            med_data = monthly_sales[monthly_sales['id'] == med_id].sort_values('month_year')
            counts = med_data['count'].values.astype(float)
            
            # We need at least LOOKBACK + 1 data points to train meaningfully (or just LOOKBACK to predict next)
            # If simplistic, just ensure we have enough points, otherwise assume average or 0
            if len(counts) < LOOKBACK:
                # Not enough data, use average as fallback
                pred_val = np.mean(counts) if len(counts) > 0 else 0
            else:
                # Preprocessing
                scaler = MinMaxScaler(feature_range=(0, 1))
                counts_normalized = scaler.fit_transform(counts.reshape(-1, 1))

                # Prepare sequences
                X, y = [], []
                for i in range(len(counts_normalized) - LOOKBACK):
                    X.append(counts_normalized[i:(i + LOOKBACK), 0])
                    y.append(counts_normalized[i + LOOKBACK, 0])
                
                if len(X) > 0:
                    X_train = torch.tensor(np.array(X), dtype=torch.float32).unsqueeze(2).to(device)
                    y_train = torch.tensor(np.array(y), dtype=torch.float32).unsqueeze(1).to(device)
                    
                    # Train Model
                    model = LSTMModel().to(device)
                    criterion = nn.MSELoss()
                    optimizer = torch.optim.Adam(model.parameters(), lr=0.01)
                    
                    for epoch in range(EPOCHS):
                        model.train()
                        outputs = model(X_train)
                        loss = criterion(outputs, y_train)
                        optimizer.zero_grad()
                        loss.backward()
                        optimizer.step()
                    
                    # Predict next value
                    model.eval()
                    last_sequence = counts_normalized[-LOOKBACK:].reshape(1, LOOKBACK, 1)
                    last_sequence_tensor = torch.tensor(last_sequence, dtype=torch.float32).to(device)
                    
                    with torch.no_grad():
                        prediction_normalized = model(last_sequence_tensor).cpu().numpy()
                        prediction = scaler.inverse_transform(prediction_normalized)
                        pred_val = prediction[0][0]
                else:
                    # If we have exactly LOOKBACK items, we can't train "past" vs "next" traditionally without splitting, 
                    # but we can try to overfit or just use last mean. 
                    # For simplicity, if we have enough for one sequence, we predict.
                    # If not, use mean.
                     pred_val = np.mean(counts)

            predictions.append({
                'medicine_id': med_id,
                'predicted_sales': int(round(max(0, pred_val))),
                'forecast_date': next_month.date()
            })

        # Save to DB
        save_predictions(conn, predictions)
        print("Predictions saved successfully.")

    except Exception as e:
        print(f"Error during training/prediction: {e}")
    finally:
        conn.close()

def save_predictions(conn, predictions):
    cursor = conn.cursor()
    try:
        # Clear old forecasts for simplicity or we could Update.
        # User wants "next month" forecast.
        cursor.execute("TRUNCATE TABLE medicine_forecast")
        
        insert_query = """
            INSERT INTO medicine_forecast (medicine_id, predicted_sales, forecast_date)
            VALUES (%s, %s, %s)
        """
        for pred in predictions:
            cursor.execute(insert_query, (pred['medicine_id'], pred['predicted_sales'], pred['forecast_date']))
        
        conn.commit()
    except Exception as e:
        print(f"Error saving predictions: {e}")
        conn.rollback()
    finally:
        cursor.close()

if __name__ == "__main__":
    # Delay to ensure DB is up when container starts
    time.sleep(10) 
    
    # Run once immediately
    train_and_predict()
    
    # Schedule to run daily (or as needed)
    schedule.every().day.at("00:00").do(train_and_predict)
    
    while True:
        schedule.run_pending()
        time.sleep(60)
