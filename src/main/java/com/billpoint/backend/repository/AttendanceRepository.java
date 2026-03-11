package com.billpoint.backend.repository;

import com.billpoint.backend.model.Attendance;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface AttendanceRepository extends CrudRepository<Attendance, Long> {

    @Query("SELECT * FROM attendance WHERE shop_id = :shopId AND date = :date")
    Iterable<Attendance> findByShopIdAndDate(@Param("shopId") Long shopId, @Param("date") LocalDate date);
    
    @Query("SELECT * FROM attendance WHERE staff_id = :staffId AND date = :date")
    Attendance findByStaffIdAndDate(@Param("staffId") Long staffId, @Param("date") LocalDate date);
}
