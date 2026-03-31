package com.ecommerce.controller;

import com.ecommerce.dto.order.OrderItemResponse;
import com.ecommerce.dto.order.OrderResponse;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.User;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
public class SellerController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private User getLoggedInUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSellerStats() {
        User seller = getLoggedInUser();
        
        List<Order> orders = orderRepository.findOrdersBySeller(seller);
        long totalProducts = productRepository.findBySeller(seller).size();
        
        // Calculate total revenue from just this seller's products in those orders
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Order order : orders) {
            if (!order.getStatus().name().equals("CANCELLED")) {
                for (OrderItem item : order.getItems()) {
                    if (item.getProduct().getSeller().getId().equals(seller.getId())) {
                        totalRevenue = totalRevenue.add(item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity())));
                    }
                }
            }
        }
        
        return ResponseEntity.ok(Map.of(
                "totalRevenue", totalRevenue,
                "totalOrders", orders.size(),
                "totalProducts", totalProducts
        ));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getSellerOrders() {
        User seller = getLoggedInUser();
        List<Order> orders = orderRepository.findOrdersBySeller(seller);
        
        List<OrderResponse> responses = orders.stream().map(order -> {
            // Filter items to only show this seller's products
            List<OrderItemResponse> filteredItems = order.getItems().stream()
                    .filter(item -> item.getProduct().getSeller().getId().equals(seller.getId()))
                    .map(item -> OrderItemResponse.builder()
                            .productId(item.getProduct().getId())
                            .productName(item.getProduct().getName())
                            .quantity(item.getQuantity())
                            .priceAtPurchase(item.getPriceAtPurchase())
                            .build())
                    .collect(Collectors.toList());
                    
            return OrderResponse.builder()
                    .id(order.getId())
                    .totalAmount(order.getTotalAmount()) // Note: this is the total for the whole order, frontend can ignore
                    .status(order.getStatus().name())
                    .createdAt(order.getCreatedAt())
                    .items(filteredItems)
                    .build();
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
}
