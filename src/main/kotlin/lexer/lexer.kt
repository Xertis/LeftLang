package lexer

import TokenTypes
import tokens.Token
import scripts.utils.Fsm
import scripts.utils.TokenBuffer

class Lexer(override var source: String) : LexerInterface {
    override var pos: Int = 0
    override var col: Int = 1
    override var line: Int = 1
    override var tokens = mutableListOf<Token>()
    override var buffer = TokenBuffer(0, 0)

    init {
        source += " "
    }

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

    override fun nextIt(values: Array<String>, skip: Boolean, escape: Char?): Boolean {
        var offset = 0
        val sourceLength = source.length

        while (pos + offset <= sourceLength) {
            var matched: Int? = null

            for ((index, value) in values.withIndex()) {
                if (matched != null) {break}

                if (peek(pos + offset - 1) == escape) continue

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

    override fun isEOF(): Boolean {
        return (pos+1 > source.length)
    }

    override fun putToken(type: TokenTypes, move: () -> Unit, popLast: Boolean) {
        val curPos = pos
        val ln = line
        val column = col
        move()

        var code: String = source.substring(curPos, pos)

        if (popLast) {
            code = code.dropLast(1)
        }

        tokens.add(
            Token(
                type = type,
                value = code,
                ln = ln,
                col = column
            )
        )
    }

    override fun putToken(type: TokenTypes, buffer: TokenBuffer, popLast: Boolean) {
        if (popLast) {
            buffer.popRight()
        }
        tokens.add(
            Token(
                type = type,
                value = buffer.buffer,
                ln = buffer.ln,
                col = buffer.col
            )
        )
    }

    override fun toTokens() {
        while (!isEOF()) {
            fsm.process()
        }
    }
}