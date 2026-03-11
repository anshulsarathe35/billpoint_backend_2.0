package com.billpoint.backend.controller;

import com.billpoint.backend.dto.MessageResponse;
import com.billpoint.backend.model.Attendance;
import com.billpoint.backend.model.Product;
import com.billpoint.backend.model.Shop;
import com.billpoint.backend.model.Staff;
import com.billpoint.backend.model.User;
import com.billpoint.backend.repository.*;
import com.billpoint.backend.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/shop-owner")
@PreAuthorize("hasRole('SHOP_OWNER')")
public class ShopOwnerController {

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private PasswordEncoder encoder;

    private Long getAuthShopId(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Shop shop = shopRepository.findByOwnerId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: Shop not found for this owner"));
        return shop.getId();
    }

    // --- Inventory Management ---
    @GetMapping("/products")
    public ResponseEntity<?> getProducts(Authentication authentication) {
        Long shopId = getAuthShopId(authentication);
        return ResponseEntity.ok(productRepository.findByShopId(shopId));
    }

    @PostMapping("/products")
    public ResponseEntity<?> addProduct(@RequestBody Product product, Authentication authentication) {
        Long shopId = getAuthShopId(authentication);
        product.setShopId(shopId);
        product.setCreatedAt(LocalDateTime.now());
        productRepository.save(product);
        return ResponseEntity.ok(new MessageResponse("Product added successfully"));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id, Authentication authentication) {
        productRepository.deleteById(id);
        return ResponseEntity.ok(new MessageResponse("Product deleted successfully"));
    }

    // --- Staff Management ---
    @GetMapping("/staff")
    public ResponseEntity<?> getStaffList(Authentication authentication) {
        Long shopId = getAuthShopId(authentication);
        return ResponseEntity.ok(staffRepository.findByShopId(shopId));
    }

    @PostMapping("/staff")
    public ResponseEntity<?> addStaff(@RequestBody User staffUserRequest, Authentication authentication) {
        Long shopId = getAuthShopId(authentication);
        
        if (userRepository.existsByUsername(staffUserRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        // 1. Create User for Staff
        User user = new User();
        user.setUsername(staffUserRequest.getUsername());
        user.setEmail(staffUserRequest.getEmail());
        user.setPhone(staffUserRequest.getPhone());
        user.setPassword(encoder.encode(staffUserRequest.getPassword()));
        user.setRole("STAFF");
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        User savedUser = userRepository.findByUsername(staffUserRequest.getUsername()).get();

        // 2. Create Staff profile
        Staff staff = new Staff();
        staff.setUserId(savedUser.getId());
        staff.setShopId(shopId);
        staff.setName(staffUserRequest.getUsername()); // using username as name
        staff.setCreatedAt(LocalDateTime.now());
        staffRepository.save(staff);

        return ResponseEntity.ok(new MessageResponse("Staff member added successfully"));
    }

    // --- Attendance Management ---
    @GetMapping("/attendance/{date}")
    public ResponseEntity<?> getAttendanceForDate(@PathVariable String date, Authentication authentication) {
        Long shopId = getAuthShopId(authentication);
        LocalDate attendanceDate = LocalDate.parse(date);
        return ResponseEntity.ok(attendanceRepository.findByShopIdAndDate(shopId, attendanceDate));
    }

}
