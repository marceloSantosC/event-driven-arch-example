package com.example.eventdrivenarchexample.app.client;

import com.example.eventdrivenarchexample.app.exception.MessageBrokerException;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SQSClient {

    private final SqsTemplate sqsTemplate;

    public String sendToSQS(String queue, Object payload) {
        if (queue == null) {
            throw new MessageBrokerException("Cannot send message to null queue.", null);
        }

        if (payload == null) {
            throw new MessageBrokerException("Cannot send message with null payload.", queue);
        }

        return sqsTemplate.send(queue, payload).messageId().toString();
    }

}
