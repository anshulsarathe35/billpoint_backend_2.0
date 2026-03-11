package com.billpoint.backend.controller;

import com.billpoint.backend.dto.BillRequest;
import com.billpoint.backend.dto.MessageResponse;
import com.billpoint.backend.model.*;
import com.billpoint.backend.repository.*;
import com.billpoint.backend.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/staff")
@PreAuthorize("hasRole('STAFF')")
public class StaffController {

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private BillItemRepository billItemRepository;
    
    @Autowired
    private ProductRepository productRepository;

    private Staff getAuthStaff(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Staff staff = staffRepository.findByUserId(userDetails.getId());
        if(staff == null) throw new RuntimeException("Error: Staff not found for this user");
        return staff;
    }

    // --- Attendance ---
    @PostMapping("/attendance/check-in")
    public ResponseEntity<?> checkIn(Authentication authentication) {
        Staff staff = getAuthStaff(authentication);
        LocalDate today = LocalDate.now();
        
        Attendance existing = attendanceRepository.findByStaffIdAndDate(staff.getId(), today);
        if (existing != null && existing.getCheckIn() != null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Already checked in today."));
        }

        Attendance attendance = existing != null ? existing : new Attendance();
        attendance.setStaffId(staff.getId());
        attendance.setShopId(staff.getShopId());
        attendance.setDate(today);
        attendance.setCheckIn(LocalTime.now());
        attendance.setStatus("PRESENT");
        
        attendanceRepository.save(attendance);
        return ResponseEntity.ok(new MessageResponse("Checked in successfully at " + LocalTime.now()));
    }

    @PostMapping("/attendance/check-out")
    public ResponseEntity<?> checkOut(Authentication authentication) {
        Staff staff = getAuthStaff(authentication);
        LocalDate today = LocalDate.now();
        
        Attendance existing = attendanceRepository.findByStaffIdAndDate(staff.getId(), today);
        if (existing == null || existing.getCheckIn() == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Must check in first."));
        }
        
        if (existing.getCheckOut() != null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Already checked out today."));
        }

        existing.setCheckOut(LocalTime.now());
        attendanceRepository.save(existing);
        return ResponseEntity.ok(new MessageResponse("Checked out successfully at " + LocalTime.now()));
    }

    // --- Customer Management ---
    @GetMapping("/customers/search")
    public ResponseEntity<?> searchCustomerByPhone(@RequestParam String phone, Authentication authentication) {
        Staff staff = getAuthStaff(authentication);
        Optional<Customer> customer = customerRepository.findByPhoneAndShopId(phone, staff.getShopId());
        
        if (customer.isPresent()) {
            return ResponseEntity.ok(customer.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/customers")
    public ResponseEntity<?> addCustomer(@RequestBody Customer customer, Authentication authentication) {
        Staff staff = getAuthStaff(authentication);
        
        Optional<Customer> existing = customerRepository.findByPhoneAndShopId(customer.getPhone(), staff.getShopId());
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Customer with this phone already exists in this shop."));
        }

        customer.setShopId(staff.getShopId());
        customer.setCreatedAt(LocalDateTime.now());
        Customer savedCustomer = customerRepository.save(customer);
        return ResponseEntity.ok(savedCustomer);
    }

    // --- Billing ---
    @PostMapping("/bills")
    public ResponseEntity<?> createBill(@RequestBody BillRequest billRequest, Authentication authentication) {
        Staff staff = getAuthStaff(authentication);
        
        Bill bill = new Bill();
        bill.setShopId(staff.getShopId());
        bill.setStaffId(staff.getId());
        bill.setCustomerId(billRequest.getCustomerId());
        bill.setTotalAmount(billRequest.getTotalAmount());
        bill.setDiscount(billRequest.getDiscount() != null ? billRequest.getDiscount() : java.math.BigDecimal.ZERO);
        bill.setFinalAmount(billRequest.getFinalAmount());
        bill.setPaymentMode(billRequest.getPaymentMode());
        bill.setCreatedAt(LocalDateTime.now());

        Bill savedBill = billRepository.save(bill);

        // Save Bill Items and update product stock
        for (BillItem item : billRequest.getItems()) {
            item.setBillId(savedBill.getId());
            billItemRepository.save(item);

            // Deduct stock
            Optional<Product> productOpt = productRepository.findById(item.getProductId());
            if (productOpt.isPresent()) {
                Product p = productOpt.get();
                p.setStockQuantity(p.getStockQuantity() - item.getQuantity());
                productRepository.save(p);
            }
        }

        return ResponseEntity.ok(new MessageResponse("Bill created successfully. Bill ID: " + savedBill.getId()));
    }
}
