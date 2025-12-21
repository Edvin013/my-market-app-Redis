FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Копируем JAR файл
COPY market-app/target/market-app-*.jar app.jar

# Открываем порт 8080
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]

