import console.Command
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

import console.Console
import generator.Generator
import lexer.Lexer
import parser.Parser
import semantic.Semantic
import scripts.utils.OsCmd


class LeftTests {
    private fun canTranslate(source: String): Boolean {
        return Console().process(arrayOf("translate", source, "-itContent"))
    }

    private fun runCommand(command: String): String {
        return OsCmd.run(OsCmd.prepareCommand(command))
    }

    private fun translate(content: String): String {
        val lexer = Lexer(source = " $content ")

        lexer.toTokens()
        val parser = Parser(lexer.tokens)
        val program = Semantic.analyze(parser.makeAst())

        return Generator(program).startGen()
    }

    private fun runCode(content: String): String? {
        try {
            val code = translate(content)

            val res = runCommand("echo '$code' | gcc -xc - && ./a.out")

            return res
        } catch (e: Exception) {
            println("Error: ${e.message}")
            return null
        }
    }

    @Test
    fun valAndVar() {
        val status1 = runCode("""
            fun main() {
                val x: Heavy = ?
                x = 0
            }
            """
        )

        val status2 = runCode("""
            fun main() {
                var x: Heavy = ?
                x = 0
            }
            """
        )

        assertEquals(null, status1)
        assertEquals(true, status2 != null)
    }

    @Test
    fun whenTest() {
        var res = ""
        for (i in 1..10) {
            val status = runCode(
                """
                #include <stdio.h>
                fun main() {
                    var x: Int = $i
                    when (x) {
                        1 -> {printf("1")}
                        2 -> {printf("2")}
                        3 -> {printf("3")}
                        4, 5 -> {printf("4")}
                        6, 7, 8 -> {printf("5")}
                        9, 10 -> {printf("6")}
                    }
                }
                """
            )
            res += status
        }

        assertEquals("1234455566", res)
    }
}