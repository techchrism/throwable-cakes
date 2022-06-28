package me.techchrism.throwablecakes.data

import org.bukkit.DyeColor
import org.bukkit.Material

class CakeOptions(
    val splashParticleMaterials: List<Material>? = null,
    val dripParticleMaterials: List<Material>? = null,
    val trailParticleMaterials: List<Material>? = null,
    val bitesTaken: Int = 0,
    val slideDownDelayTicks: Int = 5000,
    val slideDownSpeed: Double = 1.0
) {
    companion object {
        fun materialsFromColor(color: DyeColor): List<Material> {
            return listOf(
                Material.valueOf("${color.name}_WOOL"),
                Material.valueOf("${color.name}_CONCRETE"),
                Material.valueOf("${color.name}_CONCRETE_POWDER")
            )
        }
    }
}