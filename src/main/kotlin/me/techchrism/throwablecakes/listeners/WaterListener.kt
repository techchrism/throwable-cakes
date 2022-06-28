package me.techchrism.throwablecakes.listeners

import me.techchrism.throwablecakes.ThrowableCakes
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionType

class WaterListener : Listener {
    @EventHandler
    private fun onWaterUse(event: PlayerInteractAtEntityEvent) {
        val item = event.player.inventory.getItem(event.hand) ?: return
        if(item.type != Material.POTION) return
        val meta = (item.itemMeta ?: return) as PotionMeta
        if(meta.basePotionData.type != PotionType.WATER) return
        
        val tracker = ThrowableCakes.tracker
        
        val entity: LivingEntity? = if(event.rightClicked is LivingEntity) event.rightClicked as LivingEntity else let {
            for(cake in tracker.cakes) {
                if(event.rightClicked == cake.stand || cake.stand.passengers.contains(event.rightClicked)) {
                    if(cake.trackedEntity != null) {
                        return@let cake.trackedEntity!!.entity
                    }
                    return@let null
                }
            }
            return@let null
        }
        
        if(entity == null) return
        event.isCancelled = true
        
        val removed = tracker.cakes.removeIf { 
            val tracked = it.trackedEntity
            if(tracked?.entity == entity) {
                for (passenger in it.stand.passengers) {
                    passenger.remove()
                }
                it.stand.remove()
                true
            } else {
                false
            }
        }
        
        if(removed) {
            entity.world.playSound(entity.location, Sound.ENTITY_FISHING_BOBBER_SPLASH, 1.0F, 2.0F)
        }
    }
}