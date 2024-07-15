package com.example.eventdrivenarchexample.product.dto.command;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandPayload<T> {

    private T body;

    private String callbackQueue;

    private String customId;

    @JsonIgnore
    private String traceId;

}
