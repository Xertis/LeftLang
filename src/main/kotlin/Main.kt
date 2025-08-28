import lexer.*

fun main() {
    var lexer = Lexer(source = """
        #include <stdout.h>
        
        /* ИТС МАЙ КОМЕНТ УЕЕЕЕЕЕЕЕ
        */
        
        ASADSADSA_234  234324324324 
    """.trimIndent())

    while (!lexer.isEOF()) {
        //println("${tokenizer.pos}, ${tokenizer.peek(tokenizer.pos+1)}")
        lexer.nextToken()
    }

    println(lexer.tokens)
    println(lexer.peek())
}