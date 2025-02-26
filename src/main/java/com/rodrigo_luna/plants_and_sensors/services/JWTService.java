package com.rodrigo_luna.plants_and_sensors.services;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.rodrigo_luna.plants_and_sensors.models.Role;
import com.rodrigo_luna.plants_and_sensors.models.UserModel;
import com.rodrigo_luna.plants_and_sensors.repositories.IUserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@Service
public class JWTService {

    // @Value("${magic.word}")
    private final String secret_key = "pUi0MP0B8w8C9j5prhkErjEcSW9YwBwz9PfJl2ha6YPmVcDMIkKA";

    @Autowired
    IUserRepository userRepository;

    public String getToken(UserDetails user) {
        return getToken(new HashMap<>(), user);
    }

    private String getToken(Map<String, Object> extraClaims, UserDetails user) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1440000))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();

    }

    private Key getKey() {
        byte[] byteKey = Decoders.BASE64.decode(secret_key);
        return Keys.hmacShaKeyFor(byteKey);
    }

    public String userFromJWT(@AuthenticationPrincipal String jwt) {
        Claims claims = Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(jwt).getBody();
        String username = claims.getSubject();
        return username;
    }

    public boolean validateToken(String token) {
        try {
            String username = userFromJWT(token);
            if (username == null) {
                System.out.println("Username null");
                throw new MalformedJwtException("Not a valid Token");
            }
            UserModel user = userRepository.findByUsername(username);
            if (user == null) {
                System.out.println("User not found");
                throw new UsernameNotFoundException("User not found");
            }
            return true;
        } catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException | SignatureException
                | IllegalArgumentException e) {
            throw new AuthenticationCredentialsNotFoundException("Token has expired or is incorrect");
        }
    }

    public boolean validateAdminToken(String token) {
        try {
            String username = userFromJWT(token);
            if (username == null) {
                throw new MalformedJwtException("Not a valid Token");
            }
            UserModel user = userRepository.findByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException("User not found");
            }
            if (user.getRole() != Role.ADMIN) {
                throw new IllegalArgumentException("Forbiden. You can't access this resource");
            }
            return true;
        } catch (UsernameNotFoundException | IllegalArgumentException | MalformedJwtException e) {
            throw new AuthenticationCredentialsNotFoundException("Token has expired or is incorrect");
        }
    }

}