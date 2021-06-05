package com.cniekirk.twitchhook.command

import com.cniekirk.twitchhook.data.twitch.IRCProvider
import com.cniekirk.twitchhook.StreamEvent
import com.cniekirk.twitchhook.TwitchHook
import com.cniekirk.twitchhook.data.DataStreamMuxer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player

@FlowPreview
class ChaosCommand(
    private val provider: DataStreamMuxer,
    private val accessToken: String,
    private val username: String
): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player && args.size == 1) {

            if (command.name.equals("chaos", true)) {

                if (sender.hasPermission("twitchhook.chaos")) {

                    provider.connectToStream(accessToken, username, args[0])

                    CoroutineScope(Dispatchers.IO).launch {
                        provider.combinedEventStream().collect { message: StreamEvent ->
                            coroutineContext.ensureActive()
                            when (message) {
                                is StreamEvent.TwitchChatMessage -> {
                                    if (message.content.contains("PING", true)) {
                                        provider.sendMessage("PONG :tmi.twitch.tv")
                                    }
//                                    Bukkit.broadcastMessage("[".trimEnd() + ChatColor.BLUE + "".trimEnd() + ChatColor.BOLD + message.username.trimEnd() +
//                                            ChatColor.RESET + "]: ${message.content}".trimEnd())
                                }
                                is StreamEvent.TwitchGiftSubscription -> {
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
                                is StreamEvent.TwitchNormalSubscription -> {
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

                } else {

                    Bukkit.broadcastMessage("" + ChatColor.RED + "Insufficient permissions for user [${sender.player?.displayName}]")

                }

            }

        }

        return true

    }

}