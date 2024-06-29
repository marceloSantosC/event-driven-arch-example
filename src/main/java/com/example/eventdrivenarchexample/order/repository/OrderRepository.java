package com.example.eventdrivenarchexample.order.repository;

import com.example.eventdrivenarchexample.order.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
}
