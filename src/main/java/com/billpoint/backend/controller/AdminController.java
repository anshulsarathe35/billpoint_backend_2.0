package com.billpoint.backend.controller;

import com.billpoint.backend.dto.ApprovalRequest;
import com.billpoint.backend.dto.MessageResponse;
import com.billpoint.backend.model.Shop;
import com.billpoint.backend.model.ShopRequest;
import com.billpoint.backend.model.Subscription;
import com.billpoint.backend.model.User;
import com.billpoint.backend.repository.ShopRepository;
import com.billpoint.backend.repository.ShopRequestRepository;
import com.billpoint.backend.repository.SubscriptionRepository;
import com.billpoint.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private ShopRequestRepository shopRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private PasswordEncoder encoder;

    @GetMapping("/requests")
    public ResponseEntity<?> getAllRequests() {
        return ResponseEntity.ok(shopRequestRepository.findAll());
    }

    @PostMapping("/requests/{id}/approve")
    public ResponseEntity<?> approveRequest(@PathVariable Long id, @RequestBody ApprovalRequest approvalRequest) {
        Optional<ShopRequest> requestOpt = shopRequestRepository.findById(id);
        if (requestOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Request not found."));
        }

        ShopRequest request = requestOpt.get();
        if ("APPROVED".equals(request.getStatus())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Request already approved."));
        }

        // 1. Create a user for the shop owner
        // In a real app, generate a random password and email it. Here we use a default.
        String defaultPassword = "password123";
        String username = request.getEmail().split("@")[0] + "_" + System.currentTimeMillis() % 1000;

        User owner = new User();
        owner.setUsername(username);
        owner.setEmail(request.getEmail());
        owner.setPhone(request.getPhone());
        owner.setRole("SHOP_OWNER");
        owner.setPassword(encoder.encode(defaultPassword));
        owner.setIsActive(true);
        owner.setCreatedAt(LocalDateTime.now());
        
        userRepository.save(owner);
        // We need the saved user's ID
        User savedOwner = userRepository.findByUsername(username).get();

        // 2. Create the Shop
        Shop shop = new Shop();
        shop.setOwnerId(savedOwner.getId());
        shop.setName(request.getShopName());
        shop.setAddress(request.getAddress());
        shop.setEmail(request.getEmail());
        shop.setPhone(request.getPhone());
        shop.setIsActive(true);
        shop.setCreatedAt(LocalDateTime.now());
        
        Shop savedShop = shopRepository.save(shop);

        // 3. Create Subscription
        Subscription sub = new Subscription();
        sub.setShopId(savedShop.getId());
        sub.setPlanName(approvalRequest.getPlanName() != null ? approvalRequest.getPlanName() : "Basic Plan");
        sub.setValidFrom(LocalDate.now());
        sub.setValidTo(LocalDate.now().plusMonths(approvalRequest.getValidMonths() > 0 ? approvalRequest.getValidMonths() : 1));
        sub.setIsActive(true);
        subscriptionRepository.save(sub);

        // 4. Update request status
        request.setStatus("APPROVED");
        request.setUpdatedAt(LocalDateTime.now());
        shopRequestRepository.save(request);

        return ResponseEntity.ok(new MessageResponse("Shop Request Approved. Shop Owner credentials generated."));
    }

    @PostMapping("/requests/{id}/reject")
    public ResponseEntity<?> rejectRequest(@PathVariable Long id) {
        Optional<ShopRequest> requestOpt = shopRequestRepository.findById(id);
        if (requestOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Request not found."));
        }

        ShopRequest request = requestOpt.get();
        request.setStatus("REJECTED");
        request.setUpdatedAt(LocalDateTime.now());
        shopRequestRepository.save(request);

        return ResponseEntity.ok(new MessageResponse("Shop Request Rejected."));
    }
    
    @GetMapping("/shops")
    public ResponseEntity<?> getAllShops() {
        return ResponseEntity.ok(shopRepository.findAll());
    }
}
