package com.api.medicine.application.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class SymptomAnalysisService {

    // Docker içinden host'a erişim için host.docker.internal kullanıyoruz
    private final String OLLAMA_API_URL = "http://host.docker.internal:11434/api/generate";

    public String analyzeComplaint(String userComplaint) {
        RestTemplate restTemplate = new RestTemplate();

        // Model eğitim formatımıza uygun prompt hazırlıyoruz
        String prompt = "### Instruction:\n" +
                "Şikayeti analiz et ve önemli tıbbi bulguları maddeleyerek özetle.\n\n" +
                "### Input:\n" +
                userComplaint + "\n\n" +
                "### Response:\n";

        // JSON Request Body hazırlığı
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "medicine-ai"); // User's custom model name
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // Ollama'ya POST isteği atıyoruz
            ResponseEntity<Map> response = restTemplate.postForEntity(OLLAMA_API_URL, entity, Map.class);

            // Cevabı alıyoruz
            if (response.getBody() != null && response.getBody().containsKey("response")) {
                return response.getBody().get("response").toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "AI Servisine ulaşılamadı. Lütfen Ollama'nın çalıştığından emin olun. Hata: " + e.getMessage();
        }
        return "Analiz yapılamadı.";
    }
}
