from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import joblib
import pandas as pd
import numpy as np

# API Uygulamasını Başlat
app = FastAPI(title="Medicine AI Decision Support System")

# --- 1. DOSYALARI VE MODELİ YÜKLE ---
print("⏳ Sistem başlatılıyor ve dosyalar yükleniyor...")

try:
    # Eğitilmiş Modeli Yükle
    model = joblib.load('medicine_model.pkl')
    symptom_list = joblib.load('symptom_list.pkl')
    label_encoder = joblib.load('label_encoder.pkl')  # XGBoost için label encoder

    # Ekstra Bilgi CSV'lerini Yükle (Dosya adlarının birebir aynı olduğundan emin ol!)
    # Kaggle'dan indirdiğin dosya adlarına göre burayı düzenle:
    desc_df = pd.read_csv('Disease_Description.csv')
    doctor_df = pd.read_csv('Doctor_Versus_Disease.csv', encoding='latin1')  # Bazen encoding hatası olabilir

    print("✅ Tüm sistemler hazır!")

except Exception as e:
    print(f"❌ Kritik Hata: Dosyalar yüklenemedi. {e}")
    # Hata olsa bile API çalışsın ama hata versin
    model = None
    label_encoder = None


# --- 2. İSTEK FORMATI (JSON) ---
class SymptomRequest(BaseModel):
    symptoms: list[str]  # Örn: ["itching", "skin_rash"]


# --- 2.5. SEMPTOM LISTESİ ENDPOINT'İ ---
@app.get("/symptoms")
def get_symptoms():
    """Modelin tanıdığı tüm geçerli semptomları döndürür."""
    if not symptom_list:
        raise HTTPException(status_code=500, detail="Semptom listesi yüklenemedi.")
    return {"symptoms": symptom_list}


# --- 3. TAHMİN ENDPOINT'İ ---
@app.post("/predict")
def predict_disease(request: SymptomRequest):
    if not model:
        raise HTTPException(status_code=500, detail="Model yüklenemedi.")

    # A. Kullanıcıdan gelen semptomları modele uygun (0 ve 1) hale getir
    input_vector = np.zeros(len(symptom_list))

    matched_symptoms = []

    for symptom in request.symptoms:
        # Gelen semptom bizim listemizde var mı? (Boşlukları temizle)
        clean_symptom = symptom.strip().lower()  # Küçük harfe çevirip arayalım

        # Tam eşleşme ara (Model eğitimiyle aynı mantıkta)
        # Not: Burada fuzzy matching (benzerlik) yapılabilir ama şimdilik tam eşleşme yapıyoruz.
        if clean_symptom in symptom_list:
            index = symptom_list.index(clean_symptom)
            input_vector[index] = 1
            matched_symptoms.append(clean_symptom)

    if len(matched_symptoms) == 0:
        return {
            "error": "Semptomlar veritabanındaki terimlerle eşleşmedi.",
            "valid_symptoms_example": symptom_list[:5],  # Örnek göster
            "received_symptoms": request.symptoms
        }

    # B. Tahmin Yap (XGBoost with confidence)
    prediction_raw = model.predict([input_vector])[0]
    prediction_proba = model.predict_proba([input_vector])[0]  # Olasılık dağılımı
    
    # Modelin doğrudan string mi yoksa encoded value mu döndürdüğünü kontrol et
    if isinstance(prediction_raw, (int, np.integer)):
        # Sayısal tahmin - Label encoder ile hastalık ismine çevir
        prediction = label_encoder.inverse_transform([prediction_raw])[0]
        confidence = float(prediction_proba[prediction_raw])
    else:
        # Doğrudan string döndürüyor
        prediction = str(prediction_raw)
        # Hastalığın index'ini bul
        try:
            disease_index = list(label_encoder.classes_).index(prediction)
            confidence = float(prediction_proba[disease_index])
        except (ValueError, IndexError):
            # En yüksek olasılığı al
            confidence = float(np.max(prediction_proba))

    # C. Ek Bilgileri Getir (Açıklama ve Doktor)
    description = "Açıklama bulunamadı."
    specialist = "Genel Pratisyen (General Physician)"

    # Açıklamayı bul
    desc_row = desc_df[desc_df['Disease'] == prediction]
    if not desc_row.empty:
        description = desc_row.iloc[0]['Description']

    # Doktoru bul (Dosyadaki sütun adlarına dikkat: 'Drug Reaction', 'Allergist' gibi olabilir)
    # Genelde ilk sütun Hastalık, ikinci sütun Doktordur.
    doc_row = doctor_df[doctor_df.iloc[:, 0] == prediction]
    if not doc_row.empty:
        specialist = doc_row.iloc[:, 1].values[0]

    # D. Sonucu Döndür
    return {
        "diagnosis": prediction,
        "confidence_score": round(confidence, 4),  # Gerçek güven skoru (0-1 arası)
        "description": description,
        "recommended_specialist": specialist,
        "matched_symptoms": matched_symptoms
    }

