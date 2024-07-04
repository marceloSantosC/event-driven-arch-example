package com.example.eventdrivenarchexample.order.listener;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetrievedProductsForOrderSQSListener {

    @SqsListener("${event-queues.order.queried-products-result-events}")
    public void onRerieveProducts(String message) {
        log.info(message);
    }
}
