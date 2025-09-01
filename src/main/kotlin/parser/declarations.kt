package parser

import TokenTypes

sealed class Node

// --- верхний уровень ---
data class Program(val decls: List<Node>) : Node()
data class ConstDecl(val name: String, val type: String, val value: Expr) : Node()
data class FunDecl(
    val name: String,
    val returnType: String,
    val params: List<Param>,
    val body: Block
) : Node()

data class LogicDecl(
    val type: TokenTypes,
    val logicExpr: Expr,
    val body: Block,
    val middlewares: List<LogicDecl>?=null,
    val elseWare: Block?=null
) : Node()

data class WhenDecl(
    val middlewares: List<LogicDecl>,
    val elseWare: Block
) : Node()

data class Param(val name: String, val type: String)

// --- операторы ---
data class VarDecl(val mutable: Boolean, val name: String, val type: String, val value: Expr) : Node()
data class Assign(val target: String, val value: Expr) : Node()
data class Return(val value: Expr) : Node()
data class Block(val statements: List<Node>) : Node()

// --- выражения ---
sealed class Expr : Node()
data class Literal(val value: Any) : Expr()
data class VarRef(val name: String) : Expr()
data class BinaryExpr(val left: Expr, val op: String, val right: Expr) : Expr()
data class CallExpr(val name: String, val args: List<Expr>) : Expr()

// --- системное ---
data class Include(val path: String, val isLeftScript: Boolean, val isStd: Boolean): Expr()
data class PreProcDecl(val data: String, val directive: Expr) : Expr()