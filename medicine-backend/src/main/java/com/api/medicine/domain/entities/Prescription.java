package com.api.medicine.domain.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Prescription sınıfı, bir hastaya bir doktor tarafından yazılan reçeteyi temsil eder.
 *
 * Reçete nesnesi; benzersiz bir kimlik (UUID), yazılma tarihi,
 * doktor bilgisi, hasta bilgisi ve reçetedeki ilaçlardan oluşur.
 *
 * Domain katmanında yer alır ve temel iş verisini taşır.
 */

@Entity
@Table(name = "prescription")
public class Prescription {

    @Id
    private UUID id;
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", referencedColumnName = "id")
    private Doctor doctor;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", referencedColumnName = "id")
    private Patient patient;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "prescription_medicine",
            joinColumns = @JoinColumn(name = "prescription_id"),
            inverseJoinColumns = @JoinColumn(name = "medicine_id")
    )
    private List<Medicine> medicines;

    public Prescription() { // JPA için zorunlu boş constructor
    }
    /**
     * Yeni bir Prescription nesnesi oluşturur.
     *
     * @param id Reçeteye atanacak benzersiz kimlik
     * @param date Reçetenin yazıldığı tarih
     * @param doctor Reçeteyi yazan doktor
     * @param patient Reçeteyi alan hasta
     * @param medicines Reçeteye yazılan ilaçlar
     */
    public Prescription(UUID id, LocalDate date, Doctor doctor, Patient patient, List<Medicine> medicines) {
        this.id = id;
        this.date = date;
        this.doctor = doctor;
        this.patient = patient;
        this.medicines = medicines;
    }
    public String getInfo() {
        return "Prescription ID: " + id + ", Doctor: " + doctor.getName() + ", Patient: " + patient.getName();
    }
    public UUID getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public Patient getPatient() {
        return patient;
    }

    public List<Medicine> getMedicines() {
        return medicines;
    }
}
