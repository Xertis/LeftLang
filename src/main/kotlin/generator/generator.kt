package generator

import parser.Assign
import parser.BinaryExpr
import parser.Block
import parser.CallExpr
import parser.ConstDecl
import parser.Expr
import parser.FunDecl
import parser.Include
import parser.Literal
import parser.LogicDecl
import parser.Node
import parser.PreProcDecl
import parser.Program
import parser.Return
import parser.VarDecl
import parser.VarRef
import parser.WhenDecl
import tokens.Token

class Generator(val program: Program) {
    private fun isUnsigned(type: String): Boolean {
        return when (type) {
            "u8", "u16", "u32", "u64", "usize" -> true
            else -> false
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

    private fun genFunc(decl: FunDecl): String {
        val params = mutableListOf<String>()
        for (param in decl.params) {
            val isUnsigned = if (isUnsigned(param.type)) "unsigned" else ""
            params += "$isUnsigned ${left2Ctype(param.type)} ${param.name}"
        }
        val body = gen(decl.body)
        return "${left2Ctype(decl.returnType)} ${decl.name}${params.joinToString(
            separator = ",",
            prefix = "(",
            postfix = ")"
        )} {\n$body\n}\n"
    }

    private fun genElse(decl: Block): String {
        return "else {\n${gen(decl)}}"
    }

    private fun genLogic(decl: LogicDecl): String {
        val name = if (decl.type == TokenTypes.KW_IF) "if" else "else if"

        var fullLogicBlock = "$name ${gen(decl.logicExpr)} {\n${gen(decl.body)}}"

        if (decl.middlewares != null) {
            for (ware in decl.middlewares) {
                fullLogicBlock += genLogic(ware)
            }
        }

        if (decl.elseWare != null) {
            fullLogicBlock += genElse(decl.elseWare)
        }

        return fullLogicBlock
    }

    private fun genWhen(decl: WhenDecl): String {
        val code = StringBuilder()

        for (ware in decl.middlewares) {
            code.append(genLogic(ware))
        }

        code.append(genElse(decl.elseWare))

        return code.toString()
    }

    private fun genBlock(decl: Block): String {
        val code = StringBuilder()
        for (dec in decl.statements) {
            code.append(gen(dec))
            when (dec) {
                is VarDecl, is CallExpr, is Return, is Assign -> code.append(";\n")
                else -> code.append("\n")
            }
        }
        return code.toString()
    }

    private fun genVar(decl: VarDecl): String {
        val isUnsigned = if (isUnsigned(decl.type)) "unsigned" else ""
        return "$isUnsigned ${left2Ctype(decl.type)} ${decl.name} = ${gen(decl.value)}"
    }

    private fun genCall(decl: CallExpr): String {
        val args = decl.args.joinToString(
            separator = ",",
            prefix = "(",
            postfix = ")"
        ) { gen(it) }
        return "${decl.name}$args"
    }

    private fun genInclude(decl: Include): String {
        val prefix = if (decl.isStd) '<' else '"'
        val postfix = if (decl.isStd) '>' else '"'
        return "include $prefix${decl.path}$postfix\n"
    }

    private fun gen(decl: Node): String {
        return when (decl) {
            is Assign -> throw RuntimeException("не реализован $decl")
            is Block -> genBlock(decl)
            is ConstDecl -> throw RuntimeException("не реализован $decl")
            is BinaryExpr -> "(${gen(decl.left)} ${decl.op} ${gen(decl.right)})"
            is CallExpr -> genCall(decl)
            is Include -> genInclude(decl)
            is Literal -> "${decl.value}"
            is PreProcDecl -> "#" + gen(decl.directive) + "\n"
            is VarRef -> decl.name
            is FunDecl -> genFunc(decl)
            is Program -> throw RuntimeException("не реализован $decl")
            is Return -> "return ${gen(decl.value)}"
            is VarDecl -> genVar(decl)
            is LogicDecl -> genLogic(decl)
            is WhenDecl -> genWhen(decl)
        }
    }

    fun startGen(): String {
        val code = StringBuilder()
        for (dec in program.decls) {
            code.append(gen(dec))

            if (dec is VarDecl) code.append(";\n")
        }
        return code.toString()
    }
}
