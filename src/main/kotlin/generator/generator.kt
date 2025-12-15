package generator

import StrToType
import TokenTypes
import parser.Arg
import parser.ArrayExpr
import parser.Assign
import parser.BinaryExpr
import parser.Block
import parser.Break
import parser.CallExpr
import parser.ConstDecl
import parser.Continue
import parser.Expr
import parser.ForDecl
import parser.FromInclude
import parser.FunDecl
import parser.Include
import parser.IndexExpr
import parser.Literal
import parser.LogicDecl
import parser.LoopDecl
import parser.Node
import parser.Program
import parser.Range
import parser.RepeatUntilDecl
import parser.Return
import parser.SingleRange
import parser.UnaryExpr
import parser.VarBinaryExpr
import parser.VarDecl
import parser.VarLink
import parser.VarRef
import parser.WhenDecl
import parser.WhileDecl

class Generator(val program: Program) {
    private fun isUnsigned(type: String): Boolean? {
        if (type !in StrToType) {
            return null
        }

        return when (StrToType[type]) {
            TokenTypes.KW_CHAR_UNSIGNED, TokenTypes.KW_SHORT_UNSIGNED, TokenTypes.KW_INT_UNSIGNED,
            TokenTypes.KW_LONG_UNSIGNED, TokenTypes.KW_HEAVY_UNSIGNED -> true

            TokenTypes.KW_CHAR, TokenTypes.KW_SHORT, TokenTypes.KW_INT,
            TokenTypes.KW_LONG, TokenTypes.KW_HEAVY -> false

            else -> null
        }
    }

    private fun left2Ctype(type: String): String {
        if (type !in StrToType) {
            return type
        }
        return when(StrToType[type]) {
            TokenTypes.KW_CHAR, TokenTypes.KW_CHAR_UNSIGNED -> "char"
            TokenTypes.KW_SHORT, TokenTypes.KW_SHORT_UNSIGNED -> "short"
            TokenTypes.KW_INT, TokenTypes.KW_INT_UNSIGNED -> "int"
            TokenTypes.KW_LONG, TokenTypes.KW_LONG_UNSIGNED -> "long"
            TokenTypes.KW_HEAVY, TokenTypes.KW_HEAVY_UNSIGNED -> "long long"
            TokenTypes.KW_F32 -> "float"
            TokenTypes.KW_F64 -> "double"
            TokenTypes.KW_BOOL -> "bool"
            TokenTypes.KW_VOID -> "void"

            TokenTypes.KW_U8 -> "uint8_t"
            TokenTypes.KW_U16 -> "uint16_t"
            TokenTypes.KW_U32 -> "uint32_t"
            TokenTypes.KW_U64 -> "uint64_t"
            TokenTypes.KW_UMAX -> "uintmax_t"

            TokenTypes.KW_I8 -> "int8_t"
            TokenTypes.KW_I16 -> "int16_t"
            TokenTypes.KW_I32 -> "int32_t"
            TokenTypes.KW_I64 -> "int64_t"
            TokenTypes.KW_IMAX -> "intmax_t"

            TokenTypes.KW_U8_FAST -> "uint_fast8_t"
            TokenTypes.KW_U16_FAST -> "uint_fast16_t"
            TokenTypes.KW_U32_FAST -> "uint_fast32_t"
            TokenTypes.KW_U64_FAST -> "uint_fast64_t"

            TokenTypes.KW_I8_FAST -> "int_fast8_t"
            TokenTypes.KW_I16_FAST -> "int_fast16_t"
            TokenTypes.KW_I32_FAST -> "int_fast32_t"
            TokenTypes.KW_I64_FAST -> "int_fast64_t"
            else -> throw RuntimeException("Unknown type: $type")
        }
    }

    fun findFunDecl(name: String, root: List<Node>): FunDecl? {
        for (decl in root) {
            when (decl) {
                is FunDecl -> if (decl.name == name) return decl
                else -> {}
            }
        }
        return null
    }

    private fun genFunc(decl: FunDecl, root: List<Node>): String {
        val params = mutableListOf<String>()

        for (param in decl.params) {
            val isUnsigned = isUnsigned(param.type)
            val sign = when (isUnsigned) {
                true -> "unsigned"
                false -> "signed"
                null -> ""
            }

            val base =
                "$sign ${left2Ctype(param.type)} ${param.name}${genDimensions(param.dimensions, root)}"

            val paramWithDefault =
                if (param.defaultValue != null) "$base = ${gen(param.defaultValue, root)}"
                else base

            params += paramWithDefault
        }

        val body = gen(decl.body, root)

        return "${left2Ctype(decl.returnType)} ${decl.name}" +
                params.joinToString(", ", "(", ")") +
                " {\n$body\n}\n"
    }

    private fun genElse(decl: Block, root: List<Node>): String {
        return "else {\n${gen(decl, root)}}"
    }

    private fun genLogic(decl: LogicDecl, root: List<Node>): String {
        val name = if (decl.type == TokenTypes.KW_IF) "if" else "else if"

        var fullLogicBlock = "$name (${gen(decl.logicExpr, root)}) {\n${gen(decl.body, root)}}"

        if (decl.middlewares != null) {
            for (ware in decl.middlewares) {
                fullLogicBlock += genLogic(ware, root)
            }
        }

        if (decl.elseWare != null) {
            fullLogicBlock += genElse(decl.elseWare, root)
        }

        return fullLogicBlock
    }

    private fun genWhen(decl: WhenDecl, root: List<Node>): String {
        val code = StringBuilder()

        for (ware in decl.middlewares) {
            code.append(genLogic(ware, root))
        }

        if (decl.elseWare != null) code.append(genElse(decl.elseWare, root))

        return code.toString()
    }

    private fun genWhile(decl: WhileDecl, root: List<Node>): String {
        var logic = gen(decl.expr, root)
        if (logic.first() != '(') logic = "($logic)"

        val body = gen(decl.body, root)
        return "while $logic {\n$body}\n"
    }

    private fun genRepeatUntil(decl: RepeatUntilDecl, root: List<Node>): String {
        var logic = gen(decl.expr, root)
        if (logic.first() != '(') logic = "($logic)"

        val body = gen(decl.body, root)
        return "do {\n$body} while $logic; \n"
    }

    private fun genFor(decl: ForDecl, root: List<Node>): String {
        // Писал не я и я его боюсь
        val params = decl.params
        val body = gen(decl.body, root)
        val stepExprStr = gen(decl.step, root)

        if (decl.range is ArrayExpr) {
            val arr = decl.range
            val tmpArrName = "__for_vals_${params.first().name}"
            val arrValuesStr = genArray(arr, root) // "{...}"
            val code = StringBuilder()

            // создаём временный массив значений
            val firstParamType = params.first().type
            val isUnsigned = isUnsigned(firstParamType)
            val sign = when (isUnsigned) {
                true -> "unsigned"
                false -> "signed"
                null -> ""
            }
            val ctype = left2Ctype(firstParamType)
            code.append("$sign $ctype $tmpArrName[] = $arrValuesStr;\n")

            // массив размеров (для каждого параметра один и тот же размер)
            val sizesList = (0 until params.size).joinToString(", ") { "sizeof($tmpArrName)/sizeof($tmpArrName[0])" }
            code.append("size_t __sizes_${tmpArrName}[] = { $sizesList };\n")

            // вычисляем общее количество комбинаций (произведение размеров)
            code.append("size_t __total_${tmpArrName} = 1;\n")
            code.append("for (size_t __j = 0; __j < sizeof(__sizes_${tmpArrName})/sizeof(__sizes_${tmpArrName}[0]); ++__j) __total_${tmpArrName} *= __sizes_${tmpArrName}[__j];\n")

            // один цикл по линейному индексу
            code.append("for (size_t __idx = 0; __idx < __total_${tmpArrName}; ++__idx) {\n")
            code.append("    size_t __tmp = __idx;\n")

            // для каждого параметра вычисляем свою координату (mod/div) и объявляем переменную
            for ((k, param) in params.withIndex()) {
                code.append("    size_t __i$k = __tmp % __sizes_${tmpArrName}[$k]; __tmp /= __sizes_${tmpArrName}[$k];\n")
                val pIsUnsigned = isUnsigned(param.type)
                val pSign = when (pIsUnsigned) {
                    true -> "unsigned"
                    false -> "signed"
                    null -> ""
                }
                val pCtype = left2Ctype(param.type)
                code.append("    $pSign $pCtype ${param.name} = $tmpArrName[__i$k];\n")
            }

            // тело
            code.append("    $body")
            code.append("}\n")

            return code.toString()
        }

        if (decl.range is Range || decl.range is SingleRange) {
            val rangeExpr = decl.range
            val code = StringBuilder()

            for ((level, param) in params.withIndex()) {
                val initVal = when {
                    param.defaultValue != null -> gen(param.defaultValue, root)
                    rangeExpr is SingleRange && rangeExpr.start != null -> gen(rangeExpr.start, root)
                    rangeExpr is Range && rangeExpr.ranges.isNotEmpty() && rangeExpr.ranges[0].start != null -> gen(rangeExpr.ranges[0].start!!, root)
                    else -> "0"
                }

                val pIsUnsigned = isUnsigned(param.type)
                val pSign = when (pIsUnsigned) {
                    true -> "unsigned"
                    false -> "signed"
                    null -> ""
                }
                val pCtype = left2Ctype(param.type)

                val cond = if (rangeExpr is Range) genRange(rangeExpr, root, param.name)
                else if (rangeExpr is SingleRange) genSingleRange(rangeExpr, root, param.name)
                else "1"

                code.append("for ($pSign $pCtype ${param.name} = $initVal; $cond; ${param.name} += $stepExprStr) {\n")
            }

            code.append(body)
            for (i in params.indices.reversed()) code.append("}\n")
            return code.toString()
        }

        // --- FALLBACK: range — произвольное выражение: делаем временный массив из одного элемента и применяем ту же линейную логику ---
        val fallbackTmp = "__for_vals_${params.first().name}_fallback"
        val firstParamTypeF = params.first().type
        val isUnsignedF = isUnsigned(firstParamTypeF)
        val signF = when (isUnsignedF) {
            true -> "unsigned"
            false -> "signed"
            null -> ""
        }
        val ctypeF = left2Ctype(firstParamTypeF)
        val fallbackCode = StringBuilder()
        fallbackCode.append("$signF $ctypeF $fallbackTmp[] = { ${gen(decl.range, root)} };\n")
        val sizesListF = (0 until params.size).joinToString(", ") { "sizeof($fallbackTmp)/sizeof($fallbackTmp[0])" }
        fallbackCode.append("size_t __sizes_$fallbackTmp[] = { $sizesListF };\n")
        fallbackCode.append("size_t __total_$fallbackTmp = 1;\n")
        fallbackCode.append("for (size_t __j = 0; __j < sizeof(__sizes_$fallbackTmp)/sizeof(__sizes_$fallbackTmp[0]); ++__j) __total_$fallbackTmp *= __sizes_$fallbackTmp[__j];\n")
        fallbackCode.append("for (size_t __idx = 0; __idx < __total_$fallbackTmp; ++__idx) {\n")
        fallbackCode.append("    size_t __tmp = __idx;\n")

        for ((k, param) in params.withIndex()) {
            fallbackCode.append("    size_t __i$k = __tmp % __sizes_$fallbackTmp[$k]; __tmp /= __sizes_$fallbackTmp[$k];\n")
            val pIsUnsigned = isUnsigned(param.type)
            val pSign = when (pIsUnsigned) {
                true -> "unsigned"
                false -> "signed"
                null -> ""
            }
            val pCtype = left2Ctype(param.type)
            fallbackCode.append("    $pSign $pCtype ${param.name} = $fallbackTmp[__i$k];\n")
        }

        fallbackCode.append("    $body")
        fallbackCode.append("}\n")
        return fallbackCode.toString()
    }


    private fun genLoop(decl: LoopDecl, root: List<Node>): String {
        return "for (;;) {\n${gen(decl.body, root)}};"
    }

    private fun genBlock(decl: Block, root: List<Node>): String {
        val code = StringBuilder()
        val blockRoot = if (decl.ownScopeStack) decl.statements else root
        for (dec in decl.statements) {
            code.append(gen(dec, blockRoot))
            when (dec) {
                is VarDecl, is CallExpr, is Return, is Assign, is VarBinaryExpr, is UnaryExpr, is BinaryExpr -> code.append(";\n")
                else -> code.append("\n")
            }
        }
        return code.toString()
    }

    private fun genDimensions(dimensions: List<Expr?>?, root: List<Node>): String {
        var dimensionsStr = ""
        if (dimensions == null) return ""
        for (dimension in dimensions) {
            val dimensionStrPart = if (dimension != null) gen(dimension, root) else ""
            dimensionsStr += "[$dimensionStrPart]"
        }

        return dimensionsStr
    }

    private fun genVar(decl: VarDecl, root: List<Node>): String {
        val isUnsigned = isUnsigned(decl.type)
        val sign = when (isUnsigned) {
            true -> "unsigned"
            false -> "signed"
            null -> ""
        }

        val dimensions = genDimensions(decl.dimensions, root)


        if (!decl.isNull) return "$sign ${left2Ctype(decl.type)} ${decl.name}$dimensions = ${gen(decl.value!!, root)}"
        return "$sign ${left2Ctype(decl.type)} ${decl.name}$dimensions"
    }

    private fun genConst(decl: ConstDecl, root: List<Node>): String {
        val isUnsigned = isUnsigned(decl.type)
        val sign = when (isUnsigned) {
            true -> "unsigned"
            false -> "signed"
            null -> ""
        }
        return "const $sign ${left2Ctype(decl.type)} ${decl.name} = ${gen(decl.value, root)}"
    }

    private fun genCall(decl: CallExpr, root: List<Node>): String {
        val funDecl = findFunDecl(decl.name, root) ?: findFunDecl(decl.name, program.decls)

        if (funDecl == null) {
            val argsStr = decl.args.joinToString(
                separator = ", ",
                prefix = "(",
                postfix = ")"
            ) { gen(it, root) }
            return "${decl.name}$argsStr"
        }

        val namedArgs = mutableMapOf<String, Expr>()
        val notNamedArgs = mutableListOf<Expr>()

        for (arg in decl.args) {
            when (arg) {
                is Arg -> namedArgs[arg.name] = arg.value
                else -> notNamedArgs += arg
            }
        }

        val finalArgs = mutableListOf<Expr>()
        var posIndex = 0

        for (param in funDecl.params) {
            finalArgs += when {
                posIndex < notNamedArgs.size -> notNamedArgs[posIndex++]
                namedArgs.containsKey(param.name) -> namedArgs[param.name]!!
                param.defaultValue != null -> param.defaultValue
                else -> throw RuntimeException("Передано неверное кол-во аргументов")
            }
        }

        val argsStr = finalArgs.joinToString(
            separator = ", ",
            prefix = "(",
            postfix = ")"
        ) { gen(it, root) }

        return "${decl.name}$argsStr"
    }

    private fun genInclude(decl: Include, root: List<Node>): String {
        return "#include \"${decl.path}\"\n"
    }

    private fun genFromInclude(decl: FromInclude, root: List<Node>): String {
        val include = gen(decl.include, root)
        var uses = ""
        for (use in decl.using) {
            uses += when (use.alias) {
                is String -> {
                    "auto& ${use.alias} = ${decl.namespace}::${use.name};\n"
                }

                null -> {
                    "using ${decl.namespace}::${use.name};\n"
                }
            }
        }

        return "$include\n$uses"
    }

    private fun genArray(decl: ArrayExpr, root: List<Node>): String {
        var array = "{"
        for ((index, expr) in decl.values.withIndex()) {
            array += if (index != 0)", ${gen(expr, root)}"
            else gen(expr, root)
        }

        return "$array}"
    }

    private fun genSingleRange(singleRange: SingleRange, root: List<Node>, name: String="0"): String {
        val start = if (singleRange.start != null) gen(singleRange.start, root) else null
        val end = if (singleRange.end != null) gen(singleRange.end, root) else null

        val startOp = if (singleRange.startIsStrong) ">" else ">="
        val endOp = if (singleRange.endIsStrong) "<" else "<="

        return when {
            singleRange.onlyStart -> "($name == $start)"
            start == null && end != null -> "($name $endOp $end)"
            start != null && end == null -> "($name $startOp $start)"
            start != null && end != null -> "($name $startOp $start && $name $endOp $end)"
            else -> "(1)"
        }
    }

    private fun genRange(decl: Range, root: List<Node>, name: String="0"): String {
        if (decl.ranges.isEmpty()) {
            return "false"
        }

        val conditions = decl.ranges.map { genSingleRange(it, root, name) }

        return if (conditions.size == 1) {
            conditions[0]
        } else {
            conditions.joinToString(separator = " || ", prefix = "(", postfix = ")")
        }
    }

    private fun genAssign(decl: Assign, root: List<Node>): String {
        val dimensions = genDimensions(decl.dimensions, root)
        return "${decl.target.name}$dimensions = ${gen(decl.value, root)}"
    }

    private fun gen(decl: Node, root: List<Node>): String {
        return when (decl) {
            is Assign -> genAssign(decl, root)
            is Block -> genBlock(decl, root)
            is ConstDecl -> genConst(decl, root)
            is BinaryExpr -> "(${gen(decl.left, root)} ${decl.op} ${gen(decl.right, root)})"
            is CallExpr -> genCall(decl, root)
            is Include -> genInclude(decl, root)
            is FromInclude -> genFromInclude(decl, root)
            is Literal -> decl.value
            is VarRef -> decl.name
            is FunDecl -> genFunc(decl, root)
            is Program -> throw RuntimeException("не реализован $decl")
            is Return -> "return ${gen(decl.value, root)}"
            is VarDecl -> genVar(decl, root)
            is LogicDecl -> genLogic(decl, root)
            is WhenDecl -> genWhen(decl, root)
            is Arg -> "${decl.name} = ${gen(decl.value, root)}"
            is VarBinaryExpr -> "${gen(decl.variable, root)} ${decl.op} ${gen(decl.expr, root)}"
            is VarLink -> "&${gen(decl.ref, root)}"
            is WhileDecl -> genWhile(decl, root)
            is ForDecl -> genFor(decl, root)
            is Range -> genRange(decl, root)
            is SingleRange -> genSingleRange(decl, root)
            is Break -> "break;"
            is Continue -> "continue;"
            is RepeatUntilDecl -> genRepeatUntil(decl, root)
            is ArrayExpr -> genArray(decl, root)
            is IndexExpr -> "${gen(decl.array, root)}${genDimensions(decl.dimensions, root)}"
            is LoopDecl -> genLoop(decl, root)
            is UnaryExpr -> if (decl.isPrefixed) "${decl.op}${gen(decl.value, root)}"
                            else "${gen(decl.value, root)}${decl.op}"
            else -> throw RuntimeException("Неизвестная декларация для генератора: $decl")
        }
    }

    fun startGen(): String {
        val code = StringBuilder()
        for (dec in program.decls) {
            code.append(gen(dec, program.decls))

            if (dec is VarDecl || dec is ConstDecl) code.append(";\n")
        }
        return code.toString()
    }
}
