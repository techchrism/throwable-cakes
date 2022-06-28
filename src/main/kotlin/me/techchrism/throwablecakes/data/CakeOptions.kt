package me.techchrism.throwablecakes.data

import me.techchrism.throwablecakes.ThrowableCakes
import org.bukkit.ChatColor
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class CakeOptions(
    val splashParticleMaterials: List<Material>? = null,
    val dripParticleMaterials: List<Material>? = null,
    val trailParticleMaterials: List<Material>? = null,
    val bitesTaken: Int = 0,
    val slideDownDelayTicks: Int = 50,
    val slideDownSpeed: Double = 1.0,
    val speedMultiplier: Double = 1.0
) {
    companion object {
        fun materialsFromColor(color: DyeColor): List<Material> {
            return listOf(
                Material.valueOf("${color.name}_WOOL"),
                Material.valueOf("${color.name}_CONCRETE"),
                Material.valueOf("${color.name}_CONCRETE_POWDER")
            )
        }
        
        fun generateCakeItem(color: DyeColor?, trail: Boolean = false, sticky: Boolean = false, slow: Boolean = false) : ItemStack {
            val item = ItemStack(Material.CAKE, 1)
            val meta = item.itemMeta!!
            if(color != null) {
                meta.setDisplayName("${color.name.uppercase()} Cake")
                meta.persistentDataContainer.set(ThrowableCakes.colorKey, PersistentDataType.STRING, color.name)
            }
            
            val desc = ArrayList<String>()
            if(trail) {
                desc.add("${ChatColor.GOLD} ● Trail")
                meta.persistentDataContainer.set(ThrowableCakes.trailKey, PersistentDataType.BYTE, 1)
            }
            if(sticky) {
                desc.add("${ChatColor.GOLD} ● Sticky")
                meta.persistentDataContainer.set(ThrowableCakes.slideDownDelayTicksKey, PersistentDataType.INTEGER, -1)
            }
            if(slow) {
                desc.add("${ChatColor.GOLD} ● Slow")
                meta.persistentDataContainer.set(ThrowableCakes.speedKey, PersistentDataType.FLOAT, 0.3F)
            }
            meta.lore = desc
            item.itemMeta = meta
            return item
        }
    }
}