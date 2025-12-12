package com.api.medicine.presentation.controllers;

import com.api.medicine.application.use_cases.AddStockUseCase;
import com.api.medicine.domain.entities.ATM;
import com.api.medicine.domain.interfaces.ATMRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * PharmacyController handles pharmacy staff operations related to ATM stock
 * management.
 * Provides endpoints for adding stock and viewing current inventory.
 */
@RestController
@RequestMapping("/api/pharmacy")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost", "http://127.0.0.1" })
public class PharmacyController {

    private final AddStockUseCase addStockUseCase;
    private final ATMRepository atmRepository;

    public PharmacyController(AddStockUseCase addStockUseCase, ATMRepository atmRepository) {
        this.addStockUseCase = addStockUseCase;
        this.atmRepository = atmRepository;
    }

    /**
     * POST /api/pharmacy/add-stock
     * Adds stock to a specific ATM for a given medicine.
     * 
     * @param request Map containing:
     *                - atmId (Long, mandatory): The ATM ID to add stock to
     *                - medicineName (String, mandatory): Name of the medicine
     *                - quantity (Integer, mandatory): Quantity to add (must be
     *                positive)
     * @return ResponseEntity with success message or error details
     */
    @PostMapping("/add-stock")
    public ResponseEntity<?> addStock(@RequestBody Map<String, Object> request) {
        try {
            // Extract and validate parameters
            if (!request.containsKey("atmId") || request.get("atmId") == null) {
                return ResponseEntity.badRequest().body("ATM ID gereklidir.");
            }

            if (!request.containsKey("medicineName") || request.get("medicineName") == null) {
                return ResponseEntity.badRequest().body("İlaç adı gereklidir.");
            }

            if (!request.containsKey("quantity") || request.get("quantity") == null) {
                return ResponseEntity.badRequest().body("Miktar gereklidir.");
            }

            // Parse parameters
            Long atmId = Long.valueOf(request.get("atmId").toString());
            String medicineName = request.get("medicineName").toString().trim();
            Integer quantity = Integer.valueOf(request.get("quantity").toString());

            // Additional validation
            if (medicineName.isEmpty()) {
                return ResponseEntity.badRequest().body("İlaç adı boş olamaz.");
            }

            if (quantity <= 0) {
                return ResponseEntity.badRequest().body("Miktar pozitif bir sayı olmalıdır.");
            }

            // Execute use case
            addStockUseCase.execute(atmId, medicineName, quantity);

            // Return success response
            return ResponseEntity.ok(Map.of(
                    "message", "Stok başarıyla eklendi.",
                    "atmId", atmId,
                    "medicineName", medicineName,
                    "quantityAdded", quantity));

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Geçersiz sayı formatı: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Stok eklenirken bir hata oluştu: " + e.getMessage());
        }
    }

    /**
     * GET /api/pharmacy/stock/{atmId}
     * Retrieves the current stock inventory for a specific ATM.
     * 
     * @param atmId The ATM ID to retrieve stock for
     * @return ResponseEntity containing the stock map (medicine name -> quantity)
     *         or error message
     */
    @GetMapping("/stock/{atmId}")
    public ResponseEntity<?> getStock(@PathVariable Long atmId) {
        try {
            // Validate ATM ID
            if (atmId == null) {
                return ResponseEntity.badRequest().body("ATM ID gereklidir.");
            }

            // Find ATM
            Optional<ATM> atmOptional = atmRepository.findById(atmId);

            if (atmOptional.isEmpty()) {
                return ResponseEntity.status(404).body("ATM bulunamadı: ID = " + atmId);
            }

            ATM atm = atmOptional.get();
            Map<String, Integer> stock = atm.getStock();

            // Return stock information
            return ResponseEntity.ok(Map.of(
                    "atmId", atmId,
                    "location", atm.getLocation() != null ? atm.getLocation() : "Bilinmiyor",
                    "stock", stock != null ? stock : Map.of()));

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Stok bilgisi alınırken bir hata oluştu: " + e.getMessage());
        }
    }
}
