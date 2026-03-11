package com.billpoint.backend.repository;

import com.billpoint.backend.model.BillItem;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface BillItemRepository extends CrudRepository<BillItem, Long> {

    @Query("SELECT * FROM bill_items WHERE bill_id = :billId")
    Iterable<BillItem> findByBillId(@Param("billId") Long billId);
}
