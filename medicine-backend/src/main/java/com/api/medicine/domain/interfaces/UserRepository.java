package com.api.medicine.domain.interfaces;

import com.api.medicine.domain.entities.Patient;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    boolean save(User user);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    /**
     * Tüm Patient (Hasta) tipli kullan?c?lar? d?ndürür.
     */
    List<Patient> findAllPatients();
}