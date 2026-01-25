package com.api.medicine.presentation.controllers;

import com.api.medicine.application.services.SymptomAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medical-analysis")
@CrossOrigin(origins = "http://localhost") // Adjust if frontend port changes, docker compose map 80:80
public class MedicalAnalysisController {

    @Autowired
    private SymptomAnalysisService symptomAnalysisService;

    @Autowired
    private com.api.medicine.application.services.DiagnosisOrchestratorService diagnosisOrchestratorService;

    @PostMapping
    public ResponseEntity<String> analyze(@RequestBody String complaint) {
        // Simple string body handling
        String result = symptomAnalysisService.analyzeComplaint(complaint);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/diagnose")
    public ResponseEntity<java.util.Map<String, Object>> diagnose(@RequestBody String complaint) {
        java.util.Map<String, Object> result = diagnosisOrchestratorService.diagnosePatient(complaint);
        return ResponseEntity.ok(result);
    }
}
