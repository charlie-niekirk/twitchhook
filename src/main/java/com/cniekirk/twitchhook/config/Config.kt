package com.cniekirk.twitchhook.config

import com.squareup.moshi.Json

data class Config(
    @Json(name = "access_token")
    val accessToken: String,
    @Json(name = "username")
    val username: String
)