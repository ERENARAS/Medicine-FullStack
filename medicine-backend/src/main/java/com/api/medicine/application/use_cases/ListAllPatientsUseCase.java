// src/main/java/com/api/medicine/application/use_cases/ListAllPatientsUseCase.java
package com.api.medicine.application.use_cases;

import com.api.medicine.domain.entities.Patient;
import com.api.medicine.domain.interfaces.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListAllPatientsUseCase {
    private final UserRepository userRepository;

    public ListAllPatientsUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Sistemdeki tüm kayıtlı hastaları döndürür.
     * @return Tüm hastaların listesi.
     */
    public List<Patient> execute() {
        // UserRepository içindeki findAllPatients metodunu çağırır.
        return userRepository.findAllPatients();
    }
}