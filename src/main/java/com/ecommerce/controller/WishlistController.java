package com.ecommerce.controller;

import com.ecommerce.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> toggleWishlist(@PathVariable Long productId) {
        return ResponseEntity.ok(wishlistService.toggleWishlist(productId));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getWishlist() {
        return ResponseEntity.ok(wishlistService.getWishlist());
    }

    @GetMapping("/ids")
    public ResponseEntity<List<Long>> getWishlistIds() {
        return ResponseEntity.ok(wishlistService.getWishlistProductIds());
    }
}
