package com.api.medicine.infrastructure.repositories;

import com.api.medicine.domain.entities.Patient;
import com.api.medicine.domain.entities.Prescription;
import com.api.medicine.domain.interfaces.JpaPrescriptionRepository;
import com.api.medicine.domain.interfaces.PrescriptionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PostgresPrescriptionRepository implements PrescriptionRepository {

    private final JpaPrescriptionRepository jpaRepository;

    public PostgresPrescriptionRepository(JpaPrescriptionRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Prescription prescription) {
        jpaRepository.save(prescription);
    }

    @Override
    public List<Prescription> getAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Optional<List<Prescription>> findByPatient(Patient patient) {
        return jpaRepository.findByPatient(patient);
    }

    @Override
    public void delete(String id) {
        // String ID'yi UUID'ye çevirmemiz gerekiyor
        try {
            jpaRepository.deleteById(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            System.out.println("Geçersiz UUID formatı: " + id);
        }
    }

    @Override
    public Optional<Prescription> findByID(String id) {
        try {
            return jpaRepository.findById(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteByID(String id) {
        delete(id); // delete metodu ile aynı işi yapıyor
    }
}