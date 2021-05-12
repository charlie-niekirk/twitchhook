package com.cniekirk.twitchhook.command

import com.cniekirk.twitchhook.IRCProvider
import com.cniekirk.twitchhook.TwitchEvent
import com.cniekirk.twitchhook.TwitchHook
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class CommandHandler: CommandExecutor {

    private val job = Job()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player && args.size == 1) {

            val client = OkHttpClient.Builder()
                .build()
            val request = Request.Builder()
                .url("wss://irc-ws.chat.twitch.tv:443")
                .build()

            val provider = IRCProvider(client, request, Bukkit.getLogger())

            provider.connectToStream(
                "sr8gpumtgclrbq9738xzu9fm2bnbnf",
                "charlief120",
                args[0])

            CoroutineScope(Dispatchers.IO + job).launch {
                provider.twitchEventFlow.collect { message: TwitchEvent ->
                    when (message) {
                        is TwitchEvent.Message -> {
                            if (message.content.contains("PING", true)) {
                                provider.sendMessage("PONG :tmi.twitch.tv")
                            }
                            Bukkit.broadcastMessage("[".trimEnd() + ChatColor.BLUE + "".trimEnd() + ChatColor.BOLD + message.username.trimEnd() +
                                    ChatColor.RESET + "]: ${message.content}".trimEnd())
                        }
                        is TwitchEvent.GiftSubscription -> {
                            Bukkit.broadcastMessage("[GIFTED SUBS]: ${message.giftingUsername} gifted ${message.numSubs} subs!".trimEnd())
                            repeat(message.numSubs) {
                                Bukkit.getServer().scheduler.scheduleSyncDelayedTask(TwitchHook.plugin()) {
                                    sender.player?.world?.spawnEntity(
                                        sender.player?.location!!.add(1.0, 1.0, 1.0),
                                        EntityType.ZOMBIE
                                    )
                                }
                            }
                        }
                        is TwitchEvent.NormalSubscription -> {
                            Bukkit.broadcastMessage("[USER SUB]: " + ChatColor.DARK_RED + "${message.username} just subscribed!".trimEnd())
                            Bukkit.getServer().scheduler.scheduleSyncDelayedTask(TwitchHook.plugin()) {
                                sender.player?.world?.spawnEntity(
                                    sender.player?.location!!.add(1.0, 1.0, 1.0),
                                    EntityType.ZOMBIE
                                )
                            }
                        }
                    }
                }
            }

        }

        return true

    }

    fun registerTwitchEventListener() {

    }

}