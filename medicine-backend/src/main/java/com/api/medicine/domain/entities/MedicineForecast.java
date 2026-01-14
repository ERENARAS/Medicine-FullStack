package com.api.medicine.domain.entities;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "medicine_forecast")
public class MedicineForecast {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    private int predictedSales; // gelecek ay tahmini
    private LocalDate forecastDate; // tahmin tarihi

    public MedicineForecast() {
    }

    public MedicineForecast(Medicine medicine, int predictedSales, LocalDate forecastDate) {
        this.medicine = medicine;
        this.predictedSales = predictedSales;
        this.forecastDate = forecastDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
    }

    public int getPredictedSales() {
        return predictedSales;
    }

    public void setPredictedSales(int predictedSales) {
        this.predictedSales = predictedSales;
    }

    public LocalDate getForecastDate() {
        return forecastDate;
    }

    public void setForecastDate(LocalDate forecastDate) {
        this.forecastDate = forecastDate;
    }

}
