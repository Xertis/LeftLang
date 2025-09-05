import lexer.*
import parser.*
import generator.*

fun main() {
    val lexer = Lexer(source = """
#include <stdio.h>

fun disk(a: f64, b: f64, c: f64) -> f64 {
    return b*b-4*a*c
}

fun main() -> i32 {
  var a: f64 = 0
  var b: f64 = 0
  var c: f64 = 0
  
  var count: u16 = scanf("%lf %lf %lf", &a, &b, &c)
  printf("%f", disk(a, b, c) )
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