package ua.snakeai.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

val Typography = Typography(
    // Design System Typography Tokens
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold, // weights.bold: 700
        fontSize = 48.sp // sizes.display: 48px
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium, // weights.medium: 500
        fontSize = 24.sp // sizes.title: 24px
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal, // weights.regular: 400
        fontSize = 14.sp // sizes.body: 14px
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal, // weights.regular: 400
        fontSize = 12.sp // sizes.caption: 12px
    ),

    // Maintain compatibility for existing Headline & Body styles
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp, // mapped to sizes.body
    ),
    headlineLarge = TextStyle(
        fontSize = 24.sp, // mapped to sizes.title
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        lineHeight = 32.sp,
        textAlign = TextAlign.Left,
    ),
    headlineMedium = TextStyle(
        fontSize = 16.sp,
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 22.sp,
        textAlign = TextAlign.Left,
    ),
    headlineSmall = TextStyle(
        fontSize = 12.sp, // mapped to sizes.caption
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 16.sp,
        textAlign = TextAlign.Left,
    ),
)