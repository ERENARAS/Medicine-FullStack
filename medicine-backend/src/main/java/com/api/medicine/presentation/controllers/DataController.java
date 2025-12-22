package com.api.medicine.presentation.controllers;

import com.api.medicine.application.use_cases.GetSalesDataUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/data")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost", "http://127.0.0.1" })
public class DataController {

    private final GetSalesDataUseCase getSalesDataUseCase;

    public DataController(GetSalesDataUseCase getSalesDataUseCase) {
        this.getSalesDataUseCase = getSalesDataUseCase;
    }

    /**
     * GET /api/data/historical-sales
     * Python ML servisi bu endpoint'ten geçmiş satış verilerini çekecektir.
     */
    @GetMapping("/historical-sales")
    public ResponseEntity<List<Map<String, Object>>> getHistoricalSalesData() {
        List<Map<String, Object>> salesData = getSalesDataUseCase.execute();
        return ResponseEntity.ok(salesData);
    }
}