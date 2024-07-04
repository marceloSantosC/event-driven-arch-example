package com.example.eventdrivenarchexample.product.service;

import com.example.eventdrivenarchexample.app.client.SQSClient;
import com.example.eventdrivenarchexample.product.config.ProductNotificationProperties;
import com.example.eventdrivenarchexample.product.config.ProductQueueProperties;
import com.example.eventdrivenarchexample.product.dto.events.*;
import com.example.eventdrivenarchexample.product.entity.ProductEntity;
import com.example.eventdrivenarchexample.product.enumeration.ProductEventResult;
import com.example.eventdrivenarchexample.product.enumeration.ProductEventType;
import com.example.eventdrivenarchexample.product.mapper.ProductMapper;
import com.example.eventdrivenarchexample.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    private final SQSClient sqsClient;

    private final ProductNotificationProperties notificationProperties;

    private final ProductQueueProperties productEventQueues;

    public QueryProductsResponseDataEventPayload queryProduct(QueryProductsEventPayload payload) {
        List<ProductEntity> retrievedProducts = productRepository.findAllById(payload.productIds());
        return QueryProductsResponseDataEventPayload.valueOf(payload.eventId(), retrievedProducts);
    }

    public void createProduct(NewProductEventPayload eventPayload) {
        log.info("Creating product with name {}...", eventPayload.name());

        if (productRepository.existsByName(eventPayload.name())) {
            log.error("Couldn't create product with name {}: Product already exists.", eventPayload.name());

            notifyProductCreationFail(eventPayload);
            return;
        }

        var product = ProductMapper.newProductDTOToProductEntity(eventPayload);
        productRepository.save(product);
        log.info("Product with name {} created with id {}.", product.getName(), product.getId());
        notifyProductCreationSuccess(product, eventPayload);

    }

    private void notifyProductCreationFail(NewProductEventPayload payload) {
        var notification = notificationProperties.getCreationFailed();
        notification.formatMessage(payload.name(), "the product already exists");
        notification.formatTittle(payload.name());
        sqsClient.sendToSQS(productEventQueues.getNotificationEventsQueue(), notification);

        if (StringUtils.isNotBlank(payload.callbackQueue())) {
            var callbackPayload = ProductEventCallbackPayload.builder()
                    .eventId(payload.eventId())
                    .eventType(ProductEventType.CREATION)
                    .eventResult(ProductEventResult.FAIL)
                    .build();
            sqsClient.sendToSQS(payload.callbackQueue(), callbackPayload);
        }


    }

    private void notifyProductCreationSuccess(ProductEntity product, NewProductEventPayload payload) {
        var notification = notificationProperties.getCreationSuccess();
        notification.formatTittle(product.getName());
        notification.formatMessage(product.getName(), product.getId().toString());
        sqsClient.sendToSQS(productEventQueues.getNotificationEventsQueue(), notification);

        if (StringUtils.isNotBlank(payload.callbackQueue())) {
            var callbackPayload = ProductEventCallbackPayload.builder()
                    .eventId(payload.eventId())
                    .productId(product.getId())
                    .eventType(ProductEventType.CREATION)
                    .eventResult(ProductEventResult.SUCCESS)
                    .build();

            sqsClient.sendToSQS(payload.callbackQueue(), callbackPayload);
        }
    }

    public void updateProduct(UpdateProductEventPayload payload) {

        var productOptional = productRepository.findById(payload.id());

        if (productOptional.isEmpty()) {
            log.error("Couldn't update product with id {}. Product does not exists.", payload.id());
            notifyProductUpdateEventFail(payload, "the product does not exists");
            return;
        }

        var product = productOptional.get();
        if (product.getQuantity() <= 0) {
            log.error("Couldn't update product with id {}. Negative quantity, value: {}.", payload.id(), payload.quantity());
            notifyProductUpdateEventFail(payload, "Product quantity shouldn't be negative.");
            return;
        }


        product.copyNonNullValuesFrom(payload);
        productRepository.save(product);
        notifyProductUpdateEventSuccess(payload);

    }

    private void notifyProductUpdateEventFail(UpdateProductEventPayload payload, String reason) {
        var notification = notificationProperties.getUpdateFailed();
        notification.formatMessage(reason);
        notification.formatTittle(payload.id().toString());
        sqsClient.sendToSQS(productEventQueues.getNotificationEventsQueue(), notification);

        if (StringUtils.isNotBlank(payload.callbackQueue())) {
            var callbackPayload = ProductEventCallbackPayload.builder()
                    .eventId(payload.eventId())
                    .productId(payload.id())
                    .eventType(ProductEventType.UPDATE)
                    .eventResult(ProductEventResult.FAIL)
                    .build();

            sqsClient.sendToSQS(payload.callbackQueue(), callbackPayload);
        }
    }

    private void notifyProductUpdateEventSuccess(UpdateProductEventPayload payload) {
        var notification = notificationProperties.getUpdateSuccess();
        notification.formatTittle(payload.id().toString());
        sqsClient.sendToSQS(productEventQueues.getNotificationEventsQueue(), notification);

        if (StringUtils.isNotBlank(payload.callbackQueue())) {
            var callbackPayload = ProductEventCallbackPayload.builder()
                    .eventId(payload.eventId())
                    .productId(payload.id())
                    .eventType(ProductEventType.UPDATE)
                    .eventResult(ProductEventResult.SUCCESS)
                    .build();

            sqsClient.sendToSQS(payload.callbackQueue(), callbackPayload);
        }

    }
}
