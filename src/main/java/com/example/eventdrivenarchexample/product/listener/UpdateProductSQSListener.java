package com.example.eventdrivenarchexample.product.listener;

import com.example.eventdrivenarchexample.product.dto.events.UpdateProductEventPayload;
import com.example.eventdrivenarchexample.product.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateProductSQSListener {

    private final ProductService productService;

    private final ObjectMapper objectMapper;

    @SqsListener(queueNames = {"${event-queues.product.update-events}"})
    public void onCreateProductEvent(String eventPayload) throws JsonProcessingException {
        var event = objectMapper.readValue(eventPayload, UpdateProductEventPayload.class);
        productService.updateProduct(event);
    }

}
