package com.api.medicine.domain.interfaces;

import com.api.medicine.domain.entities.ATM;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * ATMRepository arayüzü, ATM stok verilerinin kalıcı ortamla etkileşimini
 * tanımlar. Uygulamadaki ATM nesnesini yükleme ve kaydetme işlemleri bu
 * arayüz üzerinden gerçekleştirilir.
 */
public interface ATMRepository extends JpaRepository<ATM, Long> {

}
