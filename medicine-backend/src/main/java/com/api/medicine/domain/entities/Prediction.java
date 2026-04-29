package com.api.medicine.domain.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "predictions")
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "medicine_name", nullable = false)
    private String medicineName;

    @Column(name = "predicted_value")
    private Integer predictedValue;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "prediction_date")
    private LocalDate predictionDate;

    public Prediction() {
    }

    public Prediction(String medicineName, Integer predictedValue, Double confidenceScore, LocalDate predictionDate) {
        this.medicineName = medicineName;
        this.predictedValue = predictedValue;
        this.confidenceScore = confidenceScore;
        this.predictionDate = predictionDate;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public Integer getPredictedValue() {
        return predictedValue;
    }

    public void setPredictedValue(Integer predictedValue) {
        this.predictedValue = predictedValue;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public LocalDate getPredictionDate() {
        return predictionDate;
    }

    public void setPredictionDate(LocalDate predictionDate) {
        this.predictionDate = predictionDate;
    }
}
