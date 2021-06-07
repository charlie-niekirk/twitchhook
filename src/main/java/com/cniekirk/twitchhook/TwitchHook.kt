package com.cniekirk.twitchhook

import com.cniekirk.twitchhook.command.ChaosCommand
import com.cniekirk.twitchhook.command.StopCommand
import com.cniekirk.twitchhook.data.DataStreamMuxer
import com.cniekirk.twitchhook.data.twitch.TwitchProvider
import kotlinx.coroutines.FlowPreview
import okhttp3.OkHttpClient
import okhttp3.Request
import org.bukkit.Bukkit
import org.bukkit.EntityEffect
import org.bukkit.entity.EntityType
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

@FlowPreview
class TwitchHook: JavaPlugin() {

    private lateinit var muxer: DataStreamMuxer

    override fun onEnable() {
        super.onEnable()
        saveDefaultConfig()
        if (config["access_token"].toString().isNotEmpty() &&
            config["username"].toString().isNotEmpty()) {
            logger.info("Access token: ${config["access_token"].toString()}")
            registerContentProviders()
        } else {
            logger.info("Please fill out the plugins/Twitchhook/config.yml and then re-launch the server")
        }
    }

    private fun registerContentProviders() {
        val client = OkHttpClient.Builder()
            .build()
        val request = Request.Builder()
            .url("wss://irc-ws.chat.twitch.tv:443")
            .build()

        // Twitch data stream
        val provider = TwitchProvider(client, request, Bukkit.getLogger())
        // Allows multiple providers to be muxed together
        muxer = DataStreamMuxer(mapOf("twitch" to provider))

        if (config["sub_action"].toString().isNotEmpty()) {
            val configuration = config["sub_action"].toString().parseConfiguration()
            val commandHandler = ChaosCommand(muxer, config["access_token"].toString(), config["username"].toString(), config.getBoolean("chat_enabled"), configuration)
            val stopCommand = StopCommand(muxer)
            getCommand("chaos")?.setExecutor(commandHandler)
            getCommand("calm")?.setExecutor(stopCommand)
        }
    }

    companion object {
        fun plugin(): Plugin {
            return getPlugin(TwitchHook::class.java)
        }
    }

    override fun onDisable() {
        muxer.stop()
        muxer.clean()
        super.onDisable()
    }

}