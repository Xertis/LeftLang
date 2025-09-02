import lexer.*
import parser.*
import generator.*

fun main() {
    val lexer = Lexer(source = """
#include <stdio.h>

fun sum(a: f64, b: f64=1+2) -> f64 {
    return a+b
}

fun main() -> i32 {
  sum(6, 5)

  return 0
}
        
    """.trimIndent())

    while (!lexer.isEOF()) {
        //println("${tokenizer.pos}, ${tokenizer.peek(tokenizer.pos+1)}")
        lexer.toTokens()
    }

    val parser = Parser(lexer.tokens)
    val program = parser.makeAst()
    println(program)
    val generator = Generator(program)
    println(generator.startGen())
}