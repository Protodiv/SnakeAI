package ua.snakeai.app.ui.shared

import kotlin.math.pow

fun formatEpisodes(count: Long): String {
    return if (count >= 1_000_000) {
        "${(count / 1_000_000.0).let { if (it % 1.0 == 0.0) it.toInt() else it }}M+"
    } else if (count >= 1_000) {
        "${count / 1_000}K"
    } else {
        count.toString()
    }
}

fun formatDouble(value: Double, decimals: Int): String {
    if (value.isNaN() || value.isInfinite()) return "0.0"
    val factor = 10.0.pow(decimals)
    val rounded = kotlin.math.round(value * factor) / factor
    val str = rounded.toString()
    
    if (!str.contains('e') && !str.contains('E')) {
        return str
    }
    
    val parts = str.lowercase().split('e')
    val coeffParts = parts[0].split('.')
    val integerPart = coeffParts[0]
    val fractionalPart = if (coeffParts.size > 1) coeffParts[1] else ""
    val exponent = parts[1].toInt()
    
    if (exponent < 0) {
        val absExponent = -exponent
        val sb = StringBuilder()
        sb.append("0.")
        for (i in 1 until absExponent) {
            sb.append('0')
        }
        sb.append(integerPart)
        sb.append(fractionalPart)
        
        var result = sb.toString()
        while (result.endsWith('0') && result.substringAfter('.').length > 1) {
            result = result.substring(0, result.length - 1)
        }
        return result
    }
    return str
}
