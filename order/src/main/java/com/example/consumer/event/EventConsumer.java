package com.example.consumer.event;

import com.example.client.SQSClient;
import com.example.dto.event.Event;
import com.example.enumeration.EventResult;
import com.example.enumeration.OrderEventType;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public abstract class EventConsumer<O> {

    protected final SQSClient sqsClient;

    protected void sendEvent(Event<?, OrderEventType> event, O output, EventResult result, String traceId) {
        Event<O, OrderEventType> eventPayload = Event.<O, OrderEventType>builder()
                .customId(event.getCustomId())
                .eventType(generatedEventType())
                .eventResult(result)
                .body(output)
                .build();

        sqsClient.sendToSQS(generatedEventQueue(), eventPayload, Map.of(SQSClient.HEADER_TRACE_ID_NAME, traceId));
    }

    protected abstract OrderEventType generatedEventType();

    protected abstract String generatedEventQueue();

}
