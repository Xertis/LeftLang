import lexer.*
import parser.*
import generator.*

fun main() {
    val lexer = Lexer(source = """
#include <stdio.h>

fun main() -> i32 {
  var s: i32 = 0
  var x: i32 = s
  
  while scanf("%d", &x) == 1 && x != 0 {
    if x % 2 == 0 {s += x}
  }
  printf("s = %d\n", s)
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