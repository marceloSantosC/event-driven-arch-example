package com.example.eventdrivenarchexample.product.dto.output;

import java.util.List;

public record TakenProductsEventResult(List<TakenProductOutput> products) {
}
