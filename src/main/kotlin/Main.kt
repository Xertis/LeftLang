import lexer.*
import parser.*
import generator.*

fun main() {
    val lexer = Lexer(source = """
#include <stdio.h>

fun main() -> i32 {
    fun sum(a: i32=0, b: i32=0) -> i32 {
        return a+b
    }
    
   printf("%d", sum(b=5, 1))

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