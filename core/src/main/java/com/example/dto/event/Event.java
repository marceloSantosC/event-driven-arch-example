package com.example.dto.event;


import com.example.enumeration.EventResult;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event<T, E extends Enum<E>> {

    private T body;

    private String customId;

    private E eventType;

    private EventResult eventResult;

}
