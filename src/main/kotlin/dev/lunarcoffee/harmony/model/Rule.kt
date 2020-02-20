package dev.lunarcoffee.harmony.model

// Each program is just a collection of rules.
data class Program(val name: String, val code: List<Rule>)

sealed class Rule

data class RSay(val source: RExpression) : Rule()
data class RDelay(val ms: Int) : Rule()
data class RAssign(val destination: String, val source: RExpression) : Rule()
data class RIfStatement(val branches: List<RIfBranch>) : Rule()
data class RIfBranch(val source: RExpression?, val code: List<Rule>) : Rule()
data class RLoop(val code: List<Rule>) : Rule()
object RBreak : Rule()

// Expressions which can be used in an expression context.
sealed class RExpression : Rule()

data class RString(val content: String) : RExpression()
data class RRandom(val upperBound: Int) : RExpression()
data class RNumber(val value: Double) : RExpression()
data class RVarRef(val name: String) : RExpression()
data class RConcat(val parts: List<RExpression>) : RExpression()
object RInput : RExpression()

sealed class RBinOp(val x: RExpression, val y: RExpression) : RExpression()

data class RIs(val a: RExpression, val b: RExpression) : RBinOp(a, b)
data class RIsnt(val a: RExpression, val b: RExpression) : RBinOp(a, b)
data class ROr(val a: RExpression, val b: RExpression) : RBinOp(a, b)
data class RAnd(val a: RExpression, val b: RExpression) : RBinOp(a, b)
data class RPlus(val a: RExpression, val b: RExpression) : RBinOp(a, b)
data class RMinus(val a: RExpression, val b: RExpression) : RBinOp(a, b)
data class RLte(val a: RExpression, val b: RExpression) : RBinOp(a, b)
