import lexer.*

fun main() {
    val lexer = Lexer(source = """
        #include <stdout.h>
        
        const Pi: 
        
        fun main() -> u32 {
            val x: String = "AAAAAAAAAAAAAAA" && 'a'
            var x: f32 = 34.8
            
            x = true
            return 0
        }       
    """.trimIndent())

    while (!lexer.isEOF()) {
        //println("${tokenizer.pos}, ${tokenizer.peek(tokenizer.pos+1)}")
        lexer.nextToken()
    }

    println(lexer.tokens)
    println(lexer.peek())
}