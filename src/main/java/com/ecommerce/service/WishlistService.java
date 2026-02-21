package com.ecommerce.service;

import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.entity.WishlistItem;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public Map<String, Object> toggleWishlist(Long productId) {
        User user = getLoggedInUser();
        var existing = wishlistRepository.findByUserAndProductId(user, productId);

        if (existing.isPresent()) {
            wishlistRepository.delete(existing.get());
            return Map.of("wishlisted", false, "message", "Removed from wishlist");
        } else {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            WishlistItem item = WishlistItem.builder().user(user).product(product).build();
            wishlistRepository.save(item);
            return Map.of("wishlisted", true, "message", "Added to wishlist");
        }
    }

    public List<Map<String, Object>> getWishlist() {
        User user = getLoggedInUser();
        return wishlistRepository.findByUser(user).stream().map(w -> Map.<String, Object>of(
                "id", w.getId(),
                "productId", w.getProduct().getId(),
                "productName", w.getProduct().getName(),
                "price", w.getProduct().getPrice(),
                "categoryName", w.getProduct().getCategory() != null ? w.getProduct().getCategory().getName() : ""))
                .collect(Collectors.toList());
    }

    public List<Long> getWishlistProductIds() {
        User user = getLoggedInUser();
        return wishlistRepository.findByUser(user).stream()
                .map(w -> w.getProduct().getId()).collect(Collectors.toList());
    }

    private User getLoggedInUser() {
        UserDetails ud = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByEmail(ud.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
