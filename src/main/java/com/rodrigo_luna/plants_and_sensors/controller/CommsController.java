package com.rodrigo_luna.plants_and_sensors.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.rodrigo_luna.plants_and_sensors.dtos.LogDTO;
import com.rodrigo_luna.plants_and_sensors.dtos.PasswordRecoveryDTO;
import com.rodrigo_luna.plants_and_sensors.dtos.RequestDTO;
import com.rodrigo_luna.plants_and_sensors.dtos.ResponseDTO;
import com.rodrigo_luna.plants_and_sensors.services.JWTService;
import com.rodrigo_luna.plants_and_sensors.services.LogService;
import com.rodrigo_luna.plants_and_sensors.services.PlantsAndSensorsService;
import com.rodrigo_luna.plants_and_sensors.services.UserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping()
public class CommsController {

    @Autowired
    UserService userService;

    @Autowired
    PlantsAndSensorsService plantsAndSensorsService;

    @Autowired
    LogService logServices;

    @Autowired
    JWTService jwtService;

    /**
     * I wanted to use the least amount of endpoints in order to make my front-end
     * work easier.
     * Using this "comms" entrance I managed to check all comunications between the
     * front and the backend.
     * I implemented a logger to keep an eye on any changes and two validators (user
     * o admin) to check for
     * authorities in every request.
     * 
     * @param requestDTO
     * @return responseDTO
     */
    @PostMapping("/comms")
    public ResponseEntity<ResponseDTO> RequesteManager(@Valid @RequestBody RequestDTO requestDTO) {

        boolean willLog = false;
        ResponseDTO responseDTO = new ResponseDTO();
        switch (requestDTO.getArea()) {
            case "users" -> {
                switch (requestDTO.getCommand()) {
                    case "login" -> {
                        responseDTO = userService.login(requestDTO.getCredentials());
                    }
                    case "register" -> {
                        System.out.println(requestDTO.getRegInfo());
                        responseDTO = userService.register((requestDTO.getRegInfo()));
                    }
                    case "recovery" -> {
                        responseDTO = userService.authRecovery((requestDTO.getRecoveryDTO()));
                    }
                    case "delete" -> {
                        willLog = true;
                        try {
                            jwtService.validateAdminToken(requestDTO.getKey());
                            responseDTO = userService.deleteUser(requestDTO.getTarget());
                        } catch (Exception e) {
                            responseDTO.setStatus(
                                    "ILLEGAL. You have no access to this function." + requestDTO.getCommand());
                        }
                    }
                    case "upgrade" -> {
                        willLog = true;
                        try {
                            jwtService.validateAdminToken(requestDTO.getKey());
                            responseDTO = userService.upgradeUser(requestDTO.getTarget());
                        } catch (Exception e) {
                            responseDTO.setStatus(
                                    "ILLEGAL. You have no access to this function." + requestDTO.getCommand());
                        }

                    }
                    case "downgrade" -> {
                        willLog = true;
                        try {
                            jwtService.validateAdminToken(requestDTO.getKey());
                            responseDTO = userService.downgradeAdmin(requestDTO.getTarget());
                        } catch (Exception e) {
                            responseDTO.setStatus(
                                    "ILLEGAL. You have no access to this function." + requestDTO.getCommand());
                        }
                    }
                    case "passwordChange" -> {
                        try {
                            jwtService.validateToken(requestDTO.getKey());
                            responseDTO = userService.changePassword(requestDTO.getNewCredentials());
                        } catch (Exception e) {
                            responseDTO.setStatus(
                                    "ILLEGAL. You have no access to this function." + requestDTO.getCommand());
                        }
                    }
                    case "validate" -> {
                        try {
                            jwtService.validateToken(requestDTO.getKey());
                            responseDTO.setPack("Valid");
                            responseDTO.setStatus("OK");
                        } catch (Exception e) {
                            responseDTO.setPack("Not Valid");
                            responseDTO.setStatus("OK");
                        }
                    }
                    default -> {
                        responseDTO.setStatus("BAD_REQUEST. Wrong command:" + requestDTO.getCommand());
                    }
                }
            }
            case "logs" -> {
                try {
                    jwtService.validateAdminToken(requestDTO.getKey());
                    responseDTO = logServices.readLogs();
                } catch (Exception e) {
                    responseDTO.setStatus("ILLEGAL. You have no access to this function.");
                }
            }
            case "plants_sensors" -> {
                switch (requestDTO.getCommand()) {
                    case "createPlant" -> {
                        willLog = true;
                        try {
                            jwtService.validateAdminToken(requestDTO.getKey());
                            responseDTO = plantsAndSensorsService.createPlant(requestDTO.getPlant());
                        } catch (Exception e) {
                            responseDTO.setStatus(
                                    "ILLEGAL. You have no access to this function." + requestDTO.getCommand());
                        }
                    }
                    case "getPlants" -> {
                        try {
                            System.out.println(jwtService.validateToken(requestDTO.getKey()));
                            responseDTO = plantsAndSensorsService.returnPlantList();
                        } catch (Exception e) {
                            responseDTO.setStatus(
                                    "ILLEGAL. You have no access to this function." + requestDTO.getCommand());
                        }
                    }
                    case "deletePlant" -> {
                        willLog = true;
                        try {
                            jwtService.validateAdminToken(requestDTO.getKey());
                            responseDTO = plantsAndSensorsService.deletePlant(requestDTO.getTargetId());
                        } catch (Exception e) {
                            responseDTO.setStatus(
                                    "ILLEGAL. You have no access to this function." + requestDTO.getCommand());
                        }
                    }
                    case "addSensor" -> {
                        willLog = true;
                        try {
                            jwtService.validateAdminToken(requestDTO.getKey());
                            responseDTO = plantsAndSensorsService.addSensor(requestDTO.getSensor());
                        } catch (Exception e) {
                            responseDTO.setStatus(
                                    "ILLEGAL. You have no access to this function." + requestDTO.getCommand());
                        }
                    }
                    case "deleteSensor" -> {
                        willLog = true;
                        try {
                            jwtService.validateAdminToken(requestDTO.getKey());
                            responseDTO = plantsAndSensorsService.deleteSensor(requestDTO.getTarget());
                        } catch (Exception e) {
                            responseDTO.setStatus(
                                    "ILLEGAL. You have no access to this function." + requestDTO.getCommand());
                        }
                    }
                    case "getAllHistoricsByType" -> {
                        try {
                            jwtService.validateToken(requestDTO.getKey());
                            responseDTO = plantsAndSensorsService.getAllHistoricsByType();
                        } catch (Exception e) {
                            responseDTO.setStatus(
                                    "ILLEGAL. You have no access to this function." + requestDTO.getCommand());
                        }
                    }
                    case "updateAllValues" -> {
                        willLog = true;
                        try {
                            jwtService.validateToken(requestDTO.getKey());
                            responseDTO = plantsAndSensorsService.updateAllSensors();
                        } catch (Exception e) {
                            responseDTO.setStatus(
                                    "ILLEGAL. You have no access to this function." + requestDTO.getCommand());
                        }
                    }
                    case "updatePlantValues" -> {
                        willLog = true;
                        try {
                            jwtService.validateToken(requestDTO.getKey());
                            responseDTO = plantsAndSensorsService.updatePlantSensors(requestDTO.getTargetId());
                        } catch (Exception e) {
                            responseDTO.setStatus(
                                    "ILLEGAL. You have no access to this function: " + requestDTO.getCommand());
                        }

                    }
                    default -> {
                        responseDTO.setStatus("BAD_REQUEST. Wrong command:" + requestDTO.getCommand());
                    }
                }
            }
            default -> {
                responseDTO.setStatus("BAD REQUEST. Invalid Area");
            }
        }
        if (responseDTO.getStatus().equals("OK")) {
            if (willLog) {
                String targetName;
                targetName = switch (requestDTO.getCommand()) {
                    case "createPlant" -> requestDTO.getPlant().getName();
                    case "addSensor" -> responseDTO.getToLog();
                    default -> requestDTO.getTarget();
                };
                LogDTO log = LogDTO.builder()
                        .admin(jwtService.userFromJWT(requestDTO.getKey()))
                        .area(requestDTO.getArea())
                        .command(requestDTO.getCommand())
                        .target(targetName)
                        .build();
                logServices.createLog(log);
            }
            return new ResponseEntity(responseDTO, HttpStatus.OK);
        } else if (responseDTO.getStatus().startsWith("ILLEGAL.")) {
            return new ResponseEntity(responseDTO, HttpStatus.FORBIDDEN);
        } else {
            return new ResponseEntity(responseDTO, HttpStatus.BAD_REQUEST);
        }

    }

    /**
     * From this point forward I'll only work with "local routes" using thymeleaf to
     * create the views. These routes are templates with no conection to the front
     * end and only work using a "key" that is created for each request. Till now I
     * only made one route "password-recovery" the link to this resource is created
     * in the authRecovery method of user services and sent in an email (+ key) to
     * the user email account.
     * 
     * @param view
     * @param key
     * @return template.
     */
    @GetMapping("/password-recovery")
    public String getForm(Model view, @RequestParam(name = "key") String key) {
        PasswordRecoveryDTO newPass = PasswordRecoveryDTO.builder()
                .username(this.jwtService.userFromJWT(key))
                .newPass("")
                .confirmation("")
                .build();
        view.addAttribute("passDto", newPass);
        return "password";
    }

    @PostMapping("/password-recovery")
    public String postForm(
            @ModelAttribute PasswordRecoveryDTO recoveryDto) {
        System.out.println("In post form" + recoveryDto);
        this.userService.passRecovery(recoveryDto);
        return "success";

    }

}
