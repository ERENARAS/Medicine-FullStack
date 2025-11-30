package com.api.medicine.application.use_cases;

import com.api.medicine.domain.interfaces.User;
import com.api.medicine.domain.interfaces.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class LoginUseCase {
    private final UserRepository userRepository;

    public LoginUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Verilen kimlik bilgileriyle kullanıcıyı doğrulamaya çalışır.
     * Eğer başarılıysa User nesnesini, başarısızsa null döner.
     */
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !user.getPassword().equals(password)) {
            return null;
        }
        return user;
    }
}