package ofis.tool.pdf.compress.service
import ofis.tool.pdf.compress.model.NativeCommand
import ofis.tool.pdf.compress.model.ProcessResult

/**
 * Runs a native command and captures its output.
 * Implemented per platform via expect/actual.
 */
expect fun runProcess(command: NativeCommand): ProcessResult
