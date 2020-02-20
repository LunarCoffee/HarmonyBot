package dev.lunarcoffee.harmony

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

fun GuildMessageReceivedEvent.send(message: Any) {
    channel.sendMessage(message.toString()).queue()
}
