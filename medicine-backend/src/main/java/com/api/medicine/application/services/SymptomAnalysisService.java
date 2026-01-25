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

        // Simple symptom extraction prompt - Turkish output
        String systemMessage = "Sen bir tıbbi asistan botsun. Hastanın şikayetlerinden semptomları çıkar.";

        String finalPrompt = """
                GÖREV: Aşağıdaki hasta şikayetinden semptomları çıkar ve Türkçe listele.

                KURALLAR:
                - Semptomları madde madde listele
                - Her semptom için süre ve şiddet bilgisi varsa ekle
                - Sadece semptomları yaz, ek yorum yapma

                ÖRNEK:
                Metin: "Başım 2 gündür ağrıyor ve karnımda şişlik var"
                Çıktı:
                Semptom: Baş ağrısı
                Süre: 2 gün
                Şiddet/Detay: Belirtilmemiş

                Semptom: Karın şişliği
                Süre: Belirtilmemiş
                Şiddet/Detay: Belirtilmemiş
                ---

                GERÇEK VERİ:
                Metin: "%s"
                Çıktı:
                """.formatted(userComplaint);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "medicine-ai");
        requestBody.put("system", systemMessage);
        requestBody.put("prompt", finalPrompt);
        requestBody.put("stream", false);

        Map<String, Object> options = new HashMap<>();
        options.put("temperature", 0.1);
        options.put("num_predict", 300);
        requestBody.put("options", options);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(OLLAMA_API_URL, entity, Map.class);

            if (response.getBody() != null && response.getBody().containsKey("response")) {
                return response.getBody().get("response").toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "AI Servisine ulaşılamadı. Lütfen Ollama'nın çalıştığından emin olun. Hata: " + e.getMessage();
        }
        return "Analiz yapılamadı.";
    }

    public String askLlamaSpecificPrompt(String customPrompt) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "medicine-ai");
        requestBody.put("prompt", customPrompt);
        requestBody.put("stream", false);

        Map<String, Object> options = new HashMap<>();
        options.put("temperature", 0.0);
        requestBody.put("options", options);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(OLLAMA_API_URL, entity, Map.class);
            if (response.getBody() != null && response.getBody().containsKey("response")) {
                return response.getBody().get("response").toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "[]";
    }
}
