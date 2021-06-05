package com.cniekirk.twitchhook

fun String.parseTags(): Map<String, String> {
    val tagMap = HashMap<String, String>()
    this.split(";").forEach {
        val tagPair = it.split("=")
        if (tagPair.size > 1) {
            tagMap[tagPair[0]] = tagPair[1]
        } else {
            tagMap[tagPair[0]] = ""
        }
    }
    return tagMap
}