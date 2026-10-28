package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun getUserByIdFlow(userId: String): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET passwordResetOtp = :otp, resetOtpExpiresAt = :expiresAt WHERE email = :email")
    suspend fun setPasswordResetOtp(email: String, otp: String, expiresAt: Long)

    @Query("UPDATE users SET passwordHash = :newHash, salt = :newSalt, passwordResetOtp = NULL, resetOtpExpiresAt = NULL WHERE email = :email")
    suspend fun updatePassword(email: String, newHash: String, newSalt: String)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}
