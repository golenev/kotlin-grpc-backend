package com.example.analyticsservice.repository

import com.example.analyticsservice.model.SellerAggregateEntity
import org.springframework.data.jpa.repository.JpaRepository

interface SellerAggregateRepository : JpaRepository<SellerAggregateEntity, String>
