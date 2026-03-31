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

import com.ecommerce.repository.RoleRepository;
import com.ecommerce.entity.Role;

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
    private final RoleRepository roleRepository;

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
                "role", u.getRole().getName(),
                "enabled", u.isEnabled(),
                "verified", u.isVerified())).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/users/{id}/role")
    public ResponseEntity<Map<String, Object>> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new com.ecommerce.exception.ResourceNotFoundException("User not found"));
        
        String newRoleName = body.get("role");
        Role role = roleRepository.findByName(newRoleName)
            .orElseThrow(() -> new com.ecommerce.exception.ResourceNotFoundException("Role not found"));
            
        user.setRole(role);
        userRepository.save(user);
        
        return ResponseEntity.ok(Map.of("message", "User role updated successfully", "role", role.getName()));
    }
    
    @PutMapping("/users/{id}/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleUserStatus(@PathVariable Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new com.ecommerce.exception.ResourceNotFoundException("User not found"));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User status updated", "enabled", user.isEnabled()));
    }
    
    @PutMapping("/users/{id}/verify")
    public ResponseEntity<Map<String, Object>> verifyUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new com.ecommerce.exception.ResourceNotFoundException("User not found"));
        user.setVerified(true);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User verified successfully"));
    }
    
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Map<String, Object>> deleteProductAny(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(Map.of("message", "Product deleted by admin"));
    }
    
    @GetMapping("/reports")
    public ResponseEntity<Map<String, Object>> getAdminReports() {
        // Mock revenue by day data (in production, this would query GroupBy DAY)
        List<Map<String, Object>> revenueData = List.of(
            Map.of("name", "Mon", "revenue", 5000),
            Map.of("name", "Tue", "revenue", 8000),
            Map.of("name", "Wed", "revenue", 12000),
            Map.of("name", "Thu", "revenue", 7000),
            Map.of("name", "Fri", "revenue", 15000),
            Map.of("name", "Sat", "revenue", 22000),
            Map.of("name", "Sun", "revenue", 18000)
        );
        return ResponseEntity.ok(Map.of("revenueData", revenueData));
    }

    // === PRODUCTS ===
    @GetMapping("/products")
    public ResponseEntity<?> getAllProducts(@PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(null, null, pageable));
    }
}
