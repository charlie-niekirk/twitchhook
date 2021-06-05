package com.cniekirk.twitchhook.data

import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.logging.Logger

sealed class DataStreamConfig {
    data class TwitchConfig(val accessToken: String,
                            val username: String,
                            val targetChannelName: String): DataStreamConfig()
    data class StreamLabsConfig(
        private val accessToken: String
    ): DataStreamConfig()
}