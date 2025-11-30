package com.api.medicine.application.use_cases;

import com.api.medicine.domain.entities.ATM;
import com.api.medicine.domain.entities.Medicine;
import com.api.medicine.domain.entities.Prescription;
import com.api.medicine.domain.interfaces.ATMRepository;
import com.api.medicine.domain.interfaces.PrescriptionRepository;
import com.api.medicine.domain.interfaces.User;
import com.api.medicine.domain.interfaces.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DispenseMedicineUseCase {
    private final PrescriptionRepository prescriptionRepository;
    private final ATMRepository atmRepository;
    private final UserRepository userRepository;

    public DispenseMedicineUseCase(PrescriptionRepository prescriptionRepository,
                                   ATMRepository atmRepository,
                                   UserRepository userRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.atmRepository = atmRepository;
        this.userRepository = userRepository;
    }

    @Transactional // Stok düşürme ve reçete silme aynı anda yapılmalıdır (ACID)
    public boolean execute(String prescriptionId, String patientEmail, Long atmId) {

        Optional<User> patient;
        patient =  userRepository.findByEmail(patientEmail);
        if (patient == null) return false;

        Optional<Prescription> optionalPrescription = prescriptionRepository.findByID(prescriptionId);
        if (optionalPrescription.isEmpty()) return false;

        Prescription prescription = optionalPrescription.get();

        if (!prescription.getPatient().getEmail().equals(patientEmail)) return false;

        ATM atm = atmRepository.findById(atmId).orElse(null);
        if (atm == null) return false;

        Map<String, Integer> stock = atm.getStock();
        List<Medicine> medicines = prescription.getMedicines();

        for (Medicine med : medicines) {
            String name = med.getName();
            int available = stock.getOrDefault(name, 0);
            if (available <= 0) {
                return false;
            }
        }
        for (Medicine med : medicines) {
            String name = med.getName();
            stock.put(name, stock.get(name) - 1);
        }
        atmRepository.save(atm);
        prescriptionRepository.delete(prescriptionId);

        return true;
    }
}