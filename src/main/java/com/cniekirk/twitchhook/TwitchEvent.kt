package com.cniekirk.twitchhook

sealed class TwitchEvent {
    data class GiftSubscription(val numSubs: Int,
                                val giftingUsername: String): TwitchEvent()
    data class NormalSubscription(val numSubs: Int,
                                  val username: String): TwitchEvent()
    data class Message(val username: String,
                       val content: String): TwitchEvent()
}
