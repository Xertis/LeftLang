package console

import java.io.File
import generator.Generator
import lexer.Lexer
import parser.Parser
import scripts.utils.Logger
import scripts.utils.LogLevel

data class Command(
    val name: String,
    val description: String,
    val handler: (Array<String>) -> Unit,
    val needArgs: Boolean=false
)

class Console {
    val logger = Logger.getLogger("Left")
    var commands: MutableList<Command> = mutableListOf()

    init {
        Logger.setGlobalLevel(LogLevel.DEBUG)
        Logger.setGlobalColors(true)
        Logger.setGlobalTime(true)
        Logger.setGlobalTimeFormat("HH:mm:ss")
        bind()
    }

    private fun bind() {
        addCommand("help", "Displays information about commands", false, fun (args: Array<String>) {
            logger.info("Usage: left [command]")
            for (command in commands) {
                logger.info("${command.name} -> ${command.description}", 2)
            }
        })

        addCommand("translate", "Translates Left in C99", true, fun (args: Array<String>) {
            val path = args[0]
            try {
                val content = File(path).readText(Charsets.UTF_8).trimIndent()

                logger.info("Starting the translate")
                val lexer = Lexer(source = " $content ")

                logger.info("Translating \"$path\"...")
                lexer.toTokens()
                logger.info("The lexer's work is finished...", 2)

                val parser = Parser(lexer.tokens)
                val program = parser.makeAst()
                logger.info("The parser's work is finished...", 2)
                val generator = Generator(program)
                val res = generator.startGen()
                logger.info("Translating finished. Result:\n$res", 2)
            } catch (e: Exception) {
                logger.fatal("Left fatal error: ${e.message}")
            }
        })
    }

    private fun addCommand(name: String, description: String, needArgs: Boolean, handler: (Array<String>) -> Unit) {
        commands += Command(name, description, handler, needArgs)
    }

    fun process(args: Array<String>) {
        if (args.isEmpty()) {
            logger.error("0 arguments received")
            return
        }

        val mainArg = args[0]
        val withoutMain = args.sliceArray(1 until args.size)

        for (command in commands) {
            if (command.name == mainArg) {
                if (!command.needArgs or withoutMain.isNotEmpty()) {
                    command.handler(withoutMain)
                } else {
                    logger.error("Expected arguments not passed")
                }

                return
            }
        }

        throw RuntimeException("Unknown argument: $mainArg")
    }

    companion object
}