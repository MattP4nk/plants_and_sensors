package com.rodrigo_luna.plants_and_sensors.models;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "plants")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PlantModel {
    @Id
    private Long id;
    private String name;
    private String country;
    private Integer sensorCounter;
    private List<SensorModel> sensorList;

    public void addSensor(SensorModel sensor) {
        this.sensorList.add(sensor);
    }

    public String deleteSensor(String sensorName) {
        if (sensorList.stream().anyMatch(sensor -> sensorName.equals(sensor.getName()))) {
            sensorList.removeIf(sensor -> sensorName.equals(sensor.getName()));
            return "deleted";
        } else {
            return "Failure";
        }

    }

    public Integer getTotalSafes() {
        int safeReadings = 0;
        if (sensorList.isEmpty()) {
            return safeReadings;
        }
        for (SensorModel sensor : sensorList) {
            safeReadings += sensor.getTotalSafes();
        }
        return safeReadings;
    }

    public Integer getTotalWarnings() {
        int warnings = 0;
        if (sensorList.isEmpty()) {
            return warnings;
        }
        for (SensorModel sensor : sensorList) {
            warnings += sensor.getTotalWarnings();
        }
        return warnings;
    }

    public Integer getTotalRedAlerts() {
        int redAlerts = 0;
        if (sensorList.isEmpty()) {
            return redAlerts;
        }
        for (SensorModel sensor : sensorList) {
            redAlerts += sensor.getTotalRedAlerts();
        }
        return redAlerts;
    }

    public int getDisabledSensors() {
        int disabledSensors = 0;
        if (sensorList.isEmpty()) {
            return disabledSensors;
        }
        for (SensorModel sensor : sensorList) {
            if (sensor.getDisabled() == null) {
                sensor.setDisabled(false);
            }
            if (sensor.getDisabled() == true) {
                disabledSensors += 1;
            }
        }
        return disabledSensors;
    }
}
