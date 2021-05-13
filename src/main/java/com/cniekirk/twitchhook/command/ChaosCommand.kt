package com.cniekirk.twitchhook.command

import com.cniekirk.twitchhook.IRCProvider
import com.cniekirk.twitchhook.TwitchEvent
import com.cniekirk.twitchhook.TwitchHook
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import okhttp3.OkHttpClient
import okhttp3.Request
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player

class ChaosCommand(
    private val accessToken: String,
    private val username: String
): CommandExecutor {

    private lateinit var job: Job

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player && args.size == 1) {

            if (command.name.equals("chaos", true)) {

                val client = OkHttpClient.Builder()
                    .build()
                val request = Request.Builder()
                    .url("wss://irc-ws.chat.twitch.tv:443")
                    .build()

                val provider = IRCProvider(client, request, Bukkit.getLogger())

                provider.connectToStream(accessToken, username, args[0])

                job = CoroutineScope(Dispatchers.IO).launch {
                    provider.twitchEventFlow.collect { message: TwitchEvent ->
                        coroutineContext.ensureActive()
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

            } else if (command.name.equals("stop", true)) {
                job.cancel()
            }

        }

        return true

    }

}