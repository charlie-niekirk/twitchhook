package com.cniekirk.twitchhook.command

import com.cniekirk.twitchhook.StreamEvent
import com.cniekirk.twitchhook.actions.Configuration
import com.cniekirk.twitchhook.data.DataStreamConfig
import com.cniekirk.twitchhook.data.DataStreamMuxer
import com.cniekirk.twitchhook.manipulateBlock
import com.cniekirk.twitchhook.spawnEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Enables all data streams and consumes all message types and performs the configured
 * actions
 */
@FlowPreview
class ChaosCommand(
    private val provider: DataStreamMuxer,
    private val accessToken: String,
    private val username: String,
    private val chatEnabled: Boolean,
    private val configuration: Configuration
): CommandExecutor {

    // So we can keep track and not register multiple consumers of the data streams
    private var job: Job? = null

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        if (sender is Player && args.size == 1) {

            if (command.name.equals("chaos", true)) {

                if (sender.hasPermission("twitchhook.chaos")) {

                    val twitchIrcConfig = DataStreamConfig.TwitchConfig(accessToken, username, args[0])
                    provider.start(mapOf("twitch" to twitchIrcConfig))

                    if (job == null) {

                        job = CoroutineScope(Dispatchers.IO).launch {
                            provider.combinedEventStream().collect { streamEvent ->
                                coroutineContext.ensureActive()
                                when (streamEvent) {
                                    is StreamEvent.TwitchChatMessage -> {
                                        if (chatEnabled) {
                                            Bukkit.broadcastMessage(
                                                "[".trimEnd() + ChatColor.BLUE + "".trimEnd() + ChatColor.BOLD + streamEvent.username.trimEnd() +
                                                        ChatColor.RESET + "]: ${streamEvent.content}".trimEnd()
                                            )
                                        }
                                    }
                                    is StreamEvent.TwitchSubscription -> {
                                        Bukkit.broadcastMessage("" + ChatColor.RED + streamEvent.systemMessage)
                                        when (configuration) {
                                            is Configuration.SpawnEntityAction -> {
                                                sender.spawnEntity(
                                                    configuration.entityType,
                                                    configuration.named,
                                                    streamEvent.gifter
                                                )
                                            }
                                            is Configuration.ManipulateBlockAction -> {
                                                sender.manipulateBlock(configuration.action)
                                            }
                                            is Configuration.PlaceBlockAction -> {

                                            }
                                        }
                                    }
                                    is StreamEvent.TwitchMassGiftMessage -> {
                                        Bukkit.broadcastMessage("" + ChatColor.BLUE + streamEvent.systemMessage)
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