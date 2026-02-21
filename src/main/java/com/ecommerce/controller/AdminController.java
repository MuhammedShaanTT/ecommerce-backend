package com.ecommerce.controller;

import com.ecommerce.dto.category.CategoryRequest;
import com.ecommerce.dto.category.CategoryResponse;
import com.ecommerce.dto.order.OrderResponse;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.User;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.CategoryService;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final CategoryService categoryService;
    private final OrderService orderService;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    // === STATS ===
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        long totalUsers = userRepository.count();
        long totalProducts = productRepository.count();
        List<Order> allOrders = orderRepository.findAll();
        long totalOrders = allOrders.size();
        BigDecimal totalRevenue = allOrders.stream()
                .filter(o -> !o.getStatus().name().equals("CANCELLED"))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ResponseEntity.ok(Map.of(
                "totalUsers", totalUsers,
                "totalProducts", totalProducts,
                "totalOrders", totalOrders,
                "totalRevenue", totalRevenue));
    }

    // === CATEGORIES ===
    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok("Category deleted successfully");
    }

    // === ORDERS ===
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    // === USERS ===
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> response = users.stream().map(u -> Map.<String, Object>of(
                "id", u.getId(),
                "name", u.getName(),
                "email", u.getEmail(),
                "role", u.getRole().getName())).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // === PRODUCTS ===
    @GetMapping("/products")
    public ResponseEntity<?> getAllProducts(@PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }
}
