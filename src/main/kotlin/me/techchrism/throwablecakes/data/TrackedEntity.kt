package me.techchrism.throwablecakes.data

import org.bukkit.entity.LivingEntity
import org.bukkit.util.Vector

data class TrackedEntity(
    val entity: LivingEntity,
    val relativePosition: Vector,
    val initialDirection: Vector,
    val hitVector: Vector,
    var trackedTicks: Int = 0
)