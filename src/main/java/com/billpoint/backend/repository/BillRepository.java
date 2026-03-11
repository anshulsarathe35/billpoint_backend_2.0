package com.billpoint.backend.repository;

import com.billpoint.backend.model.Bill;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface BillRepository extends CrudRepository<Bill, Long> {

    @Query("SELECT * FROM bills WHERE shop_id = :shopId")
    Iterable<Bill> findByShopId(@Param("shopId") Long shopId);

    @Query("SELECT * FROM bills WHERE customer_id = :customerId")
    Iterable<Bill> findByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT * FROM bills WHERE shop_id = :shopId ORDER BY created_at DESC LIMIT 50")
    Iterable<Bill> findRecentByShopId(@Param("shopId") Long shopId);
}
