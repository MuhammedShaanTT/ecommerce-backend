package com.ecommerce.service;

import com.ecommerce.dto.product.ProductRequest;
import com.ecommerce.dto.product.ProductResponse;
import com.ecommerce.entity.*;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.UnauthorizedActionException;
import com.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductService {

        private final ProductRepository productRepository;
        private final CategoryRepository categoryRepository;
        private final UserRepository userRepository;

        // ADD PRODUCT (SELLER ONLY)
        public ProductResponse addProduct(ProductRequest request) {
                User seller = getLoggedInUser();
                if (!seller.getRole().getName().equals("SELLER"))
                        throw new UnauthorizedActionException("Only sellers can add products");

                Category category = categoryRepository.findById(request.getCategoryId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Category not found with id: " + request.getCategoryId()));

                Product product = Product.builder()
                                .name(request.getName())
                                .description(request.getDescription())
                                .price(request.getPrice())
                                .stock(request.getStock())
                                .imageUrl(request.getImageUrl())
                                .category(category)
                                .seller(seller)
                                .createdAt(LocalDateTime.now())
                                .build();

                product = productRepository.save(product);
                return mapToResponse(product);
        }

        // UPDATE PRODUCT (SELLER owns it)
        public ProductResponse updateProduct(Long id, ProductRequest request) {
                Product product = productRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

                User seller = getLoggedInUser();
                if (!product.getSeller().getId().equals(seller.getId())) {
                        throw new UnauthorizedActionException("You can only edit your own products");
                }

                product.setName(request.getName());
                product.setDescription(request.getDescription());
                product.setPrice(request.getPrice());
                product.setStock(request.getStock());
                product.setImageUrl(request.getImageUrl());

                if (request.getCategoryId() != null) {
                        Category category = categoryRepository.findById(request.getCategoryId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
                        product.setCategory(category);
                }

                product = productRepository.save(product);
                return mapToResponse(product);
        }

        // DELETE PRODUCT (SELLER owns it)
        public void deleteProduct(Long id) {
                Product product = productRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
                User seller = getLoggedInUser();
                if (!product.getSeller().getId().equals(seller.getId())) {
                        throw new UnauthorizedActionException("You can only delete your own products");
                }
                productRepository.delete(product);
        }

        // GET ALL PRODUCTS (paginated)
        public Page<ProductResponse> getAllProducts(Pageable pageable) {
                return productRepository.findAll(pageable).map(this::mapToResponse);
        }

        // GET PRODUCT BY ID
        public ProductResponse getProductById(Long id) {
                Product product = productRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
                return mapToResponse(product);
        }

        // GET PRODUCTS BY CATEGORY (paginated)
        public Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
                return productRepository.findByCategoryId(categoryId, pageable).map(this::mapToResponse);
        }

        // GET SELLER'S OWN PRODUCTS (paginated)
        public Page<ProductResponse> getSellerProducts(Pageable pageable) {
                User seller = getLoggedInUser();
                return productRepository.findBySeller(seller, pageable).map(this::mapToResponse);
        }

        // SEARCH PRODUCTS (paginated)
        public Page<ProductResponse> searchProducts(String query, Pageable pageable) {
                return productRepository.findByNameContainingIgnoreCase(query, pageable).map(this::mapToResponse);
        }

        private User getLoggedInUser() {
                UserDetails userDetails = (UserDetails) SecurityContextHolder
                                .getContext().getAuthentication().getPrincipal();
                return userRepository.findByEmail(userDetails.getUsername())
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }

        private ProductResponse mapToResponse(Product product) {
                return ProductResponse.builder()
                                .id(product.getId())
                                .name(product.getName())
                                .description(product.getDescription())
                                .price(product.getPrice())
                                .stock(product.getStock())
                                .imageUrl(product.getImageUrl())
                                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                                .sellerName(product.getSeller() != null ? product.getSeller().getName() : null)
                                .createdAt(product.getCreatedAt())
                                .build();
        }
}
