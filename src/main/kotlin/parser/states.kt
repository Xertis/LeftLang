package parser

import OPEQ_TOKEN_TYPES
import VALID_VARTYPE_GROUP_TOKEN_TYPES
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin


fun bindStates(parser: Parser) {

    // -- Переменные --
    parser.addStatement({ p ->
        val mut = when (p.advance()!!.type) {
            TokenTypes.VAR -> true
            TokenTypes.VAL -> false
            else -> throw RuntimeException("Expected val or var")
        }
        val name = p.expect(TokenTypes.IDENT)!!.value
        p.expect(TokenTypes.COL)

        val type = p.expect(arrayOf(TokenTypes.IDENT) + VALID_VARTYPE_GROUP_TOKEN_TYPES)!!.value

        p.expect(TokenTypes.EQ)

        if (p.peek()?.type == TokenTypes.QMARK) {
            p.advance()
            VarDecl(mut, name, type, isNull = true)
        } else {
            val value = p.parseExpr()
            VarDecl(mut, name, type, value)
        }
    }) { _, t -> t ==TokenTypes.VAL || t == TokenTypes.VAR }

    parser.addStatement({ p ->
        p.expect(TokenTypes.CONST)
        val name = p.expect(TokenTypes.IDENT)!!.value
        p.expect(TokenTypes.COL)
        val type = p.expect(TokenGroups.VARTYPE)!!.value
        p.expect(TokenTypes.EQ)
        val value = p.parseExpr()
        ConstDecl(name, type, value)
    }) { _, t -> t == TokenTypes.CONST }

    parser.addStatement({ p ->
        val name = p.expect(TokenTypes.IDENT)!!.value
        val dimensions = p.parseDimensions()

        p.expect(TokenTypes.EQ)
        val value = p.parseExpr()

        Assign(VarRef(name), dimensions.filterNotNull(), value)
    }) { p, t ->
        t == TokenTypes.IDENT &&
                p.peek(offset = 1)?.type == TokenTypes.EQ
    }

    // -- Операции --
    parser.addStatement({ p ->
        val name = p.expect(TokenTypes.IDENT)!!.value
        val opToken = p.advance()!!

        val expr = p.parseExpr()

        when (opToken.type) {
            TokenTypes.PLUSEQ -> VarBinaryExpr(VarRef(name), "+=", expr)
            TokenTypes.MINUSEQ -> VarBinaryExpr(VarRef(name), "-=", expr)
            TokenTypes.MULEQ -> VarBinaryExpr(VarRef(name), "*=", expr)
            TokenTypes.DIVEQ -> VarBinaryExpr(VarRef(name), "/=", expr)
            TokenTypes.MODEQ -> VarBinaryExpr(VarRef(name), "%=", expr)
            else -> throw RuntimeException("Ожидался оператор присваивания")
        }
    }) { p, t ->
        t == TokenTypes.IDENT &&
                p.peek(offset = 1)?.type in OPEQ_TOKEN_TYPES
    }

    // -- Функции --

    parser.addStatement({ p ->
        p.expect(TokenTypes.KW_FUN)
        val name = p.expect(TokenTypes.IDENT)!!.value
        p.expect(TokenTypes.LPAREN)

        val params = p.parseParams(TokenTypes.RPAREN)

        p.expect(TokenTypes.RPAREN)

        var returnType = "Void"

        if (p.peek()?.type == TokenTypes.ARROW) {
            p.advance()
            returnType = p.expect(TokenGroups.VARTYPE)!!.value
        }

        val body = p.parseBlock()
        FunDecl(name, returnType, params, body)
    }) { _, t -> t == TokenTypes.KW_FUN }

    parser.addStatement({ p ->
        p.expect(TokenTypes.KW_RETURN)
        val expr = p.parseExpr()
        Return(expr)
    }) { _, t -> t == TokenTypes.KW_RETURN }

    // -- Лог-выражения --

    parser.addStatement({ p ->
        p.expect(TokenTypes.KW_IF)
        val condition = p.parseExpr()
        val body = p.parseBlock(false)

        val elifs = mutableListOf<LogicDecl>()
        while (p.peek()?.type == TokenTypes.KW_ELIF) {
            p.advance()
            val elifCond = p.parseExpr()
            val elifBody = p.parseBlock(false)
            elifs.add(LogicDecl(TokenTypes.KW_ELIF, elifCond, elifBody))
        }

        var elseBlock: Block? = null
        if (p.peek()?.type == TokenTypes.KW_ELSE) {
            p.advance()
            elseBlock = p.parseBlock(false)
        }

        LogicDecl(TokenTypes.KW_IF, condition, body, elifs.ifEmpty { null }, elseBlock)
    }) { _, t -> t == TokenTypes.KW_IF }

    // -- Циклы --

    parser.addStatement({ p ->
        p.expect(TokenTypes.KW_WHILE)
        val logic = p.parseExpr()
        val body = p.parseBlock(false)
        WhileDecl(logic, body)
    }) { _, t -> t == TokenTypes.KW_WHILE }

    parser.addStatement({ p ->
        p.expect(TokenTypes.KW_LOOP)
        val body = p.parseBlock(false)
        LoopDecl(body)
    }) { _, t -> t == TokenTypes.KW_LOOP }

    parser.addStatement({ p ->
        p.expect(TokenTypes.KW_FOR)
        val params = p.parseParams(
            stopToken = TokenTypes.KW_IN,
            defaultType = TokenTypes.KW_I32_FAST,
            defaultValue = Literal("")
        ).toMutableList()

        p.expect(TokenTypes.KW_IN)

        val ranges = p.parseRange()
        var defaultValue = Int.MAX_VALUE
        var value = 0
        for (singleRange in ranges.ranges) {
            if (singleRange.start != null) {
                when (singleRange.start) {
                    is Literal -> {
                        value = singleRange.start.value.toDouble().toInt()
                    }
                    is UnaryExpr if singleRange.start.value is Literal && singleRange.start.isPrefixed -> {
                        value = "${singleRange.start.op}${singleRange.start.value.value}".toDouble().toInt()
                    }
                    else -> {}
                }
            }

            if (singleRange.startIsStrong) value += 1
            defaultValue = min(defaultValue, value)
        }

        for ((index, param) in params.withIndex()) {
            if (param.defaultValue == Literal("")) {
                params[index] = Param(
                    name = param.name,
                    type = param.type,
                    defaultValue = Literal(defaultValue.toString()),
                    dimensions = param.dimensions
                )
            }
        }

        val body = p.parseBlock()

        ForDecl(
            params,
            ranges,
            Literal("1"),
            body
        )
    }) { _, t -> t == TokenTypes.KW_FOR }

    parser.addStatement({ p ->
        val type = p.expect(arrayOf(TokenTypes.KW_BREAK, TokenTypes.KW_CONTINUE))!!.type

        when (type) {
            TokenTypes.KW_BREAK -> Break()
            else -> Continue()
        }
    }) { _, t -> t == TokenTypes.KW_BREAK || t == TokenTypes.KW_CONTINUE }

    // -- Системное --

    parser.addStatement({ p ->
        p.expect(TokenTypes.INCLUDE)
        val path = p.expect(TokenTypes.STRING)!!.value

        Include(path, isLeft = false)
    }) { _, t -> t == TokenTypes.INCLUDE }
}