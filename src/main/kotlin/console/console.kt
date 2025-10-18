package console

import java.io.File
import generator.Generator
import lexer.Lexer
import parser.Parser
import scripts.utils.Logger
import scripts.utils.LogLevel
import console.bindCommands

data class Command(
    val name: String,
    val description: String,
    val handler: (Array<String>) -> Boolean?,
    val needArgs: Boolean=false,
    val args: Array<String>?=null
)

class Console {
    val logger = Logger.getLogger("Left")
    var commands: MutableList<Command> = mutableListOf()

    init {
        Logger.setGlobalLevel(LogLevel.DEBUG)
        Logger.setGlobalColors(true)
        Logger.setGlobalTime(true)
        Logger.setGlobalTimeFormat("HH:mm:ss")
        bindCommands(this)
    }

    fun addCommand(name: String, description: String, needArgs: Boolean, handler: (Array<String>) -> Boolean?) {
        commands += Command(name, description, handler, needArgs, null)
    }

    fun process(args: Array<String>): Boolean {
        if (args.isEmpty()) {
            logger.error("0 arguments received")
            return true
        }

        val mainArg = args[0]
        val withoutMain = args.sliceArray(1 until args.size)

        for (command in commands) {
            if (command.name == mainArg) {
                if (!command.needArgs or withoutMain.isNotEmpty()) {
                    val status = command.handler(withoutMain)

                    if (status == false || status == null) {
                        return false
                    }
                }
                else {
                    logger.error("Expected arguments not passed")
                    return false
                }

                return true
            }
        }

        logger.error("Unknown argument: $mainArg")

        return false
    }

    companion object
}