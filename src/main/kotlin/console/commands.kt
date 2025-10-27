package console

import generator.Generator
import lexer.Lexer
import parser.Parser
import semantic.Semantic
import java.io.File
import builder.Manager

fun bindCommands(console: Console) {
    val logger = console.logger
    val commands = console.commands
    console.addCommand("help", "Displays information about commands", false, fun (args: Array<String>): Boolean? {
        logger.info("Usage: left [command]")
        for (command in commands) {
            logger.info("${command.name} -> ${command.description}", 2)
        }

        return true
    })

    console.addCommand("translate", "Translates Left in C99", true, fun (args: Array<String>): Boolean? {
        val path = args[0]
        try {
            val content = when("-itContent" in args) {
                false -> File(path).readText(Charsets.UTF_8).trimIndent()
                true -> path
            }

            logger.info("Starting the translate")
            val lexer = Lexer(source = " $content ")

            logger.info("Translating \"$path\"...")
            lexer.toTokens()
            logger.info("The lexer's work is finished...", 2)

            val parser = Parser(lexer.tokens)
            var program = parser.makeAst()
            logger.info("The parser's work is finished...", 2)
            program = Semantic.analyze(program)
            logger.info("The semantic's work is finished...", 2)
            val generator = Generator(program)
            val res = generator.startGen()
            logger.info("Translating finished. Result:\n$res", 2)
        } catch (e: Exception) {
            logger.fatal("Left fatal error: ${e.message}")
            return false
        }

        return true
    })

    console.addCommand("create", "Creates a new project with a basic structure", false, fun (args: Array<String>): Boolean? {
        Manager.create()
        return true
    })

    console.addCommand("build", "Building a project", false, fun (args: Array<String>): Boolean? {
        Manager.translateFolder()
        Manager.build()

        return true
    })

    console.addCommand("run", "Run the project", false, fun (args: Array<String>): Boolean? {
        if ("-rebuild" in args) {
            Manager.translateFolder()
            Manager.build()
        }

        Manager.run()

        return true
    })
}