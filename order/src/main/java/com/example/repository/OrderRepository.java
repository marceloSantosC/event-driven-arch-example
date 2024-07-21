package com.example.repository;


import com.example.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @Query("FROM OrderEntity o LEFT JOIN FETCH o.products WHERE o.id = :id")
    Optional<OrderEntity> findByIdFetchProducts(Long id);

}
