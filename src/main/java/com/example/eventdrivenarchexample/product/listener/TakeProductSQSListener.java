package com.example.eventdrivenarchexample.product.listener;

import com.example.eventdrivenarchexample.app.client.SQSClient;
import com.example.eventdrivenarchexample.product.config.ProductNotificationProperties;
import com.example.eventdrivenarchexample.product.dto.input.EventPayload;
import com.example.eventdrivenarchexample.product.dto.input.NotificationBodyInput;
import com.example.eventdrivenarchexample.product.dto.input.TakeProductsInput;
import com.example.eventdrivenarchexample.product.dto.output.TakenProductOutput;
import com.example.eventdrivenarchexample.product.dto.output.TakenProductsEvent;
import com.example.eventdrivenarchexample.product.enumeration.ProductEventResult;
import com.example.eventdrivenarchexample.product.enumeration.ProductEventType;
import com.example.eventdrivenarchexample.product.enumeration.TakeProductStatus;
import com.example.eventdrivenarchexample.product.service.ProductNotificationService;
import com.example.eventdrivenarchexample.product.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.eventdrivenarchexample.product.enumeration.TakeProductStatus.*;

@Slf4j
@Service
public class TakeProductSQSListener extends EventListenerWithNotification {

    private static final List<TakeProductStatus> TAKE_PRODUCTS_FAIL_STATUS = List.of(NOT_FOUND, NOT_TAKEN, OUT_OF_STOCK);

    private final ProductNotificationProperties notificationProperties;

    private final ProductService productService;

    private final ObjectMapper objectMapper;

    private final SQSClient sqsClient;

    public TakeProductSQSListener(ProductNotificationService notificationService,
                                  ProductNotificationProperties notificationProperties,
                                  ProductService productService,
                                  ObjectMapper objectMapper,
                                  SQSClient sqsClient) {
        super(notificationService);
        this.notificationProperties = notificationProperties;
        this.productService = productService;
        this.objectMapper = objectMapper;
        this.sqsClient = sqsClient;
    }


    @SqsListener("${event-queues.product.take-events}")
    public void onTakeProductEvent(String payload, @Headers Map<String, Object> headers) {

        String traceId = (String) headers.get(SQSClient.HEADER_TRACE_ID_NAME);


        try {
            EventPayload<TakeProductsInput> event = objectMapper.readValue(payload, new TypeReference<>() {
            });

            List<TakenProductOutput> products = productService.takeProduct(event.getBody());

            var hasProductsWithFailStatus = products.stream().anyMatch(product -> TAKE_PRODUCTS_FAIL_STATUS.contains(product.status()));

            var eventResult = TakenProductsEvent.builder()
                    .type(ProductEventType.TAKE)
                    .result(hasProductsWithFailStatus ? ProductEventResult.FAIL : ProductEventResult.SUCCESS)
                    .products(products)
                    .build();

            var notificationDTO = hasProductsWithFailStatus ? notificationProperties.getTakeFailed() : notificationProperties.getTakeSuccess();
            var notificationBody = NotificationBodyInput.valueOf(notificationDTO);


            var productIds = event.getBody().products().stream()
                    .map(TakeProductsInput.Product::id)
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));


            if (hasProductsWithFailStatus) {
                notificationBody.formatMessage(productIds, " check the individual products for more info.");
            } else {
                notificationBody.formatMessage(productIds);
            }

            sendNotification(notificationBody, traceId);

            if (StringUtils.isNotBlank(event.getCallbackQueue())) {
                sqsClient.sendToSQS(event.getCallbackQueue(), eventResult, Map.of(SQSClient.HEADER_TRACE_ID_NAME, traceId));
            }

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message for product event {} with trace id {}.", getEventType(), traceId);
        }
    }

    @Override
    protected ProductEventType getEventType() {
        return ProductEventType.TAKE;
    }
}
