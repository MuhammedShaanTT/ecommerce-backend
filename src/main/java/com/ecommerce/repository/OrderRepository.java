package com.ecommerce.repository;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByBuyerOrderByCreatedAtDesc(User buyer);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.items i WHERE i.product.seller = :seller ORDER BY o.createdAt DESC")
    List<Order> findOrdersBySeller(@Param("seller") User seller);
}
