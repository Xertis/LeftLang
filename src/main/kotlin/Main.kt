import lexer.*
import parser.*
import generator.*

fun main() {
    val lexer = Lexer(source = """
        #include <stdio.h>
        
        fun max(a: i32, b: i32) -> i32 {
            if a > b {return a}
            elif a == b {return 0}
            else {return b}
        }
        
        fun main() -> i32 {
            var x: i32 = -10
            var y: i32 = 5
            printf("%d", max(x, y))
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