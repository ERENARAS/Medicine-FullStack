package com.api.medicine.domain.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Medicine sınıfı, sistemdeki ilaçları temsil eder.
 * Her Medicine nesnesi, reçetede yer alabilir ve ATM üzerinden hastaya verilebilir.
 */

@Entity
@Table(name = "medicine")
public class Medicine {
    private String  name;

    @Id
    private final UUID id;
    public Medicine(String name){
        this.name = name;
        this.id = UUID.randomUUID();
    }
    public Medicine(){
        this.id = UUID.randomUUID();
    }
    public String getName() {
        return name;
    }
    public UUID getId() {
        return id;
    }
    @Override
    public String toString() {
        return name;
    }
}
