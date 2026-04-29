# 💊 Medicine ATM: PostgreSQL'den MySQL'e Migrasyon Raporu

Bu proje, staj kabul süreci kapsamında verilen **"Veritabanını PostgreSQL'den MySQL'e taşıma"** görevini başarıyla tamamlamış halini içermektedir. Bu dökümanda, migrasyon sürecinde yapılan teknik değişiklikler ve projenin mimari yapısının bu geçişi ne kadar kolaylaştırdığı detaylandırılmıştır.

---

## 🚀 Migrasyon Sürecinde Neler Değişti?

Veritabanı değişikliği, sistemin tüm katmanlarında (Backend, Veritabanı, ML Servisi ve Orkestrasyon) koordineli bir güncelleme gerektirmiştir:

### 1. Veritabanı ve Docker Seviyesi
*   **Docker Image:** `postgres:15-alpine` imajı `mysql:8.0` ile değiştirildi.
*   **Port Yapılandırması:** Çakışmaları önlemek adına dış port `3307`, iç port `3306` olarak yapılandırıldı.
*   **Seed Data:** PostgreSQL'e özgü olan `ON CONFLICT DO NOTHING` ifadeleri, MySQL uyumlu `INSERT IGNORE INTO` yapısına dönüştürüldü.
*   **Volume Yönetimi:** PostgreSQL veri klasörleri temizlendi ve MySQL için yeni bir `mysql_data` volume yapısı oluşturuldu.

### 2. Backend (Spring Boot) Seviyesi
*   **Bağımlılıklar:** `pom.xml` dosyasındaki PostgreSQL sürücüsü kaldırılarak `mysql-connector-j` eklendi.
*   **Yapılandırma:** `application.properties` içindeki `datasource.url`, `driver-class-name` ve `hibernate.dialect` (MySQL8Dialect) ayarları güncellendi.
*   **Refactoring:** Altyapı katmanındaki `PostgresPrescriptionRepository` ve `PostgresUserRepository` sınıfları, isimlendirme standartlarına uygun olarak `MySql...` şeklinde yeniden adlandırıldı.

### 3. Python ML Servisi Seviyesi
*   **Bağımlılıklar:** `psycopg2` yerine `pymysql` ve `sqlalchemy` eklendi. Build süresini optimize etmek için PyTorch'un sadece CPU versiyonu kullanılacak şekilde `requirements.txt` revize edildi.
*   **SQL Güncellemeleri:** PostgreSQL'e özgü `gen_random_uuid()` fonksiyonu, MySQL 8.0'ın `UUID_TO_BIN(UUID())` yapısı ile değiştirilerek veri tutarlılığı sağlandı.

### 4. Güvenlik ve Konfigürasyon Yönetimi
*   **Hassas Veri Gizleme:** Veritabanı şifreleri ve bağlantı URL'leri artık kod içerisinde (hardcoded) tutulmamaktadır.
*   **Environment Variables:** Tüm konfigürasyonlar `${VARIABLE:-default}` yapısı ile çevre değişkenlerine bağlanmıştır.
*   **`.env` Desteği:** Hassas veriler için `.gitignore` tarafından korunan bir `.env` dosyası yapısı kurulmuştur. Bu, profesyonel projelerde uygulanan bir güvenlik standartıdır.

---

## 💡 Geçiş Neden Bu Kadar "Kolay" Oldu?

Bu migrasyonun minimum kod değişikliği ile ve sistemin iş mantığına (Business Logic) dokunmadan tamamlanabilmesinin temel sebepleri projenin **Temiz Mimari (Clean Architecture)** prensiplerine dayanmasıdır:

### 🛡️ 1. Interface (Arayüz) Kullanımı ve Soyutlama
Sistem, veritabanı işlemlerini doğrudan sınıflar üzerinden değil, **`UserRepository`** ve **`PrescriptionRepository`** gibi domain katmanındaki arayüzler üzerinden yürütür. 
*   Servis katmanı (Service Layer) sadece bu arayüzlere bağımlıdır.
*   Veritabanı değiştiğinde sadece bu arayüzleri implemente eden altyapı sınıfları (Infrastructure) güncellenmiş, servislerde tek bir satır dahi değiştirilmemiştir.

### 🏗️ 2. Spring Data JPA (Hibernate) Abstraction
Spring Data JPA'nın sunduğu soyutlama katmanı sayesinde, kullanılan sorguların çoğu (save, findById, findAll vb.) veritabanından bağımsızdır. Hibernate Dialect ayarının MySQL'e çekilmesiyle, JPA otomatik olarak MySQL uyumlu sorgular üretmeye başlamıştır.

### 📦 3. Containerization (Docker)
Docker kullanımı sayesinde, yerel makinede PostgreSQL veya MySQL kurulu olmasına bakılmaksızın, sadece bir `environment` değişikliği ile tüm ortam saniyeler içinde yeni veritabanına hazır hale getirilmiştir.

---

## 👩‍💻 Kullanıcı Giriş Bilgileri (Test İçin)

Sistemdeki rollere göre test girişleri:

| Rol | E-posta Uzantısı |
| :--- | :--- |
| **Doktor** | `@dr.medicine` |
| **Hasta** | `@pt.medicine` |
| **Personel** | `@ph.medicine` |

---

### Proje Durumu: ✅ MySQL Migrasyonu Başarıyla Tamamlandı.
