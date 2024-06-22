package com.example.eventdrivenarchexample.product.repository;

import com.example.eventdrivenarchexample.product.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    boolean existsByName(String name);

}
