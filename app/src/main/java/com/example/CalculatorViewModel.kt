package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CalculatorUiState(
  val expression: String = "",
  val previewResult: String? = null,
  val isCalculated: Boolean = false,
  val errorMessage: String? = null,
  val isScientificOpen: Boolean = false,
  val history: List<CalculationHistoryItem> = emptyList(),
  val showHistorySheet: Boolean = false,
)

class CalculatorViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(CalculatorUiState())
  val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

  fun onDigit(digit: String) {
    _uiState.update { state ->
      val currentExpr = if (state.isCalculated || state.errorMessage != null) "" else state.expression
      val newExpr = if (currentExpr == "0") digit else currentExpr + digit
      val preview = CalculatorEngine.evaluatePreview(newExpr)
      state.copy(
        expression = newExpr,
        previewResult = preview,
        isCalculated = false,
        errorMessage = null,
      )
    }
  }

  fun onOperator(op: String) {
    _uiState.update { state ->
      val baseExpr = when {
        state.errorMessage != null -> ""
        state.expression.isEmpty() && op == "−" -> "−"
        state.expression.isEmpty() -> "0 $op "
        else -> state.expression
      }

      // Check if ends with operator and replace
      val trimmed = baseExpr.trimEnd()
      val lastOpMatch = Regex("""[+−×÷^]$""")
      val newExpr = if (lastOpMatch.containsMatchIn(trimmed)) {
        trimmed.replace(lastOpMatch, op) + " "
      } else {
        "$trimmed $op "
      }

      val preview = CalculatorEngine.evaluatePreview(newExpr)
      state.copy(
        expression = newExpr,
        previewResult = preview,
        isCalculated = false,
        errorMessage = null,
      )
    }
  }

  fun onDecimal() {
    _uiState.update { state ->
      val currentExpr = if (state.isCalculated || state.errorMessage != null) "" else state.expression
      
      // Find the last number token
      val lastNumber = currentExpr.split(Regex("""[\s+−×÷^()√]""")).lastOrNull() ?: ""
      val newExpr = when {
        lastNumber.contains(".") -> currentExpr // Already has decimal point
        lastNumber.isEmpty() -> "${currentExpr}0."
        else -> "$currentExpr."
      }

      val preview = CalculatorEngine.evaluatePreview(newExpr)
      state.copy(
        expression = newExpr,
        previewResult = preview,
        isCalculated = false,
        errorMessage = null,
      )
    }
  }

  fun onBackspace() {
    _uiState.update { state ->
      if (state.errorMessage != null) {
        return@update state.copy(errorMessage = null, expression = "")
      }
      if (state.isCalculated) {
        return@update state.copy(expression = "", previewResult = null, isCalculated = false)
      }
      if (state.expression.isEmpty()) return@update state

      var newExpr = state.expression
      if (newExpr.endsWith(" ")) {
        // e.g., " + " -> remove operator and spacing
        newExpr = newExpr.dropLast(1)
        if (newExpr.isNotEmpty() && newExpr.last() in "+−×÷^") {
          newExpr = newExpr.dropLast(1)
        }
        if (newExpr.endsWith(" ")) {
          newExpr = newExpr.dropLast(1)
        }
      } else {
        newExpr = newExpr.dropLast(1)
      }

      val preview = CalculatorEngine.evaluatePreview(newExpr)
      state.copy(
        expression = newExpr,
        previewResult = preview,
        isCalculated = false,
      )
    }
  }

  fun onClear() {
    _uiState.update { state ->
      state.copy(
        expression = "",
        previewResult = null,
        isCalculated = false,
        errorMessage = null,
      )
    }
  }

  fun onEquals() {
    val state = _uiState.value
    if (state.expression.isBlank()) return

    val result = CalculatorEngine.evaluate(state.expression)
    result.fold(
      onSuccess = { resString ->
        val historyItem = CalculationHistoryItem(
          expression = state.expression.trim(),
          result = resString,
        )
        _uiState.update { current ->
          current.copy(
            expression = resString,
            previewResult = null,
            isCalculated = true,
            errorMessage = null,
            history = listOf(historyItem) + current.history,
          )
        }
      },
      onFailure = { error ->
        val msg = error.message ?: "Error"
        _uiState.update { current ->
          current.copy(
            errorMessage = msg,
            previewResult = null,
            isCalculated = false,
          )
        }
      },
    )
  }

  fun onPercent() {
    _uiState.update { state ->
      if (state.expression.isEmpty()) return@update state
      val newExpr = "${state.expression}%"
      val preview = CalculatorEngine.evaluatePreview(newExpr)
      state.copy(
        expression = newExpr,
        previewResult = preview,
        isCalculated = false,
      )
    }
  }

  fun onNegate() {
    _uiState.update { state ->
      if (state.expression.isEmpty()) return@update state
      // Split into tokens to find the last operand
      val tokens = state.expression.trim().split(" ")
      if (tokens.isEmpty()) return@update state

      val lastToken = tokens.last()
      if (lastToken.toDoubleOrNull() != null) {
        val negated = if (lastToken.startsWith("-")) {
          lastToken.substring(1)
        } else {
          "-$lastToken"
        }
        val prefix = tokens.dropLast(1).joinToString(" ")
        val newExpr = if (prefix.isEmpty()) negated else "$prefix $negated"
        val preview = CalculatorEngine.evaluatePreview(newExpr)
        state.copy(
          expression = newExpr,
          previewResult = preview,
        )
      } else {
        state
      }
    }
  }

  fun onScientific(token: String) {
    _uiState.update { state ->
      val baseExpr = if (state.isCalculated || state.errorMessage != null) "" else state.expression
      val newExpr = when (token) {
        "x²" -> if (baseExpr.isNotEmpty()) "$baseExpr ^ 2" else "0 ^ 2"
        "√" -> "${baseExpr}√"
        "(" -> "$baseExpr("
        ")" -> "$baseExpr)"
        "^" -> "$baseExpr ^ "
        "π" -> "${baseExpr}π"
        "e" -> "${baseExpr}e"
        else -> baseExpr + token
      }
      val preview = CalculatorEngine.evaluatePreview(newExpr)
      state.copy(
        expression = newExpr,
        previewResult = preview,
        isCalculated = false,
        errorMessage = null,
      )
    }
  }

  fun toggleScientific() {
    _uiState.update { it.copy(isScientificOpen = !it.isScientificOpen) }
  }

  fun toggleHistorySheet(show: Boolean) {
    _uiState.update { it.copy(showHistorySheet = show) }
  }

  fun onSelectHistory(item: CalculationHistoryItem) {
    _uiState.update { state ->
      state.copy(
        expression = item.result,
        previewResult = null,
        isCalculated = true,
        showHistorySheet = false,
        errorMessage = null,
      )
    }
  }

  fun onClearHistory() {
    _uiState.update { it.copy(history = emptyList()) }
  }
}
