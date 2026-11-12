FROM maven:3.10.0-eclipse-temurin-25 AS build

WORKDIR /app

COPY pom.xml .

RUN mvn -B dependency:go-offline

COPY src/ ./src

RUN mvn -B package -DskipTests

FROM eclipse-temurin:25-jre

WORKDIR /app

RUN groupadd -r eventhub && useradd -r -g eventhub usr_eventhub

COPY --from=build --chown=usr_eventhub:eventhub /app/target/*.jar app.jar

USER usr_eventhub

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]




