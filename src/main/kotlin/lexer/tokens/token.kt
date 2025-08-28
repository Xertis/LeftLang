package lexer.tokens

data class Token(val type: Types, val value: String, val ln: Int, val col: Int)