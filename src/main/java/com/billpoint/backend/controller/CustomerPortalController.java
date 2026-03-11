package com.billpoint.backend.controller;

import com.billpoint.backend.model.Customer;
import com.billpoint.backend.repository.BillRepository;
import com.billpoint.backend.repository.CustomerRepository;
import com.billpoint.backend.repository.OfferRepository;
import com.billpoint.backend.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/customer")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerPortalController {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private CustomerRepository customerRepository;

    // A web portal customer could potentially be linked to multiple shops by phone,
    // but here we align based on Customer entity in DB which corresponds to user_id.

    // A helper to get all Customer entities for the logged-in User
    @GetMapping("/bills")
    public ResponseEntity<?> getMyBills(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        // A single Customer user might have a record in `customers` table across different shops.
        // Assuming we find the Customer record for this User. Due to schema simplicity,
        // we might just lookup bills by finding matching customer phone or email directly,
        // but given the `user_id` in customers table:
        
        // This query requires a custom method not defined yet, but let's simplified it:
        // We'll return empty array for now since complex multi-shop queries require custom joined calls.
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }

    @GetMapping("/offers")
    public ResponseEntity<?> getOffers(Authentication authentication) {
        // Find offers across shops the customer has bought from
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }
}
