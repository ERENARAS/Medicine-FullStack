package com.api.medicine.presentation.controllers;

import com.api.medicine.domain.entities.ATM;
import com.api.medicine.domain.entities.PharmacyStaff;
import com.api.medicine.domain.interfaces.ATMRepository;
import com.api.medicine.infrastructure.repositories.jpa.JpaPharmacyStaffRepository;
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
    private final JpaPharmacyStaffRepository pharmacyStaffRepository;

    public ATMController(ATMRepository atmRepository, JpaPharmacyStaffRepository pharmacyStaffRepository) {
        this.atmRepository = atmRepository;
        this.pharmacyStaffRepository = pharmacyStaffRepository;
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
     * POST /api/atm/create
     * Creates a new ATM with the given location.
     * Request body: { "location": "String", "staffId": 1 }
     * Response: Created ATM object
     */
    @PostMapping("/create")
    public ResponseEntity<?> createATM(@RequestBody Map<String, Object> request) {
        String location = (String) request.get("location");
        Object staffIdObj = request.get("staffId");

        if (location == null || location.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Konum bilgisi gereklidir."));
        }

        if (staffIdObj == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Eczane personeli ID'si gereklidir."));
        }

        Long staffId;
        try {
            staffId = Long.valueOf(staffIdObj.toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Geçersiz ID formatı."));
        }

        PharmacyStaff staff = pharmacyStaffRepository.findById(staffId).orElse(null);
        if (staff == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Eczane personeli bulunamadı."));
        }

        ATM newATM = new ATM(location);
        newATM.setResponsibleStaff(staff);
        atmRepository.save(newATM);

        return ResponseEntity.ok(newATM);
    }

    /**
     * GET /api/atm/all
     * Retrieves ATMs in the system.
     * Optional param: staffId to filter by responsible staff.
     * Response: List of ATM objects
     */
    @GetMapping("/all")
    public ResponseEntity<List<ATM>> getAll(@RequestParam(required = false) Long staffId) {
        if (staffId != null) {
            return ResponseEntity.ok(atmRepository.findAllByResponsibleStaffId(staffId));
        }
        return ResponseEntity.ok(atmRepository.findAll());
    }
}
