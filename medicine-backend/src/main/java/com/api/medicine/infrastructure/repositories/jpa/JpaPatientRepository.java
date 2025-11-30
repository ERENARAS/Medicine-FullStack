package com.api.medicine.infrastructure.repositories.jpa;

import com.api.medicine.domain.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// DİKKAT: Burada "extends UserRepository" YOK. Sadece JpaRepository.
@Repository
public interface JpaPatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByEmail(String email);
    boolean existsByEmail(String email);
}