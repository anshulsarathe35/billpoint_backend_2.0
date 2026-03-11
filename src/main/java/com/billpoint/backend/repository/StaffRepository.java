package com.billpoint.backend.repository;

import com.billpoint.backend.model.Staff;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface StaffRepository extends CrudRepository<Staff, Long> {

    @Query("SELECT * FROM staff WHERE shop_id = :shopId")
    Iterable<Staff> findByShopId(@Param("shopId") Long shopId);

    @Query("SELECT * FROM staff WHERE user_id = :userId")
    Staff findByUserId(@Param("userId") Long userId);
}
