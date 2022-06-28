package me.techchrism.throwablecakes

import me.techchrism.throwablecakes.listeners.ThrowListener
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
import kotlin.math.roundToInt

class ThrowableCakes : JavaPlugin() {
    companion object {
        val tracker = CakeTracker()
    }
    
    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(ThrowListener(), this)
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, {
            tracker.tick()
        }, 1L, 1L)
    }
}