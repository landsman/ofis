package ofis.utils.format

fun formatSize(size: Long): String {
    val tenths = size * 10 / (1024 * 1024)
    return "${tenths / 10}.${tenths % 10} MB"
}
