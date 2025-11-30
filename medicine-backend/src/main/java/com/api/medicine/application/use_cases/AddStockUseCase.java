package com.api.medicine.application.use_cases;

import com.api.medicine.domain.entities.ATM;
import com.api.medicine.domain.interfaces.ATMRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AddStockUseCase {
    private final ATMRepository atmRepository;

    public AddStockUseCase(ATMRepository atmRepository) {
        this.atmRepository = atmRepository;
    }

    public void execute(String medicineName, int quantity) {
        // Eski load() yerine findById() kullanıyoruz.
        // ID'si 1 olan ATM'yi varsayalım (Tek ATM senaryosu)
        Long defaultAtmId = 1L;

        ATM atm = atmRepository.findById(defaultAtmId).orElse(null);

        if (atm == null) {
            System.out.println("ATM bulunamadı!");
            return;
        }

        // ATM entity içindeki stok haritasını güncelle
        atm.increaseStock(medicineName, quantity);

        // Eski saveATM() yerine standart save() kullanıyoruz
        atmRepository.save(atm);

        System.out.println(medicineName + " stoğa eklendi: +" + quantity);
    }
}