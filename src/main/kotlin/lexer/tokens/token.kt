package lexer.tokens

import lexer.TokenTypes

data class Token(val type: TokenTypes, val value: String, val ln: Int, val col: Int)