package me.techchrism.throwablecakes.listeners

import me.techchrism.throwablecakes.ThrowableCakes
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.player.PlayerInteractEvent

class ThrowListener : Listener {
    @EventHandler
    private fun onCakeInteract(event: PlayerInteractEvent) {
        if ((event.action == Action.RIGHT_CLICK_AIR ||
                    (event.action == Action.RIGHT_CLICK_BLOCK && !event.player.isSneaking))
            && event.item?.type == Material.CAKE) {
            ThrowableCakes.tracker.throwCake(event.player)
            event.isCancelled = true
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    private fun onCakeDispense(event: BlockDispenseEvent) {
        val block = event.block
        if(event.item.type == Material.CAKE && block.type == Material.DISPENSER) {
            event.isCancelled = true
            ThrowableCakes.tracker.dispenseCake(block)
        }
    }
}