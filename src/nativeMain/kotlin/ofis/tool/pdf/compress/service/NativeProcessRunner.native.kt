package ofis.tool.pdf.compress.service

import ofis.tool.pdf.compress.model.NativeCommand
import ofis.tool.pdf.compress.model.ProcessResult

actual fun runProcess(command: NativeCommand): ProcessResult = 
    ProcessResult(1, "", "Not implemented in nativeMain")
