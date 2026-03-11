package com.billpoint.backend.repository;

import com.billpoint.backend.model.Customer;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomerRepository extends CrudRepository<Customer, Long> {

    @Query("SELECT * FROM customers WHERE shop_id = :shopId")
    Iterable<Customer> findByShopId(@Param("shopId") Long shopId);

    @Query("SELECT * FROM customers WHERE phone = :phone AND shop_id = :shopId")
    Optional<Customer> findByPhoneAndShopId(@Param("phone") String phone, @Param("shopId") Long shopId);
}
