package com.royalcoffee.pos.ui.dashboard

import com.royalcoffee.pos.model.Money
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.royalcoffee.pos.data.local.dao.PaymentBreakdown
import com.royalcoffee.pos.data.local.dao.ProfitTrendPoint
import com.royalcoffee.pos.data.local.dao.SalesTrendPoint
import com.royalcoffee.pos.ui.theme.*
import kotlin.math.*

/**
 * Enhanced metric card with trend indicator.
 */
@Composable
fun MetricCardWithTrend(
    title: String,
    value: String,
    trend: String,
    isPositive: Boolean,
    comparisonLabel: String = "vs last period",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = CinzelFontFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = trend,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isPositive) TrendUp else TrendDown,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = comparisonLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Interactive Dual-Line Chart for Revenue and Gross Profit.
 */
@Composable
fun DashboardLineChart(
    revenueData: List<SalesTrendPoint>,
    profitData: List<ProfitTrendPoint>,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val textMeasurer = rememberTextMeasurer()
    val axisPadding = 40.dp
    
    // Combine data labels
    val labels = revenueData.map { it.dateLabel }
    
    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = axisPadding, bottom = 20.dp)
                .pointerInput(revenueData) {
                    detectTapGestures { offset ->
                        if (revenueData.isEmpty()) return@detectTapGestures
                        val width = size.width
                        val stepX = width / (revenueData.size - 1).coerceAtLeast(1)
                        selectedIndex = (offset.x / stepX).roundToInt().coerceIn(0, revenueData.size - 1)
                    }
                }
        ) {
            if (revenueData.isEmpty()) return@Canvas
            
            val width = size.width
            val height = size.height
            val maxVal = revenueData.maxOf { it.totalSales }.coerceAtLeast(100).toFloat()
            val stepX = width / (revenueData.size - 1).coerceAtLeast(1)
            
            // 1. Grid & Y-Axis
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = height - (i * height / gridLines)
                val value = (maxVal * i / gridLines / 100).toLong()
                drawLine(Color.LightGray.copy(alpha = 0.2f), Offset(0f, y), Offset(width, y), 1.dp.toPx())
                val label = if (value >= 1000) "${value / 1000}k" else "$value"
                val textLayoutResult = textMeasurer.measure(AnnotatedString("Rs $label"), TextStyle(fontSize = 10.sp, color = Color.Gray))
                drawText(textLayoutResult = textLayoutResult, topLeft = Offset(-axisPadding.toPx(), y - textLayoutResult.size.height / 2))
            }

            // 2. Plot Revenue Line (Gold)
            val revPoints = revenueData.mapIndexed { index, point ->
                Offset(index * stepX, height - (point.totalSales / maxVal * height * 0.8f) - 10.dp.toPx())
            }
            drawCurve(revPoints, RoyalGold)

            // 3. Plot Profit Line (Green)
            // Match indices with revenueData
            val profitPoints = revenueData.mapIndexed { index, revPoint ->
                val pPoint = profitData.find { it.dateLabel == revPoint.dateLabel }
                val profit = pPoint?.totalGrossProfit ?: 0
                Offset(index * stepX, height - (profit / maxVal * height * 0.8f) - 10.dp.toPx())
            }
            drawCurve(profitPoints, RoyalGreen)

            // 4. X-Axis Labels
            revPoints.forEachIndexed { index, offset ->
                val xTextResult = textMeasurer.measure(AnnotatedString(labels[index]), TextStyle(fontSize = 10.sp, color = Color.Gray))
                drawText(textLayoutResult = xTextResult, topLeft = Offset(offset.x - xTextResult.size.width / 2, height + 5.dp.toPx()))
            }
        }
        
        // Legend
        Row(modifier = Modifier.align(Alignment.BottomEnd).padding(end = 8.dp, bottom = 25.dp)) {
            LegendChip("Revenue", RoyalGold)
            Spacer(Modifier.width(12.dp))
            LegendChip("Gross Profit", RoyalGreen)
        }

        // Tooltip
        selectedIndex?.let { index ->
            val rev = revenueData[index]
            val prof = profitData.find { it.dateLabel == rev.dateLabel }?.totalGrossProfit ?: 0
            Card(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = RoyalGreenDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(rev.dateLabel, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                    Text("Revenue: Rs ${Money.format(rev.totalSales)}", color = RoyalGold, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Text("Gross Profit: Rs ${Money.format(prof)}", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCurve(points: List<Offset>, color: Color) {
    if (points.size < 2) return
    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        for (i in 0 until points.size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]
            val conPoint1 = Offset((p1.x + p2.x) / 2, p1.y)
            val conPoint2 = Offset((p1.x + p2.x) / 2, p2.y)
            cubicTo(conPoint1.x, conPoint1.y, conPoint2.x, conPoint2.y, p2.x, p2.y)
        }
    }
    drawPath(path, color, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
}

@Composable
private fun LegendChip(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(4.dp))
        Text(label, style = TextStyle(fontSize = 10.sp, color = Color.Gray))
    }
}

/**
 * Interactive Donut Chart for Payment Breakdown with clear Legend.
 */
@Composable
fun DashboardDonutChart(
    data: List<PaymentBreakdown>,
    modifier: Modifier = Modifier
) {
    val totalAmount = data.sumOf { it.totalAmount }.coerceAtLeast(1)
    var selectedSlice by remember { mutableStateOf<PaymentBreakdown?>(null) }
    
    val colors = listOf(RoyalGreen, RoyalGold, CoffeeBrown, SoftCream, RoyalGreenDark)
    
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(data) {
                        detectTapGestures { offset ->
                            val center = Offset(size.width.toFloat() / 2f, size.height.toFloat() / 2f)
                            val dx = offset.x - center.x
                            val dy = offset.y - center.y
                            val distance = sqrt(dx * dx + dy * dy)
                            val outerRadius = min(size.width.toFloat(), size.height.toFloat()) / 2.2f
                            val innerRadius = outerRadius * 0.6f
                            
                            if (distance in innerRadius..outerRadius) {
                                var angle = atan2(dy, dx) * 180 / PI
                                if (angle < 0) angle += 360
                                
                                var currentAngle = 0f
                                data.forEach { item ->
                                    val sweep = (item.totalAmount.toFloat() / totalAmount) * 360f
                                    if (angle in currentAngle..(currentAngle + sweep)) {
                                        selectedSlice = item
                                        return@detectTapGestures
                                    }
                                    currentAngle += sweep
                                }
                            } else {
                                selectedSlice = null
                            }
                        }
                    }
            ) {
                val canvasSize = min(size.width, size.height) / 1.1f
                val outerRadius = canvasSize / 2
                val innerRadius = outerRadius * 0.6f
                val center = Offset(size.width / 2, size.height / 2)
                
                var startAngle = 0f
                data.forEachIndexed { index, item ->
                    val sweepAngle = (item.totalAmount.toFloat() / totalAmount) * 360f
                    val isSelected = selectedSlice?.paymentMethod == item.paymentMethod
                    
                    drawArc(
                        color = colors[index % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                        size = Size(canvasSize, canvasSize),
                        style = Stroke(
                            width = (outerRadius - innerRadius) * (if (isSelected) 1.2f else 1f), 
                            cap = StrokeCap.Butt
                        )
                    )
                    startAngle += sweepAngle
                }
            }
            
            // Center text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (selectedSlice != null) {
                    Text(
                        text = selectedSlice!!.paymentMethod,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${(selectedSlice!!.totalAmount.toFloat() / totalAmount * 100).toInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Rs ${Money.format(selectedSlice!!.totalAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Rs ${Money.format(totalAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend (Horizontally scrollable LazyRow to prevent overflow on smaller screens)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(data) { index, item ->
                LegendItem(
                    label = item.paymentMethod,
                    color = colors[index % colors.size],
                    isSelected = selectedSlice?.paymentMethod == item.paymentMethod,
                    onClick = { selectedSlice = if (selectedSlice == item) null else item }
                )
            }
        }
    }
}

@Composable
private fun LegendItem(
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
                .then(if (isSelected) Modifier.border(1.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * Reusable Vertical Bar Chart showing Revenue and Gross Profit side-by-side or stacked.
 */
@Composable
fun DashboardBarChart(
    labels: List<String>,
    revenueValues: List<Long>,
    profitValues: List<Long>? = null,
    valuePrefix: String = "Rs ",
    modifier: Modifier = Modifier
) {
    if (labels.isEmpty()) return
    
    val maxVal = revenueValues.maxOf { it }.coerceAtLeast(1).toFloat()
    val textMeasurer = rememberTextMeasurer()
    
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val barCount = labels.size
            val groupWidth = width / barCount
            val barWidth = groupWidth * 0.5f
            val spacing = groupWidth * 0.5f
            
            revenueValues.forEachIndexed { index, rev ->
                val x = index * groupWidth + spacing / 2
                
                // Revenue Bar
                val revHeight = (rev / maxVal) * (height - 40.dp.toPx())
                val revY = height - revHeight - 20.dp.toPx()
                drawRoundRect(
                    color = RoyalGold.copy(alpha = 0.8f),
                    topLeft = Offset(x, revY),
                    size = Size(barWidth, revHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                )
                
                // Revenue Value Label
                if (profitValues == null) {
                    val valText = Money.format(rev)
                    val valResult = textMeasurer.measure(AnnotatedString("$valuePrefix$valText"), TextStyle(fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold))
                    if (revHeight > 15.dp.toPx()) {
                        drawText(textLayoutResult = valResult, topLeft = Offset(x + (barWidth - valResult.size.width) / 2, revY + 2.dp.toPx()))
                    }
                }

                // Profit Bar (Optional Overlay)
                if (profitValues != null && index < profitValues.size) {
                    val prof = profitValues[index]
                    val profHeight = (prof / maxVal) * (height - 40.dp.toPx())
                    val profY = height - profHeight - 20.dp.toPx()
                    drawRoundRect(
                        color = RoyalGreen,
                        topLeft = Offset(x, profY),
                        size = Size(barWidth, profHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                    )
                    
                    // Margin % Label
                    if (rev > 0) {
                        val margin = (prof.toDouble() / rev * 100).toInt()
                        val mText = "$margin%"
                        val mResult = textMeasurer.measure(AnnotatedString(mText), TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White))
                        drawText(textLayoutResult = mResult, topLeft = Offset(x + (barWidth - mResult.size.width) / 2, profY + 2.dp.toPx()))
                    }
                }

                // X-Axis Label
                val labelResult = textMeasurer.measure(AnnotatedString(labels[index]), TextStyle(fontSize = 9.sp, color = Color.Gray))
                drawText(textLayoutResult = labelResult, topLeft = Offset(x + (barWidth - labelResult.size.width) / 2, height - 15.dp.toPx()))
            }
        }
    }
}
