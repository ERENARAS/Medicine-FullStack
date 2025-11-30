package com.api.medicine.application.use_cases;

import com.api.medicine.domain.entities.*;
import com.api.medicine.domain.interfaces.PrescriptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WritePrescriptionUseCase {
    private final PrescriptionRepository repository;

    public WritePrescriptionUseCase(PrescriptionRepository repository) {
        this.repository = repository;
    }

    /**
     * Reçete oluşturma ve kaydetme işlemini yürütür.
     * Alerji kontrolü yapılır ve başarılı olursa kaydedilir.
     *
     * @return true işlem başarılıysa, false alerji varsa veya başka bir hata oluşursa.
     */
    public boolean execute(Prescription prescription) {
        Patient patient = prescription.getPatient();
        List<Medicine> meds = prescription.getMedicines();
        List<String> allergies = patient.getAllergicMedicines();

        // 1. Alerji Kontrolü (İş Kuralı)
        for (Medicine med : meds) {
            if (allergies.contains(med.getName())) {
                // Controller bu false'u alıp "Alerjik İlaç Hatası" dönecektir.
                return false;
            }
        }

        // 2. Kaydetme (Repository'nin save metodu JpaRepository'den gelir.)
        repository.save(prescription);
        return true;
    }
}