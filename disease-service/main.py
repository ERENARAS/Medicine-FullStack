import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score
import joblib

def train_model():
    print("⏳ Veriseti yükleniyor...")
    try:
        # 1. Load Dataset
        df = pd.read_csv('dataset.csv')
        
        # 2. Extract Symptoms (Features)
        # The dataset format is: Disease, Symptom_1, Symptom_2, ..., Symptom_17
        # We need to collect all unique symptoms from Symptom_1 to Symptom_17 columns ONLY.
        # We must EXCLUDE the 'Disease' column from potential symptoms.
        
        feature_columns = df.columns[1:] # Skip the first column (Disease)
        
        unique_symptoms = set()
        for col in feature_columns:
            # Get unique values from this column, convert to string, strip whitespace
            symptoms_in_col = df[col].dropna().unique()
            for s in symptoms_in_col:
                clean_symptom = str(s).strip().lower() # Enforce lowercase
                if clean_symptom:
                    unique_symptoms.add(clean_symptom)
        
        symptom_list = sorted(list(unique_symptoms))
        print(f"✅ Toplam {len(symptom_list)} farklı semptom tespit edildi.")
        
        # 3. Prepare Training Data 
        # We need to convert the loose symptom strings into a One-Hot encoded vector manually
        
        encoded_data = []
        labels = []
        
        for index, row in df.iterrows():
            # Get disease label
            disease = row['Disease']
            labels.append(disease)
            
            # Get symptoms for this row
            # Filter out NaNs and strip whitespace and lowercase
            row_symptoms = [
                str(val).strip().lower()
                for val in row[feature_columns].values 
                if pd.notna(val)
            ]
            
            # Create binary vector
            # 1 if symptom is present in this patient, 0 otherwise
            vector = [1 if sym in row_symptoms else 0 for sym in symptom_list]
            encoded_data.append(vector)
            
        X = np.array(encoded_data)
        y = np.array(labels)
        
        print("✅ Veri sayısallaştırıldı.")
        print(f"   X shape: {X.shape}")
        print(f"   y shape: {y.shape}")

        # 4. Train Model
        X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
        
        clf = RandomForestClassifier(n_estimators=100, random_state=42)
        clf.fit(X_train, y_train)
        
        # 5. Evaluate
        preds = clf.predict(X_test)
        acc = accuracy_score(y_test, preds)
        print(f"🎯 Model Doğruluk Oranı: %{acc * 100:.2f}")
        
        # 6. Save Artifacts
        joblib.dump(clf, 'medicine_model.pkl')
        joblib.dump(symptom_list, 'symptom_list.pkl')
        print("💾 Model ve semptom listesi kaydedildi.")

    except Exception as e:
        print(f"❌ Hata oluştu: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    train_model()