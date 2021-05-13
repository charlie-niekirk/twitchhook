package com.cniekirk.twitchhook

import com.cniekirk.twitchhook.command.ChaosCommand
import com.cniekirk.twitchhook.config.Config
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

class TwitchHook: JavaPlugin() {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val adapter: JsonAdapter<Config> = moshi.adapter(Config::class.java)

    override fun onEnable() {
        super.onEnable()
        val config = adapter.fromJson(String(getResource("config.json")!!.readBytes()))
        val commandHandler = ChaosCommand(config!!.accessToken, config.username)
        getCommand("chaos")?.setExecutor(commandHandler)
        getCommand("stop")?.setExecutor(commandHandler)
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