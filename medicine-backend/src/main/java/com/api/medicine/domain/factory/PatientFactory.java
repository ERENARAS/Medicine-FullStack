package com.api.medicine.domain.factory;

import com.api.medicine.domain.entities.Patient;
import com.api.medicine.domain.interfaces.User;

public class PatientFactory implements UserFactory {
    @Override
    public User createUser(String name) {
        return new Patient(name);
    }
}
