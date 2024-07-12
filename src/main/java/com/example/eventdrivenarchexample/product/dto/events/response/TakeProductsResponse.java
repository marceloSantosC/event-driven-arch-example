package com.example.eventdrivenarchexample.product.dto.events.response;

import com.example.eventdrivenarchexample.product.dto.events.request.TakeProductsRequest;
import com.example.eventdrivenarchexample.product.entity.ProductEntity;
import com.example.eventdrivenarchexample.product.enumeration.ProductEventResult;
import com.example.eventdrivenarchexample.product.enumeration.ProductEventType;
import com.example.eventdrivenarchexample.product.enumeration.TakeProductStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record TakeProductsResponse(
        ProductEventResult result,
        ProductEventType type,
        List<Product> products
) {
    @Builder
    public record Product(
            Long id,
            BigDecimal value,
            String name,
            String color,
            TakeProductStatus status
    ) {

        public static Product valueOf(ProductEntity product, TakeProductStatus status) {
            return TakeProductsResponse.Product.builder()
                    .color(product.getColor())
                    .id(product.getId())
                    .value(product.getPrice())
                    .name(product.getName())
                    .status(status)
                    .build();
        }

        public static Product valueOf(TakeProductsRequest.Product product) {
            return TakeProductsResponse.Product.builder()
                    .id(product.id())
                    .status(TakeProductStatus.NOT_FOUND)
                    .build();
        }
    }
}
