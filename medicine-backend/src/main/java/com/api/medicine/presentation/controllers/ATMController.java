package com.api.medicine.presentation.controllers;

import com.api.medicine.domain.entities.ATM;
import com.api.medicine.domain.interfaces.ATMRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/atm")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost", "http://127.0.0.1" })
public class ATMController {

    private final ATMRepository atmRepository;

    public ATMController(ATMRepository atmRepository) {
        this.atmRepository = atmRepository;
    }

    /**
     * POST /api/atm/check-stock
     * Checks if the given medicines are in stock for a specific ATM.
     * Request body: { "atmId": 1, "medicineNames": ["Acal", "Parol"] }
     * Response: { "Acal": true, "Parol": false }
     */
    @PostMapping("/check-stock")
    public ResponseEntity<?> checkStock(@RequestBody Map<String, Object> request) {
        Long atmId = request.containsKey("atmId")
                ? Long.valueOf(request.get("atmId").toString())
                : 1L;

        List<String> medicineNames = (List<String>) request.get("medicineNames");

        if (medicineNames == null || medicineNames.isEmpty()) {
            return ResponseEntity.badRequest().body("İlaç isimleri gereklidir.");
        }

        ATM atm = atmRepository.findById(atmId).orElse(null);

        if (atm == null) {
            return ResponseEntity.status(404).body("ATM bulunamadı.");
        }

        Map<String, Boolean> stockStatus = new HashMap<>();
        Map<String, Integer> currentStock = atm.getStock();

        for (String medicineName : medicineNames) {
            int quantity = currentStock.getOrDefault(medicineName, 0);
            stockStatus.put(medicineName, quantity > 0);
        }

        return ResponseEntity.ok(stockStatus);
    }

    /**
     * GET /api/atm/all
     * Retrieves all ATMs in the system.
     * Response: List of ATM objects
     */
    @GetMapping("/all")
    public ResponseEntity<List<ATM>> getAll() {
        return ResponseEntity.ok(atmRepository.findAll());
    }
}
