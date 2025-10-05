package console

import java.io.File
import generator.Generator
import lexer.Lexer
import parser.Parser
import scripts.utils.Logger
import scripts.utils.LogLevel

class Console {
    val logger = Logger.getLogger("Left")

    init {
        Logger.setGlobalLevel(LogLevel.DEBUG)
        Logger.setGlobalColors(true)
        Logger.setGlobalTime(true)
        Logger.setGlobalTimeFormat("HH:mm:ss")
    }

    fun translate(args: Array<String>) {
        val path = args[0]
        try {
            val content = File(path).readText(Charsets.UTF_8).trimIndent()

            logger.info("Starting the translate")
            val lexer = Lexer(source = " $content ")

            logger.info("Translating \"$path\"...")
            lexer.toTokens()
            logger.info("The lexer's work is finished...", 4)

            val parser = Parser(lexer.tokens)
            val program = parser.makeAst()
            logger.info("The parser's work is finished...", 4)
            val generator = Generator(program)
            val res = generator.startGen()
            logger.info("Translating finished. Result:\n$res")
        } catch (e: Exception) {
            logger.fatal("Left fatal error: ${e.message}")
        }
    }
    fun process(args: Array<String>) {
        if (args.isEmpty()) {
            logger.error("0 arguments received")
            return
        }
        println(args.joinToString())
        val mainArg = args[0]
        val withoutMain = args.sliceArray(1 until args.size)

        when(mainArg) {
            "translate" -> translate(withoutMain)
            else -> throw RuntimeException("Unknown argument: $mainArg")
        }
    }

    companion object
}