package com.example.eventdrivenarchexample.product.dto.input;


import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventPayload<T> {

    private T body;

    private String callbackQueue;

    private String customId;

}
