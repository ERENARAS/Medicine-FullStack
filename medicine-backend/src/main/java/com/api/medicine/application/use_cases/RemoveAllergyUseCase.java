package com.api.medicine.application.use_cases;

import com.api.medicine.domain.entities.Patient;
import com.api.medicine.domain.interfaces.User;
import com.api.medicine.domain.interfaces.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RemoveAllergyUseCase {

    private final UserRepository userRepository;

    public RemoveAllergyUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Removes an allergy from a patient's allergic medicines list.
     *
     * @param patientEmail Email of the patient
     * @param allergyName  Name of the medicine/substance to remove from allergies
     * @return true if successfully removed, false otherwise
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

        // Remove the allergy
        boolean removed = patient.getAllergicMedicines().remove(allergyName.trim());

        if (!removed) {
            return false; // Allergy didn't exist
        }

        // Save the patient
        return userRepository.save(patient);
    }
}
