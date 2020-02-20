package dev.lunarcoffee.harmony

import dev.lunarcoffee.harmony.interpreter.EventListener
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import java.io.File

class Form(val code: String)

fun main() {
    val eventListener = EventListener()
    JDABuilder(File("src/main/resources/token.txt").readText())
        .setActivity(Activity.watching("the chat."))
        .setStatus(OnlineStatus.DO_NOT_DISTURB)
        .addEventListeners(eventListener)
        .build()
}
