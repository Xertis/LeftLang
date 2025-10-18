import console.Console

fun main(args: Array<String>) {
    val status = Console().process(args)

    if (!status) throw RuntimeException("Abort")
}