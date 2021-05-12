package com.cniekirk.twitchhook

import com.cniekirk.twitchhook.command.CommandHandler
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

class TwitchHook: JavaPlugin() {

    override fun onEnable() {
        super.onEnable()
        getCommand("chaos")?.setExecutor(CommandHandler())
    }

    companion object {
        fun plugin(): Plugin {
            return getPlugin(TwitchHook::class.java)
        }
    }

    override fun onDisable() {
        // Hot source, make sure to cancel
        super.onDisable()
    }

}