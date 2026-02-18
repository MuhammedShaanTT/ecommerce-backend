package com.ecommerce.controller;

import com.ecommerce.dto.product.ProductRequest;
import com.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // SELLER ADD PRODUCT
    @PostMapping
    public String addProduct(@RequestBody ProductRequest request){
        productService.addProduct(request);
        return "Product Added Successfully";
    }
}
