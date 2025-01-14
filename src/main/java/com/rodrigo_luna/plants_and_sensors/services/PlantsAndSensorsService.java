package com.rodrigo_luna.plants_and_sensors.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;

import com.rodrigo_luna.plants_and_sensors.dtos.HistoricsDTO;
import com.rodrigo_luna.plants_and_sensors.dtos.PlantDTO;
import com.rodrigo_luna.plants_and_sensors.dtos.ResponseDTO;
import com.rodrigo_luna.plants_and_sensors.dtos.SensorDTO;
import com.rodrigo_luna.plants_and_sensors.models.PlantModel;
import com.rodrigo_luna.plants_and_sensors.models.SensorModel;
import com.rodrigo_luna.plants_and_sensors.repositories.IPlantRepository;

@Service
public class PlantsAndSensorsService {

    ResponseDTO responseDTO = new ResponseDTO();

    @Autowired
    IPlantRepository plantRepository;

    public ResponseDTO returnPlantList() {
        List<PlantModel> plants = findAllPlants();
        responseDTO.setStatus("OK");
        responseDTO.setPack(plants);
        return responseDTO;
    }

    private PlantModel findOnePlant(Long id) {
        return plantRepository.findById(id).orElseThrow();
    }

    private List<PlantModel> findAllPlants() {
        return plantRepository.findAll();
    }

    private String getNewValue() {
        Random random = new Random();
        Integer range = random.nextInt(100);
        if (range < 10) {
            return "Danger";
        } else if (range > 9 && range < 41) {
            return "Warning";
        } else {
            return "OK";
        }
    }

    public ResponseDTO createPlant(PlantDTO plantDto) {
        List<SensorModel> emptyList = new ArrayList();
        PlantModel plantModel = PlantModel.builder()
                .id(System.currentTimeMillis())
                .name(plantDto.getName())
                .sensorList(emptyList)
                .sensorCounter(0)
                .country(plantDto.getCountry())
                .build();
        try {
            plantRepository.save(plantModel);
            responseDTO.setStatus("OK");
            responseDTO.setPack(plantModel);
        } catch (Exception e) {
            if (e.getLocalizedMessage().contains("Duplicate entry")) {
                responseDTO.setStatus("FAILED. Name already in use.");
            } else {
                responseDTO.setStatus("Unknown Error");
            }
        }
        return responseDTO;
    }

    public ResponseDTO deletePlant(Long id) {
        try {
            PlantModel plantModel = findOnePlant(id);
            if (plantModel == null) {
                throw new NotFoundException();
            }
            plantRepository.delete(plantModel);
            responseDTO.setStatus("OK");
        } catch (NotFoundException e) {
            responseDTO.setStatus("FAILED. Plant Id: " + id + " not found in plants list.");
        }
        return responseDTO;
    }

    public ResponseDTO addSensor(SensorDTO sensorDTO) {
        try {
            PlantModel plant = findOnePlant(sensorDTO.getPlantId());
            if (plant == null) {
                throw new Exception();
            }
            String sensorName = plant.getName() + ": Sensor " + (plant.getSensorCounter() + 1) + " "
                    + sensorDTO.getType();
            SensorModel sensor = SensorModel.builder()
                    .type(sensorDTO.getType())
                    .value("OK")
                    .name(sensorName)
                    .totalSafes(0)
                    .totalWarnings(0)
                    .totalRedAlerts(0)
                    .build();
            plant.addSensor(sensor);
            plant.setSensorCounter(plant.getSensorCounter() + 1);
            plantRepository.save(plant);
            responseDTO.setStatus("OK");
            responseDTO.setToLog("Added " + sensorName);
            responseDTO.setPack(plant);
        } catch (Exception e) {
            responseDTO.setStatus("FAILED. Plant not found");
        }
        return responseDTO;
    }

    public ResponseDTO deleteSensor(String target) {
        String plantName = target.split("-")[0];
        String sensorName = target.split("-")[1];

        try {
            PlantModel plant = plantRepository.findByName(plantName);
            if (plant == null || plant.getSensorList().isEmpty()) {
                throw new NotFoundException();
            }
            if ("Failure".equals(plant.deleteSensor(sensorName))) {
                throw new NotFoundException();
            }
            plantRepository.save(plant);
            responseDTO.setStatus("OK");
        } catch (NotFoundException e) {
            responseDTO.setStatus("FAILED. " + target + " is not found.");
        }
        return responseDTO;
    }

    /**
     * This is just the "heavy" part of the sensor updating.
     * 
     * @param plant --> PlantModel
     * @return True for sucess and False for failure. The only fail case till now is
     *         when the sensorList is empty.
     */
    public Boolean updateSensors(PlantModel plant) {
        List<SensorModel> sensors = plant.getSensorList();
        if (sensors.isEmpty()) {
            return false;
        }
        sensors.forEach((SensorModel sensor) -> {
            sensor.setValue(getNewValue());
            switch (sensor.getValue()) {
                case "OK" -> {
                    sensor.setTotalSafes(sensor.getTotalSafes() + 1);
                }
                case "Warning" -> {
                    sensor.setTotalWarnings(sensor.getTotalWarnings() + 1);
                }
                case "Danger" -> {
                    sensor.setTotalRedAlerts(sensor.getTotalRedAlerts() + 1);
                }
            }
            plant.setSensorList(sensors);
            plantRepository.save(plant);
        });
        return true;
    }

    /**
     * This method calls the updateSensors method for just one plant.
     * 
     * @param target gets the name of the plant.
     * @returns the responseDTO in case of failure or sucess.
     */
    public ResponseDTO updatePlantSensors(Long target) {
        try {
            PlantModel plant = findOnePlant(target);
            if (updateSensors(plant)) {
                responseDTO.setStatus("OK");
                responseDTO.setPack(getHistoric(findOnePlant(target)));
                responseDTO.setToLog("Plant \"" + plant.getName() + "\": All sensors updated");
            } else {
                responseDTO.setStatus(
                        "FAILED. Plant \"" + plant.getName() + "\" has no active sensors. Add a sensor and try again.");
            }

        } catch (Exception e) {
            responseDTO.setStatus("FAILED. Plant not found");
        }
        return responseDTO;
    }

    /**
     * This works like the previous method but looping to update all plants
     * 
     * @return a responseDTO with an OK in case of sucess and more info in case of
     *         failure.
     */
    public ResponseDTO updateAllSensors() {
        List<PlantModel> allPlants = plantRepository.findAll();
        if (allPlants.isEmpty()) {
            responseDTO.setStatus("FAILED. No plants in database.");
        } else {
            try {
                boolean thereAreSensors = false;
                String result = "";
                for (PlantModel plant : allPlants) {
                    if (updateSensors(plant)) {
                        thereAreSensors = true;
                        result += "Plant \"" + plant.getName() + "\": All sensors updated\n";
                    } else {
                        result += "Plant \"" + plant.getName()
                                + "\" has no active sensors. Add a sensor and try again.\n";
                    }
                }
                if (thereAreSensors) {
                    responseDTO.setStatus("OK");
                    responseDTO.setPack(getHistoricTotals());
                    responseDTO.setToLog(result);
                } else {
                    responseDTO.setStatus(
                            "FAILED. There are no active sensor in database");
                    responseDTO.setPack(result);
                }

            } catch (Exception e) {
                responseDTO.setStatus(
                        "FAILED. Server Error");
            }
        }
        return responseDTO;
    }

    public HistoricsDTO getHistoric(PlantModel plant) {
        int safes = plant.getTotalSafes();
        int warnings = plant.getTotalWarnings();
        int redAlerts = plant.getTotalRedAlerts();
        HistoricsDTO historic = HistoricsDTO.builder()
                .safe(safes)
                .warning(warnings)
                .redAlert(redAlerts)
                .build();
        return historic;

    }

    public HistoricsDTO getHistoricTotals() {
        List<PlantModel> plants = findAllPlants();
        int safes = 0;
        int warnings = 0;
        int redAlerts = 0;
        int disabled = 0;
        for (PlantModel plant : plants) {
            safes += plant.getTotalSafes();
            warnings += plant.getTotalWarnings();
            redAlerts += plant.getTotalRedAlerts();
            disabled += plant.getDisabledSensors();
        }
        HistoricsDTO historicTotals = HistoricsDTO.builder()
                .type("totals")
                .safe(safes)
                .warning(warnings)
                .redAlert(redAlerts)
                .disabled(disabled)
                .build();
        return historicTotals;
    }

    public List<HistoricsDTO> allHistoricsByType() {
        List<PlantModel> plants = findAllPlants();
        List<HistoricsDTO> historicsByType = new ArrayList<>();
        for (PlantModel plant : plants) {
            for (SensorModel sensor : plant.getSensorList()) {
                int disabledCounter = 0;
                if (sensor.getDisabled() == null) {
                    sensor.setDisabled(false);
                }
                if (sensor.getDisabled() == false) {
                    disabledCounter += 1;
                }
                if (!(historicsByType.stream()
                        .anyMatch(target -> target.getType().equals(sensor.getType())))) {
                    HistoricsDTO historic = HistoricsDTO.builder()
                            .type(sensor.getType())
                            .safe(sensor.getTotalSafes())
                            .warning(sensor.getTotalWarnings())
                            .redAlert(sensor.getTotalRedAlerts())
                            .disabled(disabledCounter)
                            .build();
                    historicsByType.add(historic);
                } else {
                    HistoricsDTO target = historicsByType.stream()
                            .filter(historic -> historic.getType().equals(sensor.getType())).findFirst().orElseThrow();
                    target.safe += sensor.getTotalSafes();
                    target.warning += sensor.getTotalWarnings();
                    target.redAlert += sensor.getTotalRedAlerts();
                    target.disabled += disabledCounter;
                }
            }
        }
        historicsByType.add(getHistoricTotals());
        return historicsByType;
    }

    public ResponseDTO getAllHistoricsByType() {
        try {
            List<HistoricsDTO> historicsByType = allHistoricsByType();
            responseDTO.setStatus("OK");
            responseDTO.setPack(historicsByType);
        } catch (Exception e) {
            responseDTO.setStatus("Failure");
        }
        return responseDTO;
    }
}
