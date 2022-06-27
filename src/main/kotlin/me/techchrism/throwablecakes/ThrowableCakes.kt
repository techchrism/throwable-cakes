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
import kotlin.math.pow
import kotlin.random.Random

data class TrackedEntity(val entity: LivingEntity,
                         val relativePosition: Vector,
                         val initialDirection: Vector,
                         var trackedTicks: Int = 0)
data class ThrownCake(
    val stand: Entity,
    val velocity: Vector,
    val thrower: Player,
    var stillTicks: Int = -1,
    var trackedEntity: TrackedEntity? = null)

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

                val tracked = cake.trackedEntity
                if(tracked != null) {
                    if(!tracked.entity.isValid) {
                        cake.trackedEntity = null
                    } else {
                        tracked.entity.freezeTicks = tracked.entity.maxFreezeTicks - 20
                        
                        tracked.trackedTicks++
                        var downOffset = 0.0
                        val downDelay = 50
                        if(tracked.trackedTicks > downDelay) {
                            downOffset = (tracked.trackedTicks - downDelay).toDouble().pow(2.5) * (0.05 * 0.001)
                            
                            if(Random.nextInt(0, 3) == 0) {
                                cake.stand.world.playSound(cake.stand.location, Sound.BLOCK_BEEHIVE_DRIP, 0.4F, Random.nextDouble(0.5, 1.5).toFloat())
                            }
                        }
                        
                        val currentAngle = with(tracked.entity.location.direction) { atan2(x, z) }
                        val initialAngle = with(tracked.initialDirection) { atan2(x, z) }

                        val newLoc = tracked.entity.location.clone().add(tracked.relativePosition.clone()
                            .rotateAroundY(currentAngle - initialAngle))
                            .add(0.0, -1 * downOffset, 0.0)
                        cake.stand.teleportWithPassengers(newLoc)
                        
                        if(newLoc.y < tracked.entity.location.y) {
                            for (passenger in cake.stand.passengers) {
                                passenger.remove()
                            }
                            cake.stand.remove()
                        }
                    }
                    
                } else if(cake.stillTicks != -1) {
                    cake.stillTicks++

                    if(cake.stillTicks >= 7) {
                        for (passenger in cake.stand.passengers) {
                            passenger.remove()
                        }
                        cake.stand.remove()
                    }
                    
                } else {
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
                        val loc = with(traceResult.hitPosition) { Location(cake.stand.world, x, y, z) }

                        val hitEntity = traceResult.hitEntity
                        if(hitEntity != null && hitEntity is LivingEntity) {
                            // Try to prevent z-fighting by adding a small random value
                            val relative = traceResult.hitPosition.clone().subtract(hitEntity.location.toVector()).addRandom()
                            cake.trackedEntity = TrackedEntity(hitEntity, relative, hitEntity.location.direction.clone())
                            cake.stand.teleportWithPassengers(hitEntity.location.clone().add(relative))

                            // Conservation of momentum (fancy stuff)
                            // Treat bounding box volume as substitute for mass
                            hitEntity.velocity = hitEntity.velocity.add(diff.clone().multiply(cakeVolume / hitEntity.boundingBox.volume))
                            
                            // Add particle effects
                            val orthogonal = diff.clone().setY(0).normalize().rotateAroundY(PI / 2)
                            val particleLoc = loc.clone().subtract(diff.clone().normalize().multiply(0.4))
                            for(i in 0..500) {
                                val vel = diff.clone().normalize()
                                    .rotateAroundNonUnitAxis(orthogonal, Random.nextDouble(0.0, PI / 4) * -1)
                                    .rotateAroundNonUnitAxis(diff, Random.nextDouble(-0.5 * PI, 0.5 * PI))
                                    .multiply(Random.nextDouble(0.3, 0.5))
                                with(vel) {
                                    hitEntity.world.spawnParticle(Particle.SNOWFLAKE, particleLoc, 0, x, y, z)
                                }
                                
                            }
                        } else {
                            cake.stand.teleportWithPassengers(loc)
                            cake.stillTicks = 0
                        }

                        for(i in 0..4) {
                            cake.stand.world.playSound(cake.stand.location, Sound.BLOCK_MUD_BREAK, 1.0F, Random.nextDouble(0.5, 1.0).toFloat())
                        }
                    } else {
                        cake.stand.teleportWithPassengers(cake.stand.location.add(diff))
                    }
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
            //cakeData.bites = Random.nextInt(1, cakeData.maximumBites)
            val sand = event.player.world.spawnFallingBlock(event.player.eyeLocation, cakeData)
            sand.setGravity(false)
            stand.addPassenger(sand)
            
            cakes.add(ThrownCake(stand, event.player.eyeLocation.direction.clone().multiply(30), event.player))
        }
    }
}