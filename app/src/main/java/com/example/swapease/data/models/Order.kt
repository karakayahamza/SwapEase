package com.example.swapease.data.models

class Order {

    private val buyerUid: String? = null
    private val sellerUid: String? = null
    private val productIds: List<String>? = null
    private val totalPrice = 0.0
    private val orderStatus: OrderStatus? = null

    enum class OrderStatus {
        PENDING, COMPLETED, CANCELED
    }
}