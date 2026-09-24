package ofis.tool.pdf.compress.service

import ofis.tool.pdf.compress.model.NativeCommand
import ofis.tool.pdf.compress.model.ProcessResult

/**
 * Windows stub: compression needs a runner built on CreateProcessW + anonymous pipes.
 */
actual fun runProcess(command: NativeCommand): ProcessResult {
    error("Process execution not yet implemented for Windows. Contribute a CreateProcessW runner.")
}
