package com.example.eventdrivenarchexample.product.service;

import com.example.eventdrivenarchexample.app.client.SQSClient;
import com.example.eventdrivenarchexample.product.config.ProductNotificationProperties;
import com.example.eventdrivenarchexample.product.config.ProductQueueProperties;
import com.example.eventdrivenarchexample.product.dto.events.request.*;
import com.example.eventdrivenarchexample.product.dto.events.response.TakeProductsResponse;
import com.example.eventdrivenarchexample.product.entity.ProductEntity;
import com.example.eventdrivenarchexample.product.enumeration.ProductEventResult;
import com.example.eventdrivenarchexample.product.enumeration.ProductEventType;
import com.example.eventdrivenarchexample.product.enumeration.TakeProductStatus;
import com.example.eventdrivenarchexample.product.exception.ProductException;
import com.example.eventdrivenarchexample.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    private final SQSClient sqsClient;

    private final ProductNotificationProperties notificationProperties;

    private final ProductQueueProperties productEventQueues;

    public Long createProduct(NewProductDTO newProduct) {
        log.info("Creating product with name {}...", newProduct.name());

        if (productRepository.existsByName(newProduct.name())) {
            log.error("Couldn't create product with name {}: Product already exists.", newProduct.name());
            String reason = "the product already exists.";
            throw new ProductException(String.format("Product with name %s already exists.", newProduct.name()), false, reason);
        }

        var product = ProductEntity.valueOf(newProduct);

        productRepository.save(product);
        log.info("Product with name {} created with id {}.", product.getName(), product.getId());
        return product.getId();
    }


    public void updateProduct(UpdateProductDTO payload) {

        var productOptional = productRepository.findById(payload.id());

        if (productOptional.isEmpty()) {
            log.error("Couldn't update product with id {}. Product does not exists.", payload.id());
            // TODO: CRIAR E LANÇAR UMA EXCEPTION AQUI
            notifyProductUpdateEventFail(payload, "the product does not exists");
            return;
        }

        var product = productOptional.get();
        if (product.getQuantity() <= 0) {
            log.error("Couldn't update product with id {}. Negative quantity, value: {}.", payload.id(), payload.quantity());
            // TODO: CRIAR E LANÇAR UMA EXCEPTION AQUI
            notifyProductUpdateEventFail(payload, "Product quantity shouldn't be negative.");
            return;
        }


        product.copyNonNullValuesFrom(payload);
        productRepository.save(product);
        notifyProductUpdateEventSuccess(payload);

    }

    private void notifyProductUpdateEventFail(UpdateProductDTO payload, String reason) {
        var notification = NotificationBodyDTO.valueOf(notificationProperties.getUpdateFailed());
        notification.formatMessage(reason);
        notification.formatTittle(payload.id().toString());
        sqsClient.sendToSQS(productEventQueues.getNotificationEventsQueue(), notification);

        if (StringUtils.isNotBlank(payload.callbackQueue())) {
            var callbackPayload = ProductEventCallbackDTO.builder()
                    .eventId(payload.eventId())
                    .productId(payload.id())
                    .eventType(ProductEventType.UPDATE)
                    .eventResult(ProductEventResult.FAIL)
                    .build();

            sqsClient.sendToSQS(payload.callbackQueue(), callbackPayload);
        }
    }

    private void notifyProductUpdateEventSuccess(UpdateProductDTO payload) {
        var notification = NotificationBodyDTO.valueOf(notificationProperties.getUpdateSuccess());
        notification.formatTittle(payload.id().toString());
        sqsClient.sendToSQS(productEventQueues.getNotificationEventsQueue(), notification);

        if (StringUtils.isNotBlank(payload.callbackQueue())) {
            var callbackPayload = ProductEventCallbackDTO.builder()
                    .eventId(payload.eventId())
                    .productId(payload.id())
                    .eventType(ProductEventType.UPDATE)
                    .eventResult(ProductEventResult.SUCCESS)
                    .build();

            sqsClient.sendToSQS(payload.callbackQueue(), callbackPayload);
        }

    }

    public TakeProductsResponse takeProduct(TakeProductsDTO eventPayload) {

        var productIds = eventPayload.products().stream().map(TakeProductsDTO.Product::id).toList();
        var products = productRepository.findAllById(productIds);

        if (products.size() != productIds.size()) {

            var eventResult = ProductEventResult.FAIL;
            var productsToReturn = eventPayload.products().stream()
                    .map(requestedProduct -> products.stream()
                            .filter(Objects::nonNull)
                            .filter(product -> product.getId().equals(requestedProduct.id()))
                            .findFirst()
                            .map(product -> TakeProductsResponse.Product.valueOf(product, TakeProductStatus.NOT_TAKEN))
                            .orElse(TakeProductsResponse.Product.valueOf(requestedProduct)))
                    .toList();

            return TakeProductsResponse.builder()
                    .products(productsToReturn)
                    .result(eventResult)
                    .type(ProductEventType.TAKE)
                    .build();
        }

        var matchedProducts = matchFoundProductsWithRequestedProductSById(eventPayload.products(), products);
        var hasProductsOutOfStock = matchedProducts.entrySet().stream()
                .anyMatch(entry -> ! entry.getKey().quantityCanBeTaken(entry.getValue().quantity()));
        var eventResult = hasProductsOutOfStock ? ProductEventResult.FAIL : ProductEventResult.SUCCESS;
        var productsToReturn = tryToTakeProductsFromStock(matchedProducts, hasProductsOutOfStock);


        return TakeProductsResponse.builder()
                .products(productsToReturn)
                .result(eventResult)
                .type(ProductEventType.TAKE)
                .build();
    }

    private List<TakeProductsResponse.Product> tryToTakeProductsFromStock(Map<ProductEntity, TakeProductsDTO.Product> matchedProducts, boolean hasProductsOutOfStock) {
        var productsToReturn = matchedProducts.entrySet().stream().map(entry -> {
            var product = entry.getKey();
            var requestedProduct = entry.getValue();

            if (hasProductsOutOfStock) {
                TakeProductStatus status = product.quantityCanBeTaken(requestedProduct.quantity()) ? TakeProductStatus.OUT_OF_STOCK : TakeProductStatus.NOT_TAKEN;
                return TakeProductsResponse.Product.valueOf(product, status);
            }

            product.takeQuantity(requestedProduct.quantity());
            return TakeProductsResponse.Product.valueOf(product, TakeProductStatus.TAKEN);
        }).toList();

        if (! hasProductsOutOfStock) {
            productRepository.saveAll(matchedProducts.keySet());
        }

        return productsToReturn;
    }


    private Map<ProductEntity, TakeProductsDTO.Product> matchFoundProductsWithRequestedProductSById(List<TakeProductsDTO.Product> requestedProducts, List<ProductEntity> products) {
        return requestedProducts.stream()
                .map(requestedProduct -> {
                    var matchingProduct = products.stream()
                            .filter(product -> product.getId().equals(requestedProduct.id()))
                            .findFirst()
                            .get();
                    return Map.entry(matchingProduct, requestedProduct);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
