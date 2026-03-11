package com.billpoint.backend.repository;

import com.billpoint.backend.model.Offer;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface OfferRepository extends CrudRepository<Offer, Long> {

    @Query("SELECT * FROM offers WHERE shop_id = :shopId AND is_active = true")
    Iterable<Offer> findActiveByShopId(@Param("shopId") Long shopId);
}
