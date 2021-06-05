package com.cniekirk.twitchhook.command

import com.cniekirk.twitchhook.data.DataStreamMuxer
import kotlinx.coroutines.FlowPreview
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@FlowPreview
class StopCommand(
    private val provider: DataStreamMuxer
): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        if (sender is Player) {

            if (command.name.equals("calm", true)) {

                if (sender.hasPermission("twitchhook.chaos")) {

                    // Stop all data streams
                    provider.stop()

                }

            }

        }

        return true

    }

}