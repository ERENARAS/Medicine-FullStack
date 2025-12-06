## 💊 Medicine ATM Full Stack System: Kurulum ve Çalıştırma Rehberi

Bu rehber, Vue.js (Frontend), Spring Boot (Backend) ve PostgreSQL (Veritabanı) üçlüsünden oluşan Medicine ATM projesini Docker Compose kullanarak veya manuel olarak yerel ortamınızda nasıl kuracağınızı ve çalıştıracağınızı adım adım anlatır.


### 🛠️ Teknolojik Yığın (Tech Stack)

| Katman          | Teknoloji            | Detay                                                                                      |
|-----------------|----------------------|--------------------------------------------------------------------------------------------|
| Backend API     | Java 21 / Spring Boot| REST API ve iş mantığı. Maven ile yönetilir.                                              |
| Veritabanı      | PostgreSQL 15-alpine | Veri kalıcılığı için kullanılır. Docker servisi olarak çalışır.                           |
| Frontend UI     | Vue.js 3 / Vite      | Hastalar, Doktorlar ve Personel için web arayüzü.                                         |
| UI Kütüphanesi  | Vuetify              | Modern ve tutarlı kullanıcı arayüzü bileşenlerini sağlar (`^3.11.0-beta.1`).              |
| Orkestrasyon    | Docker Compose       | Üç servisin (DB, Backend, Frontend) tek bir komutla yönetilmesini sağlar.                 |

## ⚙️ Önkoşullar ve Kurulum

Bu projeyi çalıştırmanın **en kolay yolu Docker Compose kullanmaktır**.  
Eğer Docker kullanmak istemiyorsanız, her bileşeni ayrı ayrı kurabilirsiniz (Bkz. *Manuel Kurulum*).

---

### **Adım 1: Gerekli Yazılımların Kontrolü**

Aşağıdaki yazılımların sisteminizde kurulu olduğundan emin olun.

| Yazılım         | Kontrol Komutu                 | Not                                                     |
|-----------------|----------------------------------|---------------------------------------------------------|
| **Docker**       | `docker --version`              | Entegre Docker Compose ile birlikte kurulmalıdır.       |
| **Docker Compose** | `docker compose version`        | Docker Desktop ile birlikte gelir.                     |

--- 

### ⚙️ Adım 2 (Opsiyonel): Manuel Kurulum Kontrolü

Eğer projeyi **Docker olmadan** çalıştırmak isterseniz, aşağıdaki bileşenlerin kurulu olduğundan emin olun:

| Yazılım                         | Kontrol Komutu                    | Not |
|--------------------------------|------------------------------------|------|
| **Java Development Kit (JDK)** | `java -version`                    | JDK 21 veya üstü gereklidir. |
| **Maven**                      | `mvn -v`                           | — |
| **Node.js & npm**              | `node -v` ve `npm -v`              | — |
| **PostgreSQL**                 | —                                  | Harici bir PostgreSQL sunucusu veya yerel kurulum gerektirir. |

---

## 🚀 Projeyi Çalıştırma (Önerilen Yöntem: Docker Compose)

Projenin ana klasöründe (içinde `docker-compose.yml` dosyasının bulunduğu dizinde) olduğunuzu varsayarak:

---

### **Adım 1: Docker Servislerini Başlatın**

Tüm servisleri derlemek ve başlatmak için aşağıdaki komutu kullanın:


```bash
docker compose up --build 
```


### Adım 2: Uygulamaya Erişim

Tüm servisler başarıyla başlatıldıktan sonra (bu işlem biraz zaman alabilir, özellikle Backend derlemesi), tarayıcınızı açarak uygulamaya erişebilirsiniz:

| Servis               | Adres                 | Not                       |
|----------------------|----------------------|---------------------------|
| **Frontend Uygulaması** | http://localhost      | Nginx üzerinde çalışır.   |
| **Backend API**         | http://localhost:8080 | Spring Boot uygulaması.   |


## 🔧 Manuel Kurulum (Docker Kullanılmadan)

Eğer projeyi geliştirme ortamınızda ayrı ayrı çalıştırmak istiyorsanız bu adımları izleyin.

---

### **1. Veritabanı Kurulumu (PostgreSQL)**

1. Yerelinizde bir PostgreSQL sunucusu çalıştırın (Port: **5432**).
2. `medicine_db` adında bir veritabanı oluşturun  
   (Kullanıcı: `postgres`, Şifre: `postgres`).

   - Bu bilgiler `medicine-backend/src/main/resources/application.properties` dosyasında tanımlanmıştır.

---

### **2. Backend Kurulumu (Spring Boot)**

1. `medicine-backend` klasörüne gidin.
2. Maven ile bağımlılıkları indirin ve projeyi derleyin:

```bash
mvn clean install
```

3. Uygulamayı çalıştırın:

```bash
mvn spring-boot:run
```

Uygulama 8080 portunda çalışmaya başlayacaktır.

---

### **3.Frontend Kurulumu (Vue.js / Vuetify)**

1. `Medicine-Frontend ` klasörüne gidin.
2. Node.js bağımlılıklarını indirin (Bu adım Vuetify'ı da otomatik olarak kurar):

```bash
npm install
```

3.Vuetify'ı Manuel Olarak Kurma Komutu (Zaten npm install içinde yer alsa da, projeye dahil etmek için budur):

```bash
npm install vuetify@next @mdi/font
```
@mdi/font popüler Material Design ikonlarını içerir.

4.Uygulamayı geliştirme modunda çalıştırın:

```bash
npm run dev
```
Uygulama genellikle 5173 portunda (http://localhost:5173) çalışır.

---

## 👩‍💻 Kullanıcı Giriş Bilgileri

Kayıt olma ve giriş yapma işlemlerinde, kullanıcının rolü e-posta uzantısına göre belirlenir:

| Rol         | E-posta Uzantısı       | 
|-----------------|---------------------|
| **Doktor**       | `@dr.medicine`  | 
| **Hasta** | `@pt.medicine`        |
| **Eczane Personeli** | `@ph.medicine` | 






