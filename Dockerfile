FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven wrapper and project metadata first to improve Docker layer caching.
COPY .mvn/ .mvn/
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd
COPY pom.xml .

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY src/ src/

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy AS runtime

WORKDIR /app
#Installler nc utilisé par le script sh
RUN apt-get update && apt-get install -y netcat && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar

# Ajout du script
COPY wait-for-postgres.sh wait-for-postgres.sh
RUN chmod +x wait-for-postgres.sh

EXPOSE 8080

#ENTRYPOINT ["java", "-jar", "app.jar"]
ENTRYPOINT ["./wait-for-postgres.sh"]
