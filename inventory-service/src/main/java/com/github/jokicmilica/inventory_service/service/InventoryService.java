package com.github.jokicmilica.inventory_service.service;

import com.github.jokicmilica.inventory_service.constants.InventoryConstants;
import com.github.jokicmilica.inventory_service.model.InventoryItem;
import com.github.jokicmilica.inventory_service.model.OutboxEntry;
import com.github.jokicmilica.inventory_service.model.OutboxStatus;
import com.github.jokicmilica.inventory_service.repository.InventoryRepository;
import com.github.jokicmilica.inventory_service.repository.OutboxRepository;
import com.github.jokicmilica.model.OrderEvent;
import com.github.jokicmilica.model.OrderResult;
import com.github.jokicmilica.model.OrderStatus;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final OutboxRepository outboxRepository;

    @Transactional
    public OrderResult processOrder(OrderEvent event) {
        Optional<OutboxEntry> existing = outboxRepository.findByOrderId(event.orderId());
        if (existing.isPresent()) {
            log.info("Duplicate event received, reusing existing result for orderId: {}", event.orderId());
            return existing.get().getPayload();
        }
        OrderResult orderRes = tryReserve(event);
        OutboxEntry outboxEntry = new OutboxEntry(event.orderId(), orderRes, OutboxStatus.PENDING, LocalDateTime.now());
        outboxRepository.save(outboxEntry);
        return orderRes;

    }

    private OrderResult tryReserve(OrderEvent event) {
        InventoryItem inventoryItem = inventoryRepository.findById(event.itemId()).orElse(null);
        OrderResult computedResult;
        if (inventoryItem==null) {
            log.warn("Item not found: {}", event.itemId());
            computedResult = new OrderResult(event.orderId(), OrderStatus.REJECTED,
                    String.format("Item not found: %s", event.itemId()));
            return computedResult;
        }
        if (inventoryItem.getAvailableQuantity()>=event.quantity()) {
            inventoryItem.setAvailableQuantity(inventoryItem.getAvailableQuantity()-event.quantity());
            inventoryRepository.save(inventoryItem);
            log.info("Order processed, orderId: {}, itemId: {}, remaining stock: {}",
                        event.orderId(), event.itemId(), inventoryItem.getAvailableQuantity());
            computedResult = new OrderResult(event.orderId(), OrderStatus.PROCESSED,
                        String.format("Order with id %s successfully processed, stock reserved", event.orderId()));
            return computedResult;
        }
        int availableStock = inventoryItem.getAvailableQuantity();
        log.warn("Insufficient stock, orderId: {}, itemId: {}, requested: {}, available: {}",
                        event.orderId(), event.itemId(), event.quantity(), availableStock);
        computedResult = new OrderResult(event.orderId(), OrderStatus.REJECTED,
                        String.format("Insufficient stock for itemId: %s, requested: %d, available: %d",
                                event.itemId(), event.quantity(), availableStock));
        return computedResult;
    }


    public List<InventoryItem> getInventory() {
        return inventoryRepository.findAll();
    }
}