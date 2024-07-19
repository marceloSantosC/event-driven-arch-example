package com.example.service;

import com.example.client.SQSClient;
import com.example.config.ProductCommandQueues;
import com.example.dto.command.Command;
import com.example.dto.command.ShipProducts;
import com.example.dto.event.OrderCreated;
import com.example.dto.event.OrderReceived;
import com.example.entity.OrderEntity;
import com.example.entity.OrderProductEntity;
import com.example.entity.OrderProductEntityID;
import com.example.enumeration.OrderStatus;
import com.example.exception.OrderException;
import com.example.repository.OrderProductRepository;
import com.example.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final SQSClient sqsClient;

    private final OrderRepository orderRepository;

    private final ProductCommandQueues productCommandQueues;

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

    public void requestShipping(OrderCreated createdOrder) {
        OrderEntity order = orderRepository.findById(createdOrder.orderId())
                .orElseThrow(() -> {
                    log.error("Order with id {} not found.", createdOrder.orderId());
                    return new OrderException("Order not found", false);
                });

        Command<ShipProducts> shipProductsCommand = Command.<ShipProducts>builder()
                .body(ShipProducts.valueOf(createdOrder))
                .customId(createdOrder.orderId().toString())
                .build();
        String traceId = UUID.randomUUID().toString();
        sqsClient.sendToSQS(productCommandQueues.getShip(), shipProductsCommand, Map.of(SQSClient.HEADER_TRACE_ID_NAME, traceId));

        log.info("Sent event ship products for order with id {} and trace id {}.", createdOrder.orderId(), traceId);

        order.setStatus(OrderStatus.PRODUCT_SHIPPING_REQUESTED);
        orderRepository.save(order);
        log.info("Updated order with id {} status to {}.", order.getId(), order.getStatus());
    }
}
