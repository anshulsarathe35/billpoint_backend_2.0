package com.billpoint.backend.controller;

import com.billpoint.backend.dto.MessageResponse;
import com.billpoint.backend.model.Offer;
import com.billpoint.backend.model.Shop;
import com.billpoint.backend.repository.OfferRepository;
import com.billpoint.backend.repository.ShopRepository;
import com.billpoint.backend.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/offers")
@PreAuthorize("hasRole('SHOP_OWNER')")
public class OfferController {

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private ShopRepository shopRepository;

    private Long getAuthShopId(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Shop shop = shopRepository.findByOwnerId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: Shop not found"));
        return shop.getId();
    }

    @GetMapping
    public ResponseEntity<?> getOffers(Authentication authentication) {
        Long shopId = getAuthShopId(authentication);
        return ResponseEntity.ok(offerRepository.findActiveByShopId(shopId));
    }

    @PostMapping
    public ResponseEntity<?> addOffer(@RequestBody Offer offer, Authentication authentication) {
        Long shopId = getAuthShopId(authentication);
        offer.setShopId(shopId);
        offer.setIsActive(true);
        offerRepository.save(offer);
        return ResponseEntity.ok(new MessageResponse("Offer added successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deactivateOffer(@PathVariable Long id) {
        offerRepository.findById(id).ifPresent(offer -> {
            offer.setIsActive(false);
            offerRepository.save(offer);
        });
        return ResponseEntity.ok(new MessageResponse("Offer deactivated successfully"));
    }
}
