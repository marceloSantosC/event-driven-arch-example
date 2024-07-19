package com.example.product.consumer.command;

import com.example.client.SQSClient;
import com.example.dto.command.Command;
import com.example.enumeration.EventResult;
import com.example.product.config.ProductEventQueues;
import com.example.product.config.ProductNotificationProperties;
import com.example.product.dto.command.ShipProducts;
import com.example.product.dto.command.UserNotification;
import com.example.product.dto.event.ShippedProduct;
import com.example.product.dto.event.ShippedProducts;
import com.example.product.enumeration.NotificationType;
import com.example.product.enumeration.ProductEventType;
import com.example.product.enumeration.ShippedProductStatus;
import com.example.product.service.ProductNotificationService;
import com.example.product.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.product.enumeration.ShippedProductStatus.*;


@Slf4j
@Service
public class ShipProductConsumer extends CommandConsumer<ShippedProducts> {

    private static final List<ShippedProductStatus> TAKE_PRODUCTS_FAIL_STATUS = List.of(NOT_FOUND, NOT_SHIPPED, OUT_OF_STOCK);

    private final ProductNotificationProperties notificationProperties;

    private final ProductService productService;

    private final ProductEventQueues eventQueues;

    private final ObjectMapper objectMapper;


    public ShipProductConsumer(ProductNotificationService notificationService,
                               ProductNotificationProperties notificationProperties,
                               ProductService productService,
                               ObjectMapper objectMapper,
                               SQSClient sqsClient,
                               ProductEventQueues eventQueues) {
        super(notificationService, sqsClient);
        this.notificationProperties = notificationProperties;
        this.productService = productService;
        this.objectMapper = objectMapper;
        this.eventQueues = eventQueues;
    }


    @SqsListener("${sqs-queues.product.commands.ship}")
    public void shipProduct(String payload, @Headers Map<String, Object> headers) {

        String traceId = (String) headers.get(SQSClient.HEADER_TRACE_ID_NAME);

        try {
            Command<ShipProducts> event = objectMapper.readValue(payload, new TypeReference<>() {
            });
            event.setTraceId(traceId);

            Set<ShippedProduct> products = productService.shipProducts(event.getBody());

            var hasProductsWithFailStatus = products.stream().anyMatch(product -> TAKE_PRODUCTS_FAIL_STATUS.contains(product.getStatus()));

            EventResult result = hasProductsWithFailStatus ? EventResult.FAIL : EventResult.SUCCESS;
            var eventPayload = new ShippedProducts(products, LocalDateTime.now());


            UserNotification notification = getNotificationBody(eventPayload, result);
            sendNotification(notification, traceId);

            sendEvent(event, eventPayload, result);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message for product event {} with trace id {}.", getEventType(), traceId);
        }
    }

    private UserNotification getNotificationBody(ShippedProducts resultPayload, EventResult result) {
        var productIds = resultPayload.products().stream()
                .map(ShippedProduct::getId)
                .map(Object::toString)
                .collect(Collectors.joining(", "));

        if (EventResult.FAIL.equals(result)) {
            var notificationBody = notificationProperties.getShipFailed();
            notificationBody.setType(NotificationType.WINDOWS_SYSTEM_TRAY);
            notificationBody.formatMessage(productIds, "check the individual products for more info");
            return notificationBody;
        }

        var notificationBody = notificationProperties.getShipSuccess();
        notificationBody.setType(NotificationType.WINDOWS_SYSTEM_TRAY);
        notificationBody.formatMessage(productIds);
        return notificationBody;
    }

    @Override
    protected ProductEventType getEventType() {
        return ProductEventType.TAKE;
    }

    @Override
    protected String getEventQueue() {
        return eventQueues.getShipped();
    }


}
