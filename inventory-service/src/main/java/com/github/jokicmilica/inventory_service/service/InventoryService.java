package com.github.jokicmilica.inventory_service.service;

import com.github.jokicmilica.inventory_service.constants.InventoryConstants;
import com.github.jokicmilica.model.OrderEvent;
import com.github.jokicmilica.model.OrderResult;
import com.github.jokicmilica.model.OrderStatus;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class InventoryService {

    private final Map<String, Integer> inventory = new ConcurrentHashMap<>();
    private final Map<String, OrderEvent> processedOrders = new ConcurrentHashMap<>();
    private final Map<String, OrderResult> orderResults = new ConcurrentHashMap<>();

    @PostConstruct
    public void initializeInventory() {
        inventory.put("item-1", InventoryConstants.DEFAULT_ITEM_1_STOCK);
        inventory.put("item-2", InventoryConstants.DEFAULT_ITEM_2_STOCK);
        inventory.put("item-3", InventoryConstants.DEFAULT_ITEM_3_STOCK);
        log.info("Inventory initialized: {}", inventory);
    }

    public synchronized OrderResult processOrder(OrderEvent event) {
        String orderId = event.orderId();
        OrderEvent previousEvent = processedOrders.get(orderId);

        if (previousEvent != null) {
            if (!previousEvent.equals(event)) {
                log.warn("Order ID reused with different parameters, orderId: {}", orderId);
                return new OrderResult(orderId, OrderStatus.REJECTED,
                        "Order ID already used with different parameters");
            }

            OrderResult existingResult = orderResults.get(orderId);
            if (existingResult != null) {
                log.info("Duplicate event received, reusing existing result for orderId: {}", orderId);
                return existingResult;
            }

            throw new IllegalStateException(String.format("Missing cached result for already processed orderId: %s", orderId));
        }

        OrderResult computedResult;

        if (!inventory.containsKey(event.itemId())) {
            log.warn("Item not found: {}", event.itemId());
            computedResult = new OrderResult(orderId, OrderStatus.REJECTED,
                    String.format("Item not found: %s", event.itemId()));
        } else {
            boolean reserved = tryReserve(event.itemId(), event.quantity());
            if (reserved) {
                log.info("Order processed, orderId: {}, itemId: {}, remaining stock: {}",
                        orderId, event.itemId(), inventory.get(event.itemId()));
                computedResult = new OrderResult(orderId, OrderStatus.PROCESSED,
                        String.format("Order with id %s successfully processed, stock reserved", orderId));
            } else {
                Integer available = inventory.get(event.itemId());
                int availableStock = available == null ? 0 : available;
                log.warn("Insufficient stock, orderId: {}, itemId: {}, requested: {}, available: {}",
                        orderId, event.itemId(), event.quantity(), availableStock);
                computedResult = new OrderResult(orderId, OrderStatus.REJECTED,
                        String.format("Insufficient stock for itemId: %s, requested: %d, available: %d",
                                event.itemId(), event.quantity(), availableStock));
            }
        }

        processedOrders.put(orderId, event);
        orderResults.put(orderId, computedResult);
        return computedResult;
    }

    private boolean tryReserve(String itemId, int quantity) {
        var result = new boolean[]{false};
        inventory.compute(itemId, (key, current) -> {
            if (current != null && current >= quantity) {
                result[0] = true;
                return current - quantity;
            }
            return current;
        });
        return result[0];
    }

    public Map<String, Integer> getInventory() {
        return Collections.unmodifiableMap(inventory);
    }
}