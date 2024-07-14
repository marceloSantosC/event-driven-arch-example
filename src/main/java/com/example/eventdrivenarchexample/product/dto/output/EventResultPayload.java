package com.example.eventdrivenarchexample.product.dto.output;


import com.example.eventdrivenarchexample.product.enumeration.ProductEventResult;
import com.example.eventdrivenarchexample.product.enumeration.ProductEventType;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResultPayload<T> {

    private T body;

    private String customId;

    private ProductEventType eventType;

    private ProductEventResult eventResult;

}
