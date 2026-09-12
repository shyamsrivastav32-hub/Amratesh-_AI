package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testBasicArithmetic() {
    assertEquals("5", CalculatorEngine.evaluate("2 + 3").getOrNull())
    assertEquals("10", CalculatorEngine.evaluate("15 − 5").getOrNull())
    assertEquals("42", CalculatorEngine.evaluate("6 × 7").getOrNull())
    assertEquals("4", CalculatorEngine.evaluate("20 ÷ 5").getOrNull())
  }

  @Test
  fun testOperatorPrecedence() {
    // 2 + 3 * 4 should be 14, not 20
    assertEquals("14", CalculatorEngine.evaluate("2 + 3 × 4").getOrNull())
    // (2 + 3) * 4 should be 20
    assertEquals("20", CalculatorEngine.evaluate("(2 + 3) × 4").getOrNull())
  }

  @Test
  fun testDecimalsAndPrecision() {
    assertEquals("0.3", CalculatorEngine.evaluate("0.1 + 0.2").getOrNull())
    assertEquals("1,250.75", CalculatorEngine.evaluate("1000 + 250.75").getOrNull())
  }

  @Test
  fun testPowerAndSqrt() {
    assertEquals("8", CalculatorEngine.evaluate("2 ^ 3").getOrNull())
    assertEquals("4", CalculatorEngine.evaluate("√16").getOrNull())
    assertEquals("5", CalculatorEngine.evaluate("√(9 + 16)").getOrNull())
  }

  @Test
  fun testDivisionByZero() {
    val result = CalculatorEngine.evaluate("10 ÷ 0")
    assertTrue(result.isFailure)
    assertEquals("Cannot divide by zero", result.exceptionOrNull()?.message)
  }

  @Test
  fun testPreviewCalculation() {
    val preview = CalculatorEngine.evaluatePreview("25 × 4")
    assertNotNull(preview)
    assertEquals("100", preview)
  }
}

