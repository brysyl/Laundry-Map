package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Laundry Map", appName)
  }

  @Test
  fun `verify password hashing and verification with PBKDF2`() {
    val salt = com.example.data.security.SecurityUtils.generateSalt()
    val hash = com.example.data.security.SecurityUtils.hashPassword("SuperSecret@123", salt)
    val isValid = com.example.data.security.SecurityUtils.verifyPassword("SuperSecret@123", salt, hash)
    val isInvalid = com.example.data.security.SecurityUtils.verifyPassword("WrongPassword", salt, hash)

    assertEquals(true, isValid)
    assertEquals(false, isInvalid)
  }

  @Test
  fun `order state machine keys resolve correctly`() {
    val created = com.example.domain.model.OrderState.fromKey("CREATED")
    val processing = com.example.domain.model.OrderState.fromKey("PROCESSING", "DRYING")
    val delivered = com.example.domain.model.OrderState.fromKey("DELIVERED")

    assertEquals("CREATED", created.key)
    assertEquals(true, processing is com.example.domain.model.OrderState.Processing)
    assertEquals(com.example.domain.model.ProcessingStage.DRYING, (processing as com.example.domain.model.OrderState.Processing).stage)
    assertEquals(true, delivered.isTerminal)
  }
}
