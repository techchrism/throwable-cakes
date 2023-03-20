package me.techchrism.throwablecakes.data

import me.techchrism.throwablecakes.ThrowableCakes
import org.bukkit.ChatColor
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*
import kotlin.collections.ArrayList

data class CakeOptions(
    val splashParticleMaterials: List<Material>? = defaultMaterial,
    val dripParticleMaterials: List<Material>? = defaultMaterial,
    val trailParticleMaterials: List<Material>? = null,
    val color: DyeColor = DyeColor.WHITE,
    val bitesTaken: Int = 0,
    val slideDownDelayTicks: Int = 50,
    val slideDownSpeed: Double = 1.0,
    val speedMultiplier: Double = 1.0,
    val firework: Boolean = false,
    val trackingUUID: UUID? = null
) {
    companion object {
        val defaultMaterial = materialsFromColor(DyeColor.WHITE)
        
        fun materialsFromColor(color: DyeColor): List<Material> {
            return listOf(
                Material.valueOf("${color.name}_WOOL"),
                Material.valueOf("${color.name}_CONCRETE"),
                Material.valueOf("${color.name}_CONCRETE_POWDER")
            )
        }
        
        fun fromItem(item: ItemStack) : CakeOptions? {
            val meta = item.itemMeta ?: return null
            val data = meta.persistentDataContainer
            val color = data.get(ThrowableCakes.colorKey, PersistentDataType.STRING)?.let { DyeColor.valueOf(it) } ?: DyeColor.WHITE
            val materials = materialsFromColor(color)
            
            return CakeOptions(
                splashParticleMaterials = materials,
                dripParticleMaterials = materials,
                trailParticleMaterials = if(data.get(ThrowableCakes.trailKey, PersistentDataType.BYTE) == 1.toByte()) materials else null,
                color = color,
                slideDownDelayTicks = data.get(ThrowableCakes.slideDownDelayTicksKey, PersistentDataType.INTEGER) ?: 50,
                speedMultiplier = data.get(ThrowableCakes.speedKey, PersistentDataType.FLOAT)?.toDouble() ?: 1.0,
                firework = data.get(ThrowableCakes.fireworkKey, PersistentDataType.BYTE) == 1.toByte(),
                trackingUUID = data.get(ThrowableCakes.trackingUUIDKey, PersistentDataType.STRING)?.let { UUID.fromString(it) }
            )
        }
        
        fun generateCakeItem(color: DyeColor?,
                             trail: Boolean = false,
                             sticky: Boolean = false,
                             slow: Boolean = false,
                             firework: Boolean = false,
                             trackingUUID: UUID? = null,
                             existingOptions: CakeOptions? = null) : ItemStack {
            
            val item = ItemStack(Material.CAKE, 1)
            val meta = item.itemMeta!!
            val newColor = color ?: existingOptions?.color
            if(newColor != null) {
                meta.setDisplayName("${DyeToColor.chat[newColor]}${newColor.name.lowercase()
                    .replace('_', ' ')
                    .replaceFirstChar { it.uppercase() }} Cake")
                meta.persistentDataContainer.set(ThrowableCakes.colorKey, PersistentDataType.STRING, newColor.name)
            }
            
            val desc = ArrayList<String>()
            if(trail || existingOptions?.trailParticleMaterials != null) {
                desc.add("${ChatColor.GOLD} ● Trail")
                meta.persistentDataContainer.set(ThrowableCakes.trailKey, PersistentDataType.BYTE, 1)
            }
            if(sticky || existingOptions?.slideDownDelayTicks == -1) {
                desc.add("${ChatColor.GOLD} ● Sticky")
                meta.persistentDataContainer.set(ThrowableCakes.slideDownDelayTicksKey, PersistentDataType.INTEGER, -1)
            }
            val existingSpeed = existingOptions?.speedMultiplier
            if(slow || (existingSpeed != null && existingSpeed < 1.0)) {
                desc.add("${ChatColor.GOLD} ● Slow")
                meta.persistentDataContainer.set(ThrowableCakes.speedKey, PersistentDataType.FLOAT, 0.3F)
            }
            if(trackingUUID != null || existingOptions?.trackingUUID != null) {
                val uuid = (trackingUUID?:existingOptions!!.trackingUUID)!!
                desc.add("${ChatColor.GOLD} ● Tracking" + (if(uuid.version() == 0) {" (self)"} else {""}))
                meta.persistentDataContainer.set(ThrowableCakes.trackingUUIDKey, PersistentDataType.STRING, uuid.toString())
            }
            if(firework || existingOptions?.firework == true) {
                desc.add("${ChatColor.GOLD} ● Firework")
                meta.persistentDataContainer.set(ThrowableCakes.fireworkKey, PersistentDataType.BYTE, 1)
            }
            meta.lore = desc
            item.itemMeta = meta
            return item
        }
    }
}