import lexer.*
import parser.*
import generator.*

fun main() {
    val lexer = Lexer(source = """
#include <stdio.h>

fun print_num(x: i32=0) {
  printf("%d", x)
}

fun sum(a: i32, b: i32) -> i32 {
  return a+b
}

fun main() -> i32 {
  print_num() // Дефолтное значение
  print_num(sum(b = 5, 10))
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