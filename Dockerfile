FROM openjdk:21-jdk-slim
COPY /target/plants_and_sensors-0.0.1.jar app_plants_and_sensors.jar
EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "app_plants_and_sensors.jar" ]