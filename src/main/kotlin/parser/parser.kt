package parser

import POSTFIXED_UNARY_TOKEN_TYPES
import PREFIXED_UNARY_TOKEN_TYPES
import tokens.Token
import TokenTypes
import TokenGroups
import TypeToStr

data class Middleware(val isStatement: Boolean = true, val ware: (Parser) -> Node?, val trigger: (Parser, TokenTypes) -> Boolean) {
    fun run(parser: Parser, decl: TokenTypes): Node? {
        return if (trigger(parser, decl)) ware(parser)
        else null
    }
}

class Parser(val tokens: List<Token>) {
    var pos: Int = 0

    val statements = mutableListOf<Middleware>()

    fun peek(_pos: Int = pos, offset: Int = 0, skipNewLine: Boolean = true): Token? {
        var currentOffset = 0

        for (i in _pos until tokens.size) {
            val token = tokens[i]
            if (!skipNewLine || token.type != TokenTypes.NEW_LINE) {
                if (currentOffset == offset) {
                    return token
                }
                currentOffset++
            }
        }
        return null
    }

    fun advance(skipNewLine: Boolean = true): Token? {
        while (pos < tokens.size) {
            val token = tokens[pos]
            pos++
            if (!skipNewLine || token.type != TokenTypes.NEW_LINE) {
                return token
            }
        }
        return null
    }

    fun isEOF(skipNewLine: Boolean = true): Boolean {
        var i = pos
        while (i < tokens.size) {
            if (!skipNewLine || tokens[i].type != TokenTypes.NEW_LINE) {
                return false
            }
            i++
        }
        return true
    }

    fun back(skipNewLine: Boolean = true): Token? {
        if (pos <= 0) return null

        var newPos = pos - 1
        while (newPos >= 0) {
            val token = tokens[newPos]
            if (!skipNewLine || token.type != TokenTypes.NEW_LINE) {
                break
            }
            newPos--
        }

        if (newPos < 0) return null

        pos = newPos
        return tokens[newPos]
    }

    init {
        bindStates(this)
    }

    fun makeAst(): Program {
        val decls = mutableListOf<Node>()
        //println(tokens)
        while (!isEOF()) {
            val node = parseStatement()
            if (node != null) decls.add(node)
        }
        return Program(decls)
    }

    fun expect(type: TokenTypes, soft: Boolean = false, skipNewLine: Boolean = true): Token? {
        val token = advance(skipNewLine) ?: throw RuntimeException("Ожидался $type, но конец токенов")
        if (token.type != type) {
            if (!soft) throw RuntimeException("Ожидался $type, а встретилось ${token.type}")
            else return null
        }
        return token
    }

    fun expect(group: TokenGroups, soft: Boolean = false, skipNewLine: Boolean = true): Token? {
        val token = advance(skipNewLine) ?: throw RuntimeException("Ожидалась группа $group, но конец токенов")
        if (token.group != group) {
            if (!soft) throw RuntimeException("Ожидалась группа $group, а встретился ${token.type}")
            else return null
        }
        return token
    }

    fun expect(group: Array<TokenTypes>, soft: Boolean = false, skipNewLine: Boolean = true): Token? {
        val token = advance(skipNewLine) ?: throw RuntimeException("Ожидалась группа $group, но конец токенов")
        if (token.type !in group) {
            if (!soft) throw RuntimeException("Ожидалась группа $group, а встретился ${token.type}")
            else return null
        }
        return token
    }

    fun parseStatement(): Node? {
        val type = peek()?.type ?: return null

        for (ware in statements) {
            val res = ware.run(this, type)
            if (res != null) {
                return res
            }
        }

        return parseExpr()
    }

    fun addStatement(ware: (Parser) -> Node?, trigger: (Parser, TokenTypes) -> Boolean) {
        statements += Middleware(true, ware, trigger)
    }

    fun parseExpr(): Expr = parseOr()

    private fun parseOr(): Expr = parseInfix(TokenTypes.OR) { left, op, right ->
        BinaryExpr(left, op, right)
    }

    private fun parseAnd(): Expr = parseInfix(TokenTypes.AND) { left, op, right ->
        BinaryExpr(left, op, right)
    }

    private fun parseComparison(): Expr {
        var expr = parseAddSub()

        while (peek(skipNewLine = false)?.type in listOf(
                TokenTypes.EQEQ, TokenTypes.BANGEQ,
                TokenTypes.LT, TokenTypes.LTE,
                TokenTypes.GT, TokenTypes.GTE
            )
        ) {
            val op = advance(skipNewLine = false)!!.value
            val right = parseAddSub()
            expr = BinaryExpr(expr, op, right)
        }
        return expr
    }

    private fun parseAddSub(): Expr {
        var expr = parseMulDiv()

        while (peek(skipNewLine = false)?.type == TokenTypes.PLUS ||
            peek(skipNewLine = false)?.type == TokenTypes.MINUS) {
            val op = advance(skipNewLine = false)!!.value
            val right = parseMulDiv()
            expr = BinaryExpr(expr, op, right)
        }
        return expr
    }

    private fun parseMulDiv(): Expr {
        var expr = parseUnary()

        while (peek(skipNewLine = false)?.type == TokenTypes.MUL ||
            peek(skipNewLine = false)?.type == TokenTypes.DIV ||
            peek(skipNewLine = false)?.type == TokenTypes.MOD) {
            val op = advance(skipNewLine = false)!!.value
            val right = parseUnary()
            expr = BinaryExpr(expr, op, right)
        }
        return expr
    }

    private fun parseUnary(): Expr {
        val nextToken = peek()


        return when (nextToken?.type) {
            in PREFIXED_UNARY_TOKEN_TYPES -> {
                val op = advance()!!.value
                val right = parseUnary()
                UnaryExpr(right, op, isPrefixed = true)
            }
            else -> parsePostfix()
        }
    }

    private fun parsePostfix(): Expr {
        var expr = parsePrimary()

        while (peek(skipNewLine = false)?.type in POSTFIXED_UNARY_TOKEN_TYPES) {
            val op = advance(skipNewLine = false)!!.value
            expr = UnaryExpr(expr, op, isPrefixed = false)
        }

        expr = parsePostfixSuffix(expr)

        return expr
    }

    private fun parsePostfixSuffix(base: Expr): Expr {
        var expr = base

        while (true) {
            when (peek(skipNewLine = false)?.type) {
                TokenTypes.LBRACK -> {
                    advance(skipNewLine = false)
                    val index = parseExpr()
                    expect(TokenTypes.RBRACK, skipNewLine = false)
                    expr = IndexExpr(expr, listOf(index))
                }
                TokenTypes.LPAREN -> {
                    advance(skipNewLine = true)
                    val args = mutableListOf<Expr>()
                    while (peek(skipNewLine = true)?.type != TokenTypes.RPAREN) {
                        val next = peek(skipNewLine = true)
                        val nextNext = peek(offset = 1, skipNewLine = true)
                        if (next?.type == TokenTypes.IDENT && nextNext?.type == TokenTypes.EQ) {
                            val name = advance(skipNewLine = true)!!.value
                            advance(skipNewLine = true)
                            val value = parseExpr()
                            args.add(Arg(name, value))
                        } else {
                            args.add(parseExpr())
                        }

                        if (peek(skipNewLine = true)?.type == TokenTypes.COMMA) advance(skipNewLine = true)
                    }
                    expect(TokenTypes.RPAREN, skipNewLine = true)
                    if (expr is VarRef) {
                        expr = CallExpr(expr.name, args)
                    } else {
                        throw RuntimeException("Вызов функции возможен только от имени функции")
                    }
                }
                else -> break
            }
        }

        return expr
    }

    private fun parsePrimary(): Expr {
        val token = advance() ?: throw RuntimeException("Неожиданный конец файла")

        return when (token.type) {
            TokenTypes.NUMBER -> {
                var num = token.value
                if (peek(skipNewLine = false)?.type == TokenTypes.DOT) {
                    advance(skipNewLine = false)
                    val part2 = expect(TokenTypes.NUMBER, skipNewLine = false)!!
                    num += '.' + part2.value
                }
                Literal(num)
            }
            TokenTypes.LBRACE -> {
                val values = mutableListOf<Expr>()
                while (peek(skipNewLine = false)?.type != TokenTypes.RBRACE) {
                    values.add(parseExpr())
                    if (peek(skipNewLine = false)?.type == TokenTypes.COMMA) advance(skipNewLine = false)
                }
                expect(TokenTypes.RBRACE, skipNewLine = false)
                ArrayExpr(values)
            }
            TokenTypes.KW_TRUE -> Literal("1")
            TokenTypes.KW_FALSE -> Literal("0")
            TokenTypes.STRING -> Literal("\"${token.value}\"")
            TokenTypes.CHAR -> Literal("'${token.value}'")
            TokenTypes.IDENT -> {
                VarRef(token.value)
            }
            TokenTypes.LPAREN -> {
                val expr = parseExpr()
                expect(TokenTypes.RPAREN, skipNewLine = false)
                expr
            }
            else -> throw RuntimeException("Неожиданный токен в выражении: $token")
        }
    }

    private fun <T> parseInfix(stopAt: TokenTypes, builder: (Expr, String, Expr) -> T): Expr where T : Expr {
        var left = when (stopAt) {
            TokenTypes.OR -> parseAnd()
            TokenTypes.AND -> parseComparison()
            else -> throw RuntimeException("Неизвестный инфиксный оператор")
        }
        
        while (peek(skipNewLine = false)?.type == stopAt) {
            val op = advance(skipNewLine = false)!!.value
            val right = when (stopAt) {
                TokenTypes.OR -> parseAnd()
                TokenTypes.AND -> parseComparison()
                else -> throw RuntimeException("Неизвестный инфиксный оператор")
            }
            left = builder(left, op, right)
        }
        return left
    }

    fun parseBlock(ownScopeStack: Boolean = true): Block {
        expect(TokenTypes.LBRACE)
        val stmts = mutableListOf<Node>()
        while (peek()?.type != TokenTypes.RBRACE && !isEOF()) {
            val stmt = parseStatement()
            if (stmt != null) {
                stmts.add(stmt)
            }
        }
        expect(TokenTypes.RBRACE)
        return Block(stmts, ownScopeStack)
    }

    fun parseDimensions(): List<Expr?> {
        val dimensions = mutableListOf<Expr?>()
        while (peek()?.type == TokenTypes.LBRACK) {
            advance()
            dimensions += if (peek()?.type != TokenTypes.RBRACK) parseExpr()
            else null
            expect(TokenTypes.RBRACK)
        }
        return dimensions
    }

    fun parseParams(stopToken: TokenTypes = TokenTypes.RPAREN, defaultType: TokenTypes? = null, defaultValue: Literal? = null): List<Param> {
        val params = mutableListOf<Param>()

        while (peek()?.type != stopToken) {
            val pname = expect(TokenTypes.IDENT)!!.value

            var ptype: String = TypeToStr[TokenTypes.KW_I32_FAST]!!
            val dimensions = mutableListOf<Expr?>()

            if (peek()?.type == TokenTypes.COL) {
                advance()
                val ptypeToken = expect(TokenGroups.VARTYPE)
                if (ptypeToken != null && ptypeToken.type in TypeToStr) {
                    ptype = TypeToStr[ptypeToken.type]!!
                } else {
                    throw RuntimeException("Неизвестный тип параметра: ${ptypeToken?.value}")
                }
                dimensions += parseDimensions()
            } else if (defaultType != null) {
                if (defaultType in TypeToStr) {
                    ptype = TypeToStr[defaultType]!!
                } else {
                    throw RuntimeException("Неизвестный дефолтный тип параметра: $defaultType")
                }
                dimensions += parseDimensions()
            }

            var paramDefault: Literal? = defaultValue
            if (peek()?.type == TokenTypes.EQ) {
                advance()
                val expr = parseExpr()
                if (expr !is Literal) {
                    throw RuntimeException("Значение по умолчанию должно быть литералом")
                }
                paramDefault = expr
            }

            if (peek()?.type == TokenTypes.COMMA) advance()

            params.add(Param(pname, ptype, paramDefault, dimensions))
        }

        return params
    }


    fun parseRange(): Range {
        val ranges = mutableListOf<SingleRange>()

        ranges += parseSingleRange()

        while (peek()?.type == TokenTypes.PLUS) {
            advance()
            ranges += parseSingleRange()
        }

        return Range(ranges)
    }

    fun parseSingleRange(): List<SingleRange> {
        return when (peek()?.type) {
            TokenTypes.LBRACE -> {
                advance()
                val values = mutableListOf<Expr>()

                while (peek()?.type != TokenTypes.RBRACE) {
                    val expr = parseExpr()
                    values.add(expr)

                    if (peek()?.type == TokenTypes.SEMI) {
                        advance()
                    } else if (peek()?.type != TokenTypes.RBRACE) {
                        throw RuntimeException("Ожидается ; или } в перечислении значений")
                    }
                }

                expect(TokenTypes.RBRACE)

                val singleRanges = mutableListOf<SingleRange>()

                for (value in values) {
                    singleRanges += SingleRange(
                        start = value,
                        startIsStrong = true,
                        end = null,
                        endIsStrong = false,
                        onlyStart = true
                    )
                }

                singleRanges
            }

            TokenTypes.LBRACK, TokenTypes.LPAREN -> {
                val startBracket = advance()!!.type
                val startIsStrong = startBracket == TokenTypes.LPAREN

                val start: Expr?
                val startToken = peek()

                if (startToken?.type == TokenTypes.MINUS) {
                    val nextToken = peek(offset = 1)
                    if (nextToken?.type == TokenTypes.SEMI) {
                        advance()
                        start = null
                    } else {
                        start = parseExpr()
                    }
                } else if (startToken?.type == TokenTypes.SEMI) {
                    start = null
                } else {
                    start = parseExpr()
                }

                expect(TokenTypes.SEMI)

                val end: Expr?
                val endToken = peek()

                if (endToken?.type == TokenTypes.PLUS) {
                    val nextToken = peek(offset = 1)
                    if (nextToken?.type == TokenTypes.RBRACK || nextToken?.type == TokenTypes.RPAREN) {
                        advance()
                        end = null
                    } else {
                        end = parseExpr()
                    }
                } else if (endToken?.type == TokenTypes.RBRACK || endToken?.type == TokenTypes.RPAREN) {
                    end = null
                } else {
                    end = parseExpr()
                }

                val endBracket = expect(arrayOf(TokenTypes.RBRACK, TokenTypes.RPAREN))!!.type
                val endIsStrong = endBracket == TokenTypes.RPAREN

                listOf(SingleRange(
                    start = start,
                    startIsStrong = startIsStrong,
                    end = end,
                    endIsStrong = endIsStrong,
                    onlyStart = false
                ))
            }

            else -> throw RuntimeException("Ожидается диапазон: {value}, [a;b], (a;b) или подобное")
        }
    }
}
