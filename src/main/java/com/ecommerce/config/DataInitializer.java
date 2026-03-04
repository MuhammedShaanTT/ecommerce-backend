package com.ecommerce.config;

import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.RoleRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        // 1. Seed roles
        createRoleIfNotExists("BUYER");
        createRoleIfNotExists("SELLER");
        createRoleIfNotExists("ADMIN");
        log.info("Default roles seeded successfully");

        // 2. Seed categories
        List<String> categoryNames = List.of(
                "Electronics", "Clothing", "Books",
                "Home & Kitchen", "Sports", "Beauty");
        categoryNames.forEach(this::createCategoryIfNotExists);
        log.info("Default categories seeded successfully");

        // 3. Seed admin user
        createUserIfNotExists("ShopVerse Admin", "admin@shopverse.com", "admin123", "ADMIN");

        // 4. Seed seller user + sample products
        User seller = createUserIfNotExists("ShopVerse Store", "seller@shopverse.com", "seller123", "SELLER");

        // 5. Seed sample products (only if DB is empty)
        if (productRepository.count() == 0 && seller != null) {
            seedProducts(seller);
            log.info("Sample products seeded successfully");
        }
    }

    private void createRoleIfNotExists(String roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
            log.info("Created role: {}", roleName);
        }
    }

    private void createCategoryIfNotExists(String name) {
        if (categoryRepository.findByName(name).isEmpty()) {
            Category category = Category.builder().name(name).build();
            categoryRepository.save(category);
            log.info("Created category: {}", name);
        }
    }

    private User createUserIfNotExists(String name, String email, String rawPassword, String roleName) {
        if (userRepository.findByEmail(email).isPresent()) {
            return userRepository.findByEmail(email).get();
        }
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .build();
        user = userRepository.save(user);
        log.info("Created {} user: {}", roleName, email);
        return user;
    }

    private void seedProducts(User seller) {
        Map<String, List<Map<String, Object>>> products = Map.of(
                "Electronics", List.of(
                        Map.of("name", "Wireless Bluetooth Headphones", "desc",
                                "Premium noise-cancelling headphones with 30h battery life", "price", 2499.00, "stock",
                                50, "img", "https://picsum.photos/seed/headphones/400/400"),
                        Map.of("name", "Smart Watch Pro", "desc", "Fitness tracker with heart rate monitor and GPS",
                                "price", 3999.00, "stock", 30, "img", "https://picsum.photos/seed/smartwatch/400/400")),
                "Clothing", List.of(
                        Map.of("name", "Classic Cotton T-Shirt", "desc",
                                "Comfortable 100% cotton t-shirt, available in multiple colors", "price", 599.00,
                                "stock", 100, "img", "https://picsum.photos/seed/tshirt/400/400"),
                        Map.of("name", "Denim Jacket", "desc", "Stylish denim jacket perfect for all seasons", "price",
                                1899.00, "stock", 25, "img", "https://picsum.photos/seed/jacket/400/400")),
                "Books", List.of(
                        Map.of("name", "The Art of Programming", "desc",
                                "A comprehensive guide to clean code and software design", "price", 450.00, "stock", 75,
                                "img", "https://picsum.photos/seed/book1/400/400"),
                        Map.of("name", "Business Strategy 101", "desc", "Master the fundamentals of business strategy",
                                "price", 350.00, "stock", 60, "img", "https://picsum.photos/seed/book2/400/400")),
                "Home & Kitchen", List.of(
                        Map.of("name", "Stainless Steel Water Bottle", "desc",
                                "Double-walled insulated bottle, keeps drinks cold for 24h", "price", 799.00, "stock",
                                80, "img", "https://picsum.photos/seed/bottle/400/400"),
                        Map.of("name", "Non-Stick Cookware Set", "desc", "5-piece premium cookware set with glass lids",
                                "price", 2999.00, "stock", 20, "img", "https://picsum.photos/seed/cookware/400/400")),
                "Sports", List.of(
                        Map.of("name", "Yoga Mat Premium", "desc",
                                "Extra thick eco-friendly yoga mat with carrying strap", "price", 999.00, "stock", 45,
                                "img", "https://picsum.photos/seed/yogamat/400/400"),
                        Map.of("name", "Running Shoes Ultra", "desc",
                                "Lightweight breathable running shoes with cushioned sole", "price", 3499.00, "stock",
                                35, "img", "https://picsum.photos/seed/shoes/400/400")),
                "Beauty", List.of(
                        Map.of("name", "Organic Face Cream", "desc",
                                "Natural moisturizing cream with vitamin E and aloe vera", "price", 699.00, "stock", 60,
                                "img", "https://picsum.photos/seed/cream/400/400"),
                        Map.of("name", "Hair Care Gift Set", "desc", "Shampoo, conditioner, and hair serum combo pack",
                                "price", 1299.00, "stock", 40, "img", "https://picsum.photos/seed/haircare/400/400")));

        products.forEach((categoryName, productList) -> {
            Category category = categoryRepository.findByName(categoryName)
                    .orElseThrow(() -> new RuntimeException("Category not found: " + categoryName));

            productList.forEach(p -> {
                Product product = Product.builder()
                        .name((String) p.get("name"))
                        .description((String) p.get("desc"))
                        .price(BigDecimal.valueOf((Double) p.get("price")))
                        .stock((Integer) p.get("stock"))
                        .imageUrl((String) p.get("img"))
                        .category(category)
                        .seller(seller)
                        .createdAt(LocalDateTime.now())
                        .build();
                productRepository.save(product);
            });
        });
    }
}
