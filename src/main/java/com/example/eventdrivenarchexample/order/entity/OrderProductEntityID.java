package com.example.eventdrivenarchexample.order.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class OrderProductEntityID {

    private Long orderId;

    private Long productId;

}
