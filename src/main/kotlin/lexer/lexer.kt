package lexer

import lexer.tokens.Token
import scripts.utils.Fsm
import scripts.utils.TokenBuffer

class Lexer(override val source: String) : LexerInterface {
    override var pos: Int = 0
    override var col: Int = 1
    override var line: Int = 1
    override var tokens = mutableListOf<Token>()
    override var buffer = TokenBuffer(0, 0)

    val fsm = bindStates(Fsm(this))

    override fun peek(_pos: Int): Char {
        if (_pos < source.length) {
            return source[_pos]
        } else {
            return '\u0000'
        }
    }

    override fun advance(): Char {
        val ch = peek()
        pos++

        if (ch == '\n') {line++; col = 0} else col++
        return ch
    }

    override fun nextLine() {
        while (peek() != '\n' && peek() != '\u0000') {
            advance()
        }
    }

    override fun singleToToken(char: Char): Token {
        val type = when (char) {
            '+'  -> TokenTypes.PLUS
            '-'  -> TokenTypes.MINUS
            '*'  -> TokenTypes.MUL
            '/'  -> TokenTypes.DIV
            '('  -> TokenTypes.LPAREN
            ')'  -> TokenTypes.RPAREN
            '{'  -> TokenTypes.LBRACE
            '}'  -> TokenTypes.RBRACE
            ','  -> TokenTypes.COMMA
            ';'  -> TokenTypes.NEW_LINE
            '\n' -> TokenTypes.NEW_LINE
            '.'  -> TokenTypes.DOT
            '#'  -> TokenTypes.PREPROC
            else -> throw Exception("Unexpected char '$char' at $line:$col")
        }

        return Token(type = type, value = char.toString(), ln = line, col = col)
    }

    override fun nextIt(values: Array<String>, skip: Boolean): Boolean {
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

    override fun isIt(value: String, skip: Boolean): Boolean {
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

    override fun getIndent(skip: Boolean): String {
        val validIndentChars =
            "abcdefghijklmnopqrstuvwxyz" +
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "0123456789" +
            "_"

        var offset: Int = 0
        var indent: String = ""

        if (source[pos].isLetter()) {
            while (source[pos + offset] in validIndentChars) {
                indent += source[pos + offset]
                offset++
            }
        }

        if (skip) {
            repeat(offset) { advance() }
        }

        return indent
    }

    override fun getOperator(skip: Boolean): String {
        val validOperatorChars = "+-=,;(){}[]"

        var offset: Int = 0
        var operator: String = ""
        while (source[pos+offset] in validOperatorChars) {
            operator += source[pos+offset]
            offset++
        }

        if (skip) {
            repeat(offset) { advance() }
        }

        return operator
    }

    override fun isEOF(): Boolean {
        return (pos+1 > source.length)
    }

    override fun putToken(type: TokenTypes, move: () -> Unit) {
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

    override fun putToken(type: TokenTypes, buffer: TokenBuffer) {
        tokens.add(
            Token(
                type = type,
                value = buffer.buffer,
                ln = buffer.ln,
                col = buffer.col
            )
        )
    }


    override fun nextToken() {
        fsm.process()
    }
}