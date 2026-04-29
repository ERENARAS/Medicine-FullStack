package com.api.medicine.application.use_cases;

import com.api.medicine.domain.factory.DoctorFactory;
import com.api.medicine.domain.factory.PatientFactory;
import com.api.medicine.domain.factory.PharmacyFactory;
import com.api.medicine.domain.factory.UserFactory;
import com.api.medicine.domain.interfaces.User;
import com.api.medicine.domain.interfaces.UserRepository;
import org.springframework.stereotype.Service; // YENİ

@Service
public class RegisterUseCase {
    private final UserRepository userRepository;

    public RegisterUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean register(String name, String email, String password) {
        if (!isValidEmail(email)) {
            return false;
        }

        if (userRepository.existsByEmail(email)) {
            return false;
        }

        UserFactory factory;
        try {
            factory = getFactoryByEmail(email);
        } catch (IllegalArgumentException e) {
            return false;
        }

        User user = factory.createUser(name);
        user.setEmail(email);
        user.setPassword(password);

        return userRepository.save(user);
    }

    private boolean isValidEmail(String email) {
        return email.endsWith("@dr.medicine") || email.endsWith("@pt.medicine") || email.endsWith("@ph.medicine");
    }

    private UserFactory getFactoryByEmail(String email) {
        if (email.endsWith("@dr.medicine")){
            return new DoctorFactory();
        }
        if (email.endsWith("@pt.medicine")){
            return new PatientFactory();
        }
        if (email.endsWith("@ph.medicine")) {
            return new PharmacyFactory();
        }
        throw new IllegalArgumentException("Tanınmayan kullanıcı tipi: " + email);
    }
}