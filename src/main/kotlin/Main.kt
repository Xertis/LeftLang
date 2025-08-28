import lexer.Tokenizer

fun main() {
    var tokenizer = Tokenizer(source = """
        #include <stdout.h>
        
        /* ИТС МАЙ КОМЕНТ УЕЕЕЕЕЕЕЕ
        */
    """.trimIndent())

    while (!tokenizer.isEOF()) {
        //println("${tokenizer.pos}, ${tokenizer.peek(tokenizer.pos+1)}")
        tokenizer.nextToken()
    }

    println(tokenizer.tokens)
    println(tokenizer.peek())
}