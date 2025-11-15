package com.csis231.api.service;

import com.csis231.api.dto.OrderDto;
import com.csis231.api.dto.OrderItemDto;
import com.csis231.api.entity.*;
import com.csis231.api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final TableRepository tableRepository;
    
    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        return convertToDto(order);
    }
    
    public OrderDto createOrder(OrderDto orderDto) {
        // Validate entities exist
        Customer customer = customerRepository.findById(orderDto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + orderDto.getCustomerId()));
        String name = customer.getName();

        Table table = tableRepository.findById(orderDto.getTableId())
                .orElseThrow(() -> new RuntimeException("Table not found with id: " + orderDto.getTableId()));
        
        // Check if table is available
        if (table.getStatus() != Table.TableStatus.AVAILABLE) {
            throw new RuntimeException("Table is not available");
        }
        
        // Create order
        Order order = Order.builder()
                .customer(customer)
                .name(name)
                .table(table)
                .datetime(LocalDateTime.now())
                .status(Order.OrderStatus.OPEN)
                .build();
        
        Order savedOrder = orderRepository.save(order);
        
        // Update table status
        table.setStatus(Table.TableStatus.OCCUPIED);
        tableRepository.save(table);
        
        return convertToDto(savedOrder);
    }
    
    public OrderDto updateOrder(Long id, OrderDto orderDto) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        // Only allow updates to open orders
        if (order.getStatus() != Order.OrderStatus.OPEN) {
            throw new RuntimeException("Cannot update closed or paid orders");
        }
        
        // Update order details if needed
        if (orderDto.getStatus() != null) {
            order.setStatus(orderDto.getStatus());
        }
        
        Order savedOrder = orderRepository.save(order);
        return convertToDto(savedOrder);
    }
    
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        // Only allow deletion of open orders
        if (order.getStatus() != Order.OrderStatus.OPEN) {
            throw new RuntimeException("Cannot delete closed or paid orders");
        }
        
        // Free up the table
        Table table = order.getTable();
        table.setStatus(Table.TableStatus.AVAILABLE);
        tableRepository.save(table);
        
        // Delete order items first
        orderItemRepository.deleteByOrder(order);
        
        // Delete the order
        orderRepository.delete(order);
    }
    
    public OrderDto closeOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        if (order.getStatus() != Order.OrderStatus.OPEN) {
            throw new RuntimeException("Order is not open");
        }
        
        // Check if order has items
        List<OrderItem> items = orderItemRepository.findByOrder(order);
        if (items.isEmpty()) {
            throw new RuntimeException("Cannot close order without items");
        }
        
        // Update order status
        order.setStatus(Order.OrderStatus.CLOSED);
        Order savedOrder = orderRepository.save(order);
        
        // Free up the table
        Table table = order.getTable();
        table.setStatus(Table.TableStatus.AVAILABLE);
        tableRepository.save(table);
        
        return convertToDto(savedOrder);
    }
    
    public List<OrderDto> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    private OrderDto convertToDto(Order order) {
        List<OrderItemDto> orderItems = orderItemRepository.findByOrder(order).stream()
                .map(this::convertItemToDto)
                .collect(Collectors.toList());
        
        return OrderDto.builder()
                .orderId(order.getOrderId())
                .customerId(order.getCustomer().getCustomerId())
                .name(order.getName())
                .tableId(order.getTable().getTableId())
                .datetime(order.getDatetime())
                .status(order.getStatus())
                .orderItems(orderItems)
                .total(order.getTotal())
                .build();
    }
    
    private OrderItemDto convertItemToDto(OrderItem item) {
        return OrderItemDto.builder()
                .id(item.getId())
                .orderId(item.getOrder().getOrderId())
                .productId(item.getProduct().getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .build();
    }
}
