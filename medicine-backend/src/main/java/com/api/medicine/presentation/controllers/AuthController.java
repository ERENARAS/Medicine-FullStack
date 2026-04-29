package com.api.medicine.presentation.controllers;

import com.api.medicine.application.use_cases.LoginUseCase;
import com.api.medicine.application.use_cases.RegisterUseCase;
import com.api.medicine.domain.interfaces.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
// Vue.js (genelde localhost:5173 veya 8080)den gelen isteklere izin ver
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost", "http://127.0.0.1"})
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;

    public AuthController(RegisterUseCase registerUseCase, LoginUseCase loginUseCase) {
        this.registerUseCase = registerUseCase;
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        // Basitçe JSON body'den verileri alıyoruz
        boolean success = registerUseCase.register(
                request.get("name"),
                request.get("email"),
                request.get("password")
        );

        if (success) {
            return ResponseEntity.ok("Kayıt başarılı!");
        } else {
            return ResponseEntity.badRequest().body("Kayıt başarısız (Email kullanımda veya geçersiz).");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        User user = loginUseCase.login(
                request.get("email"),
                request.get("password")
        );

        if (user != null) {
            // Normalde burada JWT Token dönmelisin, şimdilik user bilgisini dönelim.
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(401).body("Giriş başarısız: Hatalı bilgiler.");
        }
    }
}