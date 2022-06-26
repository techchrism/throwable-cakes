package me.techchrism.throwablecakes

import org.bukkit.*
import org.bukkit.block.data.type.Cake
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.random.Random

data class TrackedEntity(val entity: LivingEntity, val relativePosition: Vector, val initialDirection: Vector)
data class ThrownCake(val stand: Entity, val velocity: Vector, val thrower: Player, var deadTime: Int = -1, var trackedEntity: TrackedEntity? = null)

class ThrowableCakes : JavaPlugin(), Listener {
    private val cakes: HashSet<ThrownCake> = HashSet()
    private val earthGravity = Vector(0.0, -9.807, 0.0)
    private val minecraftGravity = earthGravity.clone().multiply(2.9)
    private val gravityPerTick = minecraftGravity.clone().multiply(0.05)
    
    // Subtract 0.125 from a side for each bite
    private val cakeVolume = (0.5 * 0.875 * 0.875)
    
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
                for(passenger in cake.stand.passengers) {
                    if(passenger is FallingBlock) {
                        passenger.ticksLived = 1
                    }
                }
                
                if(cake.deadTime != -1) {
                    cake.deadTime++
                    
                    val tracked = cake.trackedEntity
                    if(tracked != null) {
                        if(!tracked.entity.isValid) {
                            cake.trackedEntity = null
                        } else {
                            val currentAngle = with(tracked.entity.location.direction) { atan2(x, z) }
                            val initialAngle = with(tracked.initialDirection) { atan2(x, z) }
                            
                            val newLoc = tracked.entity.location.clone().add(tracked.relativePosition.clone()
                                .rotateAroundY(currentAngle - initialAngle))
                            cake.stand.teleportWithPassengers(newLoc)
                        }
                    }
                    
                    if(cake.deadTime > 10000) {
                        for (passenger in cake.stand.passengers) {
                            passenger.remove()
                        }
                        cake.stand.remove()
                    }
                    continue
                }
                
                cake.velocity.add(gravityPerTick)
                val diff = cake.velocity.clone().multiply(0.05)

                val traceResult = cake.stand.world.rayTrace(
                    cake.stand.location,
                    diff.clone().normalize(),
                    diff.length(),
                    FluidCollisionMode.NEVER,
                    true,
                    0.0
                ) {
                    it != cake.thrower &&
                            it != cake.stand &&
                            !cake.stand.passengers.contains(it) &&
                            it is LivingEntity &&
                            !it.isInvisible
                }

                if(traceResult != null) {
                    cake.deadTime = 0
                    var loc: Location
                    with(traceResult.hitPosition) {
                        loc = Location(cake.stand.world, x, y, z)
                    }
                    
                    val hitEntity = traceResult.hitEntity
                    if(hitEntity != null && hitEntity is LivingEntity) {
                        // Try to prevent z-fighting by adding a small random value
                        val relative = traceResult.hitPosition.clone().subtract(hitEntity.location.toVector()).addRandom()
                        cake.trackedEntity = TrackedEntity(
                            hitEntity,
                            relative,
                            hitEntity.location.direction.clone()
                        )
                        cake.stand.teleportWithPassengers(hitEntity.location.clone().add(relative))
                        
                        // Conservation of momentum (fancy stuff)
                        // Treat bounding box volume as substitute for mass
                        hitEntity.velocity = hitEntity.velocity.add(diff.clone().multiply(cakeVolume / hitEntity.boundingBox.volume))
                    } else {
                        cake.stand.teleportWithPassengers(loc)
                    }
                    
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
    
    private fun Vector.addRandom(): Vector {
        val range = 0.05
        this.x += Random.nextDouble(-1 * range, range)
        this.y += Random.nextDouble(-1 * range, range)
        this.z += Random.nextDouble(-1 * range, range)
        return this
    }
    
    @EventHandler
    private fun onCakeInteract(event: PlayerInteractEvent) {
        if (event.action == Action.RIGHT_CLICK_AIR && event.hand == EquipmentSlot.HAND && event.player.inventory.itemInMainHand.type == Material.CAKE) {
            val stand = event.player.world.spawn(event.player.eyeLocation, ArmorStand::class.java) {
                with(it) {
                    setArms(false)
                    setGravity(false)
                    setAI(false)
                    setBasePlate(false)
                    isSilent = true
                    isSmall = true
                    isVisible = false
                    isMarker = true
                }
            }
            val cakeData = Material.CAKE.createBlockData() as Cake
            cakeData.bites = Random.nextInt(1, cakeData.maximumBites)
            val sand = event.player.world.spawnFallingBlock(event.player.eyeLocation, cakeData)
            sand.setGravity(false)
            stand.addPassenger(sand)
            
            cakes.add(ThrownCake(stand, event.player.eyeLocation.direction.clone().multiply(30), event.player))
        }
    }
}