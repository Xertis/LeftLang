import lexer.*
import parser.*
import generator.*

fun main() {
    val lexer = Lexer(source = """
        #include <stdio.h>
        
        fun sum(a: i8, b: i8) -> i16 {
            return a+b
        }
        
        
        fun main() -> i32 {
            var x: Bool = 1
            printf("%d", x)
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