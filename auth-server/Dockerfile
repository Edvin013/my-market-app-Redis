# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

# Copy entire project
COPY . .

# Build the application
RUN ./mvnw clean package -pl auth-server -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/auth-server/target/auth-server-*.jar app.jar
EXPOSE 9000
ENTRYPOINT ["java", "-jar", "app.jar"]
