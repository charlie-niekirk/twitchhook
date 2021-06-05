package com.cniekirk.twitchhook

sealed class StreamEvent {
    data class TwitchSubscription(val systemMessage: String, val gifter: String): StreamEvent()
    data class TwitchMassGiftMessage(val systemMessage: String): StreamEvent()
    data class TwitchChatMessage(val username: String,
                                 val content: String): StreamEvent()
}
