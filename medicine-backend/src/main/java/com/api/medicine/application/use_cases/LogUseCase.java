package com.api.medicine.application.use_cases;

import com.api.medicine.domain.interfaces.User;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class LogUseCase {
    private final String logFilePath = "logs.txt";

    public void log(User user) {
        String line = user.getEmail() + "," + LocalDateTime.now();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath, true))) {
            writer.write(line);
            writer.newLine();
            System.out.println(" Giriş loglandı: " + line);
        } catch (IOException e) {
            System.out.println(" Log dosyasına yazarken hata oluştu.");
        }
    }
}
