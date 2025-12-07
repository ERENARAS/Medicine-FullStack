package com.api.medicine.domain.interfaces;

import com.api.medicine.domain.entities.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface MedicineRepository extends JpaRepository<Medicine, UUID> {
    Medicine findByName(String name);
}
