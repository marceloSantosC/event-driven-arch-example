package com.example.eventdrivenarchexample;

import com.example.eventdrivenarchexample.app.client.SQSClient;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/send-sqs")
public class TestController {

    private final SQSClient sqsClient;

    private final SqsTemplate sqsTemplate;

    @PostMapping
    public void sendSQS(@RequestParam String queue, @RequestBody Object message) {
        sqsClient.sendToSQS(queue, message);
    }


}
