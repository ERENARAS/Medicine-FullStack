package com.api.medicine.application.use_cases;

import com.api.medicine.domain.entities.Patient;
import com.api.medicine.domain.entities.Prescription;
import com.api.medicine.domain.interfaces.PrescriptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ViewPatientRecordUseCase {

    private final PrescriptionRepository prescriptionRepository;

    public ViewPatientRecordUseCase(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    /**
     * Belirtilen hastanın geçmiş reçetelerini döndürür.
     * Alerjiler Patient nesnesinden zaten erişilebilirdir.
     *
     * @param patient Görüntülenecek hasta
     * @return Reçete listesi veya hiç reçete yoksa boş bir liste.
     */
    public List<Prescription> getPrescriptionHistory(Patient patient) {

        Optional<List<Prescription>> optionalPrescriptions = prescriptionRepository.findByPatient(patient);

        return optionalPrescriptions.orElse(List.of());
    }
}