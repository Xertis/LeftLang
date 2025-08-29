import lexer.*

fun main() {
    val lexer = Lexer(source = """
        #include <stdout.h>
        
        fun main() -> u32 {
            var x: String = 'AAAAAAAAAAAAAAA'
            return x
        }       
    """.trimIndent())

    while (!lexer.isEOF()) {
        //println("${tokenizer.pos}, ${tokenizer.peek(tokenizer.pos+1)}")
        lexer.nextToken()
    }

    println(lexer.tokens)
    println(lexer.peek())
}