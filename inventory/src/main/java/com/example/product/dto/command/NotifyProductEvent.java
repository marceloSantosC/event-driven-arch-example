package com.example.product.dto.command;

import lombok.Builder;


@Builder
public record NotifyProductEvent(UserNotification body,
                                 String traceId) {
}
