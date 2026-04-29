package com.api.medicine.domain.factory;

import com.api.medicine.domain.entities.PharmacyStaff;
import com.api.medicine.domain.interfaces.User;

public class PharmacyFactory implements UserFactory{
    @Override
    public User createUser(String name) {
        return new PharmacyStaff(name);
    }
}
