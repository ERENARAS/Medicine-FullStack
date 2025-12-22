package com.api.medicine.presentation.controllers;

import com.api.medicine.domain.entities.Patient;
import com.api.medicine.domain.entities.Prescription;
import com.api.medicine.domain.interfaces.UserRepository;
import com.api.medicine.domain.interfaces.PrescriptionRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/doctor/dashboard")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost", "http://127.0.0.1" })
public class DashboardController {

    private final PrescriptionRepository prescriptionRepository;
    private final UserRepository userRepository;

    public DashboardController(PrescriptionRepository prescriptionRepository, UserRepository userRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/{doctorEmail}")
    public ResponseEntity<Map<String, Object>> getDoctorStats(@PathVariable String doctorEmail) {

        List<Prescription> allPrescriptions = prescriptionRepository.getAll().stream()
                .filter(p -> p.getDoctor() != null && p.getDoctor().getEmail().equals(doctorEmail))
                .collect(Collectors.toList());

        long todayCount = 0;
        long last7DaysCount = 0;
        long last30DaysCount = 0;
        int allergicPatientCount = 0;

        LocalDate today = LocalDate.now();

        for (Prescription p : allPrescriptions) {
            long daysBetween = ChronoUnit.DAYS.between(p.getDate(), today);

            if (daysBetween == 0) {
                todayCount++;
            }
            if (daysBetween >= 0 && daysBetween <= 7) {
                last7DaysCount++;
            }
            if (daysBetween >= 0 && daysBetween <= 30) {
                last30DaysCount++;
            }
        }

        List<Patient> allPatients = userRepository.findAllPatients();
        allergicPatientCount = (int) allPatients.stream()
                .filter(p -> p.getAllergicMedicines() != null && !p.getAllergicMedicines().isEmpty())
                .count();

        List<Map<String, Object>> lastTransactions = allPrescriptions.stream()
                .map(p -> {
                    Patient patient = p.getPatient();

                    Map<String, Object> transactionMap = new HashMap<>();

                    // Safely parse patient name
                    String fullName = patient.getName() != null ? patient.getName() : "";
                    String[] nameParts = fullName.split(" ");
                    String firstName = nameParts.length > 0 ? nameParts[0] : "";
                    String lastName = nameParts.length > 1 ? nameParts[1] : "";

                    transactionMap.put("name", firstName);
                    transactionMap.put("surname", lastName);
                    transactionMap.put("age", 35);
                    transactionMap.put("prescriptionCode", p.getId().toString().substring(0, 8));
                    transactionMap.put("date", p.getDate().toString());

                    return transactionMap;
                })
                .collect(Collectors.toCollection(ArrayList::new));

        Map<String, Object> response = new HashMap<>();
        response.put("todayPrescriptions", todayCount);
        response.put("last7DaysPrescriptions", last7DaysCount);
        response.put("last30DaysPrescriptions", last30DaysCount);
        response.put("allergicPatientCount", allergicPatientCount);
        response.put("allPrescriptions", lastTransactions);

        return ResponseEntity.ok(response);
    }
}