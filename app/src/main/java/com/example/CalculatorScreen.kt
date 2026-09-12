package com.example

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.CalcBgDark
import com.example.ui.theme.CalcDisplayError
import com.example.ui.theme.CalcDisplayExpression
import com.example.ui.theme.CalcDisplayPreview
import com.example.ui.theme.CalcDisplayResult
import com.example.ui.theme.CalcEqualsKey
import com.example.ui.theme.CalcFunctionKey
import com.example.ui.theme.CalcFunctionKeyText
import com.example.ui.theme.CalcNumberKey
import com.example.ui.theme.CalcNumberKeyText
import com.example.ui.theme.CalcOperatorKey
import com.example.ui.theme.CalcOperatorKeyText
import com.example.ui.theme.CalcSurfaceDark
import com.example.ui.theme.PrimaryOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
  viewModel: CalculatorViewModel = viewModel(),
  modifier: Modifier = Modifier,
) {
  val uiState by viewModel.uiState.collectAsState()
  val haptic = LocalHapticFeedback.current

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(CalcBgDark)
      .statusBarsPadding()
      .navigationBarsPadding(),
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      // Top App Bar / Utility Ribbon
      CalculatorTopBar(
        isScientificOpen = uiState.isScientificOpen,
        historyCount = uiState.history.size,
        onToggleScientific = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          viewModel.toggleScientific()
        },
        onOpenHistory = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          viewModel.toggleHistorySheet(true)
        },
      )

      // Display Area (Expression & Result / Live Preview)
      CalculatorDisplay(
        expression = uiState.expression,
        previewResult = uiState.previewResult,
        errorMessage = uiState.errorMessage,
        isCalculated = uiState.isCalculated,
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
      )

      // Scientific Controls Strip (Expandable)
      AnimatedVisibility(
        visible = uiState.isScientificOpen,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
      ) {
        ScientificKeyStrip(
          onScientificToken = { token ->
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.onScientific(token)
          },
          modifier = Modifier.padding(bottom = 8.dp),
        )
      }

      // Main Calculator Keypad
      CalculatorKeypad(
        hasInput = uiState.expression.isNotEmpty(),
        onDigit = { digit ->
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          viewModel.onDigit(digit)
        },
        onOperator = { op ->
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          viewModel.onOperator(op)
        },
        onDecimal = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          viewModel.onDecimal()
        },
        onClear = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          viewModel.onClear()
        },
        onBackspace = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          viewModel.onBackspace()
        },
        onPercent = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          viewModel.onPercent()
        },
        onNegate = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          viewModel.onNegate()
        },
        onEquals = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          viewModel.onEquals()
        },
        modifier = Modifier.fillMaxWidth(),
      )
    }

    // History Modal Bottom Sheet
    if (uiState.showHistorySheet) {
      CalculationHistoryBottomSheet(
        history = uiState.history,
        onDismiss = { viewModel.toggleHistorySheet(false) },
        onSelect = { item ->
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          viewModel.onSelectHistory(item)
        },
        onClearHistory = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          viewModel.onClearHistory()
        },
      )
    }
  }
}

@Composable
fun CalculatorTopBar(
  isScientificOpen: Boolean,
  historyCount: Int,
  onToggleScientific: () -> Unit,
  onOpenHistory: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .background(PrimaryOrange.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.Calculate,
          contentDescription = "Calculator App",
          tint = PrimaryOrange,
          modifier = Modifier.size(18.dp),
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "Calculator",
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          color = Color.White,
          letterSpacing = 0.5.sp,
        ),
      )
    }

    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      // Toggle scientific mode button
      Surface(
        onClick = onToggleScientific,
        shape = RoundedCornerShape(16.dp),
        color = if (isScientificOpen) PrimaryOrange else CalcFunctionKey,
        modifier = Modifier.testTag("btn_scientific_toggle"),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Icon(
            imageVector = Icons.Default.Functions,
            contentDescription = "Toggle Scientific Functions",
            tint = if (isScientificOpen) Color.White else CalcFunctionKeyText,
            modifier = Modifier.size(16.dp),
          )
          Text(
            text = "Sci",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isScientificOpen) Color.White else CalcFunctionKeyText,
          )
        }
      }

      // History button
      IconButton(
        onClick = onOpenHistory,
        modifier = Modifier.testTag("btn_history"),
      ) {
        Box(contentAlignment = Alignment.TopEnd) {
          Icon(
            imageVector = Icons.Default.History,
            contentDescription = "History",
            tint = if (historyCount > 0) Color.White else CalcDisplayExpression,
            modifier = Modifier.size(24.dp),
          )
          if (historyCount > 0) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(PrimaryOrange),
            )
          }
        }
      }
    }
  }
}

@Composable
fun CalculatorDisplay(
  expression: String,
  previewResult: String?,
  errorMessage: String?,
  isCalculated: Boolean,
  modifier: Modifier = Modifier,
) {
  val scrollState = rememberScrollState()

  // Scroll to end when expression updates
  LaunchedEffect(expression, previewResult) {
    scrollState.animateScrollTo(scrollState.maxValue)
  }

  Column(
    modifier = modifier
      .padding(horizontal = 4.dp, vertical = 12.dp),
    horizontalAlignment = Alignment.End,
    verticalArrangement = Arrangement.Bottom,
  ) {
    // Expression Row (with horizontal scrolling for lengthy calculations)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(scrollState),
      contentAlignment = Alignment.CenterEnd,
    ) {
      Text(
        text = if (expression.isEmpty()) "0" else expression,
        color = if (expression.isEmpty()) CalcDisplayExpression.copy(alpha = 0.5f) else CalcDisplayExpression,
        fontSize = if (isCalculated) 26.sp else 38.sp,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.End,
        maxLines = 1,
        modifier = Modifier.testTag("display_expression"),
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Result or Preview Row
    when {
      errorMessage != null -> {
        Text(
          text = errorMessage,
          color = CalcDisplayError,
          fontSize = 24.sp,
          fontWeight = FontWeight.Medium,
          textAlign = TextAlign.End,
          modifier = Modifier.testTag("display_result"),
        )
      }
      previewResult != null -> {
        Text(
          text = "= $previewResult",
          color = CalcDisplayPreview,
          fontSize = 28.sp,
          fontWeight = FontWeight.SemiBold,
          textAlign = TextAlign.End,
          modifier = Modifier.testTag("display_result"),
        )
      }
      isCalculated -> {
        Text(
          text = expression,
          color = CalcDisplayResult,
          fontSize = 42.sp,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.End,
          modifier = Modifier.testTag("display_result"),
        )
      }
      else -> {
        // Placeholder height to prevent layout jump
        Text(
          text = "",
          fontSize = 28.sp,
          modifier = Modifier.height(34.dp),
        )
      }
    }
  }
}

@Composable
fun ScientificKeyStrip(
  onScientificToken: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val tokens = listOf("(", ")", "√", "x²", "^", "π", "e")

  LazyRow(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    items(tokens) { token ->
      Surface(
        onClick = { onScientificToken(token) },
        shape = RoundedCornerShape(16.dp),
        color = CalcSurfaceDark,
        modifier = Modifier
          .height(44.dp)
          .testTag("btn_sci_$token"),
      ) {
        Box(
          modifier = Modifier.padding(horizontal = 16.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = token,
            color = CalcFunctionKeyText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
          )
        }
      }
    }
  }
}

@Composable
fun CalculatorKeypad(
  hasInput: Boolean,
  onDigit: (String) -> Unit,
  onOperator: (String) -> Unit,
  onDecimal: () -> Unit,
  onClear: () -> Unit,
  onBackspace: () -> Unit,
  onPercent: () -> Unit,
  onNegate: () -> Unit,
  onEquals: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val spacing = 10.dp

  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(spacing),
  ) {
    // Row 1: Clear / Backspace / % / ÷
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
      CalcKey(
        text = if (hasInput) "C" else "AC",
        backgroundColor = CalcFunctionKey,
        textColor = CalcFunctionKeyText,
        testTag = "btn_clear",
        onClick = onClear,
        modifier = Modifier.weight(1f),
      )
      CalcIconKey(
        icon = Icons.AutoMirrored.Filled.Backspace,
        contentDescription = "Backspace",
        backgroundColor = CalcFunctionKey,
        iconColor = CalcFunctionKeyText,
        testTag = "btn_backspace",
        onClick = onBackspace,
        modifier = Modifier.weight(1f),
      )
      CalcKey(
        text = "%",
        backgroundColor = CalcFunctionKey,
        textColor = CalcFunctionKeyText,
        testTag = "btn_percent",
        onClick = onPercent,
        modifier = Modifier.weight(1f),
      )
      CalcKey(
        text = "÷",
        backgroundColor = CalcOperatorKey,
        textColor = CalcOperatorKeyText,
        testTag = "btn_divide",
        onClick = { onOperator("÷") },
        modifier = Modifier.weight(1f),
      )
    }

    // Row 2: 7, 8, 9, ×
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
      CalcKey(
        text = "7",
        backgroundColor = CalcNumberKey,
        textColor = CalcNumberKeyText,
        testTag = "btn_7",
        onClick = { onDigit("7") },
        modifier = Modifier.weight(1f),
      )
      CalcKey(
        text = "8",
        backgroundColor = CalcNumberKey,
        textColor = CalcNumberKeyText,
        testTag = "btn_8",
        onClick = { onDigit("8") },
        modifier = Modifier.weight(1f),
      )
      CalcKey(
        text = "9",
        backgroundColor = CalcNumberKey,
        textColor = CalcNumberKeyText,
        testTag = "btn_9",
        onClick = { onDigit("9") },
        modifier = Modifier.weight(1f),
      )
      CalcKey(
        text = "×",
        backgroundColor = CalcOperatorKey,
        textColor = CalcOperatorKeyText,
        testTag = "btn_multiply",
        onClick = { onOperator("×") },
        modifier = Modifier.weight(1f),
      )
    }

    // Row 3: 4, 5, 6, −
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
      CalcKey(
        text = "4",
        backgroundColor = CalcNumberKey,
        textColor = CalcNumberKeyText,
        testTag = "btn_4",
        onClick = { onDigit("4") },
        modifier = Modifier.weight(1f),
      )
      CalcKey(
        text = "5",
        backgroundColor = CalcNumberKey,
        textColor = CalcNumberKeyText,
        testTag = "btn_5",
        onClick = { onDigit("5") },
        modifier = Modifier.weight(1f),
      )
      CalcKey(
        text = "6",
        backgroundColor = CalcNumberKey,
        textColor = CalcNumberKeyText,
        testTag = "btn_6",
        onClick = { onDigit("6") },
        modifier = Modifier.weight(1f),
      )
      CalcKey(
        text = "−",
        backgroundColor = CalcOperatorKey,
        textColor = CalcOperatorKeyText,
        testTag = "btn_subtract",
        onClick = { onOperator("−") },
        modifier = Modifier.weight(1f),
      )
    }

    // Row 4: 1, 2, 3, +
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
      CalcKey(
        text = "1",
        backgroundColor = CalcNumberKey,
        textColor = CalcNumberKeyText,
        testTag = "btn_1",
        onClick = { onDigit("1") },
        modifier = Modifier.weight(1f),
      )
      CalcKey(
        text = "2",
        backgroundColor = CalcNumberKey,
        textColor = CalcNumberKeyText,
        testTag = "btn_2",
        onClick = { onDigit("2") },
        modifier = Modifier.weight(1f),
      )
      CalcKey(
        text = "3",
        backgroundColor = CalcNumberKey,
        textColor = CalcNumberKeyText,
        testTag = "btn_3",
        onClick = { onDigit("3") },
        modifier = Modifier.weight(1f),
      )
      CalcKey(
        text = "+",
        backgroundColor = CalcOperatorKey,
        textColor = CalcOperatorKeyText,
        testTag = "btn_add",
        onClick = { onOperator("+") },
        modifier = Modifier.weight(1f),
      )
    }

    // Row 5: ±, 0, ., =
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
      CalcKey(
        text = "±",
        backgroundColor = CalcNumberKey,
        textColor = CalcNumberKeyText,
        testTag = "btn_negate",
        onClick = onNegate,
        modifier = Modifier.weight(1f),
      )
      CalcKey(
        text = "0",
        backgroundColor = CalcNumberKey,
        textColor = CalcNumberKeyText,
        testTag = "btn_0",
        onClick = { onDigit("0") },
        modifier = Modifier.weight(1f),
      )
      CalcKey(
        text = ".",
        backgroundColor = CalcNumberKey,
        textColor = CalcNumberKeyText,
        testTag = "btn_decimal",
        onClick = onDecimal,
        modifier = Modifier.weight(1f),
      )
      CalcKey(
        text = "=",
        backgroundColor = CalcEqualsKey,
        textColor = Color.White,
        testTag = "btn_equals",
        onClick = onEquals,
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
fun CalcKey(
  text: String,
  backgroundColor: Color,
  textColor: Color,
  testTag: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 48.dp) {
    Surface(
      onClick = onClick,
      shape = RoundedCornerShape(20.dp),
      color = backgroundColor,
      modifier = modifier
        .aspectRatio(1.2f)
        .testTag(testTag),
    ) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
      ) {
        Text(
          text = text,
          color = textColor,
          fontSize = 24.sp,
          fontWeight = FontWeight.Medium,
          textAlign = TextAlign.Center,
        )
      }
    }
  }
}

@Composable
fun CalcIconKey(
  icon: ImageVector,
  contentDescription: String,
  backgroundColor: Color,
  iconColor: Color,
  testTag: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 48.dp) {
    Surface(
      onClick = onClick,
      shape = RoundedCornerShape(20.dp),
      color = backgroundColor,
      modifier = modifier
        .aspectRatio(1.2f)
        .testTag(testTag),
    ) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
      ) {
        Icon(
          imageVector = icon,
          contentDescription = contentDescription,
          tint = iconColor,
          modifier = Modifier.size(24.dp),
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculationHistoryBottomSheet(
  history: List<CalculationHistoryItem>,
  onDismiss: () -> Unit,
  onSelect: (CalculationHistoryItem) -> Unit,
  onClearHistory: () -> Unit,
) {
  val sheetState = rememberModalBottomSheetState()

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = CalcSurfaceDark,
    contentColor = Color.White,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(bottom = 24.dp),
    ) {
      // Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "Calculation History",
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            color = Color.White,
          ),
        )
        if (history.isNotEmpty()) {
          IconButton(
            onClick = onClearHistory,
            modifier = Modifier.testTag("btn_clear_history"),
          ) {
            Icon(
              imageVector = Icons.Default.DeleteSweep,
              contentDescription = "Clear History",
              tint = PrimaryOrange,
            )
          }
        }
      }

      HorizontalDivider(color = CalcFunctionKey.copy(alpha = 0.5f))

      if (history.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
          contentAlignment = Alignment.Center,
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
              imageVector = Icons.Default.History,
              contentDescription = null,
              tint = CalcDisplayExpression.copy(alpha = 0.5f),
              modifier = Modifier.size(48.dp),
            )
            Text(
              text = "No calculations yet",
              color = CalcDisplayExpression,
              fontSize = 16.sp,
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          items(history, key = { it.id }) { item ->
            Surface(
              onClick = { onSelect(item) },
              shape = RoundedCornerShape(12.dp),
              color = CalcBgDark,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("history_item_${item.id}"),
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
                horizontalAlignment = Alignment.End,
              ) {
                Text(
                  text = item.expression,
                  color = CalcDisplayExpression,
                  fontSize = 16.sp,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis,
                  textAlign = TextAlign.End,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "= ${item.result}",
                  color = Color.White,
                  fontSize = 22.sp,
                  fontWeight = FontWeight.Bold,
                  textAlign = TextAlign.End,
                )
              }
            }
          }
        }
      }
    }
  }
}
