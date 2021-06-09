package com.cniekirk.twitchhook

import com.cniekirk.twitchhook.actions.Configuration
import kotlinx.coroutines.FlowPreview
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import java.util.concurrent.ThreadLocalRandom

/**
 * Parses Twitch IRC tags into a [Map]
 * @see [Tags][https://dev.twitch.tv/docs/irc/tags]
 *
 * @return [Map] containing tags mapped to their respective values
 */
fun String.parseTags(): Map<String, String> =
    this.split(";").associate {
        val (left, right) = it.split("=")
        left to right
    }

/**
 * Removes JSON encoded whitespace and replaces with real whitespace
 *
 * @return
 */
fun String?.parseSystemMsg(): String = this.toString().replace("""\s""", " ")

/**
 * Spawns the [EntityType] into the world and optionally names it
 *
 * @param type the [String] value that maps to an [EntityType]
 * @param named whether or not this Entity should be named
 * @param name the (optional) name to give the entity
 */
@FlowPreview
fun Player.spawnEntity(type: String, named: Boolean, name: String = "") {
    Bukkit.getServer().scheduler.scheduleSyncDelayedTask(TwitchHook.plugin()) {
        val entity = this.player?.world?.spawnEntity(
            this.player?.location!!.add(1.0, 1.0, 1.0),
            EntityType.valueOf(type)
        )
        if (named) {
            entity?.customName = name
            entity?.isCustomNameVisible = true
        }
    }
}

/**
 *
 */
@FlowPreview
fun Player.manipulateBlock(action: String) {
    Bukkit.getServer().scheduler.scheduleSyncDelayedTask(TwitchHook.plugin()) {
        val chunk = this.player?.location?.chunk!!
        val xCoord = ThreadLocalRandom.current().nextInt(16) + chunk.x
        val zCoord = ThreadLocalRandom.current().nextInt(16) + chunk.z
        val block = this.player?.world?.getHighestBlockAt(xCoord, zCoord)
        when {
            action.equals("fire", true) -> {
                if (block?.getRelative(BlockFace.UP)?.type?.equals(Material.AIR)!!) {
                    block.getRelative(BlockFace.UP).type = Material.FIRE
                }
            }
            action.equals("delete", true) -> {
                block?.type = Material.AIR
            }
            action.equals("water", true) -> {
                if (block?.getRelative(BlockFace.UP)?.type?.equals(Material.AIR)!!) {
                    block.getRelative(BlockFace.UP).type = Material.WATER
                }
            }
        }
    }
}

fun String.parseConfiguration(): Configuration {
    val elements = this.split(" ")
    return when (elements[0]) {
        "SPAWN" -> {
            Configuration.SpawnEntityAction(elements[1], elements.getOrNull(2).toBoolean())
        }
        "BLOCK" -> {
            Configuration.ManipulateBlockAction(elements[1])
        }
        else -> {
            // Default just spawns a zombie
            Configuration.SpawnEntityAction("ZOMBIE", true)
        }
    }
}