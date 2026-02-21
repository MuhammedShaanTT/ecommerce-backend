package com.ecommerce.service;

import com.ecommerce.entity.Review;
import com.ecommerce.entity.User;
import com.ecommerce.exception.DuplicateResourceException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.ReviewRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public Map<String, Object> addReview(Long productId, int rating, String comment) {
        User user = getLoggedInUser();

        if (reviewRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new DuplicateResourceException("You have already reviewed this product");
        }

        var product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Review review = Review.builder()
                .user(user).product(product)
                .rating(rating).comment(comment)
                .createdAt(LocalDateTime.now())
                .build();
        reviewRepository.save(review);

        return Map.of("message", "Review added!", "rating", rating);
    }

    public Map<String, Object> getProductReviews(Long productId) {
        List<Map<String, Object>> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream().map(r -> Map.<String, Object>of(
                        "id", r.getId(),
                        "userName", r.getUser().getName(),
                        "rating", r.getRating(),
                        "comment", r.getComment() != null ? r.getComment() : "",
                        "createdAt", r.getCreatedAt().toString()))
                .collect(Collectors.toList());

        Double avg = reviewRepository.getAverageRatingByProductId(productId);

        return Map.of(
                "reviews", reviews,
                "averageRating", avg != null ? Math.round(avg * 10.0) / 10.0 : 0,
                "totalReviews", reviews.size());
    }

    private User getLoggedInUser() {
        UserDetails ud = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByEmail(ud.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
