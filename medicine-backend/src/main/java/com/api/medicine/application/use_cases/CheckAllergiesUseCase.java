package com.api.medicine.application.use_cases;

import com.api.medicine.domain.entities.Medicine;
import com.api.medicine.domain.entities.Patient;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * CheckPatientAllergiesUseCase, bir ilacın hastanın alerji listesiyle çakışıp çakışmadığını kontrol eder.
 */
@Service // Spring tarafından yönetilen bir servis olduğunu belirtir
public class CheckAllergiesUseCase {

    /**
     * Verilen ilacın, hastanın alerji listesinde olup olmadığını kontrol eder.
     *
     * @param patient  Alerji bilgisi kontrol edilecek hasta
     * @param medicine Kontrol edilecek ilaç
     * @return true → hasta bu ilaca alerjiktir, false → güvenli
     */
    public boolean hasAllergy(Patient patient, Medicine medicine) {
        List<String> allergicMeds = patient.getAllergicMedicines();
        return allergicMeds.contains(medicine.getName());
    }
}