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
    fun valAndVarTest() {
        println("run test valAndVarTest")
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
        println("run test whenTest")
        var res = ""
        for (i in 1..10) {
            val status = runCode(
                """
                include "stdio.h"
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

    @Test
    fun arrayTest() {
        println("run test arrayTest")

        val res1 = runCode("""
            include "stdio.h"
            fun main() {
                val x: int[1] = ?
                val y: int[2][1] = ?
                var z: short[] = {
                    1, 2, 3, -255, -100
                }
                
                z[1] = -89
                printf("%d", z[1])
            }
            """
        )

        assertEquals("-89", res1)

        val res2 = runCode("""
            fun main() {
                val x: int[3] = {1, 0, -100}
                x[1] = -256
            }
        """.trimIndent())

        assertEquals(null, res2)

        val res3 = runCode("""
            include "stdio.h"
            fun sum(rows: int, cols: int, arr: int[rows][cols]) -> int {
                var sum: int = 0
                
                for (var i: int=0 in 0..rows-1) {
                    for (var j: int=0 in 0..cols-1) {
                        sum += arr[i][j]
                    }
                }
                
                return sum
            }
            fun main() {
                val x: int[3][4] = {
                    {1, 2, 3, 4},
                    {5, 6, 7, 8},
                    {9, 10, 11, -10}
                }
                printf("%d", sum(arr=x, rows=3, cols=4))
            }
        """.trimIndent())

        assertEquals("56", res3)
    }

    @Test
    fun loopTest() {
        println("run test loopTest")

        val res1 = runCode("""
                include "stdio.h"
                
                fun main() {
                    var x: int = 0
                
                    loop {
                        if x > 10 {
                            break
                        }
                
                        x += 1
                    }
                
                    printf("%d", x)
                }
            """
        )

        assertEquals("11", res1)

        val res2 = runCode("""
                include "stdio.h"
                
                fun main() {
                    val x: int = 0
                
                    loop {
                        if x > 10 {
                            break
                        }
                
                        x += 1
                    }
                
                    printf("%d", x)
                }
            """
        )

        assertEquals(null, res2)
    }
}