package com.api.medicine.presentation.controllers;

import com.api.medicine.application.use_cases.ListAllPatientsUseCase;
import com.api.medicine.application.use_cases.ViewPatientRecordUseCase;
import com.api.medicine.application.use_cases.WritePrescriptionUseCase;
import com.api.medicine.domain.entities.Doctor;
import com.api.medicine.domain.entities.Patient;
import com.api.medicine.domain.entities.Prescription;
import com.api.medicine.domain.interfaces.PrescriptionRepository;
import com.api.medicine.domain.interfaces.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.api.medicine.domain.entities.Medicine;
import com.api.medicine.domain.factory.PrescriptionFactory;

@RestController
@RequestMapping("/api/doctor")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost", "http://127.0.0.1" })
public class DoctorController {

    private final WritePrescriptionUseCase writePrescriptionUseCase;
    private final ViewPatientRecordUseCase viewPatientRecordUseCase; // Eksik UseCase
    private final PrescriptionRepository prescriptionRepository;
    private final UserRepository userRepository;
    private final ListAllPatientsUseCase listAllPatientsUseCase;
    private final com.api.medicine.domain.interfaces.MedicineRepository medicineRepository;

    public DoctorController(WritePrescriptionUseCase writePrescriptionUseCase,
            ViewPatientRecordUseCase viewPatientRecordUseCase,
            PrescriptionRepository prescriptionRepository,
            UserRepository userRepository, ListAllPatientsUseCase listAllPatientsUseCase,
            com.api.medicine.domain.interfaces.MedicineRepository medicineRepository) {
        this.writePrescriptionUseCase = writePrescriptionUseCase;
        this.viewPatientRecordUseCase = viewPatientRecordUseCase;
        this.prescriptionRepository = prescriptionRepository;
        this.userRepository = userRepository;
        this.listAllPatientsUseCase = listAllPatientsUseCase;
        this.medicineRepository = medicineRepository;
    }

    // 1. Hasta Kay?tlar?n? G?r?nt?leme (Doctor Dashboard'da kullan?lmaz ama
    // sistemde olmal?)
    @GetMapping("/patient-record/{patientEmail}")
    public ResponseEntity<?> viewPatientRecord(@PathVariable String patientEmail) {
        Object user = userRepository.findByEmail(patientEmail).orElse(null);

        if (!(user instanceof Patient patient)) {
            return ResponseEntity.status(404).body("Hasta bulunamad?.");
        }

        List<Prescription> history = viewPatientRecordUseCase.getPrescriptionHistory(patient);

        return ResponseEntity.ok(Map.of(
                "patient", patient,
                "prescriptionHistory", history));
    }

    // 2. Yeni Reçete Yazma (WritePrescriptionUseCase'i kullanır)
    @PostMapping("/write-prescription")
    public ResponseEntity<?> writePrescription(@RequestBody Map<String, Object> request) {

        String doctorEmail = (String) request.get("doctorEmail");
        String patientEmail = (String) request.get("patientEmail");
        List<String> medicineNames = (List<String>) request.get("medicineNames");

        Doctor doctor = (Doctor) userRepository.findByEmail(doctorEmail).orElse(null);
        Patient patient = (Patient) userRepository.findByEmail(patientEmail).orElse(null);

        if (doctor == null || patient == null) {
            return ResponseEntity.badRequest().body("Doktor veya Hasta bulunamad?.");
        }

        try {
            // Medicine listesini DB'den çek
            List<Medicine> medicines = new java.util.ArrayList<>();
            for (String mName : medicineNames) {
                Medicine m = medicineRepository.findByName(mName);
                if (m != null) {
                    medicines.add(m);
                } else {
                    // İlaç bulunamadıysa ne yapmalı? Şimdilik loglayıp devam edelim veya hata
                    // fırlatalım.
                    // Basitlik için atlıyoruz.
                    System.out.println("Medicine not found: " + mName);
                }
            }

            if (medicines.isEmpty()) {
                return ResponseEntity.badRequest().body("Seçilen ilaçlar sistemde bulunamadı.");
            }

            // Reçete nesnesini olu?tur (PrescriptionFactory kullan?m?)
            Prescription prescription = PrescriptionFactory.createPrescription(doctor, patient, medicines);

            // Use Case'i çal??t?r (Alerji kontrolü burada yap?l?r)
            boolean success = writePrescriptionUseCase.execute(prescription);

            if (success) {
                return ResponseEntity
                        .ok(Map.of("message", "Reçete başarıyla kaydedildi.", "prescriptionId", prescription.getId()));
            } else {
                return ResponseEntity.status(400).body("Reçete kaydedilemedi: Hastanın ilaca alerjisi var.");
            }
        } catch (Exception e) {
            // Reçete olu?turma s?ras?nda (Factory) veya use case'te hata olu?ursa yakala
            return ResponseEntity.internalServerError().body("Hata: " + e.getMessage());
        }
    }

    @GetMapping("/patients")
    public ResponseEntity<List<Patient>> getAllPatients() {
        // ListAllPatientsUseCase'i çağır
        List<Patient> patients = listAllPatientsUseCase.execute();

        // Örnek bir Patient listesi döner
        return ResponseEntity.ok(patients);
    }
}