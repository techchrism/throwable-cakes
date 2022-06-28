package me.techchrism.throwablecakes

import org.bukkit.entity.LivingEntity
import org.bukkit.util.Vector

data class TrackedEntity(
    val entity: LivingEntity,
    val relativePosition: Vector,
    val initialDirection: Vector,
    var trackedTicks: Int = 0
)