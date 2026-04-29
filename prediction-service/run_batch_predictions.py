import os
import pandas as pd
import numpy as np
import torch
import pymysql
from sqlalchemy import create_engine, text
from datetime import datetime, date
from dateutil.relativedelta import relativedelta
from train_models import train_and_save_model
from prediction_service import load_model_and_scaler, make_prediction

# Database Configuration - MySQL
DB_URL = os.getenv("DATABASE_URL", "mysql+pymysql://medicine_user:medicine_pass@db:3306/medicine_db")

def get_db_connection():
    return create_engine(DB_URL)

def fetch_all_medicines(engine):
    """Fetch distinct medicine names from the database."""
    query = text("SELECT DISTINCT name FROM medicine")
    with engine.connect() as conn:
        result = conn.execute(query)
        medicines = [row[0] for row in result]
    return medicines

def fetch_historical_sales(engine):
    """Fetch all historical sales data."""
    query = text("""
        SELECT m.name as product, p.date, 1 as sales 
        FROM prescription p
        JOIN prescription_medicine pm ON p.id = pm.prescription_id
        JOIN medicine m ON pm.medicine_id = m.id
    """)
    
    with engine.connect() as conn:
        result = conn.execute(query)
        data = [{"product": row[0], "date": row[1], "sales": row[2]} for row in result]
    
    return data

def save_prediction(engine, medicine_name, predicted_value, confidence, target_date):
    """Save prediction to database."""
    # Prediction table: id (binary(16) in MySQL if using UUID), medicine_name, predicted_value, confidence_score, prediction_date
    
    check_query = text("""
        SELECT id FROM predictions 
        WHERE medicine_name = :name AND prediction_date = :date
    """)
    
    update_query = text("""
        UPDATE predictions 
        SET predicted_value = :val, confidence_score = :conf 
        WHERE id = :id
    """)
    
    # MySQL 8.0 UUID to Binary(16) conversion for JPA compatibility if needed, 
    # or just use UUID() if the column is varchar/text. 
    # Since JPA uses 'binary(16)', we use UUID_TO_BIN(UUID()).
    insert_query = text("""
        INSERT INTO predictions (id, medicine_name, predicted_value, confidence_score, prediction_date)
        VALUES (UUID_TO_BIN(UUID()), :name, :val, :conf, :date)
    """)
    
    with engine.connect() as conn:
        existing = conn.execute(check_query, {"name": medicine_name, "date": target_date}).fetchone()
        
        if existing:
            conn.execute(update_query, {"val": predicted_value, "conf": confidence, "id": existing[0]})
        else:
            conn.execute(insert_query, {"name": medicine_name, "val": predicted_value, "conf": confidence, "date": target_date})
        
        conn.commit()

def run_batch():
    print("Starting Batch Prediction Job...")
    engine = get_db_connection()
    
    # 1. Get Medicines
    medicines = fetch_all_medicines(engine)
    print(f"Found {len(medicines)} medicines.")
    
    # 2. Get Data
    raw_data = fetch_historical_sales(engine)
    print(f"Fetched {len(raw_data)} sales records.")
    
    if not raw_data:
        print("No sales data found.")
        return

    # Process Data (Aggregate like in prediction_service)
    df = pd.DataFrame(raw_data)
    df['date'] = pd.to_datetime(df['date'])
    df['month'] = df['date'].dt.to_period('M').dt.to_timestamp().dt.strftime('%Y-%m-%d')
    df_agg = df.groupby(['product', 'month'])['sales'].count().reset_index() # Count occurrences as sales
    historical_data = df_agg.to_dict('records')
    
    # Target Date (Next Month)
    today = date.today()
    next_month = today + relativedelta(months=1)
    target_date = next_month.replace(day=1)
    
    print(f"Target Prediction Date: {target_date}")

    for medicine in medicines:
        # Train & Predict
        # We can reuse train_and_save_model logic or just load if exists?
        # Better to Retrain fresh for batch job?
        # Let's call train_and_save_model to ensure fresh model
        
        # Filter for specific medicine to avoid noise if train function filters? 
        # train_and_save_model takes all data and filters inside prepare_data.
        
        # Suppress prints for loop
        # train_and_save_model(medicine, historical_data) 
        # Actually `train_and_save_model` trains and saves to disk. 
        
        # Optimization: Only train if we want to update models. For now, yes.
        # But `train_models.py` is designed for a list. We can refactor or just call it.
        # Let's import the specific function.
        
        try:
             # Train
             train_and_save_model(medicine, historical_data)
             
             # Load & Predict
             model, scaler, metrics = load_model_and_scaler(medicine)
             
             if model and scaler:
                 pred = make_prediction(medicine, historical_data, model, scaler)
                 
                 rmse = metrics.get("rmse", 0.5)
                 confidence = max(0.0, 1.0 - rmse)
                 
                 save_prediction(engine, medicine, int(pred), confidence, target_date)
                 print(f"Saved prediction for {medicine}: {int(pred)} (Conf: {confidence:.2f})")
             else:
                 # Default if no data/model
                 save_prediction(engine, medicine, 0, 0.0, target_date)
                 
        except Exception as e:
            print(f"Error processing {medicine}: {e}")

    print("Batch Job Completed.")

if __name__ == "__main__":
    run_batch()
