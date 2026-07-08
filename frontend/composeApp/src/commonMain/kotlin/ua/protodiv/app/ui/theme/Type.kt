package ui.compose.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val Typography = Typography(

    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
    ),

    headlineLarge = TextStyle(
        fontSize = 16.sp,
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        lineHeight = 22.sp,
        textAlign = TextAlign.Left,
    ),
    headlineMedium = TextStyle(
        fontSize = 15.sp,
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 20.sp,
        textAlign = TextAlign.Left,
    ),

    headlineSmall = TextStyle(
        fontSize = 10.sp,
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 14.sp,
        textAlign = TextAlign.Left,
    ),
)