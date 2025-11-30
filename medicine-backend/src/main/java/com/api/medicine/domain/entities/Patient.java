package com.api.medicine.domain.entities;

import com.api.medicine.domain.interfaces.User;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Patient sınıfı, sistemdeki hasta kullanıcıları temsil eder.
 * Her hasta, bir reçete alabilir ve ATM üzerinden ilaçlarını alabilir.
 * Bu sınıf sadece hastaya ait temel bilgileri içerir.
 *
 */

@Entity
@Table(name = "patient")
public class Patient implements User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private float amount;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "patient_allergies", joinColumns = @JoinColumn(name = "patient_id"))
    @Column(name = "allergy_name")
    private List<String> allergicMedicines;

    @Column(unique = true,  nullable = false)
    private String email;
    private String password;



    public String getEmail() {
        return email;
    }

    public void setEmail(String mail) {
        this.email = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Patient() {
        this.allergicMedicines = new ArrayList<>();
    }
    public Patient(String name) {
        this.name = name;
        this.allergicMedicines = new ArrayList<>();
    }
    @Override
    public void login() {
        System.out.println("Patient logged the system");
    }
    public String getName() {
        return name;
    }
    public List<String> getAllergicMedicines() {
        return allergicMedicines;
    }
    public void setAllergicMedicines(List<String> allergicMedicines) {
        this.allergicMedicines = allergicMedicines;
    }
    public float getAmount() {
        return amount;
    }
   public void setAmount(float amount) {
        this.amount = amount;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    /**
     * ilaç alım işleminden sonra ödeme olarak azaltma işlemi
     *
     * @param amount azaltılacack miktar
     */
    public void decreaseAmount(float amount){
        this.amount -= amount;
    }

}
