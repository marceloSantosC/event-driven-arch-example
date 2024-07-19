package com.example;

import com.example.client.SQSClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/send-sqs")
public class TestController {

    private final SQSClient sqsClient;

    @PostMapping
    public void sendSQS(@RequestParam String queue, @RequestBody Object message) {
        sqsClient.sendToSQS(queue, message, Map.of(SQSClient.HEADER_TRACE_ID_NAME, UUID.randomUUID().toString()));
    }


}
