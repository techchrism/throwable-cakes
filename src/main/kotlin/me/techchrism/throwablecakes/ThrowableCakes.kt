package me.techchrism.throwablecakes

import me.techchrism.throwablecakes.listeners.FreezeListener
import me.techchrism.throwablecakes.listeners.ThrowListener
import me.techchrism.throwablecakes.listeners.WaterListener
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class ThrowableCakes : JavaPlugin() {
    companion object {
        val tracker = CakeTracker()
    }
    
    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(ThrowListener(), this)
        Bukkit.getPluginManager().registerEvents(FreezeListener(), this)
        Bukkit.getPluginManager().registerEvents(WaterListener(), this)
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, {
            tracker.tick()
        }, 1L, 1L)
    }
}