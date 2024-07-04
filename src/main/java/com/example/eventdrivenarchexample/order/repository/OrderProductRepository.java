package com.example.eventdrivenarchexample.order.repository;

import com.example.eventdrivenarchexample.order.entity.OrderProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductRepository extends JpaRepository<OrderProductEntity, Long> {
}
