package com.api.medicine.presentation.controllers;

import com.api.medicine.application.use_cases.AddAllergyUseCase;
import com.api.medicine.application.use_cases.DispenseMedicineUseCase;
import com.api.medicine.application.use_cases.RemoveAllergyUseCase;
import com.api.medicine.application.use_cases.ViewPatientRecordUseCase;
import com.api.medicine.domain.entities.Patient;
import com.api.medicine.domain.entities.Prescription;
import com.api.medicine.domain.interfaces.ATMRepository;
import com.api.medicine.domain.interfaces.PrescriptionRepository;
import com.api.medicine.domain.interfaces.User;
import com.api.medicine.domain.interfaces.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/patient")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost", "http://127.0.0.1" })
public class PatientController {

    private final ViewPatientRecordUseCase viewPatientRecordUseCase;
    private final DispenseMedicineUseCase dispenseMedicineUseCase;
    private final AddAllergyUseCase addAllergyUseCase;
    private final RemoveAllergyUseCase removeAllergyUseCase;
    private final UserRepository userRepository;
    private final ATMRepository atmRepository;
    private final PrescriptionRepository prescriptionRepository;

    public PatientController(ViewPatientRecordUseCase viewPatientRecordUseCase,
            DispenseMedicineUseCase dispenseMedicineUseCase,
            AddAllergyUseCase addAllergyUseCase,
            RemoveAllergyUseCase removeAllergyUseCase,
            UserRepository userRepository,
            ATMRepository atmRepository,
            PrescriptionRepository prescriptionRepository) {
        this.viewPatientRecordUseCase = viewPatientRecordUseCase;
        this.dispenseMedicineUseCase = dispenseMedicineUseCase;
        this.addAllergyUseCase = addAllergyUseCase;
        this.removeAllergyUseCase = removeAllergyUseCase;
        this.userRepository = userRepository;
        this.atmRepository = atmRepository;
        this.prescriptionRepository = prescriptionRepository;
    }

    /**
     * GET /api/patient/my-prescriptions/{email}
     * Retrieves the prescription history for the patient with the given email.
     *
     * @param email Patient's email address
     * @return List of prescriptions or 404 if patient not found
     */
    @GetMapping("/my-prescriptions/{email}")
    public ResponseEntity<?> getMyPrescriptions(@PathVariable String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body("Hasta bulunamadı.");
        }

        User user = userOptional.get();

        if (!(user instanceof Patient patient)) {
            return ResponseEntity.status(404).body("Kullanıcı hasta değil.");
        }

        List<Prescription> prescriptions = viewPatientRecordUseCase.getPrescriptionHistory(patient);

        return ResponseEntity.ok(prescriptions);
    }

    /**
     * GET /api/patient/info/{email}
     * Retrieves patient information including allergies.
     *
     * @param email Patient's email address
     * @return Patient entity or 404 if not found
     */
    @GetMapping("/info/{email}")
    public ResponseEntity<?> getPatientInfo(@PathVariable String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body("Hasta bulunamadı.");
        }

        User user = userOptional.get();

        if (!(user instanceof Patient patient)) {
            return ResponseEntity.status(404).body("Kullanıcı hasta değil.");
        }

        return ResponseEntity.ok(patient);
    }

    /**
     * POST /api/patient/dispense
     * Dispenses medicine for a prescription at a specific ATM.
     * Request body should contain: prescriptionId, patientEmail, and optional
     * atmId.
     * If atmId is not provided, defaults to 1L.
     *
     * @param request Map containing prescriptionId, patientEmail, and optional
     *                atmId
     * @return Success or error message based on the dispense operation result
     */
    @PostMapping("/dispense")
    public ResponseEntity<?> dispenseMedicine(@RequestBody Map<String, Object> request) {
        String prescriptionId = (String) request.get("prescriptionId");
        String patientEmail = (String) request.get("patientEmail");

        // Default atmId to 1L if not provided
        Long atmId = request.containsKey("atmId")
                ? Long.valueOf(request.get("atmId").toString())
                : 2L;

        if (prescriptionId == null || patientEmail == null) {
            return ResponseEntity.badRequest().body("Reçete ID ve hasta e-postası gereklidir.");
        }

        boolean success = dispenseMedicineUseCase.execute(prescriptionId, patientEmail, atmId);

        if (success) {
            return ResponseEntity.ok(Map.of(
                    "message", "İlaçlar başarıyla teslim edildi.",
                    "prescriptionId", prescriptionId));
        } else {
            return ResponseEntity.status(400).body(
                    "İlaç teslim edilemedi. Reçete bulunamadı, stok yetersiz veya hasta bilgisi eşleşmedi.");
        }
    }

    /**
     * GET /api/patient/check-stock/{atmId}/{prescriptionId}
     * Checks stock availability for medicines in a prescription at a specific ATM.
     *
     * @param atmId          ATM ID
     * @param prescriptionId Prescription ID
     * @return Map of medicine names to stock availability
     */
    @GetMapping("/check-stock/{atmId}/{prescriptionId}")
    public ResponseEntity<?> checkStock(@PathVariable Long atmId, @PathVariable String prescriptionId) {
        try {
            // Get prescription
            Optional<Prescription> optionalPrescription = prescriptionRepository.findByID(prescriptionId);

            if (optionalPrescription.isEmpty()) {
                return ResponseEntity.status(404).body("Reçete bulunamadı.");
            }

            // Get ATM
            Optional<com.api.medicine.domain.entities.ATM> optionalATM = atmRepository.findById(atmId);

            if (optionalATM.isEmpty()) {
                return ResponseEntity.status(404).body("ATM bulunamadı.");
            }

            Prescription prescription = optionalPrescription.get();
            com.api.medicine.domain.entities.ATM atm = optionalATM.get();

            // Check stock for each medicine
            Map<String, Object> stockInfo = new java.util.HashMap<>();
            for (com.api.medicine.domain.entities.Medicine medicine : prescription.getMedicines()) {
                int available = atm.getStock().getOrDefault(medicine.getName(), 0);
                stockInfo.put(medicine.getName(), Map.of(
                        "available", available > 0,
                        "quantity", available));
            }

            return ResponseEntity.ok(stockInfo);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Stok kontrolü yapılamadı: " + e.getMessage());
        }
    }

    /**
     * POST /api/patient/allergies/add
     * Adds an allergy to the patient's allergy list.
     *
     * @param request Map containing patientEmail and allergyName
     * @return Success or error message
     */
    @PostMapping("/allergies/add")
    public ResponseEntity<?> addAllergy(@RequestBody Map<String, String> request) {
        String patientEmail = request.get("patientEmail");
        String allergyName = request.get("allergyName");

        if (patientEmail == null || allergyName == null || allergyName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Hasta e-postası ve alerji adı gereklidir.");
        }

        boolean success = addAllergyUseCase.execute(patientEmail, allergyName);

        if (success) {
            return ResponseEntity.ok(Map.of(
                    "message", "Alerji başarıyla eklendi.",
                    "allergyName", allergyName.trim()));
        } else {
            return ResponseEntity.status(400).body(
                    "Alerji eklenemedi. Hasta bulunamadı veya alerji zaten mevcut.");
        }
    }

    /**
     * DELETE /api/patient/allergies/remove
     * Removes an allergy from the patient's allergy list.
     *
     * @param request Map containing patientEmail and allergyName
     * @return Success or error message
     */
    @DeleteMapping("/allergies/remove")
    public ResponseEntity<?> removeAllergy(@RequestBody Map<String, String> request) {
        String patientEmail = request.get("patientEmail");
        String allergyName = request.get("allergyName");

        if (patientEmail == null || allergyName == null || allergyName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Hasta e-postası ve alerji adı gereklidir.");
        }

        boolean success = removeAllergyUseCase.execute(patientEmail, allergyName);

        if (success) {
            return ResponseEntity.ok(Map.of(
                    "message", "Alerji başarıyla silindi.",
                    "allergyName", allergyName.trim()));
        } else {
            return ResponseEntity.status(400).body(
                    "Alerji silinemedi. Hasta bulunamadı veya alerji mevcut değil.");
        }
    }
}
