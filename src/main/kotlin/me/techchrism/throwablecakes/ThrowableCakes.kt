package me.techchrism.throwablecakes

import org.bukkit.*
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector

data class ThrownCake(val stand: Entity, val velocity: Vector, val thrower: Player, var deadTime: Int = -1)

class ThrowableCakes : JavaPlugin(), Listener {
    private val cakes: HashSet<ThrownCake> = HashSet()
    private val earthGravity = Vector(0.0, -9.807, 0.0)
    private val minecraftGravity = earthGravity.clone().multiply(2.9)
    private val gravityPerTick = minecraftGravity.clone().multiply(0.05)
    
    private fun Entity.teleportWithPassengers(location: Location) {
        val passengerSet: HashSet<Entity> = HashSet()
        for(passenger in passengers) {
            passengerSet.add(passenger)
        }
        for(passenger in passengerSet) {
            removePassenger(passenger)
        }
        teleport(location)
        for(passenger in passengerSet) {
            addPassenger(passenger)
        }
    }
    
    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this)
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, {
            cakes.removeIf { !(it.stand.isValid) }
            
            for(cake in cakes) {
                if(cake.deadTime != -1) {
                    cake.deadTime++
                    if(cake.deadTime < 500) continue
                    
                    
                    for(passenger in cake.stand.passengers) {
                        passenger.remove()
                    }
                    cake.stand.remove()
                }
                
                cake.velocity.add(gravityPerTick)
                val diff = cake.velocity.clone().multiply(0.05)

                val traceResult = cake.stand.world.rayTrace(
                    cake.stand.location,
                    diff.clone().normalize(),
                    diff.length(),
                    FluidCollisionMode.NEVER,
                    true,
                    0.5
                ) {
                    //it != cake.thrower && it != cake.stand && !cake.stand.passengers.contains(it)
                    false
                }

                if(traceResult != null) {
                    cake.deadTime = 0
                    var loc: Location
                    with(traceResult.hitPosition) {
                        loc = Location(cake.stand.world, x, y, z)
                    }
                    cake.stand.teleportWithPassengers(loc)
                    cake.stand.world.playSound(cake.stand.location, Sound.BLOCK_MUD_BREAK, 1.0F, 1.0F)
                } else {
                    cake.stand.teleportWithPassengers(cake.stand.location.add(diff))
                }
                
                
            }
        }, 1L, 1L)
    }
    
    private fun velocityDif(entity: Entity, location: Location) {
        entity.velocity = location.toVector().subtract(entity.location.toVector()).normalize()
    }
    
    @EventHandler
    private fun onCakeInteract(event: PlayerInteractEvent) {
        if (event.action == Action.RIGHT_CLICK_AIR && event.hand == EquipmentSlot.HAND && event.player.inventory.itemInMainHand.type == Material.CAKE) {
            //val snowball = event.player.launchProjectile(Snowball::class.java)
            //snowball.setMetadata("cake", FixedMetadataValue(this, true))
            
            /*val stand = event.player.world.spawn(event.player.eyeLocation, ArmorStand::class.java) {
                with(it) {
                    setArms(false)
                    setGravity(false)
                    setAI(false)
                    setBasePlate(false)
                    isSilent = true
                    isSmall = true
                    isVisible = false
                    isMarker = true
                    //equipment?.helmet = ItemStack(Material.CAKE, 1)
                    //equipment?.setItemInMainHand(ItemStack(Material.CAKE, 1))
                }
            }*/
            val stand = event.player.world.spawn(event.player.eyeLocation, Minecart::class.java) {
                with(it) {
                    
                    setGravity(false)
                    
                    //equipment?.helmet = ItemStack(Material.CAKE, 1)
                    //equipment?.setItemInMainHand(ItemStack(Material.CAKE, 1))
                }
            }
            val sand = event.player.world.spawnFallingBlock(event.player.eyeLocation, Material.CAKE.createBlockData())
            sand.setGravity(false)
            stand.addPassenger(sand)
            
            cakes.add(ThrownCake(stand, event.player.eyeLocation.direction.clone().multiply(30), event.player))
        }
    }
}