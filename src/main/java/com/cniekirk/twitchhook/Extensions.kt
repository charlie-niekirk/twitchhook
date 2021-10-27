package com.cniekirk.twitchhook

import com.cniekirk.twitchhook.actions.Configuration
import kotlinx.coroutines.FlowPreview
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.EntityType
import org.bukkit.entity.FallingBlock
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
        val playerBlock = this.player?.location?.block
        val xCoord = ThreadLocalRandom.current().nextInt(5) + playerBlock!!.x
        val zCoord = ThreadLocalRandom.current().nextInt(5) + playerBlock.z
        val block = this.player?.world?.getHighestBlockAt(xCoord, zCoord)
        block?.let {
            val above = player?.world?.getBlockAt(block.x, block.y + 1, block.z)
            above?.let {
                when {
                    action.equals("lava", true) -> {
                        if (above.type == Material.AIR) {
                            above.setType(Material.LAVA, true)
                        }
                    }
                    action.equals("delete", true) -> {
                        block.setType(Material.AIR, true)
                    }
                    action.equals("water", true) -> {
                        if (above.type == Material.AIR) {
                            above.setType(Material.WATER, true)
                        }
                    }
                }
            }
        }
    }
}

@FlowPreview
fun Player.dropGravityBlock(blockType: String) {
    Bukkit.getServer().scheduler.scheduleSyncDelayedTask(TwitchHook.plugin()) {
        when (blockType) {
            "anvil" -> {
                val playerBlock = this.player?.location?.block
                val x = ThreadLocalRandom.current().nextInt(5) + playerBlock!!.x
                val z = ThreadLocalRandom.current().nextInt(5) + playerBlock.z
                player?.world?.getBlockAt(x, playerBlock.y + 45, z)?.apply {
                    setType(Material.ANVIL, true)
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
        "RAIN" -> {
            Configuration.DropBlockAction(elements[1])
        }
        else -> {
            // Default just spawns a zombie
            Configuration.SpawnEntityAction("ZOMBIE", true)
        }
    }
}