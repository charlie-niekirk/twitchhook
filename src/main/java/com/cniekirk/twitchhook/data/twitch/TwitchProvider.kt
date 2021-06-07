package com.cniekirk.twitchhook.data.twitch

import com.cniekirk.twitchhook.StreamEvent
import com.cniekirk.twitchhook.data.DataStreamConfig
import com.cniekirk.twitchhook.data.DataStreamProvider
import com.cniekirk.twitchhook.parseSystemMsg
import com.cniekirk.twitchhook.parseTags
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.*
import java.util.logging.Logger
import java.util.regex.Pattern

/**
 * [DataStreamProvider] for Twitch chat messages including subscriptions,
 * bits and chat messages
 */
class TwitchProvider(private val webSocketClient: OkHttpClient,
                     private val request: Request,
                     private val logger: Logger
): WebSocketListener(), DataStreamProvider {

    // Stream of data is represented by a [Flow]
    private val _twitchEventFlow = MutableSharedFlow<StreamEvent>()
    override val eventStream = _twitchEventFlow.asSharedFlow()

    // USERNOTICE tag regex
    private val usernoticePattern = Pattern.compile("""^@(?<tags>.*?) :tmi\.twitch\.tv USERNOTICE #(?<channel>.*?) ?(:(?<message>.*))?$""")
    // PRIVMSG tag regex
    private val privmsgPattern = Pattern.compile("""^@(?<tags>.*?) (:(?<user>.*?)!.*?\.tmi\.twitch\.tv) PRIVMSG #(?<channel>.*?) :(?<msg>.*)$""")

    private var webSocket: WebSocket? = null
    private lateinit var targetChannelName: String
    private lateinit var accessToken: String
    private lateinit var username: String

    override fun startDataStream(dataStreamConfig: DataStreamConfig) {
        when (dataStreamConfig) {
            is DataStreamConfig.TwitchConfig -> {
                webSocket = webSocketClient.newWebSocket(request, this)
                webSocketClient.connectionPool.evictAll()
                this.targetChannelName = dataStreamConfig.targetChannelName
                this.accessToken = dataStreamConfig.accessToken
                this.username = dataStreamConfig.username
            }
            is DataStreamConfig.StreamLabsConfig -> {
                logger.info("The plugin has been corrupted: Wrong Config!!")
            }
        }
    }

    override fun stopDataStream() {
        webSocket?.close(1000, null)
        webSocket = null
    }

    override fun clean() {
        webSocketClient.dispatcher.executorService.shutdown()
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        // Acquire capabilities
        webSocket.send("CAP REQ :twitch.tv/tags twitch.tv/commands twitch.tv/membership")
        // Login and configure user
        webSocket.send("PASS oauth:$accessToken")
        webSocket.send("NICK $username")
        webSocket.send("USER $username 8 * :$username")
        // Join specified channel
        webSocket.send("JOIN #${targetChannelName.lowercase()}")
    }

    /**
     * Called on each Websocket message received from Twitch
     *
     * @param webSocket the [WebSocket] instance the message was received on
     * @param text the [String] representation of the received message
     */
    override fun onMessage(webSocket: WebSocket, text: String) {
        val privMatcher = privmsgPattern.matcher(text.trim())
        val usernoticeMatcher = usernoticePattern.matcher(text.trim())
        CoroutineScope(Dispatchers.IO).launch {
            when {
                // Handle PING <-> PONG
                text.contains("PING :tmi.twitch.tv") -> {
                    webSocket.send("PONG :tmi.twitch.tv")
                }
                // On chat message
                privMatcher.matches() -> {
                    val tags = privMatcher.group("tags").parseTags()
                    val message = privMatcher.group("msg")
                    _twitchEventFlow.emit(StreamEvent.TwitchChatMessage(tags["display-name"] ?: privMatcher.group("user"), message))
                }
                // If a sub/gifted sub/bits donation
                usernoticeMatcher.matches() -> {
                    val tags = usernoticeMatcher.group("tags").parseTags()
                    when (tags["msg-id"]) {
                        // Individual sub messages
                        "sub", "resub", "subgift", "anonsubgift" -> {
                            _twitchEventFlow.emit(StreamEvent.TwitchSubscription(tags["system-msg"].parseSystemMsg(), tags["display-name"].toString()))
                        }
                        // User gifted X subs to the community
                        "submysterygift" -> {
                            _twitchEventFlow.emit(StreamEvent.TwitchMassGiftMessage(tags["system-msg"].parseSystemMsg()))
                        }
                    }
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