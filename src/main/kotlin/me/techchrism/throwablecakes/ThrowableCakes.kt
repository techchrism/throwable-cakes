package me.techchrism.throwablecakes

import me.techchrism.throwablecakes.listeners.CakeCraftListener
import me.techchrism.throwablecakes.listeners.FreezeListener
import me.techchrism.throwablecakes.listeners.ThrowListener
import me.techchrism.throwablecakes.listeners.WaterListener
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

class ThrowableCakes : JavaPlugin() {
    companion object {
        val tracker = CakeTracker()
        
        lateinit var colorKey: NamespacedKey
        lateinit var trailKey: NamespacedKey
        lateinit var slideDownDelayTicksKey: NamespacedKey
        lateinit var speedKey: NamespacedKey
        lateinit var timeCrystalKey: NamespacedKey
        lateinit var trackingUUIDKey: NamespacedKey
    }
    
    override fun onEnable() {
        colorKey = NamespacedKey.fromString("color", this)!!
        trailKey = NamespacedKey.fromString("trail", this)!!
        slideDownDelayTicksKey = NamespacedKey.fromString("slidedelay", this)!!
        speedKey = NamespacedKey.fromString("speed", this)!!
        timeCrystalKey = NamespacedKey.fromString("timecrystal", this)!!
        trackingUUIDKey = NamespacedKey.fromString("trackinguuid", this)!!
        
        Bukkit.getPluginManager().registerEvents(ThrowListener(), this)
        Bukkit.getPluginManager().registerEvents(FreezeListener(), this)
        Bukkit.getPluginManager().registerEvents(WaterListener(), this)
        Bukkit.getPluginManager().registerEvents(CakeCraftListener(), this)
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, {
            tracker.tick()
        }, 1L, 1L)
        
        val timeCrystalItem = ItemStack(Material.AMETHYST_SHARD, 1)
        val timeCrystalMeta = timeCrystalItem.itemMeta!!
        timeCrystalMeta.setDisplayName("${ChatColor.LIGHT_PURPLE}Time Crystal")
        timeCrystalMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        timeCrystalMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, true)
        timeCrystalMeta.persistentDataContainer.set(timeCrystalKey, PersistentDataType.BYTE, 1)
        timeCrystalItem.itemMeta = timeCrystalMeta
        val timeCrystalRecipe = ShapelessRecipe(timeCrystalKey, timeCrystalItem)
        timeCrystalRecipe.addIngredient(Material.CAKE)
        timeCrystalRecipe.addIngredient(Material.AMETHYST_SHARD)
        Bukkit.addRecipe(timeCrystalRecipe)
    }

    override fun onDisable() {
        Bukkit.removeRecipe(timeCrystalKey)
        tracker.cakes.removeIf {
            for(passenger in it.stand.passengers) {
                passenger.remove()
            }
            it.stand.remove()
            true
        }
    }
}