package com.api.medicine.domain.interfaces;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.api.medicine.domain.entities.MedicineForecast;

@Repository
public interface MedicineForecastRepository extends JpaRepository<MedicineForecast, Long> {

    // Get Top 5 medicines with highest predicted sales
    List<MedicineForecast> findTop5ByOrderByPredictedSalesDesc();

    // Search forecast by medicine name (using the relationship)
    @Query("SELECT f FROM MedicineForecast f WHERE LOWER(f.medicine.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<MedicineForecast> findByMedicineNameContainingIgnoreCase(String name);

    // Exact match optional
    Optional<MedicineForecast> findByMedicineName(String name);
}
