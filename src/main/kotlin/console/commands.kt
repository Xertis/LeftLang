package console

import generator.Generator
import lexer.Lexer
import parser.Parser
import java.io.File
import java.nio.file.Files
import builder.Manager

fun bindCommands(console: Console) {
    val logger = console.logger
    val commands = console.commands
    console.addCommand("help", "Displays information about commands", false, fun (args: Array<String>) {
        logger.info("Usage: left [command]")
        for (command in commands) {
            logger.info("${command.name} -> ${command.description}", 2)
        }
    })

    console.addCommand("translate", "Translates Left in C99", true, fun (args: Array<String>) {
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

    console.addCommand("create", "Creates a new project with a basic structure", false, fun (args: Array<String>) {
        Manager.create()
    })

    console.addCommand("build", "Building a project", false, fun (args: Array<String>) {
        Manager.translateFolder()
        Manager.build()
    })
}