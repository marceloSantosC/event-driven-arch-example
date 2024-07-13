package com.example.eventdrivenarchexample.product.listener;

import com.example.eventdrivenarchexample.app.client.SQSClient;
import com.example.eventdrivenarchexample.product.dto.events.request.TakeProductsDTO;
import com.example.eventdrivenarchexample.product.service.ProductService;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TakeProductSQSListener {

    private final ProductService productService;

    private final ObjectMapper objectMapper;

    private final SQSClient sqsClient;


    @SqsListener("${event-queues.product.take-events}")
    public void onTakeProductEvent(String message) {
        try {
            var eventPayload = objectMapper.readValue(message, TakeProductsDTO.class);
            var responsePayload = productService.takeProduct(eventPayload);
            sqsClient.sendToSQS(eventPayload.callbackQueue(), responsePayload);
        } catch (JacksonException e) {
            log.error("Failed to serialize message.");
        }
    }

}
