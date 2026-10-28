package com.example.data.remote

import com.example.data.remote.dto.OrderCreateRequest
import com.example.data.remote.dto.OrderResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface LaundryApiService {
    @POST("api/v1/orders")
    suspend fun createOrder(@Body request: OrderCreateRequest): Response<OrderResponseDto>

    @GET("api/v1/orders/{id}")
    suspend fun getOrder(@Path("id") orderId: String): Response<OrderResponseDto>

    @POST("api/v1/orders/{id}/state")
    suspend fun updateOrderState(
        @Path("id") orderId: String,
        @Query("state") stateKey: String,
        @Query("stage") stage: String?
    ): Response<OrderResponseDto>

    @POST("api/v1/orders/{id}/verify-delivery")
    suspend fun verifyDelivery(
        @Path("id") orderId: String,
        @Query("otp") otp: String,
        @Query("signature") signaturePath: String
    ): Response<OrderResponseDto>
}
