package com.ecommerce.service;

import com.ecommerce.dto.order.OrderItemResponse;
import com.ecommerce.dto.order.OrderResponse;
import com.ecommerce.entity.*;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;
    private final AddressRepository addressRepository;

    @Transactional
    public OrderResponse placeOrder(Long addressId) {
        User buyer = getLoggedInUser();

        List<CartItem> cartItems = cartItemRepository.findByUser(buyer);
        if (cartItems.isEmpty()) {
            throw new ResourceNotFoundException("Cart is empty — add products before placing an order");
        }

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        Order order = Order.builder()
                .buyer(buyer)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
                
        // Set Address
        if (addressId != null) {
            Address addr = addressRepository.findById(addressId).orElse(null);
            if (addr != null && addr.getUser().getId().equals(buyer.getId())) {
                order.setShippingAddress(addr.getFullName() + ", " + addr.getStreet() + ", " + addr.getCity() + ", " + addr.getState() + " - " + addr.getPincode() + " (" + addr.getPhone() + ")");
            }
        } else {
            // Find default
            List<Address> addresses = addressRepository.findByUser(buyer);
            Address def = addresses.stream().filter(Address::isDefault).findFirst().orElse(addresses.isEmpty() ? null : addresses.get(0));
            if (def != null) {
                order.setShippingAddress(def.getFullName() + ", " + def.getStreet() + ", " + def.getCity() + ", " + def.getState() + " - " + def.getPincode() + " (" + def.getPhone() + ")");
            }
        }

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            total = total.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(product.getPrice())
                    .build();

            orderItems.add(orderItem);
        }

        order.setTotalAmount(total);
        order.setItems(orderItems);
        // Set default estimated delivery (e.g. 5 days from now)
        order.setEstimatedDelivery(java.time.LocalDate.now().plusDays(5));
        order = orderRepository.save(order);

        cartItemRepository.deleteByUser(buyer);

        // --- EMAIL NOTIFICATIONS ---
        // 1. Notify Buyer
        emailService.sendOrderConfirmation(buyer, order);

        // 2. Notify Sellers
        Map<User, List<OrderItem>> sellerItemsMap = orderItems.stream()
                .collect(Collectors.groupingBy(item -> item.getProduct().getSeller()));
        
        for (Map.Entry<User, List<OrderItem>> entry : sellerItemsMap.entrySet()) {
            emailService.sendNewOrderNotification(entry.getKey(), order, entry.getValue());
        }

        // 3. Notify Admin
        emailService.sendAdminNotification("New Order Placed", 
            "Order #" + order.getId() + " placed by " + buyer.getName() + " for ₹" + order.getTotalAmount());

        return mapToResponse(order);
    }

    public List<OrderResponse> getMyOrders() {
        User buyer = getLoggedInUser();
        return orderRepository.findByBuyerOrderByCreatedAtDesc(buyer).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return mapToResponse(order);
    }

    // CANCEL ORDER
    @Transactional
    public OrderResponse cancelOrder(Long id) {
        User user = getLoggedInUser();
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        if (!order.getBuyer().getId().equals(user.getId())) {
            throw new RuntimeException("You can only cancel your own orders");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Only PENDING orders can be cancelled");
        }

        // Restore stock
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        // Send email to buyer
        emailService.sendOrderCancellation(user, order);
        emailService.sendAdminNotification("Order Cancelled", "Order #" + order.getId() + " was cancelled by user.");

        return mapToResponse(order);
    }

    // ADMIN: Get all orders
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ADMIN: Update order status
    @Transactional
    public OrderResponse updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        String oldStatus = order.getStatus().name();
        order.setStatus(OrderStatus.valueOf(status));
        order = orderRepository.save(order);

        emailService.sendOrderStatusUpdate(order.getBuyer(), order, oldStatus, status);

        return mapToResponse(order);
    }

    private User getLoggedInUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> items = order.getItems() != null
                ? order.getItems().stream().map(this::mapItemToResponse).collect(Collectors.toList())
                : List.of();

        return OrderResponse.builder()
                .id(order.getId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }

    private OrderItemResponse mapItemToResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .priceAtPurchase(item.getPriceAtPurchase())
                .build();
    }
}
