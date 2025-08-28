package lexer

import scripts.utils.Fsm
import scripts.utils.TokenBuffer

fun bindStates(fsm: Fsm): Fsm {
    fsm.addMiddleware(TokenizerStates.DEFAULT,
        fun (lexer: LexerInterface): TokenizerStates? {
            val char = lexer.peek()
            lexer.buffer = TokenBuffer(lexer.line, lexer.col)

            // Скипаем ненужное
            when (char) {
                '#' -> {
                    lexer.putToken(TokenTypes.PREPROC, lexer::nextLine)
                    return null
                }

                ' ' -> {
                    lexer.advance(); return null
                }

                '\n' -> {
                    lexer.putToken(TokenTypes.NEW_LINE, lexer::advance)
                    return null
                }
            }

            if (lexer.isIt("//", true)) {
                lexer.nextLine(); return null
            }
            if (lexer.isIt("/*", true)) {
                lexer.nextIt(arrayOf("*/"), true); return null
            }

            // Определяем теперь то, какое состояние сделать

            when {
                char.isLetter() -> {
                    lexer.buffer.append(char)
                    return TokenizerStates.IN_IDENT
                }

                char.isDigit() -> {
                    lexer.buffer.append(char)
                    return TokenizerStates.IN_NUMBER
                }
            }

            return null
    })

    fsm.addMiddleware(TokenizerStates.IN_IDENT, fun (lexer: LexerInterface): TokenizerStates? {
        val ch = lexer.peek()
        if (ch.isLetterOrDigit() || ch == '_') {
            lexer.buffer.append(ch)
            lexer.advance()
            return null
        } else {
            lexer.putToken(TokenTypes.IDENT, lexer.buffer)
            return TokenizerStates.DEFAULT
        }
    })

    fsm.addMiddleware(TokenizerStates.IN_NUMBER, fun (lexer: LexerInterface): TokenizerStates? {
        val ch = lexer.peek()

        if (ch.isDigit()) {
            lexer.buffer.append(ch)
            lexer.advance()
            return null
        } else {
            lexer.putToken(TokenTypes.NUMBER, lexer.buffer)
            return TokenizerStates.DEFAULT
        }
    })

    return fsm
}