FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY payment-service/target/payment-service-*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]

