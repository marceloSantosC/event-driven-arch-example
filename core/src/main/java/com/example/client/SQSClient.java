package com.example.client;

import com.example.exception.MessageBrokerException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SQSClient {

    public static final String HEADER_TRACE_ID_NAME = "trace_id";

    private final SqsTemplate sqsTemplate;

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public void sendToSQS(String queue, Object payload, Map<String, Object> headers) {
        if (queue == null) {
            throw new MessageBrokerException("Cannot send message to null queue.", null);
        }

        if (payload == null) {
            throw new MessageBrokerException("Cannot send message with null payload.", queue);
        }

        if (headers == null || StringUtils.isBlank((String) headers.get(HEADER_TRACE_ID_NAME))) {
            throw new MessageBrokerException(String.format("Header %s is mandatory for all messages.", HEADER_TRACE_ID_NAME), queue);
        }

        String messageId = sqsTemplate.send(sqsSendOptions ->
        {
            try {
                sqsSendOptions
                        .queue(queue)
                        .payload(objectMapper.writeValueAsString(payload))
                        .headers(headers);
            } catch (JsonProcessingException e) {
                throw new MessageBrokerException(e, queue);
            }
        }).messageId().toString();
        log.info("Message with id {} sent to queue {}.", messageId, queue);

    }

}
