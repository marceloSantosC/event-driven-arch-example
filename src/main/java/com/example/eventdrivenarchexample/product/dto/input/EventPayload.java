package com.example.eventdrivenarchexample.product.dto.input;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class EventPayload<T> {

    private T body;

    private String callbackQueue;

    private String eventId;

    private String customId;

}
