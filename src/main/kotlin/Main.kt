import lexer.*
import parser.*
import generator.*

fun main() {
    val lexer = Lexer(source = """
        #include <stdio.h>
        
        fun main() -> i32 {
            var b: u8 = 3
            var y: u8 = 2
            when (b) {
                1 || 2 -> {printf("у нас 1")}
                y+1 -> {printf("у нас 2")}
                3 -> {printf("у нас 3")}
                else -> {printf("У нас не 1, не 2, не 3")}
            }
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