package ofis.platform.service

actual fun appDataDir(): String = throw UnsupportedOperationException("JVM target is test-only")

actual fun availableDiskSpace(dirPath: String): Long = throw UnsupportedOperationException("JVM target is test-only")

actual fun findHelperBinary(name: String): String? = throw UnsupportedOperationException("JVM target is test-only")
