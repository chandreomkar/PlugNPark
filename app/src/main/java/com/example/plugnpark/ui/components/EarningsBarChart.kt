package com.example.plugnpark.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BarData(
    val dayLabel: String,
    val amount: Double
)

@Composable
fun EarningsBarChart(
    weeklyData: List<BarData>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val barBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant.hashCode()

    val maxAmount = (weeklyData.maxOfOrNull { it.amount } ?: 1000.0).coerceAtLeast(100.0)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(vertical = 12.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val itemCount = weeklyData.size
            if (itemCount == 0) return@Canvas

            val spacing = 20.dp.toPx()
            val availableWidth = canvasWidth - (spacing * (itemCount + 1))
            val barWidth = availableWidth / itemCount

            weeklyData.forEachIndexed { index, data ->
                val x = spacing + index * (barWidth + spacing)
                val barHeight = ((data.amount / maxAmount) * (canvasHeight - 40.dp.toPx())).toFloat()
                val y = canvasHeight - 20.dp.toPx() - barHeight

                // Draw background track
                drawRoundRect(
                    color = barBackgroundColor,
                    topLeft = Offset(x, 0f),
                    size = Size(barWidth, canvasHeight - 20.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                )

                // Draw filled bar
                drawRoundRect(
                    color = if (data.amount == maxAmount) primaryColor else primaryColor.copy(alpha = 0.6f),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight.coerceAtLeast(10.dp.toPx())),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                )

                // Draw Day Label below bar
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        color = textColor
                        textSize = 12.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    canvas.nativeCanvas.drawText(
                        data.dayLabel,
                        x + barWidth / 2,
                        canvasHeight - 2.dp.toPx(),
                        paint
                    )

                    // Price text above bar
                    if (data.amount > 0) {
                        val pricePaint = android.graphics.Paint().apply {
                            color = textColor
                            textSize = 10.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                        canvas.nativeCanvas.drawText(
                            "₹${data.amount.toInt()}",
                            x + barWidth / 2,
                            (y - 6.dp.toPx()).coerceAtLeast(12.dp.toPx()),
                            pricePaint
                        )
                    }
                }
            }
        }
    }
}
