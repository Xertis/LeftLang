package generator

import parser.Arg
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
import parser.Literal
import parser.LogicDecl
import parser.Node
import parser.PreProcDecl
import parser.Program
import parser.Range
import parser.Return
import parser.VarBinaryExpr
import parser.VarDecl
import parser.VarLink
import parser.VarRef
import parser.WhenDecl
import parser.WhileDecl
import tokens.Token

class Generator(val program: Program) {
    private fun isUnsigned(type: String): Boolean? {
        return when (type) {
            "u8", "u16", "u32", "u64", "usize" -> true
            "i8", "i16", "i32", "i64", "isize" -> false
            else -> null
        }
    }

    private fun left2Ctype(type: String): String {
        return when(type) {
            "u8", "i8" -> "char"
            "u16", "i16" -> "short"
            "u32", "i32" -> "int"
            "u64", "i64" -> "long"
            "usize", "isize" -> "long long"
            "f32" -> "float"
            "f64" -> "double"
            "Bool" -> "_Bool"
            "Void" -> "void"
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
            params += "$sign ${left2Ctype(param.type)} ${param.name}"
        }
        val body = gen(decl.body, root)
        return "${left2Ctype(decl.returnType)} ${decl.name}${params.joinToString(
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

        var fullLogicBlock = "$name ${gen(decl.logicExpr, root)} {\n${gen(decl.body, root)}}"

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

    private fun genVar(decl: VarDecl, root: List<Node>): String {
        val isUnsigned = isUnsigned(decl.type)
        val sign = when (isUnsigned) {
            true -> "unsigned"
            false -> "signed"
            null -> ""
        }

        if (!decl.isNull) return "$sign ${left2Ctype(decl.type)} ${decl.name} = ${gen(decl.value!!, root)}"
        return "$sign ${left2Ctype(decl.type)} ${decl.name}"
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

    private fun genRange(decl: Range, root: List<Node>): String {
        val name = decl.name ?: "0"
        val start = gen(decl.start, root)
        val end = gen(decl.end, root)

        return "($name >= $start && $name <= $end)"
    }

    private fun gen(decl: Node, root: List<Node>): String {
        return when (decl) {
            is Assign -> "${decl.target} = ${gen(decl.value, root)}"
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
