package com.billpoint.backend.repository;

import com.billpoint.backend.model.Shop;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShopRepository extends CrudRepository<Shop, Long> {

    @Query("SELECT * FROM shops WHERE owner_id = :ownerId")
    Optional<Shop> findByOwnerId(@Param("ownerId") Long ownerId);
}
