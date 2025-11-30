package com.api.medicine.domain.factory;

import com.api.medicine.domain.interfaces.User;

/**
 * UserFactory arayüzü, sisteme ait kullanıcı nesnelerini oluşturmak için kullanılır.
 *
 * Factory Pattern kullanılarak her kullanıcı türü için ayrı factory sınıfı yazılabilir.
 * Bu sayede kullanıcı oluşturma süreci merkezileştirilir ve esnek hale gelir.
 *
 * Clean Architecture'a göre bu yapı domain katmanında yer alır ve
 * kullanıcı oluşturma mantığını soyutlar.
 */
public interface UserFactory {

    /**
     * Verilen isimle yeni bir kullanıcı nesnesi oluşturur.
     *
     * @param name Kullanıcının adı
     * @return Oluşturulan kullanıcı nesnesi (Doctor, Patient, vs.)
     */
    User createUser(String name);
}
