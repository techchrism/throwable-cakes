package me.techchrism.throwablecakes.listeners

import me.techchrism.throwablecakes.ThrowableCakes
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

class FreezeListener : Listener {
    @EventHandler
    private fun onFreezeInteract(event: PlayerInteractEvent) {
        if (event.action == Action.RIGHT_CLICK_AIR || (event.action == Action.RIGHT_CLICK_BLOCK && !event.player.isSneaking)) {
            val item = event.item ?: return
            val meta = item.itemMeta ?: return
            if(!meta.persistentDataContainer.has(ThrowableCakes.timeCrystalKey, PersistentDataType.BYTE)) return
            
            val frozen = ThrowableCakes.tracker.let { it.frozen = !it.frozen; it.frozen}
            event.player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("${ChatColor.LIGHT_PURPLE}Time is ${if(frozen) "now" else "no longer"} frozen")[0])
            event.player.playSound(event.player.location, Sound.ENTITY_ENDER_EYE_DEATH, 1.0F, 0.4F)
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun onFreezeDispense(event: BlockDispenseEvent) {
        val block = event.block
        val item = event.item
        val meta = item.itemMeta ?: return
        if(!meta.persistentDataContainer.has(ThrowableCakes.timeCrystalKey, PersistentDataType.BYTE)) return
        
        if(block.type == Material.DISPENSER) {
            event.isCancelled = true
            ThrowableCakes.tracker.let { it.frozen = !it.frozen; it.frozen}
        }
    }
}