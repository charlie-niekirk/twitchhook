package com.cniekirk.twitchhook

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.*
import java.util.logging.Logger

class IRCProvider(private val webSocketClient: OkHttpClient,
                  private val request: Request,
                  private val logger: Logger
): WebSocketListener() {

    private val _twitchEventFlow = MutableSharedFlow<TwitchEvent>()
    val twitchEventFlow = _twitchEventFlow.asSharedFlow()

    private lateinit var webSocket: WebSocket
    private lateinit var targetChannelName: String
    private lateinit var accessToken: String
    private lateinit var username: String

    fun connectToStream(accessToken: String, username: String, targetChannelName: String) {
        webSocket = webSocketClient.newWebSocket(request, this)
        this.targetChannelName = targetChannelName
        this.accessToken = accessToken
        this.username = username
    }

    fun sendMessage(message: String) {
        webSocket.send(message)
    }

    fun disconnect() {
        webSocket.close(1000, null)
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        webSocketClient.dispatcher.executorService.shutdown()
        // Acquire capabilities
        webSocket.send("CAP REQ :twitch.tv/tags twitch.tv/commands twitch.tv/membership")
        // Login and configure user
        webSocket.send("PASS oauth:$accessToken")
        webSocket.send("NICK $username")
        webSocket.send("USER $username 8 * :$username")
        // Join specified channel
        webSocket.send("JOIN #$targetChannelName")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        CoroutineScope(Dispatchers.IO).launch {
            if (text.contains("PRIVMSG")) {
                val message = text.substring(
                    text.lastIndexOf("PRIVMSG #$targetChannelName") + "PRIVMSG #$targetChannelName".length + 2,
                    text.lastIndex)
                val senderStart = text.indexOf("display-name=").plus("display-name=".length)
                val sender = text.substring(senderStart, text.indexOf(';', senderStart))
                _twitchEventFlow.emit(TwitchEvent.Message(sender, message))
            } else if (text.contains("USERNOTICE")) {
                val numSubs = if (text.contains("submysterygift", true).or(
                        text.contains("anonsubgift", true)
                    )) {
                    val numberStart = text.indexOf("msg-param-mass-gift-count=")
                        .plus("msg-param-mass-gift-count=".length)
                    text.substring(numberStart, text.indexOf(';', numberStart)).toInt()
                } else if (text.contains("=sub;")) {
                    1
                }
                else {
                    0
                }
                if (numSubs > 1) {
                    val gifterStart = text.indexOf("login=").plus("login=".length)
                    val gifter = text.substring(gifterStart, text.indexOf(';', gifterStart))
                    _twitchEventFlow.emit(TwitchEvent.GiftSubscription(numSubs, gifter))
                } else if (numSubs > 0 && text.contains("=sub;")) {
                    val senderStart = text.indexOf("display-name=").plus("display-name=".length)
                    val sender = text.substring(senderStart, text.indexOf(';', senderStart))
                    _twitchEventFlow.emit(TwitchEvent.NormalSubscription(numSubs, sender))
                }
            }
        }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        logger.info { t.localizedMessage }
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        logger.info { "Socket closed ($code)" }
    }

}