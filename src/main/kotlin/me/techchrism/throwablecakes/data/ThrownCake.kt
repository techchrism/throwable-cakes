package me.techchrism.throwablecakes.data

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.util.Vector

data class ThrownCake(
    val stand: Entity,
    val velocity: Vector,
    val thrower: Player?,
    val options: CakeOptions,
    var stillTicks: Int = -1,
    var trackedEntity: TrackedEntity? = null,
)