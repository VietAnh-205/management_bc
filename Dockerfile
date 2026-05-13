# Sử dụng JDK 21
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy Maven wrapper và pom trước để cache dependency
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src src

# Build ứng dụng
RUN ./mvnw clean package -DskipTests

# Runtime image
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy file jar từ builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
