FROM maven:3.10-eclipse-temurin-27 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -DskipTests package

FROM eclipse-temurin:27-jre
WORKDIR /app
COPY --from=build /build/target/TrainFyre-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8443
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
