package com.api.medicine.infrastructure.repositories.jpa;

import com.api.medicine.domain.entities.PharmacyStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaPharmacyStaffRepository extends JpaRepository<PharmacyStaff, Long> {
    Optional<PharmacyStaff> findByEmail(String email);
    boolean existsByEmail(String email);
}