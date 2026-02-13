# 1. Aşama: Uygulamayı Derleme (Build Stage)
FROM node:20-alpine AS build

WORKDIR /app

# Bağımlılıkları yükle
COPY package*.json ./
RUN npm install

# Proje dosyalarını kopyala
COPY . .

# 💡 YENİ: Build argümanını al
ARG VITE_API_URL

# Build sırasında VITE_API_URL'i env içine enjekte et
# Bu sayede derlenmiş Vue kodunuz doğru backend adresini kullanır
ENV VITE_API_URL=$VITE_API_URL

# Uygulamayı derle (API URL değişkeni artık erişilebilir)
# Eğer package.json'daki build komutunuz "vite build" ise çalışacaktır.
RUN npm run build

# 2. Aşama: Uygulamayı Çalıştırma (Run Stage)
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
# Nginx konfigürasyonunu kopyala
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]