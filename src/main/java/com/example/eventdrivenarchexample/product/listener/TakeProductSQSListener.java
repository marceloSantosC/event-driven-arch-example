package com.example.eventdrivenarchexample.product.listener;

import com.example.eventdrivenarchexample.app.client.SQSClient;
import com.example.eventdrivenarchexample.product.config.ProductNotificationProperties;
import com.example.eventdrivenarchexample.product.dto.input.EventPayload;
import com.example.eventdrivenarchexample.product.dto.input.NotificationBodyInput;
import com.example.eventdrivenarchexample.product.dto.input.TakeProductsInput;
import com.example.eventdrivenarchexample.product.dto.output.TakenProductOutput;
import com.example.eventdrivenarchexample.product.dto.output.TakenProductsEventResult;
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
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.eventdrivenarchexample.product.enumeration.TakeProductStatus.*;

@Slf4j
@Service
public class TakeProductSQSListener extends EventListener<TakenProductsEventResult> {

    private static final List<TakeProductStatus> TAKE_PRODUCTS_FAIL_STATUS = List.of(NOT_FOUND, NOT_TAKEN, OUT_OF_STOCK);

    private final ProductNotificationProperties notificationProperties;

    private final ProductService productService;

    private final ObjectMapper objectMapper;

    public TakeProductSQSListener(ProductNotificationService notificationService,
                                  ProductNotificationProperties notificationProperties,
                                  ProductService productService,
                                  ObjectMapper objectMapper,
                                  SQSClient sqsClient) {
        super(notificationService, sqsClient);
        this.notificationProperties = notificationProperties;
        this.productService = productService;
        this.objectMapper = objectMapper;
    }


    @SqsListener("${event-queues.product.take-events}")
    public void onTakeProductEvent(String payload, @Headers Map<String, Object> headers) {

        String traceId = (String) headers.get(SQSClient.HEADER_TRACE_ID_NAME);


        try {
            EventPayload<TakeProductsInput> event = objectMapper.readValue(payload, new TypeReference<>() {
            });
            event.setTraceId(traceId);

            List<TakenProductOutput> products = productService.takeProduct(event.getBody());

            var hasProductsWithFailStatus = products.stream().anyMatch(product -> TAKE_PRODUCTS_FAIL_STATUS.contains(product.status()));

            ProductEventResult result = hasProductsWithFailStatus ? ProductEventResult.FAIL : ProductEventResult.SUCCESS;
            var resultPayload = new TakenProductsEventResult(products);


            NotificationBodyInput notification = getNotificationBody(resultPayload, result);
            sendNotification(notification, traceId);

            super.tryToSendEventResultToCallbackQueue(event, resultPayload, result);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message for product event {} with trace id {}.", getEventType(), traceId);
        }
    }

    @Override
    protected ProductEventType getEventType() {
        return ProductEventType.TAKE;
    }

    private NotificationBodyInput getNotificationBody(TakenProductsEventResult resultPayload, ProductEventResult result) {
        var productIds = resultPayload.products().stream()
                .map(TakenProductOutput::id)
                .map(Object::toString)
                .collect(Collectors.joining(", "));

        if (ProductEventResult.FAIL.equals(result)) {
            var notificationBody = NotificationBodyInput.valueOf(notificationProperties.getTakeFailed());
            notificationBody.formatMessage(productIds, "check the individual products for more info");
            return notificationBody;
        }

        var notificationBody = NotificationBodyInput.valueOf(notificationProperties.getTakeSuccess());
        notificationBody.formatMessage(productIds);
        return notificationBody;
    }

}
