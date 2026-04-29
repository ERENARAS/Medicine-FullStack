package com.api.medicine.application.use_cases;

import com.api.medicine.domain.entities.Prediction;
import com.api.medicine.domain.interfaces.PredictionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GetPredictionUseCase {

    private final PredictionRepository predictionRepository;

    public GetPredictionUseCase(PredictionRepository predictionRepository) {
        this.predictionRepository = predictionRepository;
    }

    public Map<String, Object> execute() {
        try {
            // Target date: 1st day of next month
            LocalDate nextMonth = LocalDate.now().plusMonths(1).withDayOfMonth(1);

            // Try explicit next month
            List<Prediction> predictions = predictionRepository.findByPredictionDate(nextMonth);

            // Fallback: If no predictions for next month, try finding any recent ones (e.g.
            // current month or just all)
            // For now, let's assume the batch script sets the correct date.
            // If empty, we could broaden search or return empty.
            if (predictions.isEmpty()) {
                // Warning logic or try finding *all* and getting latest?
                // Let's keep it simple: return what we found (empty).
                // Alternatively, return a message to run the batch job.
            }

            // Convert List<Prediction> to Map<MedicineName, Data>
            Map<String, Object> resultMap = new HashMap<>();

            for (Prediction p : predictions) {
                Map<String, Object> data = new HashMap<>();
                data.put("next_month_demand", p.getPredictedValue());
                data.put("confidence", p.getConfidenceScore());
                // data.put("date", p.getPredictionDate().toString());

                resultMap.put(p.getMedicineName(), data);
            }

            return resultMap;

        } catch (Exception e) {
            System.err.println("Tahmin verileri veritabanından alınamadı: " + e.getMessage());
            return Map.of("error", "Tahmin verileri alınamadı.", "detail", e.getMessage());
        }
    }
}