package lexer

import lexer.tokens.Token
import lexer.tokens.Types

enum class TokenizerStates {
    DEFAULT, IN_IDENT, IN_NUMBER, IN_STRING, IN_COMMENT
}

class Buffer {
    var buffer: String = ""

    fun add(char: Char) {
        buffer + char
    }

    fun get(): String {
        return buffer
    }

    fun clean() {
        buffer = ""
    }
}

class Tokenizer(val source: String) {
    var pos: Int = 0
    var col: Int = 1
    var line: Int = 1
    var buffer = Buffer()
    var tokens = mutableListOf<Token>()
    var state = TokenizerStates.DEFAULT

    fun peek(_pos: Int=pos): Char {
        if (_pos < source.length) {
            return source[_pos]
        } else {
            return '\u0000'
        }
    }

    fun advance(): Char {
        val ch = peek()
        pos++

        if (ch == '\n') {line++; col = 0} else col++
        return ch
    }

    fun nextLine() {
        while (peek() != '\n' && peek() != '\u0000') {
            advance()
        }
    }

    fun single_to_token(char: Char): Token {
        val type = when (char) {
            '+' -> Types.PLUS
            '-' -> Types.MINUS
            '*' -> Types.MUL
            '/' -> Types.DIV
            '(' -> Types.LPAREN
            ')' -> Types.RPAREN
            '{' -> Types.LBRACE
            '}' -> Types.RBRACE
            ',' -> Types.COMMA
            ';' -> Types.NEW_LINE
            '\n' -> Types.NEW_LINE
            '.' -> Types.DOT
            '#' -> Types.PREPROC
            else -> throw Exception("Unexpected char '$char' at $line:$col")
        }

        return Token(type = type, value = char.toString(), ln = line, col = col)
    }

    fun nextIt(values: Array<String>, skip: Boolean=false): Boolean {
        var offset = 0
        val sourceLength = source.length

        while (pos + offset <= sourceLength) {
            var matched: Int? = null

            for ((index, value) in values.withIndex()) {
                if (matched != null) {break}

                matched = index
                for (i in value.indices) {
                    if (peek(pos + offset + i) != value[i]) {
                        matched = null
                        break
                    }
                }
            }

            if (matched != null) {
                if (skip) {
                    repeat(offset + values[matched].length) { advance() }
                }
                return true
            }

            offset++
        }

        return false
    }

    fun isIt(value: String, skip: Boolean=false): Boolean {
        for ((index, char) in value.withIndex()) {
            if (source[pos+index] != char) {
                return false
            }
        }

        if (skip) {
            repeat( value.length) { advance() }
        }

        return true
    }

    fun isEOF(): Boolean {
        return (pos >= source.length-1)
    }

    fun putToken(type: Types, move: () -> Unit) {
        val curPos = pos
        val ln = line
        val column = col
        move()

        val code: String = source.substring(curPos, pos)
        tokens.add(
            Token(
                type = type,
                value = code,
                ln = ln,
                col = column
            )
        )
    }

    fun nextToken() {
        if (state == TokenizerStates.DEFAULT) {
            val char = peek()
            println("символ: '$char'")
            when (char) {
                '#' -> {
                    putToken(Types.PREPROC, ::nextLine)
                    return
                }

                ' ' -> {
                    advance(); return
                }

                '\n' -> {
                    putToken(Types.NEW_LINE, ::advance)
                    return
                }
            }

            if (isIt("//", true)) {
                nextLine(); return
            }
            if (isIt("/*", true)) {
                nextIt(arrayOf("*/"), true); return
            }

            state = TokenizerStates.IN_IDENT
        } else if (state == TokenizerStates.IN_IDENT) {

        }
    }
}