package com.api.medicine.presentation.controllers;

import com.api.medicine.application.use_cases.RegisterUseCase;
import com.api.medicine.domain.interfaces.User;
import com.api.medicine.domain.interfaces.UserRepository;
import com.api.medicine.infrastructure.security.JwtUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost", "http://127.0.0.1" })
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    public AuthController(RegisterUseCase registerUseCase,
            AuthenticationManager authenticationManager,
            JwtUtils jwtUtils,
            UserRepository userRepository) {
        this.registerUseCase = registerUseCase;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        boolean success = registerUseCase.register(
                request.get("name"),
                request.get("email"),
                request.get("password"));

        if (success) {
            return ResponseEntity.ok("Kayıt başarılı!");
        } else {
            return ResponseEntity.badRequest().body("Kayıt başarısız (Email kullanımda veya geçersiz).");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));

            // Authentication başarılı
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String role = userDetails.getAuthorities().stream().findFirst().get().getAuthority();

            // ROLE_ prefixini temizle (Spring Security ekliyor olabilir)
            if (role.startsWith("ROLE_")) {
                role = role.substring(5);
            }

            String token = jwtUtils.generateToken(userDetails.getUsername(), role);

            // Frontend'in beklediği user objesini de dönmek için DB'den çekelim
            User user = userRepository.findByEmail(email).orElse(null);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", user);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Giriş başarısız: " + e.getMessage());
        }
    }
}