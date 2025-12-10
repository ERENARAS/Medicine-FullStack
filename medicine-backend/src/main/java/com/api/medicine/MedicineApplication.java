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
	public CommandLineRunner demo(com.api.medicine.domain.interfaces.MedicineRepository medicineRepository) {
		return (args) -> {
			if (medicineRepository.count() == 0) {
				medicineRepository
						.save(new com.api.medicine.domain.entities.Medicine("Saizen", "Büyüme Geriliği Tedavisi"));
				medicineRepository
						.save(new com.api.medicine.domain.entities.Medicine("Augmentin", "Enfeksiyon Tedavisi"));
				medicineRepository.save(new com.api.medicine.domain.entities.Medicine("Majezik", "Ağrı Kesici"));
				medicineRepository.save(new com.api.medicine.domain.entities.Medicine("Parol", "Ateş Düşürücü"));
				medicineRepository.save(new com.api.medicine.domain.entities.Medicine("Aspirin", "Kan Sulandırıcı"));
				medicineRepository.save(new com.api.medicine.domain.entities.Medicine("Lansor", "Mide Koruyucu"));
				medicineRepository.save(new com.api.medicine.domain.entities.Medicine("Crebros", "Alerji Tedavisi"));
				medicineRepository.save(new com.api.medicine.domain.entities.Medicine("Ventolin", "Astım Tedavisi"));
			}
		};
	}
}
