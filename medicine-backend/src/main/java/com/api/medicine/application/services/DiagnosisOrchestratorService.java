package com.api.medicine.application.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DiagnosisOrchestratorService {

    @Autowired
    private SymptomAnalysisService ollamaService;

    @Autowired
    private PythonAIService pythonService;

    private final String DISEASE_SERVICE_SYMPTOMS_URL = "http://disease-service:8000/symptoms";
    private List<String> cachedSymptoms = new ArrayList<>();

    private List<String> getValidSymptoms() {
        if (!cachedSymptoms.isEmpty()) {
            return cachedSymptoms;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.getForEntity(DISEASE_SERVICE_SYMPTOMS_URL, Map.class);

            if (response.getBody() != null && response.getBody().containsKey("symptoms")) {
                cachedSymptoms = (List<String>) response.getBody().get("symptoms");
                System.out.println("✅ Geçerli semptom listesi yüklendi: " + cachedSymptoms.size() + " adet");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Semptom listesi alınamadı: " + e.getMessage());
        }
        return cachedSymptoms;
    }

    public Map<String, Object> diagnosePatient(String userComplaint) {

        // STEP 1: Get valid symptoms from disease-service
        List<String> validSymptoms = getValidSymptoms();
        String symptomsContext = String.join(", ", validSymptoms);

        // STEP 2: Extract English keywords with context
        List<String> englishSymptoms = extractEnglishKeywords(userComplaint, symptomsContext);

        // STEP 3: Get disease prediction from Python service
        Map<String, Object> predictionResult = pythonService.getPrediction(englishSymptoms);

        // STEP 4: Extract Turkish symptom analysis
        String turkishAnalysis = ollamaService.analyzeComplaint(userComplaint);

        // STEP 5: Translate prediction results to Turkish
        if (predictionResult != null && !predictionResult.containsKey("error")) {
            String disease = (String) predictionResult.get("diagnosis");
            String specialist = (String) predictionResult.get("recommended_specialist");
            String description = (String) predictionResult.get("description");

            // Translate to Turkish
            Map<String, String> translatedResults = translateToTurkish(disease, specialist, description);

            predictionResult.put("disease", translatedResults.get("disease"));
            predictionResult.put("specialist", translatedResults.get("specialist"));
            predictionResult.put("description_tr", translatedResults.get("description"));
            predictionResult.put("detailed_analysis", turkishAnalysis);
            predictionResult.put("detected_symptoms_en", englishSymptoms);
        } else {
            if (predictionResult == null) {
                predictionResult = new HashMap<>();
            }
            predictionResult.put("detailed_analysis", turkishAnalysis);
            predictionResult.put("detected_symptoms_en", englishSymptoms);
        }

        return predictionResult;
    }

    private List<String> extractEnglishKeywords(String complaint, String validSymptomsContext) {
        String prompt = String.format(
                """
                        GÖREV: Aşağıdaki hasta şikayetinden semptomları çıkar ve REFERANS LİSTESİNDEKİ en uygun İngilizce terimlerle eşleştir.

                        REFERANS SEMPTOMLAR (SADECE BUNLARI KULLAN):
                        %s

                        KURALLAR:
                        1. Çıktı SADECE JSON listesi formatında: ["symptom1", "symptom2"]
                        2. Markdown, açıklama, başka metin ekleme
                        3. Referans listesinde eşleşme yoksa en yakın terimi seç

                        HASTA ŞİKAYETİ: "%s"

                        ÇIKTI (Sadece JSON listesi):
                        """,
                validSymptomsContext, complaint);

        String aiResponse = ollamaService.askLlamaSpecificPrompt(prompt);
        System.out.println("🔍 Extracted English Keywords: " + aiResponse);

        return parseListFromResponse(aiResponse);
    }

    private Map<String, String> translateToTurkish(String disease, String specialist, String description) {
        Map<String, String> result = new HashMap<>();

        String translationPrompt = String.format("""
                GÖREV: Aşağıdaki tıbbi terimleri Türkçeye çevir. Sadece çevirileri ver, açıklama ekleme.

                FORMAT:
                Hastalık: [Türkçe Hastalık Adı]
                Uzman: [Türkçe Uzman Adı]
                Açıklama: [Türkçe Açıklama]

                VERİLER:
                Hastalık (EN): %s
                Uzman (EN): %s
                Açıklama (EN): %s

                TÜRKÇE ÇEVİRİ:
                """, disease, specialist, description);

        String translationResponse = ollamaService.askLlamaSpecificPrompt(translationPrompt);
        System.out.println("🌐 Translation Response: " + translationResponse);

        // Parse the translation response
        result.put("disease", extractField(translationResponse, "Hastalık:", disease));
        result.put("specialist", extractField(translationResponse, "Uzman:", specialist));
        result.put("description", extractField(translationResponse, "Açıklama:", description));

        return result;
    }

    private String extractField(String text, String fieldName, String fallback) {
        try {
            Pattern pattern = Pattern.compile(fieldName + "\\s*(.+?)(?=\\n(?:Hastalık:|Uzman:|Açıklama:|$))",
                    Pattern.DOTALL);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (Exception e) {
            System.err.println("⚠️ Field extraction failed for " + fieldName);
        }
        return fallback;
    }

    private List<String> parseListFromResponse(String response) {
        List<String> symptoms = new ArrayList<>();
        String cleanResponse = response.replace("```json", "").replace("```", "").trim();

        Pattern pattern = Pattern.compile("\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(cleanResponse);
        while (matcher.find()) {
            symptoms.add(matcher.group(1).toLowerCase());
        }
        return symptoms;
    }
}
