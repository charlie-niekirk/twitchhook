package com.cniekirk.twitchhook

fun String.parseTags(): Map<String, String> {
    val tagMap = HashMap<String, String>()
    this.split(",").forEach {
        val tagPair = it.split("=")
        tagMap[tagPair[0]] = tagPair[1]
    }
    return tagMap
}