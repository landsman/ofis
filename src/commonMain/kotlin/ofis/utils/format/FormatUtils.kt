package ofis.utils.format

fun formatSize(size: Long): String {
    if (size < 1024) return "$size B"
    if (size < 1024 * 1024) return oneDecimal(size, 1024) + " KB"
    return oneDecimal(size, 1024 * 1024) + " MB"
}

private fun oneDecimal(size: Long, unit: Long): String {
    val tenths = size * 10 / unit
    return "${tenths / 10}.${tenths % 10}"
}
