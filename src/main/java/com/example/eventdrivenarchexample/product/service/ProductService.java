package com.example.eventdrivenarchexample.product.service;

import com.example.eventdrivenarchexample.product.dto.command.CreateProduct;
import com.example.eventdrivenarchexample.product.dto.command.ShipProducts;
import com.example.eventdrivenarchexample.product.dto.command.UpdateProduct;
import com.example.eventdrivenarchexample.product.dto.event.CreatedProduct;
import com.example.eventdrivenarchexample.product.dto.event.ShippedProduct;
import com.example.eventdrivenarchexample.product.entity.ProductEntity;
import com.example.eventdrivenarchexample.product.enumeration.ShippedProductStatus;
import com.example.eventdrivenarchexample.product.exception.ProductException;
import com.example.eventdrivenarchexample.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public CreatedProduct createProduct(CreateProduct newProduct) {
        log.info("Creating product with name {}...", newProduct.name());

        if (productRepository.existsByName(newProduct.name())) {
            log.error("Couldn't create product with name {}: Product already exists.", newProduct.name());
            String reason = "the product already exists.";
            throw new ProductException(String.format("Product with name %s already exists.", newProduct.name()), false, reason);
        }

        var product = ProductEntity.valueOf(newProduct);

        productRepository.save(product);
        log.info("Product with name {} created with id {}.", product.getName(), product.getId());
        return new CreatedProduct(product.getId());
    }


    public void updateProduct(UpdateProduct updateProductDTO) {

        if (updateProductDTO.quantity() <= 0) {
            log.error("Couldn't update product with id {}. Negative quantity, value: {}.", updateProductDTO.id(), updateProductDTO.quantity());
            String reason = "the product quantity shouldn't be negative";
            throw new ProductException("Failed to update: Product quantity shouldn't be negative.", false, reason);
        }

        var product = productRepository.findById(updateProductDTO.id())
                .orElseThrow(() -> {
                    log.error("Couldn't update product with id {}. Product does not exists.", updateProductDTO.id());
                    return new ProductException("Failed to update: Product does not exists", false, "the product does not exists.");
                });

        product.copyNonNullValuesFrom(updateProductDTO);
        productRepository.save(product);

    }


    public Set<ShippedProduct> shipProducts(ShipProducts productsToShipPayload) {

        var productIds = productsToShipPayload.products().stream().map(ShipProducts.Product::id).toList();
        var products = productRepository.findAllById(productIds);

        Map<ShippedProduct, ProductEntity> productsMap = new HashMap<>();

        boolean hasNotFoundOrOutOfStockProducts = false;
        for (ShipProducts.Product productToShip : productsToShipPayload.products()) {

            Optional<ProductEntity> matchingProduct = products.stream().filter(product -> product.getId().equals(productToShip.id())).findFirst();

            if (matchingProduct.isEmpty() || ! matchingProduct.get().hasQuantityToBeShipped(productToShip.quantity())) {
                hasNotFoundOrOutOfStockProducts = true;
                var shippedProduct = matchingProduct
                        .map(product -> ShippedProduct.valueOf(product, ShippedProductStatus.OUT_OF_STOCK))
                        .orElse(ShippedProduct.valueOf(productToShip));
                shippedProduct.setQuantityShipped(0L);
                productsMap.put(shippedProduct, matchingProduct.orElse(null));
                continue;
            }
            
            var shippedProduct = ShippedProduct.valueOf(matchingProduct.get(), ShippedProductStatus.NOT_SHIPPED);
            productsMap.put(shippedProduct, matchingProduct.get());
            shippedProduct.setQuantityShipped(productToShip.quantity());
        }


        if (hasNotFoundOrOutOfStockProducts) {
            return productsMap.keySet();
        }

        productsMap.forEach(((shippedProduct, product) -> {
            shippedProduct.setStatus(ShippedProductStatus.SHIPPED);
            product.ship(shippedProduct.getQuantityShipped());
        }));

        productRepository.saveAll(products);

        return productsMap.keySet();
    }


}
