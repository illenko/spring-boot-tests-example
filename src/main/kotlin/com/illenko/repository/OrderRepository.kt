package com.illenko.repository

import com.illenko.model.Order
import org.springframework.data.r2dbc.repository.R2dbcRepository
import java.util.UUID

interface OrderRepository : R2dbcRepository<Order, UUID>
