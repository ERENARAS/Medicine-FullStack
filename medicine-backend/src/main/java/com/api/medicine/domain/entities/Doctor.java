package com.api.medicine.domain.entities;
import com.api.medicine.domain.factory.PrescriptionFactory;
import com.api.medicine.domain.interfaces.User;
import jakarta.persistence.*;

import java.util.List;
/**
 * Doctor sınıfı, sistemdeki doktor kullanıcılarını temsil eder.
 * Doktorlar hastalara reçete yazabilir.
 *
 * Bu sınıf sadece domain düzeyindeki davranışları içerir ve veri erişimi (repository gibi) içermez.
 */

@Entity
@Table(name = "doctor")
public class Doctor implements User {

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

    public Doctor(String name){
        this.name = name;
    }
    public Doctor() {
    }

    /**
     * Doktor, bir hasta için ilaç içeren bir reçete oluşturur.
     * Bu işlem PrescriptionFactory üzerinden gerçekleştirilir.
     *
     * @param patient Reçete yazılacak hasta
     * @param medicines Reçeteye eklenecek ilaçlar
     * @return Oluşturulan Prescription nesnesi
     */
    public Prescription writePrescription(Patient patient, List<Medicine> medicines) {
        return PrescriptionFactory.createPrescription(this, patient, medicines);
    }
    @Override
    public void login() {
        System.out.println("Doctor logged the system");
    }
    public String getName() {
        return name;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
}
