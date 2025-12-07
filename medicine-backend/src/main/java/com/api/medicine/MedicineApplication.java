package com.api.medicine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner; // Added import

@SpringBootApplication
public class MedicineApplication {

	public static void main(String[] args) {
		SpringApplication.run(MedicineApplication.class, args);
	}

	@org.springframework.context.annotation.Bean
	public CommandLineRunner demo(com.api.medicine.domain.interfaces.MedicineRepository repository) {
		return (args) -> {
			if (repository.count() == 0) {
				repository.save(new com.api.medicine.domain.entities.Medicine("Saizen", "Büyüme Geriliği Tedavisi"));
				repository.save(new com.api.medicine.domain.entities.Medicine("Augmentin", "Enfeksiyon Tedavisi"));
				repository.save(new com.api.medicine.domain.entities.Medicine("Majezik", "Ağrı Kesici"));
				repository.save(new com.api.medicine.domain.entities.Medicine("Parol", "Ateş Düşürücü"));
				repository.save(new com.api.medicine.domain.entities.Medicine("Aspirin", "Kan Sulandırıcı"));
				repository.save(new com.api.medicine.domain.entities.Medicine("Lansor", "Mide Koruyucu"));
				repository.save(new com.api.medicine.domain.entities.Medicine("Crebros", "Alerji Tedavisi"));
				repository.save(new com.api.medicine.domain.entities.Medicine("Ventolin", "Astım Tedavisi"));
			}
		};
	}
}
