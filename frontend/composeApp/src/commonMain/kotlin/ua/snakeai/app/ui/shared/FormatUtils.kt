package ua.snakeai.app.ui.shared

fun formatEpisodes(count: Long): String {
    return if (count >= 1_000_000) {
        "${(count / 1_000_000.0).let { if (it % 1.0 == 0.0) it.toInt() else it }}M+"
    } else if (count >= 1_000) {
        "${count / 1_000}K"
    } else {
        count.toString()
    }
}
