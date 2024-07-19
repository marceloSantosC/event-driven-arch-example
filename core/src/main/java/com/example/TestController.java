package com.example;

import com.example.client.SQSClient;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/send-sqs")
public class TestController {

    private final SQSClient sqsClient;

    private final SqsTemplate sqsTemplate;

    @PostMapping
    public void sendSQS(@RequestParam String queue, @RequestBody Object message) {
        sqsClient.sendToSQS(queue, message, Map.of(SQSClient.HEADER_TRACE_ID_NAME, UUID.randomUUID().toString()));
    }

    @GetMapping
    public Object getMessage(@RequestParam String queue) {
        return sqsTemplate.receive(options -> options
                        .maxNumberOfMessages(1)
                        .queue(queue))
                .map(Message::getPayload)
                .orElse(null);
    }


}
