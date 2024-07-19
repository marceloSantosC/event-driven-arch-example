package com.example.service;

import com.example.dto.events.OrderCreated;
import com.example.dto.events.OrderReceived;
import com.example.entity.OrderEntity;
import com.example.entity.OrderProductEntity;
import com.example.entity.OrderProductEntityID;
import com.example.repository.OrderProductRepository;
import com.example.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final OrderProductRepository orderProductRepository;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public OrderCreated createOrder(OrderReceived newOrder) {

        var order = OrderEntity.valueOf(newOrder);
        orderRepository.save(order);
        List<OrderProductEntity> orderProducts = newOrder.products().stream()
                .map(product -> OrderProductEntity.builder()
                        .order(order)
                        .id(new OrderProductEntityID(order.getId(), product.id()))
                        .quantity(product.quantity())
                        .build())
                .toList();
        orderProductRepository.saveAll(orderProducts);

        return OrderCreated.valueOf(order.getId(), orderProducts);

    }

}
