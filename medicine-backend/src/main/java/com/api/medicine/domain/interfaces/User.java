package com.api.medicine.domain.interfaces;

/**
 * User arayüzü, sisteme giriş yapabilen tüm kullanıcı türleri için ortak davranışı tanımlar.
 *
 * Bu arayüzü uygulayan sınıflar (örneğin Doctor, Patient, PharmacyStaff),
 * login() metodunu kendilerine özgü şekilde override eder.
 */
public interface User {

    /**
     * Sisteme giriş yapılmasını temsil eder.
     * Kullanıcı türüne göre bu davranış farklı şekilde uygulanabilir.
     */
    void login();

    void setPassword(String password);
    void setEmail(String email);

    String getEmail();

    String getPassword();

    String getName();
}
