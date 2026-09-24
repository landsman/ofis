package ofis.utils.format

private const val KB = 1024L
private const val MB = KB * 1024
private const val GB = MB * 1024

fun formatSize(size: Long): String =
    when {
        size < KB -> "$size B"
        size < MB -> "${size / KB} KB"
        size < GB -> "${size / MB} MB"
        else -> "${size / GB} GB"
    }
