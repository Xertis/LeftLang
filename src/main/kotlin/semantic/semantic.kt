package semantic

import STDINT_VARTYPE_GROUP
import parser.Assign
import parser.Block
import parser.CallExpr
import parser.ForDecl
import parser.FunDecl
import parser.Include
import parser.LogicDecl
import parser.Node
import parser.PreProcDecl
import parser.Program
import parser.RepeatUntilDecl
import parser.VarBinaryExpr
import parser.VarDecl
import parser.VarLink
import parser.WhenDecl
import parser.WhileDecl

data class MiddleWare(val func: (Node, MutableList<Node>, Semantic) -> Unit) {
    val nodes = mutableListOf<Node>()
    val backups = mutableListOf(nodes)
    var level = 0

    fun run(decl: Node, semantic: Semantic) {
        func(decl, nodes, semantic)
    }

    fun clear() {
        level = 0
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
    var endHandlers = mutableListOf<(HashMap<String, Int>, MutableList<Node>, Semantic) -> MutableList<Node>>()
    var flags: HashMap<String, Int> = hashMapOf()

    init {
        bindMiddleWares(this)
    }

    fun addMiddleware(middleware: (Node, MutableList<Node>, Semantic) -> Unit) {
        middlewares += MiddleWare(middleware)
    }

    fun addEndHandler(handler: (HashMap<String, Int>, MutableList<Node>, Semantic) -> MutableList<Node>) {
        endHandlers += handler
    }

    fun setFlag(key: String, value: Int) {
        flags[key] = value
    }

    private fun runMiddlewares(decl: Node) {
        for (ware in middlewares) {
            ware.run(decl, this)
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
                is WhileDecl -> {
                    saveMiddlewares()
                    process(decl.body.statements)
                    backUpMiddlewares()
                }
                is RepeatUntilDecl -> {
                    saveMiddlewares()
                    process(decl.body.statements)
                    backUpMiddlewares()
                }
                is ForDecl -> {
                    saveMiddlewares()
                    process(listOf(decl.init))
                    process(decl.body.statements)
                    backUpMiddlewares()
                }
                is WhenDecl -> {
                    saveMiddlewares()
                    process(decl.middlewares)
                    if (decl.elseWare != null) process(decl.elseWare.statements)
                    backUpMiddlewares()
                }
                is CallExpr -> {
                    process(decl.args)
                }
                else -> {}
            }
        }
    }

    fun analyze(program: Program): Program {
        clearMiddlewares()
        process(program.decls)

        var mutableDeclList = program.decls.toMutableList()

        for (handler in endHandlers) {
            mutableDeclList = handler(flags, mutableDeclList, this)
        }

        program.decls = mutableDeclList.toList()

        return program
    }
}

fun bindMiddleWares(semantic: Semantic) {
    semantic.addMiddleware { decl, nodes, semantic ->
        when {
            decl is VarDecl && !decl.mutable -> {
                nodes += decl
            }
            decl is Assign -> {
                for (node in nodes) {
                    if (node !is VarDecl) continue
                    if (node.name == decl.target.name) {
                        throw RuntimeException("a unmutable variable cannot change its value")
                    }
                }
            }

            decl is VarLink -> {
                for (node in nodes) {
                    if (node !is VarDecl) continue
                    if (node.name == decl.ref.name) {
                        throw RuntimeException("you cannot get the address of a unmutable variable")
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

    semantic.addMiddleware { decl, nodes, semantic ->
        if (decl is VarDecl && decl.type in STDINT_VARTYPE_GROUP) {
            semantic.setFlag("exact-width-variable", 1)
        } else if (decl is PreProcDecl && decl.directive is Include && decl.directive.path == "stdint.h") {
            semantic.setFlag("has-stdint-include", 1)
        }
    }

    semantic.addEndHandler { flags, nodes, semantic ->
        if (flags["exact-width-variable"] == 1 && flags["has-stdint-include"] == null) {
            nodes.add(0, PreProcDecl(
                data = "stdint.h",
                directive = Include(
                    isStd = true,
                    isLeftScript = false,
                    path = "stdint.h"
                )
            ))
        }

        return@addEndHandler nodes
    }
}