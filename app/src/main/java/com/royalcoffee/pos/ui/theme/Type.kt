package com.royalcoffee.pos.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.royalcoffee.pos.R

val CinzelFontFamily = FontFamily(
    Font(R.font.cinzel_regular, weight = FontWeight.Normal),
    Font(R.font.cinzel_semibold, weight = FontWeight.SemiBold)
)

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = CinzelFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        letterSpacing = 1.sp
    )
)