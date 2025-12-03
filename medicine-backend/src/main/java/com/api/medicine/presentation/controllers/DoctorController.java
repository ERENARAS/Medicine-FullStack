package com.api.medicine.presentation.controllers;

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

@RestController
@RequestMapping("/api/doctor")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost", "http://127.0.0.1"})
public class DoctorController {

    private final WritePrescriptionUseCase writePrescriptionUseCase;
    private final PrescriptionRepository prescriptionRepository;
    private final UserRepository userRepository;

    // ... Constructor injection (daha sonra ekleyelim)

    public DoctorController(WritePrescriptionUseCase writePrescriptionUseCase, PrescriptionRepository prescriptionRepository, UserRepository userRepository) {
        this.writePrescriptionUseCase = writePrescriptionUseCase;
        this.prescriptionRepository = prescriptionRepository;
        this.userRepository = userRepository;
    }

    // 1. Dashboard Verilerini Çekme (Basitleştirilmiş Versiyon)
    @GetMapping("/dashboard/{doctorEmail}")
    public ResponseEntity<?> getDashboard(@PathVariable String doctorEmail) {
        // Normalde burada JWT/Session ile Doctor nesnesi alınır, şimdi repository'den çekeriz.
        Doctor doctor = (Doctor) userRepository.findByEmail(doctorEmail).orElse(null);

        if (doctor == null || !(doctor instanceof Doctor)) {
            return ResponseEntity.status(403).body("Erişim Reddedildi.");
        }

        // Basit bir dashboard yapısı döndürelim (Sadece tüm reçeteleri filtreleyelim)
        List<Prescription> allPrescriptions = prescriptionRepository.getAll();

        long todayCount = allPrescriptions.stream()
                .filter(p -> p.getDoctor().getEmail().equals(doctorEmail))
                .filter(p -> p.getDate().equals(java.time.LocalDate.now()))
                .count();

        // Daha fazla istatistik için filtreleme yapılabilir...

        return ResponseEntity.ok(Map.of(
                "todayPrescriptions", todayCount,
                "allPrescriptions", allPrescriptions
        ));
    }

    // 2. Yeni Reçete Yazma (WritePrescriptionUseCase'i kullanır)
    @PostMapping("/write-prescription")
    public ResponseEntity<?> writePrescription(@RequestBody Map<String, Object> request) {
        // İhtiyacınız olan alanlar: doctorEmail, patientEmail, medicineNames (List<String>)
        String doctorEmail = (String) request.get("doctorEmail");
        String patientEmail = (String) request.get("patientEmail");
        List<String> medicineNames = (List<String>) request.get("medicineNames");

        // Doctor ve Patient'ı repository'den çekmek gerekir
        Doctor doctor = (Doctor) userRepository.findByEmail(doctorEmail).orElse(null);
        Patient patient = (Patient) userRepository.findByEmail(patientEmail).orElse(null);

        if (doctor == null || patient == null) {
            return ResponseEntity.badRequest().body("Doktor veya Hasta bulunamadı.");
        }

        try {
            // Medicine listesini Map<String, Object> listesinden oluştur
            List<com.api.medicine.domain.entities.Medicine> medicines = medicineNames.stream()
                    .map(com.api.medicine.domain.entities.Medicine::new)
                    .toList();

            // Reçete nesnesini oluştur
            Prescription prescription = com.api.medicine.domain.factory.PrescriptionFactory.createPrescription(doctor, patient, medicines);

            // Use Case'i çalıştır (Alerji kontrolü burada yapılır)
            boolean success = writePrescriptionUseCase.execute(prescription);

            if (success) {
                return ResponseEntity.ok(Map.of("message", "Reçete başarıyla kaydedildi.", "prescriptionId", prescription.getId()));
            } else {
                return ResponseEntity.status(400).body("Reçete kaydedilemedi: Hastanın ilaca alerjisi var.");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Hata: " + e.getMessage());
        }
    }
}