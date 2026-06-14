package com.github.jokicmilica.inventory_service.repository;

import com.github.jokicmilica.inventory_service.model.OutboxEntry;
import com.github.jokicmilica.inventory_service.model.OutboxStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface OutboxRepository extends MongoRepository<OutboxEntry, String> {
    List<OutboxEntry> findByStatus(OutboxStatus status);
    Optional<OutboxEntry> findByOrderId(String orderId);
}
