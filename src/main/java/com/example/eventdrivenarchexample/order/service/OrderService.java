package com.example.eventdrivenarchexample.order.service;

import com.example.eventdrivenarchexample.app.client.SQSClient;
import com.example.eventdrivenarchexample.order.config.OrderQueueProperties;
import com.example.eventdrivenarchexample.order.dto.events.NewOrderEventPayload;
import com.example.eventdrivenarchexample.order.dto.events.QueryProductsEventPayload;
import com.example.eventdrivenarchexample.order.entity.OrderEntity;
import com.example.eventdrivenarchexample.order.entity.OrderProductEntity;
import com.example.eventdrivenarchexample.order.entity.OrderProductEntityID;
import com.example.eventdrivenarchexample.order.repository.OrderProductRepository;
import com.example.eventdrivenarchexample.order.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final OrderProductRepository orderProductRepository;

    private final SQSClient sqsClient;
    private final OrderQueueProperties orderQueueProperties;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void createOrder(NewOrderEventPayload newOrder) {

        var order = OrderEntity.valueOf(newOrder);
        orderRepository.save(order);
        var orderProducts = newOrder.products().stream()
                .map(product -> OrderProductEntity.builder()
                        .order(order)
                        .id(new OrderProductEntityID(order.getId(), product.id()))
                        .quantity(product.quantity())
                        .build())
                .toList();
        orderProductRepository.saveAll(orderProducts);


        var productIds = newOrder.products().stream().map(NewOrderEventPayload.Product::id).toList();
        var queryPayload = QueryProductsEventPayload.builder()
                .callbackQueue(orderQueueProperties.getQueriedProductsResultQueue())
                .eventId(order.getId().toString())
                .productIds(productIds)
                .build();


        sqsClient.sendToSQS(orderQueueProperties.getQueryProductsQueue(), queryPayload);

    }

}
