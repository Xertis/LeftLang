package generator

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
import parser.FunDecl
import parser.Include
import parser.IndexExpr
import parser.Literal
import parser.LogicDecl
import parser.Node
import parser.PreProcDecl
import parser.Program
import parser.Range
import parser.RepeatUntilDecl
import parser.Return
import parser.VarBinaryExpr
import parser.VarDecl
import parser.VarLink
import parser.VarRef
import parser.WhenDecl
import parser.WhileDecl

class Generator(val program: Program) {
    private fun isUnsigned(type: TokenTypes): Boolean? {
        return when (type) {
            TokenTypes.KW_CHAR_UNSIGNED, TokenTypes.KW_SHORT_UNSIGNED, TokenTypes.KW_INT_UNSIGNED,
            TokenTypes.KW_LONG_UNSIGNED, TokenTypes.KW_HEAVY_UNSIGNED -> true

            TokenTypes.KW_CHAR, TokenTypes.KW_SHORT, TokenTypes.KW_INT,
            TokenTypes.KW_LONG, TokenTypes.KW_HEAVY -> false

            else -> null
        }
    }

    private fun left2Ctype(type: TokenTypes): String {
        return when(type) {
            TokenTypes.KW_CHAR, TokenTypes.KW_CHAR_UNSIGNED -> "char"
            TokenTypes.KW_SHORT, TokenTypes.KW_SHORT_UNSIGNED -> "short"
            TokenTypes.KW_INT, TokenTypes.KW_INT_UNSIGNED -> "int"
            TokenTypes.KW_LONG, TokenTypes.KW_LONG_UNSIGNED -> "long"
            TokenTypes.KW_HEAVY, TokenTypes.KW_HEAVY_UNSIGNED -> "long long"
            TokenTypes.KW_F32 -> "float"
            TokenTypes.KW_F64 -> "double"
            TokenTypes.KW_BOOL -> "_Bool"
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
            params += "$sign ${left2Ctype(param.type)} ${param.name}${genDimensions(param.dimensions, root)}"
        }
        val body = gen(decl.body, root)
        val pointers = "*".repeat(decl.returnPointerCount)
        return "${left2Ctype(decl.returnType)}$pointers ${decl.name}${params.joinToString(
            separator = ",",
            prefix = "(",
            postfix = ")"
        )} {\n$body\n}\n"
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
        val init = gen(decl.init, root)
        val range = gen(decl.range, root)
        val step = gen(decl.step, root)
        val body = gen(decl.body, root)
        return "for ($init; $range; ${decl.init.name} += ($step)) {\n$body}\n"
    }

    private fun genBlock(decl: Block, root: List<Node>): String {
        val code = StringBuilder()
        val blockRoot = if (decl.ownScopeStack) decl.statements else root
        for (dec in decl.statements) {
            code.append(gen(dec, blockRoot))
            when (dec) {
                is VarDecl, is CallExpr, is Return, is Assign, is VarBinaryExpr -> code.append(";\n")
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

        val point = if (decl.isPointer) '*' else ' '

        if (!decl.isNull) return "$sign ${left2Ctype(decl.type)} $point${decl.name}$dimensions = ${gen(decl.value!!, root)}"
        return "$sign ${left2Ctype(decl.type)} $point${decl.name}$dimensions"
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
        val prefix = if (decl.isStd) '<' else '"'
        val postfix = if (decl.isStd) '>' else '"'
        return "include $prefix${decl.path}$postfix\n"
    }

    private fun genArray(decl: ArrayExpr, root: List<Node>): String {
        var array = "{"
        for ((index, expr) in decl.values.withIndex()) {
            array += if (index != 0)", ${gen(expr, root)}"
            else gen(expr, root)
        }

        return "$array}"
    }

    private fun genRange(decl: Range, root: List<Node>): String {
        val name = decl.name ?: "0"
        val start = gen(decl.start, root)
        val end = gen(decl.end, root)

        return "($name >= $start && $name <= $end)"
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
            is Literal -> "${decl.value}"
            is PreProcDecl -> "#" + gen(decl.directive, root) + "\n"
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
            is Break -> "break;"
            is Continue -> "continue;"
            is RepeatUntilDecl -> genRepeatUntil(decl, root)
            is ArrayExpr -> genArray(decl, root)
            is IndexExpr -> "${gen(decl.array, root)}${genDimensions(decl.dimensions, root)}"
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
