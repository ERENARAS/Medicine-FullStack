package com.api.medicine.domain.interfaces;

import com.api.medicine.domain.entities.Patient;
import com.api.medicine.domain.entities.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaPrescriptionRepository extends JpaRepository<Prescription, UUID> {
    // Spring Data JPA metod isimlerinden sorguyu otomatik türetir
    Optional<List<Prescription>> findByPatient(Patient patient);

    @Query("SELECT p FROM Prescription p JOIN p.medicines m ORDER BY p.date ASC")
    List<Prescription> findAllPrescriptionsOrderedByDate();
}