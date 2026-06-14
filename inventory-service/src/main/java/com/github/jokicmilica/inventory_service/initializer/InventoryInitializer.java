package com.github.jokicmilica.inventory_service.initializer;

import com.github.jokicmilica.inventory_service.model.InventoryItem;
import com.github.jokicmilica.inventory_service.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryInitializer implements ApplicationRunner {

    private final InventoryRepository inventoryRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (inventoryRepository.count() == 0) {
            inventoryRepository.saveAll(List.of(
                    new InventoryItem("item-1", 100),
                    new InventoryItem("item-2", 50),
                    new InventoryItem("item-3", 0)
            ));
            log.info("Inventory initialized in MongoDB");
        }
    }
}
