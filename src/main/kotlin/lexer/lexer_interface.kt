package lexer

import TokenTypes
import tokens.Token
import scripts.utils.TokenBuffer

interface LexerInterface {
    // Свойства
    var pos: Int
    val col: Int
    val line: Int
    val source: String
    val tokens: MutableList<Token>
    var buffer: TokenBuffer

    // Методы для работы с позицией
    fun peek(_pos: Int = pos): Char
    fun advance(): Char
    fun nextLine()
    fun isEOF(): Boolean

    fun nextIt(values: Array<String>, skip: Boolean = false): Boolean
    fun isIt(value: String, skip: Boolean = false): Boolean

    fun putToken(type: TokenTypes, move: () -> Unit, popLast: Boolean=false)
    fun putToken(type: TokenTypes, buffer: TokenBuffer, popLast: Boolean=false)

    fun toTokens()
}