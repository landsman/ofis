package ofis.tool

interface Tool {
    val name: String
    val description: String

    fun run(args: List<String>)
}
