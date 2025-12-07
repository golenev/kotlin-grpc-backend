package com.example.orderservice.repository

import com.example.orderservice.model.OrderGeoEntity
import org.springframework.data.jpa.repository.JpaRepository

interface OrderGeoRepository : JpaRepository<OrderGeoEntity, String>
