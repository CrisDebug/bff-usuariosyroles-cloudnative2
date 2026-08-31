# ============================================================
# ETAPA 1 - BUILD
# ============================================================
FROM maven:3.9.11-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests


# ============================================================
# ETAPA 2 - RUNTIME
# ============================================================
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/ms-bff-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# EC2 utilizará la configuración:
# application-ec2.properties
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=ec2"]
