package com.api.medicine.domain.entities;

import jakarta.persistence.*;

import java.util.HashMap;
import java.util.Map;

/**
 * ATM sınıfı, makinede bulunan ilaçların stok bilgisini tutar.
 * Şu an sadece tek bir ATM için çalışacak şekilde tasarlanmıştır.
 */

@Entity
@Table(name = "atm")
public class ATM {
    @Column(nullable = false)
    private String location;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "atm_stock", joinColumns = @JoinColumn(name = "atm_id"))
    @MapKeyColumn(name = "medicine_name")
    @Column(name = "quantity")
    private Map<String, Integer> stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_staff_id")
    private PharmacyStaff responsibleStaff;

    public ATM() {
        this.stock = new HashMap<>();
    }

    public ATM(String location) {
        this();
        this.location = location;
    }

    public Map<String, Integer> getStock() {
        return stock;
    }

    public void setStock(Map<String, Integer> stock) {
        this.stock = stock;
    }

    public int getStockFor(String medicineName) {
        return stock.getOrDefault(medicineName, 0);
    }

    public void increaseStock(String medicineName, int amount) {
        int current = stock.getOrDefault(medicineName, 0);
        stock.put(medicineName, current + amount);
    }

    public Long getId() {
        return id;
    }
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public PharmacyStaff getResponsibleStaff() {
        return responsibleStaff;
    }

    public void setResponsibleStaff(PharmacyStaff responsibleStaff) {
        this.responsibleStaff = responsibleStaff;
    }
    public boolean removeStock(String medicineName, int amount) {
        int current = stock.getOrDefault(medicineName, 0);
        if (current < amount) return false;
        stock.put(medicineName, current - amount);
        return true;
    }
}
