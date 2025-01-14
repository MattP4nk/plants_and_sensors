package com.rodrigo_luna.plants_and_sensors.models;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "logs")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class LogModel {
    @Id
    private Long id;
    private String admin;
    private LocalDateTime date;
    private String area;
    private String command;
    private String target;
}
