package me.techchrism.throwablecakes

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

class ThrowableCakes : JavaPlugin(), Listener {
    companion object {
        val tracker = CakeTracker()
    }
    
    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this)
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, {
            tracker.tick()
        }, 1L, 1L)
    }

    private fun drawVector(vector: Vector, location: Location, resolution: Double) {
        val steps = (vector.length() / resolution).roundToInt()
        val unit = vector.clone().multiply(1 / steps.toDouble())
        for(i in 0..steps) {
            val loc = location.clone().add(unit.clone().multiply(i))
            loc.world?.spawnParticle(Particle.FALLING_DUST, loc, 0, Material.ORANGE_CONCRETE.createBlockData())
        }
    }
    
    @EventHandler
    private fun onCakeInteract(event: PlayerInteractEvent) {
        if (event.action == Action.RIGHT_CLICK_AIR && event.item?.type == Material.CAKE) {
            tracker.throwCake(event.player)
        }
    }
}