FROM python:3.10-slim

WORKDIR /app

# Gerekli kütüphaneleri kur
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Uygulama kodunu kopyala
COPY prediction_service.py model_utils.py train_models.py run_batch_predictions.py ./

# *** ÖNEMLİ: EĞİTİLMİŞ MODEL VE SCALER DOSYALARINI KOPYALAYIN ***
# Eğer train_models.py'yi yerelde çalıştırıp model dosyalarını oluşturduysanız:
COPY pth_files/ pth_files/
COPY pkl_files/ pkl_files/
COPY json_files/ json_files/

# Servisin portu (sadece Docker network içinde açılacak)
EXPOSE 5000

# Uygulamayı başlat
CMD ["python", "prediction_service.py"]