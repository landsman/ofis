package ofis.tool.pdf.compress

/**
 * Windows stub — Win32 CreateProcessW implementation needed.
 * TODO: implement with CreateProcessW + anonymous pipes.
 */
actual fun runProcess(command: NativeCommand): ProcessResult {
    error("Process execution not yet implemented for Windows. Contribute a CreateProcessW runner.")
}
