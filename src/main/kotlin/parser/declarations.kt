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
    val elseWare: Block?=null
) : Node()

data class WhileDecl(
    val expr: Expr,
    val body: Block
) : Node()

data class ForDecl(
    val init: VarDecl,
    val range: Expr,
    val step: Expr,
    val body: Block
) : Node()

data class Param(val name: String, val type: String, val defaultValue: Expr?=null)
data class Arg(val name: String, val value: Expr): Expr()

// --- операторы ---
data class VarDecl(val mutable: Boolean, val name: String, val type: String, val value: Expr?=null, val isNull: Boolean=false) : Node()
data class Assign(val target: String, val value: Expr) : Node()
data class VarBinaryExpr(val variable: VarRef, val op: String, val expr: Expr): Node()
data class Return(val value: Expr) : Node()
data class Block(val statements: List<Node>, val ownScopeStack: Boolean=true) : Node()

// --- выражения ---
sealed class Expr : Node()
data class Literal(val value: Any) : Expr()
data class VarRef(val name: String) : Expr()
data class VarLink(val ref: VarRef): Expr()
data class BinaryExpr(val left: Expr, val op: String, val right: Expr) : Expr()
data class CallExpr(val name: String, val args: List<Expr>) : Expr()
data class Range(val start: Expr, val end: Expr, var name: String?) : Expr()
class Break() : Expr()
class Continue() : Expr()

// --- системное ---
data class Include(val path: String, val isLeftScript: Boolean, val isStd: Boolean): Expr()
data class PreProcDecl(val data: String, val directive: Expr) : Expr()