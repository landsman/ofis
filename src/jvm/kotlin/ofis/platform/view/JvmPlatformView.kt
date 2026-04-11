package ofis.platform.view

actual fun platformGui(): Unit = throw UnsupportedOperationException("JVM target is test-only")

actual suspend fun pickFile(allowedExtensions: List<String>): String? = throw UnsupportedOperationException("JVM target is test-only")

actual suspend fun saveFile(suggestedName: String): String? = throw UnsupportedOperationException("JVM target is test-only")
