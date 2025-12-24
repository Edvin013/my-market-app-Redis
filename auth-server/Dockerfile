FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY auth-server/target/auth-server-*.jar app.jar
EXPOSE 9000
ENTRYPOINT ["java", "-jar", "app.jar"]

