package com.cniekirk.twitchhook

import com.cniekirk.twitchhook.actions.Configuration
import kotlinx.coroutines.FlowPreview
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import java.util.concurrent.ThreadLocalRandom

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

fun String?.parseSystemMsg(): String = this.toString().replace("""\s""", " ")

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