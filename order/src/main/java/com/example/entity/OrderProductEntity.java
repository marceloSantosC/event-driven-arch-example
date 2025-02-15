package com.example.entity;

import com.example.enumeration.OrderProductStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Builder
@Table(name = "ORDER_PRODUCT")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OrderProductEntity {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private OrderProductEntityID id;

    @Column(name = "`VALUE`")
    private BigDecimal value;

    private Long quantity;

    private OrderProductStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId(value = "orderId")
    private OrderEntity order;

}
