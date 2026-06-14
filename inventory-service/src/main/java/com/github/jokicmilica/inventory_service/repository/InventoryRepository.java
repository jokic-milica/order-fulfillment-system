package com.github.jokicmilica.inventory_service.repository;

import com.github.jokicmilica.inventory_service.model.InventoryItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends MongoRepository<InventoryItem, String> {
}
