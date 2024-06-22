package com.example.eventdrivenarchexample.product.service;

import com.example.eventdrivenarchexample.app.client.SQSClient;
import com.example.eventdrivenarchexample.product.config.ProductEventQueueProperties;
import com.example.eventdrivenarchexample.product.config.ProductNotificationProperties;
import com.example.eventdrivenarchexample.product.dto.events.NewProductEventPayload;
import com.example.eventdrivenarchexample.product.entity.ProductEntity;
import com.example.eventdrivenarchexample.product.mapper.ProductMapper;
import com.example.eventdrivenarchexample.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    private final SQSClient sqsClient;

    private final ProductNotificationProperties notificationProperties;

    private final ProductEventQueueProperties productEventQueues;

    public void createProduct(NewProductEventPayload eventPayload) {
        log.info("Creating product with name {}...", eventPayload.name());

        if (productRepository.existsByName(eventPayload.name())) {
            log.error("Couldn't create product with name {}: Product already exists.", eventPayload.name());
            sendProductCreationFailEvents(eventPayload);
            return;
        }

        var product = ProductMapper.newProductDTOToProductEntity(eventPayload);
        productRepository.save(product);
        log.info("Product with name {} created with id {}.", product.getId(), product.getId());
        sendProductCreationSuccessEvents(product);

    }

    private void sendProductCreationFailEvents(NewProductEventPayload eventPayload) {
        var notification = notificationProperties.getCreationFailPushNotification();
        notification.formatMessage(eventPayload.name(), "the product already exists");
        notification.formatTittle(eventPayload.name());
        sqsClient.sendToSQS(productEventQueues.getProductNotificationEventsQueue(), notification);
    }

    private void sendProductCreationSuccessEvents(ProductEntity product) {
        var notification = notificationProperties.getCreationSuccessPushNotification();
        notification.formatTittle(product.getName());
        notification.formatMessage(product.getName(), product.getId().toString());
        sqsClient.sendToSQS(productEventQueues.getProductNotificationEventsQueue(), notification);


    }

}
