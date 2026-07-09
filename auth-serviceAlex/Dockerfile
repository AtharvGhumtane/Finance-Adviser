# ─── Stage 1: Build ───────────────────────────────────────────────────────────
# Auth service uses Java 17 bytecode but runs fine on JDK 21
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# ─── Stage 2: Run ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 5054
ENTRYPOINT ["java", "-jar", "app.jar"]
