package com.example.eventdrivenarchexample.product.listener;

import com.example.eventdrivenarchexample.product.dto.events.NewProductEventPayload;
import com.example.eventdrivenarchexample.product.service.ProductService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateProductSQSListener {

    private final ProductService productService;

    @SqsListener(queueNames = "${event-queues.product.create-events}")
    public void onCreateProductEvent(NewProductEventPayload eventPayload) {
        productService.createProduct(eventPayload);
    }

}
