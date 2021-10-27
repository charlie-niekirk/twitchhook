package com.cniekirk.twitchhook.actions

sealed class Configuration {
    data class SpawnEntityAction(val entityType: String, val named: Boolean): Configuration()
    data class PlaceBlockAction(val blockType: String): Configuration()
    data class ManipulateBlockAction(val action: String): Configuration()
    data class DropBlockAction(val blockType: String): Configuration()
}