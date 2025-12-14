//package old_parser
//
//import tokens.Token
//import TokenTypes
//import TokenGroups
//import parser.Arg
//import parser.ArrayExpr
//import parser.Assign
//import parser.BinaryExpr
//import parser.Block
//import parser.Break
//import parser.CallExpr
//import parser.ConstDecl
//import parser.Continue
//import parser.Expr
//import parser.ForDecl
//import parser.FunDecl
//import parser.Include
//import parser.IndexExpr
//import parser.Literal
//import parser.LogicDecl
//import parser.LoopDecl
//import parser.Node
//import parser.Param
//import parser.PreProcDecl
//import parser.Program
//import parser.Range
//import parser.RepeatUntilDecl
//import parser.Return
//import parser.VarBinaryExpr
//import parser.VarDecl
//import parser.VarRef
//import parser.WhenDecl
//import parser.WhileDecl
//
//class Parser(val tokens: List<Token>) {
//    var pos: Int = 0
//
//    fun peek(_pos: Int=pos, offset: Int=0): Token? = tokens.getOrNull(_pos+offset)
//    fun advance(): Token? = tokens.getOrNull(pos++)
//    fun isEOF(): Boolean = pos >= tokens.size
//    fun back(): Token? = tokens.getOrNull(pos--)
//
//    fun makeAst(): Program {
//        val decls = mutableListOf<Node>()
//        while (!isEOF()) {
//            val node = parseStatement()
//            decls.add(node)
//        }
//        return Program(decls)
//    }
//
//    fun parseStatement(): Node {
//        return when (peek()?.type) {
//            TokenTypes.VAL, TokenTypes.VAR -> parseVarDecl()
//            TokenTypes.CONST -> parseConstDecl()
//            TokenTypes.KW_FUN -> parseFunDecl()
//            TokenTypes.KW_IF -> parseIf()
//            TokenTypes.KW_RETURN -> parseReturn()
//            TokenTypes.PP_INCLUDE -> parsePreProc()
//            TokenTypes.KW_WHEN -> parseWhen()
//            TokenTypes.KW_WHILE -> parseWhile()
//            TokenTypes.KW_REPEAT -> parseRepeatUntil()
//            TokenTypes.KW_LOOP -> parseLoop()
//            TokenTypes.KW_FOR -> parseFor()
//            TokenTypes.KW_BREAK -> parseBreak()
//            TokenTypes.KW_CONTINUE -> parseContinue()
//            TokenTypes.IDENT -> {
//                when (peek(offset = 1)?.type) {
//                    TokenTypes.EQ, TokenTypes.LBRACK -> parseAssign()
//                    TokenTypes.PLUSEQ,
//                    TokenTypes.MINUSEQ,
//                    TokenTypes.MULEQ,
//                    TokenTypes.DIVEQ,
//                    TokenTypes.MODEQ, -> parseVarBinaryExp()
//                    else -> parseCall()
//                }
//            }
//            else -> throw RuntimeException("Неожиданный токен: ${peek()}")
//        }
//    }
//
//    // Условные выражения
//    private fun parseIf(): LogicDecl {
//        expect(TokenTypes.KW_IF)
//        val condition = parseExpr()
//        val body = parseBlock(false)
//        val elifs = mutableListOf<LogicDecl>()
//
//        while (peek()?.type == TokenTypes.KW_ELIF) {
//            advance()
//            val elifCond = parseExpr()
//            val elifBody = parseBlock(false)
//            elifs.add(LogicDecl(TokenTypes.KW_ELIF, elifCond, elifBody))
//        }
//
//        var elseBlock: Block? = null
//        if (peek()?.type == TokenTypes.KW_ELSE) {
//            advance()
//            elseBlock = parseBlock(false)
//        }
//
//        return LogicDecl(TokenTypes.KW_IF, condition, body, elifs.ifEmpty { null }, elseBlock)
//    }
//
//    private fun parseWhile(): WhileDecl {
//        expect(TokenTypes.KW_WHILE)
//        val logic = parseExpr()
//        val body = parseBlock(false)
//        return WhileDecl(logic, body)
//    }
//
//    private fun parseRepeatUntil(): RepeatUntilDecl {
//        expect(TokenTypes.KW_REPEAT)
//        val body = parseBlock(false)
//        expect(TokenTypes.KW_UNTIL)
//        val logic = parseExpr()
//        return RepeatUntilDecl(logic, body)
//    }
//
//    private fun parseLoop(): LoopDecl {
//        expect(TokenTypes.KW_LOOP)
//        return LoopDecl(parseBlock())
//    }
//
//    private fun parseBreak(): Break {
//        expect(TokenTypes.KW_BREAK)
//        return Break()
//    }
//
//    private fun parseContinue(): Continue {
//        expect(TokenTypes.KW_CONTINUE)
//        return Continue()
//    }
//
//    private fun parseRange(): Range {
//        val startExpr = parseExpr()
//        expect(TokenTypes.RANGE)
//        val endExpr = parseExpr()
//        return Range(startExpr, endExpr, null)
//    }
//
//    private fun parseFor(): ForDecl {
//        expect(TokenTypes.KW_FOR)
//        expect(TokenTypes.LPAREN)
//
//        val variable = parseVarDecl()
//        expect(TokenTypes.KW_IN)
//
//        val range = parseRange()
//        range.name = variable.name
//
//        var stepExpr: Expr = Literal("1")
//        if (peek()?.type == TokenTypes.COMMA) {
//            advance()
//            stepExpr = parseExpr()
//        }
//
//        expect(TokenTypes.RPAREN)
//        val body = parseBlock(false)
//        return ForDecl(
//            init = variable,
//            range = range,
//            step = stepExpr,
//            body = body
//        )
//    }
//
//    private fun parseWhen(): WhenDecl {
//        expect(TokenTypes.KW_WHEN)
//
//        val variable: Expr? = expect(TokenTypes.LPAREN, soft = true)?.let {
//            val expr = parseExpr()
//            expect(TokenTypes.RPAREN)
//            expr
//        } ?: run {
//            back()
//            null
//        }
//
//        expect(TokenTypes.LBRACE)
//
//        val branches = mutableListOf<LogicDecl>()
//        var elseBlock: Block? = null
//        var isFirst = true
//
//        while (peek()?.type != TokenTypes.RBRACE && !isEOF()) {
//            val isElse = peek()?.type == TokenTypes.KW_ELSE
//            val conditions = mutableListOf<Expr>()
//
//            if (!isElse) {
//                do {
//                    val cond = parseExpr()
//
//                    conditions += if (variable != null) {
//                        if (cond is Literal) BinaryExpr(variable, "==", cond)
//                        else cond
//                    } else {
//                        cond
//                    }
//                } while (peek()?.type == TokenTypes.COMMA && advance() != null)
//            } else {
//                advance()
//            }
//
//            expect(TokenTypes.ARROW)
//
//            val body = when {
//                peek()?.type == TokenTypes.LBRACE -> {
//                    parseBlock(false)
//                }
//
//                variable is VarRef -> {
//                    Block(listOf(Assign(VarRef(variable.name), value = parseExpr())), false)
//                }
//
//                variable == null -> {
//                    Block(listOf(parseExpr()), false)
//                }
//
//                else -> {
//                    throw RuntimeException("Ожидался блок { ... } или присваивание после ->")
//                }
//            }
//
//            if (!isElse) {
//                val merged = conditions.reduceOrNull { a, b -> BinaryExpr(a, "||", b) }
//                    ?: throw RuntimeException("Пустое условие в when")
//
//                branches += LogicDecl(
//                    if (isFirst) TokenTypes.KW_IF else TokenTypes.KW_ELIF,
//                    merged,
//                    body
//                )
//            } else {
//                elseBlock = body
//                break
//            }
//
//            isFirst = false
//        }
//
//        expect(TokenTypes.RBRACE)
//        return WhenDecl(branches, elseBlock)
//    }
//
//    // Парсинг переменной
//    private fun parseVarDecl(): VarDecl {
//        val mut = when (advance()!!.type) {
//            TokenTypes.VAR -> true
//            TokenTypes.VAL -> false
//            else -> throw RuntimeException("Expected \"val\" or \"var\" but found ${peek()}")
//        }
//        val name = expect(TokenTypes.IDENT)!!.value
//        expect(TokenTypes.COL)
//
//        val isPointer = if (peek()?.type == TokenTypes.MUL) {
//            advance()
//            true
//        } else {
//            false
//        }
//        val type = expect(TokenGroups.VARTYPE)!!.type
//
//        val dimension: MutableList<Expr?> = mutableListOf()
//        while (peek()?.type == TokenTypes.LBRACK) {
//            advance()
//
//            if (peek()?.type == TokenTypes.RBRACK) {
//                dimension += null
//                advance()
//                continue
//            }
//            dimension += parseExpr()
//            expect(TokenTypes.RBRACK)
//        }
//
//        expect(TokenTypes.EQ)
//
//        if (peek()?.type == TokenTypes.QMARK) {
//            advance()
//            return VarDecl(mut, name, type, isNull = true, isPointer = isPointer, dimensions = dimension)
//        }
//
//        val value = parseExpr()
//        return VarDecl(mut, name, type, value, isPointer = isPointer, dimensions = dimension)
//    }
//
//    private fun parseConstDecl(): ConstDecl {
//        expect(TokenTypes.CONST)
//        val name = expect(TokenTypes.IDENT)!!.value
//        expect(TokenTypes.COL)
//        val type = expect(TokenGroups.VARTYPE)!!.type
//        expect(TokenTypes.EQ)
//        val value = parseExpr()
//        return ConstDecl(name, type, value)
//    }
//
//    // Парсинг функции
//    private fun parseFunDecl(): FunDecl {
//        expect(TokenTypes.KW_FUN)
//        val name = expect(TokenTypes.IDENT)!!.value
//        expect(TokenTypes.LPAREN)
//        val params = mutableListOf<Param>()
//        while (peek()?.type != TokenTypes.RPAREN) {
//            val pname = expect(TokenTypes.IDENT)!!.value
//            expect(TokenTypes.COL)
//            val ptype = expect(TokenGroups.VARTYPE)!!.type
//            val dimensions = parseDimensions()
//            var defaultValue: Literal? = null
//
//            if (peek()?.type == TokenTypes.EQ) {
//                advance()
//                val expr = parseExpr()
//
//                if (expr !is Literal) {
//                    throw RuntimeException("Единственное допустимое значение дефолтного значения аргумента функции - Литерал")
//                }
//
//                defaultValue = expr
//
//            }
//
//            if (peek()?.type == TokenTypes.COMMA) advance()
//
//            params.add(Param(pname, ptype, defaultValue, dimensions))
//        }
//
//        expect(TokenTypes.RPAREN)
//        var returnType = TokenTypes.KW_VOID
//        var returnPointerCount = 0
//        if (expect(TokenTypes.ARROW, soft = true) != null) {
//            returnType = expect(TokenGroups.VARTYPE)!!.type
//            while (peek()?.type == TokenTypes.MUL) {
//                returnPointerCount += 1
//                advance()
//            }
//        } else {
//            back()
//        }
//
//        val body = parseBlock()
//
//        return FunDecl(name, returnType, params, body, returnPointerCount)
//    }
//
//    private fun parseBlock(ownScopeStack: Boolean=true): Block {
//        expect(TokenTypes.LBRACE)
//        val stmts = mutableListOf<Node>()
//        while (peek()?.type != TokenTypes.RBRACE && !isEOF()) {
//            stmts.add(parseStatement())
//        }
//        expect(TokenTypes.RBRACE)
//        return Block(stmts, ownScopeStack)
//    }
//
//    private fun parseReturn(): Return {
//        expect(TokenTypes.KW_RETURN)
//        val expr = parseExpr()
//        return Return(expr)
//    }
//
//    private fun parseVarBinaryExp(): VarBinaryExpr {
//        val name = expect(TokenTypes.IDENT)!!.value
//        val opToken = advance()!!
//
//        val expr = parseExpr()
//
//        return when (opToken.type) {
//            TokenTypes.PLUSEQ -> VarBinaryExpr(VarRef(name), "+=", expr)
//            TokenTypes.MINUSEQ -> VarBinaryExpr(VarRef(name), "-=", expr)
//            TokenTypes.MULEQ -> VarBinaryExpr(VarRef(name), "*=", expr)
//            TokenTypes.DIVEQ -> VarBinaryExpr(VarRef(name), "/=", expr)
//            TokenTypes.MODEQ -> VarBinaryExpr(VarRef(name), "%=", expr)
//            else -> throw RuntimeException("Ожидался оператор присваивания, а встретился ${opToken.type}")
//        }
//    }
//
//    private fun parseAssign(): Assign {
//        val name = expect(TokenTypes.IDENT)!!.value
//        val dimensions = parseDimensions()
//
//        val nonNullDimensions = dimensions.filterNotNull()
//        if (nonNullDimensions.size != dimensions.size) {
//            throw RuntimeException("The index cannot be null")
//        }
//
//        expect(TokenTypes.EQ)
//        val value = parseExpr()
//
//        return Assign(VarRef(name), nonNullDimensions, value)
//    }
//
//    private fun parseArg(): Arg {
//        val name = expect(TokenTypes.IDENT)!!.value
//        expect(TokenTypes.EQ)
//        val expr = parseExpr()
//        return Arg(name, expr)
//    }
//
//    private fun parseCall(): CallExpr {
//        val name = expect(TokenTypes.IDENT)!!.value
//        expect(TokenTypes.LPAREN)
//        val args = mutableListOf<Expr>()
//        while (peek()?.type != TokenTypes.RPAREN) {
//            args.add(parseExpr())
//            if (peek()?.type == TokenTypes.COMMA) advance()
//        }
//        expect(TokenTypes.RPAREN)
//        return CallExpr(name, args)
//    }
//
//    private fun parseArray(): Expr {
//        val values = mutableListOf<Expr>()
//        while (peek()?.type != TokenTypes.RBRACE) {
//            values += parseExpr()
//            if (peek()?.type == TokenTypes.COMMA) advance()
//        }
//        advance()
//        return ArrayExpr(values)
//    }
//
//    // Выражения с приоритетами
//    private fun parseExpr(): Expr = parseOr()
//
//    private fun parseOr(): Expr {
//        var expr = parseAnd()
//        while (peek()?.type == TokenTypes.OR) {
//            val op = advance()!!.value
//            val right = parseAnd()
//            expr = BinaryExpr(expr, op, right)
//        }
//        return expr
//    }
//
//    private fun parseAnd(): Expr {
//        var expr = parseComparison()
//        while (peek()?.type == TokenTypes.AND) {
//            val op = advance()!!.value
//            val right = parseComparison()
//            expr = BinaryExpr(expr, op, right)
//        }
//        return expr
//    }
//
//    private fun parseComparison(): Expr {
//        var expr = parseAddSub()
//        while (peek()?.type in listOf(
//                TokenTypes.EQEQ, TokenTypes.BANGEQ,
//                TokenTypes.LT, TokenTypes.LTE,
//                TokenTypes.GT, TokenTypes.GTE
//            )
//        ) {
//            val op = advance()!!.value
//            val right = parseAddSub()
//            expr = BinaryExpr(expr, op, right)
//        }
//        return expr
//    }
//
//    private fun parseAddSub(): Expr {
//        var expr = parseMulDiv()
//        while (peek()?.type == TokenTypes.PLUS || peek()?.type == TokenTypes.MINUS) {
//            val op = advance()!!.value
//            val right = parseMulDiv()
//            expr = BinaryExpr(expr, op, right)
//        }
//        return expr
//    }
//
//    private fun parseMulDiv(): Expr {
//        var expr = parseUnary()
//        while (peek()?.type == TokenTypes.MUL || peek()?.type == TokenTypes.DIV || peek()?.type == TokenTypes.MOD) {
//            val op = advance()!!.value
//            val right = parseUnary()
//            expr = BinaryExpr(expr, op, right)
//        }
//        return expr
//    }
//
//    private fun parsePostfixUnary(): Expr {
//        var expr = parsePrimary()
//
//        val postfixUnaryTokens = arrayOf(TokenTypes.INC, TokenTypes.DEC)
//
//        while (peek()?.type in postfixUnaryTokens) {
//            val op = advance()!!.value
//            expr = BinaryExpr(expr, op, Literal(""))
//        }
//
//        return expr
//    }
//
//    private fun parseUnary(): Expr {
//        return when (peek()?.type) {
//            TokenTypes.NOT, TokenTypes.MINUS, TokenTypes.INC, TokenTypes.DEC, TokenTypes.LINK, TokenTypes.MUL -> {
//                val op = advance()!!.value
//                val right = parseUnary()
//                BinaryExpr(Literal(""), op, right)
//            }
//            else -> parsePostfixUnary()
//        }
//    }
//
//    private fun parseDimensions(): List<Expr?> {
//        val dimensions = mutableListOf<Expr?>()
//
//        while (peek()?.type == TokenTypes.LBRACK) {
//            advance()
//            dimensions += if (peek()?.type != TokenTypes.RBRACK) parseExpr()
//            else null
//            expect(TokenTypes.RBRACK)
//        }
//
//        return dimensions
//    }
//
//    private fun parseIdent(): Expr {
//        val token = peek(offset = -1)!!
//        val name = token.value
//        when (peek()?.type) {
//            TokenTypes.LPAREN -> {
//                advance()
//                val args = mutableListOf<Expr>()
//                while (peek()?.type != TokenTypes.RPAREN) {
//                    args.add(parseExpr())
//                    if (peek()?.type == TokenTypes.COMMA) advance()
//                }
//                expect(TokenTypes.RPAREN)
//                return CallExpr(name, args)
//            }
//            TokenTypes.EQ -> {
//                back()
//                return parseArg()
//            }
//            TokenTypes.LBRACK -> {
//                val dimensions = parseDimensions()
//                return IndexExpr(VarRef(token.value), dimensions)
//            }
//            else -> {
//                return VarRef(name)
//            }
//        }
//    }
//
//    private fun parsePrimary(): Expr {
//        val token = advance()!!
//
//        return when (token.type) {
//            TokenTypes.NUMBER -> {
//                var num = token.value
//                if (peek()?.type == TokenTypes.DOT) {
//                    expect(TokenTypes.DOT)
//                    val part2 = expect(TokenTypes.NUMBER)!!
//                    num += '.' + part2.value
//                }
//                Literal(num)
//            }
//            TokenTypes.LBRACE -> parseArray()
//            TokenTypes.KW_TRUE -> Literal("1")
//            TokenTypes.KW_FALSE -> Literal("0")
//            TokenTypes.STRING -> Literal("\"${token.value}\"")
//            TokenTypes.CHAR -> Literal("'${token.value}'")
//            TokenTypes.IDENT -> parseIdent()
//            TokenTypes.LPAREN -> {
//                val expr = parseExpr()
//                expect(TokenTypes.RPAREN)
//                expr
//            }
//            else -> throw RuntimeException("Неожиданный токен в выражении: $token")
//        }
//    }
//
//    private fun expect(type: TokenTypes, soft: Boolean = false): Token? {
//        val token = advance() ?: throw RuntimeException("Ожидался $type, но конец токенов")
//        if (token.type != type) {
//            if (!soft) throw RuntimeException("Ожидался $type, а встретилось ${token.type}")
//            else return null
//        }
//        return token
//    }
//
//    private fun expect(group: TokenGroups, soft: Boolean = false): Token? {
//        val token = advance() ?: throw RuntimeException("Ожидалась группа $group, но конец токенов")
//        if (token.group != group) {
//            if (!soft) throw RuntimeException("Ожидалась группа $group, а встретился ${token.type}")
//            else return null
//        }
//        return token
//    }
//
//    private fun expect(arrayTypes: Array<TokenTypes>, soft: Boolean = false): Token? {
//        val token = advance() ?: throw RuntimeException("Ожидалась группа $arrayTypes, но конец токенов")
//        if (token.type !in arrayTypes) {
//            if (!soft) throw RuntimeException("Ожидалась группа $arrayTypes, а встретился ${token.type}")
//            else return null
//        }
//        return token
//    }
//
//    // Фигня прочая
//    private fun parsePreProc(): PreProcDecl {
//        val token = expect(TokenGroups.PREPROC)
//
//        when (token!!.type) {
//            TokenTypes.PP_INCLUDE -> {
//                var token = peek()!!
//                if (token.type == TokenTypes.STRING) {
//                    token = expect(TokenTypes.STRING)!!
//                    var path = token.value
//                    val isLeftScript: Boolean = path.startsWith("@")
//
//                    if (isLeftScript) path = path.drop(1)
//
//                    return PreProcDecl(data = path, directive = Include(path = path, isLeftScript, false))
//                } else {
//                    expect(TokenTypes.LT)
//                    val includeNameToken = expect(TokenTypes.IDENT)!!
//                    val path = if (expect(TokenTypes.DOT, true) != null) {
//                        val includeExtToken = expect(TokenTypes.IDENT)
//                            ?: throw RuntimeException("Expected file extension after dot")
//                        "${includeNameToken.value}.${includeExtToken.value}"
//                    } else {
//                        back()
//                        includeNameToken.value
//                    }
//                    expect(TokenTypes.GT)
//                    return PreProcDecl(data = path, directive = Include(path = path, false, true))
//                }
//            }
//            else -> throw RuntimeException("Принят необрабатываемый тип токена")
//        }
//    }
//}
