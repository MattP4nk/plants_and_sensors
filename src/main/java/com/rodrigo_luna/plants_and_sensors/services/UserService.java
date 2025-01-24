package com.rodrigo_luna.plants_and_sensors.services;

import java.util.ArrayList;

import javax.management.BadAttributeValueExpException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.rodrigo_luna.plants_and_sensors.dtos.AuthRecoveryDTO;
import com.rodrigo_luna.plants_and_sensors.dtos.ChangePasswordDTO;
import com.rodrigo_luna.plants_and_sensors.dtos.LoginDTO;
import com.rodrigo_luna.plants_and_sensors.dtos.PasswordRecoveryDTO;
import com.rodrigo_luna.plants_and_sensors.dtos.RegistrationDTO;
import com.rodrigo_luna.plants_and_sensors.dtos.ResponseDTO;
import com.rodrigo_luna.plants_and_sensors.models.Role;
import com.rodrigo_luna.plants_and_sensors.models.UserModel;
import com.rodrigo_luna.plants_and_sensors.repositories.IUserRepository;

@Service
public class UserService {

    ResponseDTO response = new ResponseDTO();

    @Autowired
    IUserRepository userRepository;

    @Autowired
    AuthenticationManager authManager;

    @Autowired
    MailService mailService;

    @Autowired
    JWTService jWTService;

    @Autowired
    PasswordEncoder passwordEncoder;

    public ArrayList<UserModel> getUsers() {
        return (ArrayList<UserModel>) userRepository.findAll();
    }

    public ResponseDTO register(RegistrationDTO registrationDTO) {

        UserModel userModel = UserModel.builder()
                .id(System.currentTimeMillis())
                .username(registrationDTO.username)
                .password(passwordEncoder.encode(registrationDTO.password))
                .email(registrationDTO.email)
                .role(Role.USER)
                .build();
        try {
            userRepository.save(userModel);
            String registrationEmail = "Welcome " + userModel.getUsername() + "! You are now part of this demo site!";
            mailService.sendEmail(registrationDTO.getEmail(), "Password Recovery - SensorWatch", registrationEmail);
            userModel.setPassword("password");
            response.setStatus("OK");
            response.setPack(userModel);
        } catch (Exception e) {
            if (e.getLocalizedMessage().contains("Email is not valid")) {
                response.setStatus("FAILED. Please check email format.");
            } else if (e.getLocalizedMessage().contains("Password is too short")) {
                response.setStatus("FAILED. Password is too short");
            } else if (e.getLocalizedMessage().contains("Duplicate entry")) {
                response.setStatus("FAILED. Username or Email already in use.");
            } else {
                response.setStatus(e.getLocalizedMessage());
            }
        }
        return response;
    }

    public ResponseDTO login(LoginDTO loginDTO) {
        try {
            authManager
                    .authenticate(
                            new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));
            UserDetails user = userRepository.findByUsername(loginDTO.getUsername());
            String token = jWTService.getToken(user);
            response.setToken(token);
            response.setStatus("OK");
            ((UserModel) user).setPassword("Password");
            response.setPack(((UserModel) user));
        } catch (AuthenticationException e) {
            if (e.getLocalizedMessage().contains("Bad credentials")) {
                response.setStatus("FAILED. Wrong username or password");
            } else {
                response.setStatus("FAILED.");
                response.setPack(e.getLocalizedMessage());
            }
        }
        return response;
    }

    public ResponseDTO deleteUser(String target) {
        try {
            UserModel user = userRepository.findByUsername(target);
            if (user == null) {
                throw new UsernameNotFoundException("Not found");
            }
            userRepository.delete(user);
            response.setStatus("OK. FAILED. " + target + " not found in users list.");
        } catch (UsernameNotFoundException e) {
            response.setStatus("FAILED. " + target + " not found in users list.");
        }
        return response;
    }

    /**
     * Este metodo transforma a un user en un admin. -Le da chamba-
     * 
     * @param target (user username)
     * @return responseDTO
     */
    public ResponseDTO upgradeUser(String target) {
        try {
            UserModel user = userRepository.findByUsername(target);
            if (user == null) {
                throw new UsernameNotFoundException("Not found");
            }
            if (user.getRole() != Role.USER) {
                throw new BadAttributeValueExpException("" + target + " is an admin already");
            }
            user.setRole(Role.ADMIN);
            userRepository.save(user);
            response.setStatus("OK");
            response.setPack(user);
        } catch (UsernameNotFoundException | BadAttributeValueExpException e) {
            response.setStatus("FAILED. Incorrect or nonexistent user");
        }
        return response;
    }

    /**
     * Este metodo le quita la chamba a un admin.
     * 
     * @param target (Admin username)
     * @return responseDTO
     */
    public ResponseDTO downgradeAdmin(String target) {
        try {
            UserModel admin = userRepository.findByUsername(target);
            if (admin == null) {
                throw new UsernameNotFoundException("Not found");
            }
            if (admin.getRole() != Role.ADMIN) {
                throw new BadAttributeValueExpException("" + target + " is an user already");
            }
            admin.setRole(Role.USER);
            userRepository.save(admin);
            response.setStatus("OK");
            response.setPack(admin);
        } catch (UsernameNotFoundException | BadAttributeValueExpException e) {
            response.setStatus("FAILED. Incorrect or nonexistent Admin");
        }
        return response;
    }

    public ResponseDTO changePassword(ChangePasswordDTO credentials) {
        String errorCatch = "";
        try {
            UserModel user = userRepository.findByUsername(credentials.getUsername());
            if (user == null) {
                throw new UsernameNotFoundException("Not found");
            }
            if (!user.getPassword().equals(passwordEncoder.encode(credentials.getOldPassword()))) {
                errorCatch = "Pass in system=" + user.getPassword() + ". Password codificado=" +  passwordEncoder.encode(credentials.getOldPassword());
                throw new BadCredentialsException("Wrong Password");
            }
            if (credentials.getNewPassword() == null) {
                throw new BadCredentialsException("Password can't be empty");
            }
            user.setPassword(passwordEncoder.encode(credentials.getNewPassword()));
            userRepository.save(user);
            response.setStatus("OK");
            response.setPack(user);
        } catch (UsernameNotFoundException | BadCredentialsException e) {
            response.setStatus(errorCatch);
        }
        return response;
    }

    public ResponseDTO authRecovery(AuthRecoveryDTO recoveryDTO) {
        try {
            UserModel user = userRepository.findByUsername(recoveryDTO.getUsername());
            if (!(recoveryDTO.getEmail().equals(user.getEmail()))) {
                throw new BadCredentialsException("Bad Credentials: \n user email: " + user.getEmail()
                        + ". request email: " + recoveryDTO.getEmail());
            }
            final String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            String link = baseUrl + "/password-recovery?key=" + jWTService.getToken(user);
            String recoveryMail = "Hello " + user.getUsername()
                    + "! You recently requested a password recovery with us. If you did not request a password reset, please contact your admin!\nFollow the link to start your recovery process:\n"
                    + link;
            mailService.sendEmail(recoveryDTO.getEmail(), "Password Recovery - SensorWatch", recoveryMail);
            response.setStatus("OK");
        } catch (Exception e) {
            response.setStatus("Failed. Check email and username and try again.");
        }
        return response;
    }

    public void passRecovery(PasswordRecoveryDTO recoveryDTO) {
        System.out.println(recoveryDTO.getUsername());
        UserModel user = this.userRepository.findByUsername(recoveryDTO.getUsername());
        user.setPassword(passwordEncoder.encode(recoveryDTO.getNewPass()));
        this.userRepository.save(user);
        String recoveryMail = "Hello " + user.getUsername()
                + "! your password recovery was successful.";
        mailService.sendEmail(user.getEmail(), "Password Recovery Success- SensorWatch", recoveryMail);
    }

}
