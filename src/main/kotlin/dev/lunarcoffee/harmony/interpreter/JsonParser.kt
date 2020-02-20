package dev.lunarcoffee.harmony.interpreter

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.lunarcoffee.harmony.model.*

private typealias JMap = Map<String, Any>

class JsonParser(private val json: String) {
    private val gson = Gson()

    @Suppress("UNCHECKED_CAST")
    fun deserialize(): Program {
        val mapType = TypeToken.getParameterized(Map::class.java, String::class.java, Any::class.java)
        val layer = gson.fromJson<JMap>(json, mapType.type)

        val name = layer["name"] as String
        val code = layer["code"] as List<JMap>

        val rules = code.map { rule(it["type"] as String, it) }
        return Program(name, rules)
    }

    @Suppress("UNCHECKED_CAST")
    private fun rule(type: String, rule: JMap): Rule {
        return when (type) {
            "say" -> {
                val expr = expression(rule["source"] as JMap)
                RSay(expr!!)
            }
            "delay" -> {
                val timeMs = ((rule["source"] as JMap)["content"] as Double).toInt()
                RDelay(timeMs)
            }
            "assign" -> {
                val dest = rule["destination"] as String
                val source = expression(rule["source"] as JMap)
                RAssign(dest, source!!)
            }
            "if" -> {
                val branches = rule["branches"] as List<JMap>
                val actualBranches = branches.map {
                    RIfBranch(
                        expression(it["source"] as JMap?),
                        (it["code"] as List<JMap>).map { r -> rule(r["type"] as String, r) }
                    )
                }
                RIfStatement(actualBranches)
            }
            "loop" -> {
                val code = (rule["code"] as List<JMap>).map { r -> rule(r["type"] as String, r) }
                RLoop(code)
            }
            "break" -> RBreak
            else -> throw IllegalStateException("json parse error")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun expression(source: JMap?): RExpression? {
        if (source == null)
            return null

        return when (source["type"] as String) {
            "string" -> RString(source["content"] as String)
            "random" -> RRandom(((source["content"] as JMap)["content"] as Double).toInt())
            "number" -> RNumber(source["content"] as Double)
            "variable" -> RVarRef(source["content"] as String)
            "input" -> RInput
            "concat" -> {
                val parts = (source["content"] as List<JMap>).map { expression(it)!! }
                RConcat(parts)
            }
            "is", "isnt", "or", "and", "-", "+", "<=" -> binOp(
                source["type"] as String,
                expression(source["a"] as JMap)!!,
                expression(source["b"] as JMap)!!
            )
            else -> throw java.lang.IllegalStateException("json parse error")
        }
    }

    private fun binOp(name: String, a: RExpression, b: RExpression): RBinOp {
        return when (name) {
            "is" -> RIs(a, b)
            "isnt" -> RIsnt(a, b)
            "or" -> ROr(a, b)
            "and" -> RAnd(a, b)
            "+" -> RPlus(a, b)
            "-" -> RMinus(a, b)
            "<=" -> RLte(a, b)
            else -> throw IllegalStateException("json parse error")
        }
    }
}