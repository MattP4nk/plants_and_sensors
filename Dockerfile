FROM maven:3.9.9-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -DskipTest

FROM eclipse-temurin-21
COPY --from=build /target/plants_and_sensors-0.0.1.jar app_plants_and_sensors.jar
EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "app_plants_and_sensors.jar" ]