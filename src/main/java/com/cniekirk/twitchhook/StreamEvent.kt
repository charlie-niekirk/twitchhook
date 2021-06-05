package com.cniekirk.twitchhook

sealed class StreamEvent {
    data class TwitchGiftSubscription(val numSubs: Int,
                                      val giftingUsername: String): StreamEvent()
    data class TwitchNormalSubscription(val numSubs: Int,
                                        val username: String): StreamEvent()
    data class TwitchChatMessage(val username: String,
                                 val content: String): StreamEvent()
}
