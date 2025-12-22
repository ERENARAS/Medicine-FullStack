package com.api.medicine.application.use_cases;

import com.api.medicine.domain.entities.Medicine;
import com.api.medicine.domain.entities.Prescription;
import com.api.medicine.domain.interfaces.PrescriptionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GetSalesDataUseCase {

    private final PrescriptionRepository prescriptionRepository;

    public GetSalesDataUseCase(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    public List<Map<String, Object>> execute() {
        // Tüm reçeteleri çek
        List<Prescription> allPrescriptions = prescriptionRepository.getAll();

        List<Map<String, Object>> salesDataList = new ArrayList<>();

        for (Prescription prescription : allPrescriptions) {

            // Sadece teslim edilenleri al
            if (!prescription.getDispensed()) {
                continue;
            }

            LocalDate date = prescription.getDate();
            List<Medicine> medicines = prescription.getMedicines();

            for (Medicine medicine : medicines) {
                Map<String, Object> saleRecord = new HashMap<>();
                saleRecord.put("date", date.toString());
                saleRecord.put("medicineName", medicine.getName());
                // Her ilaç adedini 1 kabul ediyoruz
                saleRecord.put("quantity", 1);

                salesDataList.add(saleRecord);
            }
        }

        return salesDataList;
    }
}