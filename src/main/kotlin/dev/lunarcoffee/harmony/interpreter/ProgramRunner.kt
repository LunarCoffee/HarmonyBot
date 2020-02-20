package dev.lunarcoffee.harmony.interpreter

import dev.lunarcoffee.harmony.model.*
import dev.lunarcoffee.harmony.send
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.random.Random

class ProgramRunner(
    private val program: Program,
    private val event: GuildMessageReceivedEvent,
    private val inputQueue: ConcurrentLinkedQueue<String>
) {
    private val varTable = mutableMapOf<String, Any>()
    private var needsBreak = false

    fun run() {
        val code = program.code
        for (rule in code)
            execute(rule)
    }

    private fun execute(rule: Rule) {
        when (rule) {
            is RSay -> event.send(stringifyExpr(rule.source))
            is RDelay -> Thread.sleep(rule.ms.toLong())
            is RAssign -> varTable[rule.destination] = evalExpr(rule.source)
            is RIfStatement -> {
                for (branch in rule.branches) {
                    if (evalExpr(branch.source) as Boolean) {
                        for (r in branch.code)
                            execute(r)
                        return
                    }
                }
            }
            is RLoop -> {
                while (true) {
                    for (r in rule.code) {
                        execute(r)
                        if (needsBreak)
                            return
                    }
                }
            }
            is RBreak -> needsBreak = true
        }
    }

    private fun evalExpr(expr: RExpression?): Any {
        return when (expr) {
            null -> true
            is RString -> expr.content
            is RNumber -> expr.value
            is RRandom -> Random.nextInt(expr.upperBound).toDouble()
            is RVarRef -> varTable[expr.name]!!
            is RConcat -> stringifyExpr(expr)
            RInput -> stringifyExpr(expr)
            is RBinOp -> evalBinOp(expr)
        }
    }

    private fun evalBinOp(expr: RBinOp): Any {
        val a = evalExpr(expr.x)
        val b = evalExpr(expr.y)

        return when (expr) {
            is RIs -> a == b
            is RIsnt -> a != b
            is ROr -> a as Boolean || b as Boolean
            is RAnd -> a as Boolean || b as Boolean
            is RPlus -> a as Double + b as Double
            is RMinus -> a as Double - b as Double
            is RLte -> a as Double <= b as Double
        }
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    private fun stringifyExpr(expr: RExpression): String {
        return when (expr) {
            is RString -> expr.content
            is RNumber -> (if (expr.value % 1.0 == 0.0) expr.value.toInt() else expr.value).toString()
            is RVarRef -> varTable[expr.name]!!.toString() // TODO: special case int/double?
            is RConcat -> expr.parts.joinToString("") { stringifyExpr(it) }
            RInput -> awaitInput()
            else -> throw IllegalStateException("interpreter error")
        }
    }

    private fun awaitInput(): String {
        while (inputQueue.isEmpty())
            Thread.sleep(100)
        return inputQueue.poll()
    }
}
