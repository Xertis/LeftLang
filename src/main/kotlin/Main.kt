import lexer.*
import parser.*
import generator.*

fun main() {
    val lexer = Lexer(source = """
        #include <stdio.h>
        
        fun discriminant(a: f64, b: f64, c: f64) -> f64 {
            return b*b-4*a*c
        }
        
        fun main() -> i32 {
            var a: f64 = 3
            var b: f64 = 7
            var c: f64 = -6
            printf("%f", discriminant(a, b, c))
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