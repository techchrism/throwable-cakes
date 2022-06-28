package me.techchrism.throwablecakes

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.util.Vector

data class ThrownCake(
    val stand: Entity,
    val velocity: Vector,
    val thrower: Player?,
    var stillTicks: Int = -1,
    var trackedEntity: TrackedEntity? = null,
)