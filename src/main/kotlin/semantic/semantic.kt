package semantic

import parser.Arg
import parser.Assign
import parser.BinaryExpr
import parser.Block
import parser.Break
import parser.CallExpr
import parser.ConstDecl
import parser.Continue
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
import javax.naming.Context

data class MiddleWare(val func: (Node, MutableList<Node>) -> Unit) {
    val nodes = mutableListOf<Node>()
    val backups = mutableListOf(nodes)
    var level = 0

    fun run(decl: Node) {
        func(decl, nodes)
    }

    fun clear() {
        nodes.clear()
        backups.clear()
    }

    fun backUp() {
        level--
        nodes.clear()
        nodes.addAll(backups[level])
    }

    fun save() {
        if (level in backups.indices) {
            backups[level].clear()
            backups[level].addAll(nodes)
        }
        else {
            backups += mutableListOf<Node>().apply { addAll(nodes) }
        }
        level++
    }
}

object Semantic {
    var middlewares = mutableListOf<MiddleWare>()

    init {
        bindMiddleWares(this)
    }

    fun addMiddleware(middleware: (Node, MutableList<Node>) -> Unit) {
        middlewares += MiddleWare(middleware)
    }

    private fun runMiddlewares(decl: Node) {
        for (ware in middlewares) {
            ware.run(decl)
        }
    }

    private fun saveMiddlewares() {
        for (ware in middlewares) {
            ware.save()
        }
    }

    private fun backUpMiddlewares() {
        for (ware in middlewares) {
            ware.backUp()
        }
    }

    private fun clearMiddlewares() {
        for (ware in middlewares) {
            ware.clear()
        }
    }

    private fun process(decls: List<Node>) {
        for (decl in decls) {
            runMiddlewares(decl)
            when (decl) {
                is Block -> {
                    saveMiddlewares()
                    process(decl.statements)
                    backUpMiddlewares()
                }
                is FunDecl -> {
                    saveMiddlewares()
                    process(decl.body.statements)
                    backUpMiddlewares()
                }
                is LogicDecl -> {
                    saveMiddlewares()
                    process(decl.body.statements)
                    backUpMiddlewares()
                    if (decl.middlewares != null) {
                        for (ware in decl.middlewares) {
                            saveMiddlewares()
                            process(ware.body.statements)
                            backUpMiddlewares()
                        }
                    }

                    if (decl.elseWare != null) {
                        saveMiddlewares()
                        process(decl.elseWare.statements)
                        backUpMiddlewares()
                    }
                }
                else -> {}
            }
        }
    }

    fun analyze(program: Program) {
        clearMiddlewares()
        process(program.decls)
    }
}

fun bindMiddleWares(semantic: Semantic) {
    semantic.addMiddleware { decl, nodes ->
        when {
            decl is VarDecl && !decl.mutable -> {
                nodes += decl
            }
            decl is Assign -> {
                for (node in nodes) {
                    if (node !is VarDecl) continue
                    if (node.name == decl.target) {
                        throw RuntimeException("a unmutable variable cannot change its value")
                    }
                }
            }

            decl is VarBinaryExpr -> {
                for (node in nodes) {
                    if (node !is VarDecl) continue
                    when (decl.op) {
                        "+=", "-=", "*=", "/=", "%=" -> {}
                        else -> break
                    }
                    if (node.name == decl.variable.name) {
                        throw RuntimeException("a unmutable variable cannot change its value")
                    }
                }
            }
        }
    }
}