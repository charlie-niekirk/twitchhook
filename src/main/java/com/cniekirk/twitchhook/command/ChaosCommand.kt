package com.cniekirk.twitchhook.command

import com.cniekirk.twitchhook.data.twitch.IRCProvider
import com.cniekirk.twitchhook.StreamEvent
import com.cniekirk.twitchhook.TwitchHook
import com.cniekirk.twitchhook.data.DataStreamConfig
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

                    val twitchIrcConfig = DataStreamConfig.TwitchConfig(accessToken, username, args[0])
                    provider.start(mapOf("twitch" to twitchIrcConfig))

                    CoroutineScope(Dispatchers.IO).launch {
                        provider.combinedEventStream().collect { message: StreamEvent ->
                            coroutineContext.ensureActive()
                            when (message) {
                                is StreamEvent.TwitchChatMessage -> {
                                    Bukkit.broadcastMessage("[".trimEnd() + ChatColor.BLUE + "".trimEnd() + ChatColor.BOLD + message.username.trimEnd() +
                                            ChatColor.RESET + "]: ${message.content}".trimEnd())
                                }
                                is StreamEvent.TwitchSubscription -> {
                                    Bukkit.broadcastMessage("" + ChatColor.RED + message.systemMessage)
                                    Bukkit.getServer().scheduler.scheduleSyncDelayedTask(TwitchHook.plugin()) {
                                        sender.player?.world?.spawnEntity(
                                            sender.player?.location!!.add(1.0, 1.0, 1.0),
                                            EntityType.ZOMBIE
                                        )
                                    }
                                }
                                is StreamEvent.TwitchMassGiftMessage -> {
                                    Bukkit.broadcastMessage("" + ChatColor.BLUE + message.systemMessage)
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