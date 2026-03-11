package com.billpoint.backend.repository;

import com.billpoint.backend.model.ShopRequest;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ShopRequestRepository extends CrudRepository<ShopRequest, Long> {
    
    @Query("SELECT * FROM shop_requests WHERE status = :status")
    Iterable<ShopRequest> findByStatus(@Param("status") String status);
}
