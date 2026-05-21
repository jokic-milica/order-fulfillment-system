package com.github.jokicmilica.order_service;

import com.github.jokicmilica.model.OrderStatus;
import com.github.jokicmilica.order_service.service.OrderStatusService;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusServiceTest {

    private final OrderStatusService orderStatusService = new OrderStatusService();

    @Test
    void shouldHandleConcurrentOrdersWithSameOrderId() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger duplicateCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    boolean reserved = orderStatusService.reserveIfAbsent("race-001", OrderStatus.PENDING);
                    if (reserved) {
                        successCount.incrementAndGet();
                    } else {
                        duplicateCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(duplicateCount.get()).isEqualTo(9);
    }
}