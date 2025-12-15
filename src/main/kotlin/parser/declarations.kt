package parser

import TokenTypes

sealed class Node

// --- верхний уровень ---
data class Program(var decls: List<Node>) : Node()
data class ConstDecl(val name: String, val type: String, val value: Expr) : Node()
data class FunDecl(
    val name: String,
    val returnType: String,
    val params: List<Param>,
    val body: Block,
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

data class LoopDecl(
    val body: Block
) : Node()

data class RepeatUntilDecl(
    val expr: Expr,
    val body: Block
) : Node()

data class ForDecl(
    val params: List<Param>,
    val range: Expr,
    val step: Expr,
    val body: Block
) : Node()

data class Param(val name: String, val type: String, val defaultValue: Expr?=null, val dimensions: List<Expr?> = emptyList()): Node()
data class Arg(val name: String, val value: Expr): Expr()

// --- операторы ---
data class VarDecl(
    val mutable: Boolean,
    val name: String,
    val type: String,
    val value: Expr?=null,
    val isNull: Boolean=false,
    val dimensions: List<Expr?> = emptyList()
) : Node()

data class Assign(val target: VarRef, val dimensions: List<Expr> = emptyList(),  val value: Expr) : Node()
data class VarBinaryExpr(val variable: VarRef, val op: String, val expr: Expr): Node()
data class Return(val value: Expr) : Node()
data class Block(var statements: List<Node>, val ownScopeStack: Boolean=true) : Node()

// --- выражения ---
sealed class Expr : Node()
data class Literal(val value: String) : Expr()
data class VarRef(val name: String) : Expr()
data class VarLink(val ref: VarRef): Expr()
data class ArrayExpr(val values: List<Expr> = emptyList()) : Expr()
data class BinaryExpr(val left: Expr, val op: String, val right: Expr) : Expr()
data class UnaryExpr(val value: Expr, val op: String, val isPrefixed: Boolean=true) : Expr()
data class CallExpr(val name: String, val args: List<Expr>) : Expr()

data class SingleRange(
    val start: Expr?=null,
    val startIsStrong: Boolean = false,

    val end: Expr?=null,
    val endIsStrong: Boolean = false,

    val onlyStart: Boolean = false
) : Expr()
data class Range(
    val ranges: List<SingleRange> = emptyList()
) : Expr()
data class IndexExpr(val array: Expr, val dimensions: List<Expr?>) : Expr()
class Break() : Expr()
class Continue() : Expr()

// --- системное ---
data class Include(val path: String, val isLeft: Boolean=false) : Node()
data class Use(val name: String, val alias: String?=null) : Node()

data class FromInclude(val include: Include, val namespace: String, val using: List<Use>) : Node()