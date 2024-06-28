package com.example.eventdrivenarchexample.product.entity;


import com.example.eventdrivenarchexample.product.dto.events.UpdateProductEventPayload;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Builder
@Table(name = "PRODUCT")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProductEntity {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "NAME", unique = true)
    private String name;

    @Column(name = "COLOR")
    private String color;

    @Column(name = "QUANTITY", nullable = false)
    private Long quantity;

    @Column(name = "PRICE", nullable = false)
    private BigDecimal price;

    public void copyNonNullValuesFrom(UpdateProductEventPayload payload) {
        if (name != null) {
            name = payload.name();
        }

        if (color != null) {
            color = payload.color();
        }

        if (quantity != null) {
            quantity = payload.quantity();
        }

        if (price != null) {
            price = payload.price();
        }
    }

}
