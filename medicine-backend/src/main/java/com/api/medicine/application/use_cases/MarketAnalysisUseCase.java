package com.api.medicine.application.use_cases;

import java.util.List;

import org.springframework.stereotype.Service;

import com.api.medicine.domain.entities.MedicineForecast;
import com.api.medicine.domain.interfaces.MedicineForecastRepository;

@Service
public class MarketAnalysisUseCase {

    private final MedicineForecastRepository forecastRepository;

    public MarketAnalysisUseCase(MedicineForecastRepository forecastRepository) {
        this.forecastRepository = forecastRepository;
    }

    public List<MedicineForecast> getTop5Predictions() {
        return forecastRepository.findTop5ByOrderByPredictedSalesDesc();
    }

    public List<MedicineForecast> searchPredictions(String medicineName) {
        if (medicineName == null || medicineName.trim().isEmpty()) {
            return List.of();
        }
        return forecastRepository.findByMedicineNameContainingIgnoreCase(medicineName);
    }
}