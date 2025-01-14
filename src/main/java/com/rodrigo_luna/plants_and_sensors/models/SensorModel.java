package com.rodrigo_luna.plants_and_sensors.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SensorModel {
    private String name;
    private String type;
    private String value;
    private Integer totalSafes;
    private Integer totalWarnings;
    private Integer totalRedAlerts;
    private Boolean disabled;
}
