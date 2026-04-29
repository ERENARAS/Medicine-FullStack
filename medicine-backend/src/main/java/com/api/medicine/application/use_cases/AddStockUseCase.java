package com.api.medicine.application.use_cases;

import com.api.medicine.domain.entities.ATM;
import com.api.medicine.domain.interfaces.ATMRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AddStockUseCase {
    private final ATMRepository atmRepository;

    public AddStockUseCase(ATMRepository atmRepository) {
        this.atmRepository = atmRepository;
    }

    public void execute(Long atmId, String medicineName, int quantity) {
        // Validate inputs
        if (atmId == null || medicineName == null || medicineName.trim().isEmpty() || quantity <= 0) {
            throw new IllegalArgumentException("ATM ID, ilaç adı ve pozitif miktar gereklidir.");
        }

        // Find ATM by ID
        ATM atm = atmRepository.findById(atmId)
                .orElseThrow(() -> new RuntimeException("ATM bulunamadı: ID = " + atmId));

        // Update stock using ATM entity method
        atm.increaseStock(medicineName.trim(), quantity);

        // Save updated ATM
        atmRepository.save(atm);
    }
}