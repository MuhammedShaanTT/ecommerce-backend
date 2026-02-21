package com.ecommerce.controller;

import com.ecommerce.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> addReview(
            @PathVariable Long productId,
            @RequestBody Map<String, Object> body) {
        int rating = (int) body.get("rating");
        String comment = (String) body.getOrDefault("comment", "");
        return ResponseEntity.ok(reviewService.addReview(productId, rating, comment));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> getProductReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId));
    }
}
