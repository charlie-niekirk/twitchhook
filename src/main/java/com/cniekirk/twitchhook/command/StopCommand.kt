package com.cniekirk.twitchhook.command

import com.cniekirk.twitchhook.data.DataStreamMuxer
import kotlinx.coroutines.FlowPreview
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Allows the data stream(s) to be stopped, cannot use 'stop' as it screws up
 * the real 'stop' command
 */
@FlowPreview
class StopCommand(
    private val provider: DataStreamMuxer
): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        if (sender is Player && sender.hasPermission("twitchhook.chaos") &&
            command.name.equals("calm", true)) {
            // Stop all data streams
            provider.stop()
        }

        return true

    }

}