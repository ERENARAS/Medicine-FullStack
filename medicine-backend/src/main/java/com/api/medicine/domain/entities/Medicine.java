package com.api.medicine.domain.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Medicine sınıfı, sistemdeki ilaçları temsil eder.
 * Her Medicine nesnesi, reçetede yer alabilir ve ATM üzerinden hastaya
 * verilebilir.
 */

@Entity
@Table(name = "medicine")
public class Medicine {
    private String name;
    private String treatment; // Yeni alan: Tedavi/Hastalık bilgisi

    @Id
    private final UUID id;

    public Medicine(String name, String treatment) {
        this.name = name;
        this.treatment = treatment;
        this.id = UUID.randomUUID();
    }

    public Medicine(String name) {
        this.name = name;
        this.treatment = "Genel Tedavi"; // Varsayılan
        this.id = UUID.randomUUID();
    }

    public Medicine() {
        this.id = UUID.randomUUID();
    }

    public String getName() {
        return name;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }
}
