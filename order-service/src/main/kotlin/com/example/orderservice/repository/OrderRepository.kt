package com.example.orderservice.repository

import com.example.orderservice.model.OrderEntity
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<OrderEntity, String>
