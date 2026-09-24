package ofis.tool.pdf.compress.service

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.value
import ofis.config.Logger
import ofis.tool.pdf.compress.model.NativeCommand
import ofis.tool.pdf.compress.model.ProcessResult
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSPipe
import platform.Foundation.NSTask
import platform.Foundation.NSURL

/**
 * macOS implementation using NSTask (Foundation).
 * Avoids fork() complications in multi-threaded GUI apps.
 * Safe: arguments are passed as an NSArray, never shell-interpolated.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun runProcess(command: NativeCommand): ProcessResult {
    Logger.info("[runProcess] ${command.executable} ${command.arguments.joinToString(" ")}")
    val task = NSTask()
    task.executableURL = NSURL.fileURLWithPath(command.executable)
    task.arguments = command.arguments

    Logger.info("[runProcess] executableURL=${task.executableURL} arguments=${task.arguments}")

    val stdoutPipe = NSPipe()
    val stderrPipe = NSPipe()
    task.standardOutput = stdoutPipe
    task.standardError = stderrPipe

    // Capture NSError so we know exactly why the launch failed.
    val launched: Boolean
    val launchError: String?
    memScoped {
        val errorPtr = alloc<ObjCObjectVar<NSError?>>()
        launched = task.launchAndReturnError(errorPtr.ptr)
        launchError = errorPtr.value?.localizedDescription
    }

    val pid = if (launched) task.processIdentifier else -1
    Logger.info("[runProcess] launched=$launched pid=$pid error=$launchError")

    if (!launched) {
        // ARC releases the NSPipe objects here, closing both ends — no deadlock.
        return ProcessResult(
            exitCode = ProcessResult.EXIT_COMMAND_NOT_FOUND,
            stdout = "",
            stderr = launchError ?: "Failed to launch: ${command.executable}",
        )
    }

    // Reading to EOF blocks until the process closes its stdout/stderr (i.e. exits)
    val stdoutData = stdoutPipe.fileHandleForReading.readDataToEndOfFileAndReturnError(null) ?: NSData()
    val stderrData = stderrPipe.fileHandleForReading.readDataToEndOfFileAndReturnError(null) ?: NSData()

    // Spin until NSTask records the exit status (normally 0–1 iterations after pipes close)
    while (task.running) { /* yield */ }

    val result =
        ProcessResult(
            exitCode = task.terminationStatus,
            stdout = stdoutData.utf8(),
            stderr = stderrData.utf8(),
        )
    Logger.info("[runProcess] exit=${result.exitCode} stdout=${result.stdout.length}B stderr=${result.stderr.length}B")
    return result
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.utf8(): String = if (length == 0UL) "" else bytes?.readBytes(length.toInt())?.decodeToString() ?: ""
