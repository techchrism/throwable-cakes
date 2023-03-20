package me.techchrism.throwablecakes.data

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.util.Vector

data class ThrownCake(
    val stand: Entity,
    val velocity: Vector,
    val thrower: Player?,
    val options: CakeOptions,
    val spin: Float = 0.0F,
    val size: Float = 1.0F,
    val spinVel: Float = 0.1F,
    var stillTicks: Int = -1,
    var trackedEntity: TrackedEntity? = null,
)