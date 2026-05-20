package com.github.jokicmilica.order_service.controller;

import com.github.jokicmilica.order_service.model.OrderRequest;
import com.github.jokicmilica.order_service.model.OrderResponse;
import com.github.jokicmilica.order_service.producer.OrderProducer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderProducer orderProducer;

    @Operation(summary = "Create a new order", description = "Publishes an order event to Kafka for inventory processing")
    @ApiResponse(responseCode = "202", description = "Order accepted and queued")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "503", description = "Kafka unavailable")
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        orderProducer.send(request);
        return ResponseEntity
                .accepted()
                .body(new OrderResponse(request.orderId(), "ACCEPTED", "Order received and queued for processing"));
    }
}
