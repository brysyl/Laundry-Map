package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.OrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAtTimestamp DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    fun getOrderById(orderId: String): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderByIdOnce(orderId: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE stateKey NOT IN ('DELIVERED', 'CANCELLED') ORDER BY createdAtTimestamp DESC")
    fun getActiveOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE laundromatId = :laundromatId ORDER BY createdAtTimestamp DESC")
    fun getOrdersForLaundromat(laundromatId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE courierId = :courierId AND stateKey NOT IN ('DELIVERED', 'CANCELLED')")
    fun getActiveOrdersForCourier(courierId: String): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderEntity>)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE orders SET stateKey = :stateKey, processingStage = :stage WHERE id = :orderId")
    suspend fun updateOrderState(orderId: String, stateKey: String, stage: String?)

    @Query("UPDATE orders SET courierId = :courierId WHERE id = :orderId")
    suspend fun assignCourier(orderId: String, courierId: String)

    @Query("UPDATE orders SET clientSignaturePath = :signaturePath, stateKey = 'DELIVERED' WHERE id = :orderId")
    suspend fun completeDelivery(orderId: String, signaturePath: String)

    @Query("DELETE FROM orders WHERE id = :orderId")
    suspend fun deleteOrder(orderId: String)

    @Query("DELETE FROM orders")
    suspend fun clearAll()
}
