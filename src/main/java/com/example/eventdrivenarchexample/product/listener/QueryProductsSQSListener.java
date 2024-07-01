package com.example.eventdrivenarchexample.product.listener;

import com.example.eventdrivenarchexample.app.client.SQSClient;
import com.example.eventdrivenarchexample.product.dto.events.QueryProductsEventPayload;
import com.example.eventdrivenarchexample.product.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueryProductsSQSListener {

    private final ObjectMapper objectMapper;

    private final ProductService productService;

    private final SQSClient sqsClient;

    @SqsListener(value = "${event-queues.product.query-events}")
    public void onQueryProduct(@Payload String message) throws JsonProcessingException {
        var payload = objectMapper.readValue(message, QueryProductsEventPayload.class);
        var products = productService.queryProduct(payload);
        sqsClient.sendToSQS(payload.callbackQueue(), products);
    }

}
