package com.cniekirk.twitchhook.data.streamlabs

import com.cniekirk.twitchhook.StreamEvent
import com.cniekirk.twitchhook.data.DataStreamConfig
import com.cniekirk.twitchhook.data.DataStreamProvider
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.logging.Logger

class StreamlabsProvider(
    private val logger: Logger
): DataStreamProvider {

    private val _streamLabsEventFlow = MutableSharedFlow<StreamEvent>()
    override val eventStream = _streamLabsEventFlow.asSharedFlow()

    private lateinit var accessToken: String
    private lateinit var socket: Socket

    override fun startDataStream(dataStreamConfig: DataStreamConfig) {
        when (dataStreamConfig) {
            is DataStreamConfig.StreamLabsConfig -> {
                accessToken = dataStreamConfig.accessToken
                socket = IO.socket("https://sockets.streamlabs.com?token=$accessToken")
                registerSocketListener()
                socket.connect()
            }
            is DataStreamConfig.TwitchConfig -> {
                logger.info("The plugin has been corrupted: Wrong Config!!")
            }
        }
    }

    private fun registerSocketListener() {
        socket.on("event") {
            
        }
    }

    override fun stopDataStream() {
        socket.disconnect()
    }

    override fun clean() {
        socket.close()
    }
}