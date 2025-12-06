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

import java.util.List;
import java.util.Optional;

@Repository
public class PostgresUserRepository implements UserRepository {

    private final JpaPatientRepository patientRepository;
    private final JpaDoctorRepository doctorRepository;
    private final JpaPharmacyStaffRepository staffRepository;

    public PostgresUserRepository(JpaPatientRepository patientRepository,
                                  JpaDoctorRepository doctorRepository,
                                  JpaPharmacyStaffRepository staffRepository) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.staffRepository = staffRepository;
    }

    @Override
    public boolean save(User user) {
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
        Optional<Doctor> doctor = doctorRepository.findByEmail(email);
        if (doctor.isPresent()) return Optional.of(doctor.get());

        Optional<Patient> patient = patientRepository.findByEmail(email);
        if (patient.isPresent()) return Optional.of(patient.get());

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

    @Override
    public List<Patient> findAllPatients() {
        return patientRepository.findAll();
    }
}