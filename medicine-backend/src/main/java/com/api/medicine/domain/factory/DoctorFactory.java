package com.api.medicine.domain.factory;

import com.api.medicine.domain.entities.Doctor;
import com.api.medicine.domain.interfaces.User;

/**
 * DoctorFactory sınıfı, doktor kullanıcıları oluşturmak için kullanılır.
 *
 */
public class DoctorFactory implements UserFactory {

    /**
     * Verilen isimle bir doktor nesnesi oluşturur.
     *
     * @param name Doktorun adı
     * @return Yeni oluşturulmuş Doctor nesnesi
     */
    @Override
    public User createUser(String name) {

        return new Doctor(name);
    }
}
