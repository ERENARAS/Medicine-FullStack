package com.api.medicine.domain.entities;

import com.api.medicine.domain.interfaces.User;
import jakarta.persistence.*;

@Entity
@Table(name = "pharmacy_staff")
public class PharmacyStaff implements User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
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
    public PharmacyStaff(String name){
        this.name = name;
    }
    public PharmacyStaff(){ }
    @Override
    public void login() {
        System.out.println("Pharmacy Staff logged the system");
    }
    public String getName() {
        return name;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }



}
