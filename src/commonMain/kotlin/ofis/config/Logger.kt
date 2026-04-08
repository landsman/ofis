package ofis.config

object Logger {
    var onLog: ((String) -> Unit)? = null

    fun info(msg: String) {
        println(msg)
        onLog?.invoke(msg)
    }

    fun warn(msg: String) {
        onLog?.invoke(msg)
    }

    fun debug(msg: String) {
        if (GlobalConfig.debug) {
            println("[DEBUG] $msg")
            onLog?.invoke("[DEBUG] $msg")
        }
    }
}
