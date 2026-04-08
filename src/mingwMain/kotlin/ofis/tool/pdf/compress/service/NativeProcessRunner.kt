package ofis.tool.pdf.compress.service

import ofis.tool.pdf.compress.model.NativeCommand
import ofis.tool.pdf.compress.model.ProcessResult

/**
 * Windows stub — Win32 CreateProcessW implementation needed.
 * TODO: implement with CreateProcessW + anonymous pipes.
 */
internal actual fun runProcessInternal(command: NativeCommand): ProcessResult {
    error("Process execution not yet implemented for Windows. Contribute a CreateProcessW runner.")
}
