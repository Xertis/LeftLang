import lexer.*
import parser.*
import generator.*

fun main() {
    val lexer = Lexer(source = """
#include <stdio.h>

fun main() -> i32 {
  for (var x: i32 = 10 in 0..10, -1) {
    printf("%d\n", x)
  }
  return 0
}
    """.trimIndent())

    while (!lexer.isEOF()) {
        //println("${tokenizer.pos}, ${tokenizer.peek(tokenizer.pos+1)}")
        lexer.toTokens()
    }

    println(lexer.tokens)
    val parser = Parser(lexer.tokens)
    val program = parser.makeAst()
    println(program)
    val generator = Generator(program)
    println(generator.startGen())
}