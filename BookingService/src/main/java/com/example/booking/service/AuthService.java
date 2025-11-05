package com.example.booking.service;

import com.example.booking.entity.User;
import com.example.booking.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final SecretKey key;

    public AuthService(UserRepository userRepository, @Value("${security.jwt.secret}") String secret) {
        this.userRepository = userRepository;
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, bytes.length);
            bytes = padded;
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public User register(String username, String password, boolean admin) {
        User u = new User.Builder()
                .username(username)
                .passwordHashed(BCrypt.hashpw(password, BCrypt.gensalt()))
                .role(admin ? "ADMIN" : "USER")
                .build();

        return userRepository.save(u);
    }

    public String login(String username, String password) {
        User u = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!BCrypt.checkpw(password, u.getPassword())) {
            throw new IllegalArgumentException("Bad credentials");
        }
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(u.getId().toString())
                .addClaims(Map.of(
                        "scope", u.getRole(),
                        "username", u.getUsername()
                ))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(3600)))
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    }
}