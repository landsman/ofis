package ofis.tool.pdf.compress.service

import ofis.tool.pdf.compress.model.NativeCommand
import ofis.tool.pdf.compress.model.ProcessResult

import kotlinx.cinterop.*
import platform.posix.*

/**
 * POSIX implementation using fork + execvp + pipe + waitpid.
 * Safe: arguments are passed as a native argv array, never shell-interpolated.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun runProcess(command: NativeCommand): ProcessResult = memScoped {
    val stdoutPipe = allocArray<IntVar>(2)
    val stderrPipe = allocArray<IntVar>(2)

    check(pipe(stdoutPipe) == 0) { "pipe(stdout) failed" }
    check(pipe(stderrPipe) == 0) { "pipe(stderr) failed" }

    val pid = fork()
    check(pid >= 0) { "fork() failed" }

    if (pid == 0) {
        // ── child process ──────────────────────────────────────────────────
        dup2(stdoutPipe[1], STDOUT_FILENO)
        dup2(stderrPipe[1], STDERR_FILENO)
        close(stdoutPipe[0]); close(stdoutPipe[1])
        close(stderrPipe[0]); close(stderrPipe[1])

        val all = listOf(command.executable) + command.arguments
        val argv = allocArray<CPointerVar<ByteVar>>(all.size + 1)
        all.forEachIndexed { i, s -> argv[i] = s.cstr.getPointer(this) }
        argv[all.size] = null

        execvp(command.executable, argv)
        _exit(127) // execvp failed
    }

    // ── parent process ─────────────────────────────────────────────────────
    close(stdoutPipe[1])
    close(stderrPipe[1])

    val stdout = readFd(stdoutPipe[0])
    val stderr = readFd(stderrPipe[0])

    val status = alloc<IntVar>()
    waitpid(pid, status.ptr, 0)

    ProcessResult(
        exitCode = status.value shr 8 and 0xFF, // WEXITSTATUS
        stdout = stdout,
        stderr = stderr,
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun readFd(fd: Int): String = memScoped {
    val buf = ByteArray(8192)
    val sb = StringBuilder()
    buf.usePinned { pinned ->
        while (true) {
            val n = read(fd, pinned.addressOf(0), buf.size.convert()).toInt()
            if (n <= 0) break
            sb.append(buf.decodeToString(0, n))
        }
    }
    close(fd)
    sb.toString()
}
