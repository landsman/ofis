package ofis.platform.view

private const val TEST_ONLY = "JVM target is test-only"

actual fun platformGui(): Unit = throw UnsupportedOperationException(TEST_ONLY)

actual suspend fun pickFile(allowedExtensions: List<String>): String? = throw UnsupportedOperationException(TEST_ONLY)

actual suspend fun saveFile(suggestedName: String): String? = throw UnsupportedOperationException(TEST_ONLY)

actual fun openUrl(url: String): Unit = throw UnsupportedOperationException(TEST_ONLY)
