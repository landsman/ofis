package ofis.tool.pdf.compress.service

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readBytes
import ofis.tool.pdf.compress.model.NativeCommand
import ofis.tool.pdf.compress.model.ProcessResult
import platform.Foundation.NSData
import platform.Foundation.NSPipe
import platform.Foundation.NSTask
import platform.Foundation.NSURL

/**
 * macOS implementation using NSTask (Foundation).
 * Avoids fork() complications in multi-threaded GUI apps.
 * Safe: arguments are passed as an NSArray, never shell-interpolated.
 */
actual fun runProcess(command: NativeCommand): ProcessResult {
    val task = NSTask()
    task.executableURL = NSURL.fileURLWithPath(command.executable)
    task.arguments = command.arguments

    val stdoutPipe = NSPipe()
    val stderrPipe = NSPipe()
    task.standardOutput = stdoutPipe
    task.standardError = stderrPipe

    task.launchAndReturnError(null)

    // Reading to EOF blocks until the process closes its stdout/stderr (i.e. exits)
    val stdoutData = stdoutPipe.fileHandleForReading.readDataToEndOfFileAndReturnError(null) ?: NSData()
    val stderrData = stderrPipe.fileHandleForReading.readDataToEndOfFileAndReturnError(null) ?: NSData()

    // Spin until NSTask records the exit status (normally 0–1 iterations after pipes close)
    while (task.running) { /* yield */ }

    return ProcessResult(
        exitCode = task.terminationStatus,
        stdout = stdoutData.utf8(),
        stderr = stderrData.utf8(),
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.utf8(): String = if (length == 0UL) "" else bytes?.readBytes(length.toInt())?.decodeToString() ?: ""
