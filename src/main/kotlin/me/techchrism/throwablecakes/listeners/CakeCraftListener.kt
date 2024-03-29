package me.techchrism.throwablecakes.listeners

import me.techchrism.throwablecakes.data.CakeOptions
import me.techchrism.throwablecakes.data.DyeToColor
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.inventory.ItemStack

class CakeCraftListener : Listener {
    @EventHandler(ignoreCancelled = true)
    private fun onCakeCraft(event: PrepareItemCraftEvent) {
        var feathers = 0
        var sugar = 0
        var honey = 0
        var cake: ItemStack? = null
        var dye: DyeColor? = null
        var tracker = 0
        var firework = 0
        
        for(item in event.inventory.contents) {
            if(item.type == Material.FEATHER) feathers++
            else if(item.type == Material.SUGAR) sugar++
            else if(item.type == Material.HONEY_BOTTLE) honey++
            else if(item.type == Material.REDSTONE_TORCH) tracker++
            else if(item.type == Material.FIREWORK_ROCKET) firework++
            else if(item.type == Material.CAKE && cake == null) cake = item
            else {
                val color = DyeToColor.mapping[item.type]
                if(color != null && dye == null) {
                    dye = color
                }
                else if(item.type != Material.AIR) return
            }
        }
        
        if(feathers > 1 || sugar > 1 || honey > 1 || tracker > 1 || firework > 1 || cake == null) return
        
        event.inventory.result = CakeOptions.generateCakeItem(
            color = dye,
            trail = (sugar == 1),
            slow = (feathers == 1),
            sticky = (honey == 1),
            firework = (firework == 1),
            trackingUUID = (if(tracker == 1) event.viewers[0].uniqueId else null),
            existingOptions = CakeOptions.fromItem(cake)
        )
    }
}