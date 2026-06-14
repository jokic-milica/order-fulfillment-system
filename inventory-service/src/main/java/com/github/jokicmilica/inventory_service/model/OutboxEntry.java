package com.github.jokicmilica.inventory_service.model;

import com.github.jokicmilica.model.OrderResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "outbox")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEntry {

    @Id
    private String id;
    private String orderId;
    private OrderResult payload;
    private OutboxStatus status;  // PENDING, PUBLISHED
    private LocalDateTime createdAt;

    public OutboxEntry(String orderId, OrderResult payload, OutboxStatus status, LocalDateTime createdAt) {
        this.orderId = orderId;
        this.payload = payload;
        this.status = status;
        this.createdAt = createdAt;
    }
}
