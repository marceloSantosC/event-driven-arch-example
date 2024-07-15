package com.example.eventdrivenarchexample.product.dto.command;

import lombok.Builder;


@Builder
public record NotifyProduct(NotifyProductNotification body,
                            String traceId) {
}
