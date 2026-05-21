package com.github.jokicmilica.inventory_service.service;

import com.github.jokicmilica.inventory_service.constants.InventoryConstants;
import com.github.jokicmilica.inventory_service.producer.OrderResultProducer;
import com.github.jokicmilica.model.OrderEvent;
import com.github.jokicmilica.model.OrderStatus;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryService {

    private final OrderResultProducer orderResultProducer;
    private final Map<String, Integer> inventory = new ConcurrentHashMap<>();
    private final Map<String, OrderEvent> processedOrders = new ConcurrentHashMap<>();

    @PostConstruct
    public void initializeInventory() {
        inventory.put("item-1", InventoryConstants.DEFAULT_ITEM_1_STOCK);
        inventory.put("item-2", InventoryConstants.DEFAULT_ITEM_2_STOCK);
        inventory.put("item-3", InventoryConstants.DEFAULT_ITEM_3_STOCK);
        log.info("Inventory initialized: {}", inventory);
    }

    public void processOrder(OrderEvent event) {
        if (processedOrders.containsKey(event.orderId())) {
            OrderEvent previous = processedOrders.get(event.orderId());
            if (!previous.equals(event)) {
                log.warn("Order ID reused with different parameters, orderId: {}", event.orderId());
                try {
                    orderResultProducer.send(event.orderId(), OrderStatus.REJECTED,
                            "Order ID already used with different parameters");
                } catch (Exception ignored) {}
                return;
            }
            log.warn("Duplicate order detected, skipping orderId: {}", event.orderId());
            return;
        }

        if (!inventory.containsKey(event.itemId())) {
            log.warn("Item not found: {}", event.itemId());
            try {
                orderResultProducer.send(event.orderId(), OrderStatus.REJECTED,
                        String.format("Item not found: %s", event.itemId()));
            } catch (Exception ignored) {}
            return;
        }

        boolean reserved = tryReserve(event.itemId(), event.quantity());

        if (reserved) {
            processedOrders.put(event.orderId(), event);
            log.info("Order processed, orderId: {}, itemId: {}, remaining stock: {}",
                    event.orderId(), event.itemId(), inventory.get(event.itemId()));
            try {
                orderResultProducer.send(event.orderId(), OrderStatus.PROCESSED,
                        String.format("Order with id %s successfully processed, stock reserved", event.orderId()));
            } catch (Exception ignored) {}
        } else {
            log.warn("Insufficient stock, orderId: {}, itemId: {}, requested: {}, available: {}",
                    event.orderId(), event.itemId(), event.quantity(), inventory.get(event.itemId()));
            try {
                orderResultProducer.send(event.orderId(), OrderStatus.REJECTED,
                        String.format("Insufficient stock for itemId: %s, requested: %d, available: %d",
                                event.itemId(), event.quantity(), inventory.get(event.itemId())));
            } catch (Exception ignored) {}
        }
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