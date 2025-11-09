package com.shop.spring.data.intershop.repository;

import com.shop.spring.data.intershop.model.Item;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ItemRepository extends ReactiveCrudRepository<Item, Long> {
    @Query("SELECT id, title, description, image, price, count FROM items WHERE LOWER(title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(description) LIKE LOWER(CONCAT('%', :search, '%'))")
    Flux<Item> findByTitleOrDescriptionContaining(String search);

    @Query("SELECT id, title, description, image, price, count FROM items")
    Flux<Item> findAllItems();
}
