package com.billpoint.backend.repository;

import com.billpoint.backend.model.Product;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends CrudRepository<Product, Long> {
    
    @Query("SELECT * FROM products WHERE shop_id = :shopId")
    Iterable<Product> findByShopId(@Param("shopId") Long shopId);
}
