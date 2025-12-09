package com.api.medicine.application.use_cases;

import com.api.medicine.domain.entities.Patient;
import com.api.medicine.domain.interfaces.User;
import com.api.medicine.domain.interfaces.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AddAllergyUseCase {

    private final UserRepository userRepository;

    public AddAllergyUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Adds an allergy to a patient's allergic medicines list.
     *
     * @param patientEmail Email of the patient
     * @param allergyName  Name of the medicine/substance to add as allergy
     * @return true if successfully added, false otherwise
     */
    public boolean execute(String patientEmail, String allergyName) {
        if (patientEmail == null || allergyName == null || allergyName.trim().isEmpty()) {
            return false;
        }

        Optional<User> userOptional = userRepository.findByEmail(patientEmail);
        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();
        if (!(user instanceof Patient patient)) {
            return false;
        }

        // Check if allergy already exists
        if (patient.getAllergicMedicines().contains(allergyName.trim())) {
            return false; // Already exists
        }

        // Add the allergy
        patient.getAllergicMedicines().add(allergyName.trim());

        // Save the patient
        return userRepository.save(patient);
    }
}
