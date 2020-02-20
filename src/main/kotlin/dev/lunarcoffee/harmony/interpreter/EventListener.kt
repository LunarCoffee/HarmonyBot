package dev.lunarcoffee.harmony.interpreter

import dev.lunarcoffee.harmony.model.Program
import dev.lunarcoffee.harmony.send
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class EventListener : ListenerAdapter() {
    private val programs = mutableListOf<Program>()
    private val guildToProgram = mutableMapOf<Long, Program>()

    private val inputQueue = ConcurrentLinkedQueue<String>()
    private var running = AtomicBoolean(false)

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        val content = event.message.contentRaw

        when {
            content.startsWith("::setprogram ") -> {
                val name = content.split(" ").drop(1).joinToString(" ").trim().ifEmpty { return }
                val program = programs.find { it.name == name }
                    ?: return event.send(":x:  **I can't find a program with that name.**")

                guildToProgram[event.guild.idLong] = program
                event.send(":white_check_mark:  **Program `$name` is now set!**")
            }
            content == "::runprogram" -> {
                val program = guildToProgram[event.guild.idLong]
                    ?: return event.send(":x:  **There is no program set. Set one with `::setprogram <name>`.**")

                thread(true) {
                    // TODO: wrap try/catch
                    try {
                        ProgramRunner(program, event, inputQueue).run()
                    } catch (t: Throwable) {
                        event.send(":x:  **There was an error during your program's execution!**")
                    }
                    running.set(false)
                }
                running.set(true)
            }
            content.startsWith("::compileprogram ") -> {
                val code = content.substringAfter(" ")
                File("src/main/resources/source.txt").writeText(code)
                val p = ProcessBuilder("src/main/resources/poppy", "src/main/resources/source.txt")
                    .redirectOutput(File("src/main/resources/json.json"))
                p.start()
                registerProgram(File("src/main/resources/json.json").readText())
                event.send(":white_check_mark:  **Your program has been compiled!**")
            }
            else -> {
                if (running.get() && !event.message.author.isBot)
                    inputQueue.offer(content)
            }
        }
    }

    fun registerProgram(program: String) = programs.add(JsonParser(program).deserialize())
}
