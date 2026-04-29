package com.api.medicine.domain.interfaces;

import com.api.medicine.domain.entities.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, UUID> {

    // Find latest prediction for a medicine
    Optional<Prediction> findTopByMedicineNameOrderByPredictionDateDesc(String medicineName);

    // Find all predictions for a specific date (e.g., "next month")
    List<Prediction> findByPredictionDate(LocalDate date);
}
