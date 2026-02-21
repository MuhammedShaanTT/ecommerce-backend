package com.ecommerce.controller;

import com.ecommerce.dto.cart.CartItemRequest;
import com.ecommerce.dto.cart.CartItemResponse;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<CartItemResponse> addToCart(@Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addToCart(request));
    }

    @GetMapping
    public ResponseEntity<List<CartItemResponse>> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    // UPDATE QUANTITY
    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuantity(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        int quantity = body.getOrDefault("quantity", 1);
        CartItemResponse response = cartService.updateQuantity(id, quantity);
        if (response == null) {
            return ResponseEntity.ok("Item removed from cart");
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> removeFromCart(@PathVariable Long id) {
        cartService.removeFromCart(id);
        return ResponseEntity.ok("Item removed from cart");
    }

    @DeleteMapping
    public ResponseEntity<String> clearCart() {
        cartService.clearCart();
        return ResponseEntity.ok("Cart cleared");
    }
}
