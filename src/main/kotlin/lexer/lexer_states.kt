package lexer

import scripts.utils.Fsm
import scripts.utils.TokenBuffer
import TokenTypes
import TokenizerStates

const val VALID_OPERATOR_SYMBOLS = "-+/*%=<>&|!?"
const val VALID_DELIMETER_SYMBOLS = ":;(){}[].,"

fun bindStates(fsm: Fsm): Fsm {
    fsm.addMiddleware(TokenizerStates.DEFAULT,
        fun (lexer: LexerInterface): TokenizerStates? {
            val char = lexer.peek()
            lexer.buffer = TokenBuffer(lexer.line, lexer.col)

            // Скипаем ненужное
            when (char) {

                ' ' -> {
                    lexer.advance(); return null
                }

                '\n' -> {
                    // lexer.putToken(TokenTypes.NEW_LINE, lexer::advance)
                    lexer.advance(); return null
                }
            }

            if (lexer.isIt("//", true)) {
                lexer.nextLine(); return null
            }
            if (lexer.isIt("/*", true)) {
                lexer.nextIt(arrayOf("*/"), true); return null
            }

            if (lexer.isIt("\"", true)) {
                lexer.putToken(TokenTypes.STRING, fun () {
                    lexer.nextIt(arrayOf("\""), true, escape = '\\')
                }, true)
                return null
            }

            if (lexer.isIt("'", true)) {
                lexer.putToken(TokenTypes.CHAR, fun () {
                    lexer.nextIt(arrayOf("'"), true, escape = '\\')
                }, true)
                return null
            }

            // Определяем теперь то, какое состояние сделать

            when {
                char.isLetter() -> {
                    return TokenizerStates.IN_IDENT
                }

                char.isDigit() -> {
                    return TokenizerStates.IN_NUMBER
                }

                char in VALID_OPERATOR_SYMBOLS -> {
                    return TokenizerStates.IN_OPERATOR
                }

                char in VALID_DELIMETER_SYMBOLS -> {
                    return TokenizerStates.IN_DELIMETER
                }

                char == '#' -> {
                    return TokenizerStates.IN_PREPROC
                }
            }

            lexer.advance()
            return null
    })

    fsm.addMiddleware(TokenizerStates.IN_IDENT, fun (lexer: LexerInterface): TokenizerStates? {
        val ch = lexer.peek()
        if (ch.isLetterOrDigit() || ch == '_') {
            lexer.buffer.append(ch)
            lexer.advance()
            return null
        } else {
            when (lexer.buffer.get()) {
                "val" -> lexer.putToken(TokenTypes.VAL, lexer.buffer)
                "var" -> lexer.putToken(TokenTypes.VAR, lexer.buffer)
                "const" -> lexer.putToken(TokenTypes.CONST, lexer.buffer)

                "u8" -> lexer.putToken(TokenTypes.KW_U8, lexer.buffer)
                "u16" -> lexer.putToken(TokenTypes.KW_U16, lexer.buffer)
                "u32" -> lexer.putToken(TokenTypes.KW_U32, lexer.buffer)
                "u64" -> lexer.putToken(TokenTypes.KW_U64, lexer.buffer)
                "umax" -> lexer.putToken(TokenTypes.KW_UMAX, lexer.buffer)

                "i8" -> lexer.putToken(TokenTypes.KW_I8, lexer.buffer)
                "i16" -> lexer.putToken(TokenTypes.KW_I16, lexer.buffer)
                "i32" -> lexer.putToken(TokenTypes.KW_I32, lexer.buffer)
                "i64" -> lexer.putToken(TokenTypes.KW_I64, lexer.buffer)
                "imax" -> lexer.putToken(TokenTypes.KW_IMAX, lexer.buffer)

                "FastU8" -> lexer.putToken(TokenTypes.KW_U8_FAST, lexer.buffer)
                "FastU16" -> lexer.putToken(TokenTypes.KW_U16_FAST, lexer.buffer)
                "FastU32" -> lexer.putToken(TokenTypes.KW_U32_FAST, lexer.buffer)
                "FastU64" -> lexer.putToken(TokenTypes.KW_U64_FAST, lexer.buffer)

                "FastI8" -> lexer.putToken(TokenTypes.KW_I8_FAST, lexer.buffer)
                "FastI16" -> lexer.putToken(TokenTypes.KW_I16_FAST, lexer.buffer)
                "FastI32" -> lexer.putToken(TokenTypes.KW_I32_FAST, lexer.buffer)
                "FastI64" -> lexer.putToken(TokenTypes.KW_I64_FAST, lexer.buffer)

                "char" -> lexer.putToken(TokenTypes.KW_CHAR, lexer.buffer)
                "Char" -> lexer.putToken(TokenTypes.KW_CHAR_UNSIGNED, lexer.buffer)
                "short" -> lexer.putToken(TokenTypes.KW_SHORT, lexer.buffer)
                "Short" -> lexer.putToken(TokenTypes.KW_SHORT_UNSIGNED, lexer.buffer)
                "int" -> lexer.putToken(TokenTypes.KW_INT, lexer.buffer)
                "Int" -> lexer.putToken(TokenTypes.KW_INT_UNSIGNED, lexer.buffer)
                "long" -> lexer.putToken(TokenTypes.KW_LONG, lexer.buffer)
                "Long" -> lexer.putToken(TokenTypes.KW_LONG_UNSIGNED, lexer.buffer)
                "heavy" -> lexer.putToken(TokenTypes.KW_HEAVY, lexer.buffer)
                "Heavy" -> lexer.putToken(TokenTypes.KW_HEAVY_UNSIGNED, lexer.buffer)

                "f32" -> lexer.putToken(TokenTypes.KW_F32, lexer.buffer)
                "f64" -> lexer.putToken(TokenTypes.KW_F64, lexer.buffer)

                "String" -> lexer.putToken(TokenTypes.KW_STRING, lexer.buffer)
                "Bool" -> lexer.putToken(TokenTypes.KW_BOOL, lexer.buffer)
                "Void" -> lexer.putToken(TokenTypes.KW_VOID, lexer.buffer)

                "if" -> lexer.putToken(TokenTypes.KW_IF, lexer.buffer)
                "elif" -> lexer.putToken(TokenTypes.KW_ELIF, lexer.buffer)
                "else" -> lexer.putToken(TokenTypes.KW_ELSE, lexer.buffer)
                "when" -> lexer.putToken(TokenTypes.KW_WHEN, lexer.buffer)

                "for" -> lexer.putToken(TokenTypes.KW_FOR, lexer.buffer)
                "while" -> lexer.putToken(TokenTypes.KW_WHILE, lexer.buffer)

                "repeat" -> lexer.putToken(TokenTypes.KW_REPEAT, lexer.buffer)
                "until" -> lexer.putToken(TokenTypes.KW_UNTIL, lexer.buffer)

                "break" -> lexer.putToken(TokenTypes.KW_BREAK, lexer.buffer)
                "continue" -> lexer.putToken(TokenTypes.KW_CONTINUE, lexer.buffer)

                "in" -> lexer.putToken(TokenTypes.KW_IN, lexer.buffer)

                "true" -> lexer.putToken(TokenTypes.KW_TRUE, lexer.buffer)
                "false" -> lexer.putToken(TokenTypes.KW_FALSE, lexer.buffer)

                "fun" -> lexer.putToken(TokenTypes.KW_FUN, lexer.buffer)
                "return" -> lexer.putToken(TokenTypes.KW_RETURN, lexer.buffer)

                else -> lexer.putToken(TokenTypes.IDENT, lexer.buffer)
            }
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

    fsm.addMiddleware(TokenizerStates.IN_OPERATOR, fun (lexer: LexerInterface): TokenizerStates? {
        val ch = lexer.peek()

        if (lexer.buffer.isEmpty()) {
            lexer.buffer.append(ch)
            lexer.advance()
            return null
        }

        val first = lexer.buffer.get()
        val combined = first + ch

        val doubleOps = setOf(
            "++", "--", "+=", "-=", "*=", "/=", "%=",
            "<=", ">=", "==", "!=", "||", "&&", "->"
        )

        if (combined in doubleOps) {
            lexer.buffer.append(ch)
            lexer.advance()
            when (combined) {
                "++" -> lexer.putToken(TokenTypes.INC, lexer.buffer)
                "--" -> lexer.putToken(TokenTypes.DEC, lexer.buffer)
                "+=" -> lexer.putToken(TokenTypes.PLUSEQ, lexer.buffer)
                "-=" -> lexer.putToken(TokenTypes.MINUSEQ, lexer.buffer)
                "*=" -> lexer.putToken(TokenTypes.MULEQ, lexer.buffer)
                "/=" -> lexer.putToken(TokenTypes.DIVEQ, lexer.buffer)
                "%=" -> lexer.putToken(TokenTypes.MODEQ, lexer.buffer)
                "<=" -> lexer.putToken(TokenTypes.LTE, lexer.buffer)
                ">=" -> lexer.putToken(TokenTypes.GTE, lexer.buffer)
                "==" -> lexer.putToken(TokenTypes.EQEQ, lexer.buffer)
                "!=" -> lexer.putToken(TokenTypes.BANGEQ, lexer.buffer)
                "||" -> lexer.putToken(TokenTypes.OR, lexer.buffer)
                "&&" -> lexer.putToken(TokenTypes.AND, lexer.buffer)
                "->" -> lexer.putToken(TokenTypes.ARROW, lexer.buffer)
            }
            return TokenizerStates.DEFAULT
        }

        when (first) {
            "+" -> lexer.putToken(TokenTypes.PLUS, lexer.buffer)
            "-" -> lexer.putToken(TokenTypes.MINUS, lexer.buffer)
            "*" -> lexer.putToken(TokenTypes.MUL, lexer.buffer)
            "/" -> lexer.putToken(TokenTypes.DIV, lexer.buffer)
            "%" -> lexer.putToken(TokenTypes.MOD, lexer.buffer)
            "<" -> lexer.putToken(TokenTypes.LT, lexer.buffer)
            ">" -> lexer.putToken(TokenTypes.GT, lexer.buffer)
            "=" -> lexer.putToken(TokenTypes.EQ, lexer.buffer)
            "!" -> lexer.putToken(TokenTypes.NOT, lexer.buffer)
            "&" -> lexer.putToken(TokenTypes.LINK, lexer.buffer)
            "?" -> lexer.putToken(TokenTypes.QMARK, lexer.buffer)
        }

        if (ch in VALID_OPERATOR_SYMBOLS) {
            lexer.buffer = TokenBuffer(lexer.line, lexer.col)
            return TokenizerStates.IN_OPERATOR
        }

        return TokenizerStates.DEFAULT
    })


    fsm.addMiddleware(TokenizerStates.IN_DELIMETER, fun (lexer: LexerInterface): TokenizerStates? {
        val ch = lexer.peek()

        if (ch in VALID_DELIMETER_SYMBOLS &&
            (lexer.buffer.isEmpty() || (lexer.buffer.get() == "." && ch == '.'))
        ) {
            lexer.buffer.append(ch)
            lexer.advance()
            return null
        } else {
            when (lexer.buffer.get()) {
                ":" -> lexer.putToken(TokenTypes.COL, lexer.buffer)
                "." -> lexer.putToken(TokenTypes.DOT, lexer.buffer)
                ";" -> lexer.putToken(TokenTypes.SEMI, lexer.buffer)
                "," -> lexer.putToken(TokenTypes.COMMA, lexer.buffer)
                "(" -> lexer.putToken(TokenTypes.LPAREN, lexer.buffer)
                ")" -> lexer.putToken(TokenTypes.RPAREN, lexer.buffer)
                "{" -> lexer.putToken(TokenTypes.LBRACE, lexer.buffer)
                "}" -> lexer.putToken(TokenTypes.RBRACE, lexer.buffer)
                "[" -> lexer.putToken(TokenTypes.LBRACK, lexer.buffer)
                "]" -> lexer.putToken(TokenTypes.RBRACK, lexer.buffer)
                ".." -> lexer.putToken(TokenTypes.RANGE, lexer.buffer)
            }

            return TokenizerStates.DEFAULT
        }
    })

    fsm.addMiddleware(TokenizerStates.IN_PREPROC, fun (lexer: LexerInterface): TokenizerStates? {
        lexer.advance()

        when {
            lexer.isIt("include") -> lexer.putToken(TokenTypes.PP_INCLUDE, fun() {
                lexer.isIt("include", true)
            })
        }

        return TokenizerStates.DEFAULT
    })

    return fsm
}