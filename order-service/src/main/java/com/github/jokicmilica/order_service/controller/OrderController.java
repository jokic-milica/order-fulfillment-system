package com.github.jokicmilica.order_service.controller;

import com.github.jokicmilica.order_service.model.OrderRequest;
import com.github.jokicmilica.order_service.model.OrderResponse;
import com.github.jokicmilica.order_service.producer.OrderProducer;
import com.github.jokicmilica.order_service.service.OrderStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Orders", description = "Order management endpoints")
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderProducer orderProducer;
    private final OrderStatusService orderStatusService;

    @Operation(summary = "Create a new order", description = "Publishes an order event to Kafka for inventory processing")
    @ApiResponse(responseCode = "202", description = "Order accepted and queued")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "409", description = "Duplicate order - orderId already exists")
    @ApiResponse(responseCode = "503", description = "Kafka unavailable")
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        orderProducer.send(request);
        return ResponseEntity
                .accepted()
                .body(new OrderResponse(request.orderId(), "ACCEPTED", "Order received and queued for processing"));
    }

    @Operation(summary = "Get order status", description = "Returns current processing status of an order")
    @ApiResponse(responseCode = "200", description = "Status retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @GetMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> getOrderStatus(@PathVariable String orderId) {
        return orderStatusService.getStatus(orderId)
                .map(status -> ResponseEntity.ok()
                        .body(new OrderResponse(orderId, status.name(), "Order status retrieved successfully")))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new OrderResponse(orderId, "NOT_FOUND", "Order not found")));
    }
}
