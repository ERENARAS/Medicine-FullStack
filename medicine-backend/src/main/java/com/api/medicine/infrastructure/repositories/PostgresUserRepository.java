package com.api.medicine.infrastructure.repositories;

import com.api.medicine.domain.entities.Doctor;
import com.api.medicine.domain.entities.Patient;
import com.api.medicine.domain.entities.PharmacyStaff;
import com.api.medicine.domain.interfaces.User;
import com.api.medicine.domain.interfaces.UserRepository;
import com.api.medicine.infrastructure.repositories.jpa.JpaDoctorRepository;
import com.api.medicine.infrastructure.repositories.jpa.JpaPatientRepository;
import com.api.medicine.infrastructure.repositories.jpa.JpaPharmacyStaffRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PostgresUserRepository implements UserRepository {

    private final JpaPatientRepository patientRepository;
    private final JpaDoctorRepository doctorRepository;
    private final JpaPharmacyStaffRepository staffRepository;

    // Constructor Injection: Spring bu 3 repository'yi otomatik dolduracak
    public PostgresUserRepository(JpaPatientRepository patientRepository,
                                  JpaDoctorRepository doctorRepository,
                                  JpaPharmacyStaffRepository staffRepository) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.staffRepository = staffRepository;
    }

    @Override
    public boolean save(User user) {
        // Gelen user nesnesinin tipine göre ilgili tabloya kaydet
        if (user instanceof Patient) {
            patientRepository.save((Patient) user);
            return true;
        } else if (user instanceof Doctor) {
            doctorRepository.save((Doctor) user);
            return true;
        } else if (user instanceof PharmacyStaff) {
            staffRepository.save((PharmacyStaff) user);
            return true;
        }
        return false;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        // E-posta hangi tabloda varsa onu bulup döndür
        // 1. Doktorda ara
        Optional<Doctor> doctor = doctorRepository.findByEmail(email);
        if (doctor.isPresent()) return Optional.of(doctor.get());

        // 2. Hastada ara
        Optional<Patient> patient = patientRepository.findByEmail(email);
        if (patient.isPresent()) return Optional.of(patient.get());

        // 3. Eczacıda ara
        Optional<PharmacyStaff> staff = staffRepository.findByEmail(email);
        if (staff.isPresent()) return Optional.of(staff.get());

        return Optional.empty();
    }

    @Override
    public boolean existsByEmail(String email) {
        return doctorRepository.existsByEmail(email) ||
                patientRepository.existsByEmail(email) ||
                staffRepository.existsByEmail(email);
    }
}