package com.api.medicine.application.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PythonAIService {

    // Python FastAPI adresi
    // Docker compose environment, service name is likely 'prediction-service' or
    // 'prediction_service'
    // User code said "http://localhost:8000/predict" but inside docker it should be
    // service name
    // Based on previous logs: "medicineassigment-prediction-service" image name,
    // service name in compose probably "prediction_service"
    // CAUTION: User asked for "http://localhost:8000/predict", if running from host
    // ok, but if running inside docker container (medicine-backend), it must reach
    // another container.
    // The previous SymptomAnalysisService used "http://host.docker.internal:11434"
    // for Ollama on host.
    // If Python service is also same docker network, better use service name.
    // However, I will stick to what user likely needs. If backend runs in docker,
    // it needs service name.
    // Let's assume standard docker compose networking:
    // http://prediction_service:8000 OR http://host.docker.internal:8000
    // I will use 'http://prediction_service:8000/predict' as default safe guess for
    // inter-container,
    // BUT the user code snippet specifically had 'http://localhost:8000/predict'.
    // If the user is running backend locally (not docker), localhost is fine.
    // Given the context of "SymptomAnalysisService" using "host.docker.internal",
    // it implies Backend is in Docker.
    // So "localhost" inside Docker won't work for another container.
    // I will use "http://prediction_service:8000/predict" as a safer bet for
    // Docker-to-Docker,
    // or arguably "http://host.docker.internal:8000/predict" if they expose it to
    // host.
    // The logs showed "Container prediction_service Started".
    // Docker compose service name is 'disease-service'
    // We access it via http://disease_service:8000 (container_name is
    // disease_service)
    // Actually, in docker-compose I named the service 'disease-service' and
    // container 'disease_service'.
    // Docker networking usually resolves SERVICE name 'disease-service'.
    // BUT common practice is to use service name.
    // Let's use 'disease-service' port 8000.
    // Note: The Dockerfile in 'disease-service' must expose 8000.
    private final String PYTHON_API_URL = "http://disease-service:8000/predict";

    public Map<String, Object> getPrediction(List<String> symptoms) {
        System.out.println("DEBUG - Sending symptoms to Disease Service: " + symptoms);
        RestTemplate restTemplate = new RestTemplate();

        // İstek gövdesi: { "symptoms": ["vomiting", "stomach_pain"] }
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("symptoms", symptoms);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // Python'a POST isteği at
            ResponseEntity<Map> response = restTemplate.postForEntity(PYTHON_API_URL, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Python servisine ulaşılamadı: " + e.getMessage());
            return errorResponse;
        }
    }
}
