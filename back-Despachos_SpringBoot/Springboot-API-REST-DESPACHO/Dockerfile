# Etapa 1: Compilación (Build)
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Producción (Run)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Creación de usuario sin privilegios root (¡Clave para tu nota!)
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]