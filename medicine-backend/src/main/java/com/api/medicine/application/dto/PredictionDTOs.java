package com.api.medicine.application.dto;

import java.util.List;

public class PredictionDTOs {

    // Python'a giden istek
    public static class MarketAnalysisRequest {
        public List<MedicineSalesData> medicines;

        public MarketAnalysisRequest(List<MedicineSalesData> medicines) {
            this.medicines = medicines;
        }
    }

    public static class MedicineSalesData {
        public Long medicine_id;
        public String medicine_name;
        public List<Integer> sales_history; // Örn: [10, 12, 15, 8...]
    }

    // Python'dan gelen cevap
    public static class MarketAnalysisResponse {
        public List<PredictionResult> all_predictions;
        public List<PredictionResult> top_5_bestsellers;
    }

    public static class PredictionResult {
        public Long medicine_id;
        public String medicine_name;
        public int predicted_sales;
    }
}