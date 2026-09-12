package com.example

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.Stack

data class CalculationHistoryItem(
  val id: Long = System.currentTimeMillis(),
  val expression: String,
  val result: String,
  val timestamp: Long = System.currentTimeMillis(),
)

object CalculatorEngine {

  private val mathContext = MathContext(16, RoundingMode.HALF_UP)

  fun evaluate(expression: String): Result<String> {
    if (expression.isBlank()) return Result.success("0")

    return try {
      val sanitized = sanitize(expression)
      if (sanitized.isEmpty()) return Result.success("0")

      val tokens = tokenize(sanitized)
      if (tokens.isEmpty()) return Result.success("0")

      val rpn = infixToRPN(tokens)
      val value = evaluateRPN(rpn)
      Result.success(formatNumber(value))
    } catch (e: ArithmeticException) {
      Result.failure(e)
    } catch (e: Exception) {
      Result.failure(IllegalArgumentException("Invalid expression"))
    }
  }

  fun evaluatePreview(expression: String): String? {
    if (expression.isBlank()) return null
    // If the expression is just a single number, no need for preview
    val sanitized = sanitize(expression)
    if (!sanitized.any { it in "+-*/%^√" }) return null

    // Trim trailing operators for preview
    var previewExpr = sanitized
    while (previewExpr.isNotEmpty() && (previewExpr.last() in "+-*/^(" || previewExpr.last() == '√')) {
      previewExpr = previewExpr.dropLast(1)
    }
    if (previewExpr.isBlank()) return null

    // Auto-balance open parentheses for preview
    val openParens = previewExpr.count { it == '(' }
    val closeParens = previewExpr.count { it == ')' }
    if (openParens > closeParens) {
      previewExpr += ")".repeat(openParens - closeParens)
    }

    return try {
      val tokens = tokenize(previewExpr)
      if (tokens.isEmpty()) return null
      val rpn = infixToRPN(tokens)
      val value = evaluateRPN(rpn)
      formatNumber(value)
    } catch (_: Exception) {
      null
    }
  }

  private fun sanitize(expr: String): String {
    return expr
      .replace("×", "*")
      .replace("÷", "/")
      .replace("−", "-")
      .replace(" ", "")
      .replace(",", "")
  }

  private fun tokenize(expr: String): List<String> {
    val tokens = mutableListOf<String>()
    var i = 0
    val n = expr.length

    while (i < n) {
      val c = expr[i]

      when {
        c.isDigit() || c == '.' -> {
          val sb = StringBuilder()
          while (i < n && (expr[i].isDigit() || expr[i] == '.')) {
            sb.append(expr[i])
            i++
          }
          tokens.add(sb.toString())
          continue
        }
        c == 'π' -> {
          tokens.add(Math.PI.toString())
          i++
        }
        c == 'e' && (i == n - 1 || !expr[i + 1].isLetter()) -> {
          tokens.add(Math.E.toString())
          i++
        }
        c == '-' -> {
          // Check if unary minus: if at start or after another operator or '('
          val isUnary = tokens.isEmpty() || tokens.last() in listOf("+", "-", "*", "/", "^", "(", "√")
          if (isUnary) {
            // Next could be a number or '('
            if (i + 1 < n && (expr[i + 1].isDigit() || expr[i + 1] == '.')) {
              val sb = StringBuilder("-")
              i++
              while (i < n && (expr[i + 1 - 1].isDigit() || expr[i] == '.')) {
                sb.append(expr[i])
                i++
              }
              tokens.add(sb.toString())
              continue
            } else {
              tokens.add("u-")
              i++
              continue
            }
          } else {
            tokens.add("-")
            i++
          }
        }
        c in "+*/^%()√" -> {
          tokens.add(c.toString())
          i++
        }
        else -> {
          i++ // skip unknown
        }
      }
    }
    return tokens
  }

  private fun precedence(op: String): Int {
    return when (op) {
      "+", "-" -> 1
      "*", "/", "%" -> 2
      "u-" -> 3
      "^", "√" -> 4
      else -> 0
    }
  }

  private fun isRightAssociative(op: String): Boolean {
    return op == "^" || op == "u-" || op == "√"
  }

  private fun infixToRPN(tokens: List<String>): List<String> {
    val output = mutableListOf<String>()
    val stack = Stack<String>()

    for (token in tokens) {
      if (token.toDoubleOrNull() != null) {
        output.add(token)
      } else if (token == "(") {
        stack.push(token)
      } else if (token == ")") {
        while (stack.isNotEmpty() && stack.peek() != "(") {
          output.add(stack.pop())
        }
        if (stack.isNotEmpty() && stack.peek() == "(") {
          stack.pop()
        }
      } else {
        // Operator
        while (stack.isNotEmpty() && stack.peek() != "(") {
          val top = stack.peek()
          val topPrec = precedence(top)
          val tokenPrec = precedence(token)
          if ((!isRightAssociative(token) && tokenPrec <= topPrec) ||
              (isRightAssociative(token) && tokenPrec < topPrec)
          ) {
            output.add(stack.pop())
          } else {
            break
          }
        }
        stack.push(token)
      }
    }

    while (stack.isNotEmpty()) {
      output.add(stack.pop())
    }

    return output
  }

  private fun evaluateRPN(rpn: List<String>): BigDecimal {
    val stack = Stack<BigDecimal>()

    for (token in rpn) {
      val num = token.toBigDecimalOrNull()
      if (num != null) {
        stack.push(num)
      } else if (token == "u-") {
        if (stack.isEmpty()) throw IllegalArgumentException("Malformed expression")
        stack.push(stack.pop().negate())
      } else if (token == "√") {
        if (stack.isEmpty()) throw IllegalArgumentException("Malformed expression")
        val a = stack.pop()
        if (a < BigDecimal.ZERO) throw ArithmeticException("Negative square root")
        val sqrtVal = BigDecimal(Math.sqrt(a.toDouble()), mathContext)
        stack.push(sqrtVal)
      } else {
        if (stack.size < 2) throw IllegalArgumentException("Malformed expression")
        val b = stack.pop()
        val a = stack.pop()

        val res = when (token) {
          "+" -> a.add(b, mathContext)
          "-" -> a.subtract(b, mathContext)
          "*" -> a.multiply(b, mathContext)
          "/" -> {
            if (b.compareTo(BigDecimal.ZERO) == 0) {
              throw ArithmeticException("Cannot divide by zero")
            }
            a.divide(b, mathContext)
          }
          "%" -> {
            // Percent operator: a * (b / 100) or b / 100 depending on precedence
            a.multiply(b.divide(BigDecimal(100), mathContext), mathContext)
          }
          "^" -> {
            val powVal = BigDecimal(Math.pow(a.toDouble(), b.toDouble()), mathContext)
            powVal
          }
          else -> throw IllegalArgumentException("Unknown operator: $token")
        }
        stack.push(res)
      }
    }

    if (stack.size != 1) throw IllegalArgumentException("Malformed expression")
    return stack.pop()
  }

  fun formatNumber(number: BigDecimal): String {
    // Strip trailing zeroes
    val stripped = number.stripTrailingZeros()
    val doubleVal = stripped.toDouble()

    // Scientific notation for very large or tiny numbers
    if (doubleVal != 0.0 && (Math.abs(doubleVal) >= 1e12 || Math.abs(doubleVal) < 1e-6)) {
      val format = DecimalFormat("0.######E0", DecimalFormatSymbols(Locale.US))
      return format.format(doubleVal)
    }

    val plainStr = stripped.toPlainString()
    val parts = plainStr.split(".")
    val intPart = parts[0]
    val decPart = if (parts.size > 1) parts[1] else null

    // Format integer part with commas
    val isNegative = intPart.startsWith("-")
    val absInt = if (isNegative) intPart.substring(1) else intPart
    val formattedInt = absInt.reversed().chunked(3).joinToString(",").reversed()
    val signedInt = if (isNegative) "-$formattedInt" else formattedInt

    return if (decPart != null && decPart.isNotEmpty()) {
      "$signedInt.$decPart"
    } else {
      signedInt
    }
  }

  fun formatExpressionForDisplay(expr: String): String {
    // Keeps expressions looking neat and user-friendly
    return expr
  }
}
