package com.api.medicine.presentation.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.medicine.application.use_cases.MarketAnalysisUseCase;
import com.api.medicine.domain.entities.MedicineForecast;

@RestController
@RequestMapping("/api/analysis")
@org.springframework.web.bind.annotation.CrossOrigin(origins = "*")
public class AnalysisController {

    private final MarketAnalysisUseCase marketAnalysisUseCase;

    public AnalysisController(MarketAnalysisUseCase marketAnalysisUseCase) {
        this.marketAnalysisUseCase = marketAnalysisUseCase;
    }

    @GetMapping("/predictions/top-5")
    public ResponseEntity<List<MedicineForecast>> getTop5Predictions() {
        return ResponseEntity.ok(marketAnalysisUseCase.getTop5Predictions());
    }

    @GetMapping("/predictions/search")
    public ResponseEntity<List<MedicineForecast>> searchPredictions(@RequestParam String name) {
        return ResponseEntity.ok(marketAnalysisUseCase.searchPredictions(name));
    }
}
