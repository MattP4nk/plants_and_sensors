FROM maven:3.9.9-openjdk-21 AS build
COPY . .
RUN mvn clean package -DskipTest

FROM openjdk:21-jdk-slim
COPY --from=build /target/plants_and_sensors-0.0.1.jar app_plants_and_sensors.jar
EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "app_plants_and_sensors.jar" ]