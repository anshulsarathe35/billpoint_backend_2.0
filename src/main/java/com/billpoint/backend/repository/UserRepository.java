package com.billpoint.backend.repository;

import com.billpoint.backend.model.User;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    
    @Query("SELECT * FROM users WHERE username = :username")
    Optional<User> findByUsername(@Param("username") String username);

    @Query("SELECT * FROM users WHERE email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT COUNT(*) > 0 FROM users WHERE username = :username")
    boolean existsByUsername(@Param("username") String username);

    @Query("SELECT COUNT(*) > 0 FROM users WHERE email = :email")
    boolean existsByEmail(@Param("email") String email);
}
