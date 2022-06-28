package me.techchrism.throwablecakes.listeners

import me.techchrism.throwablecakes.ThrowableCakes
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class FreezeListener : Listener {
    @EventHandler
    private fun onFreezeInteract(event: PlayerInteractEvent) {
        if ((event.action == Action.RIGHT_CLICK_AIR ||
                    (event.action == Action.RIGHT_CLICK_BLOCK && !event.player.isSneaking))
            && event.item?.type == Material.AMETHYST_SHARD) {
            val frozen = ThrowableCakes.tracker.let { it.frozen = !it.frozen; it.frozen}
            event.player.sendMessage("Frozen is now ${frozen}")
            event.isCancelled = true
        }
    }
}