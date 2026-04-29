# 1. Aşama: Uygulamayı Derleme (Build Stage)
# Java 21 ve Maven içeren bir imaj kullan
FROM maven:3.9.5-eclipse-temurin-21 AS build

WORKDIR /app

# Sadece pom.xml'i kopyala ve bağımlılıkları indir.
# Bu, bağımlılıklar değişmediğinde sonraki build'lerde önbelleği kullanır.
COPY pom.xml .
RUN mvn dependency:go-offline

# Tüm kodları kopyala
COPY src ./src

# Uygulamayı paketle (JAR oluştur)
RUN mvn clean install -DskipTests

# 2. Aşama: Uygulamayı Çalıştırma (Run Stage)
# Sadece JRE içeren daha küçük ve güvenli bir imaj kullan
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# JAR dosyasının adı 'medicine-0.0.1-SNAPSHOT.jar' olarak belirlenmiş
COPY --from=build /app/target/medicine-0.0.1-SNAPSHOT.jar app.jar

# Spring Boot'un varsayılan portu
EXPOSE 8080

# Uygulamayı başlat
ENTRYPOINT ["java", "-jar", "app.jar"]