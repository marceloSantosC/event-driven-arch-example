package com.example.entity;

import com.example.dto.event.OrderReceived;
import com.example.enumeration.CustomerDocumentType;
import com.example.enumeration.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@Table(name = "`ORDER`")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OrderEntity {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToMany(mappedBy = "order", cascade = {CascadeType.REMOVE, CascadeType.PERSIST})
    private List<OrderProductEntity> products;

    private BigDecimal total;

    private OrderStatus status;

    private String customerDocument;

    private CustomerDocumentType customerDocumentType;

    private LocalDateTime creationDate;

    public static OrderEntity valueOf(OrderReceived newOrder) {
        return OrderEntity.builder()
                .status(OrderStatus.CREATED)
                .customerDocument(newOrder.customerDocument())
                .customerDocumentType(newOrder.documentType())
                .creationDate(LocalDateTime.now())
                .build();
    }
}
