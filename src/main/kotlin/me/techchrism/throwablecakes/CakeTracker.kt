package me.techchrism.throwablecakes

import me.techchrism.throwablecakes.data.CakeOptions
import me.techchrism.throwablecakes.data.ThrownCake
import me.techchrism.throwablecakes.data.TrackedEntity
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.data.type.Cake
import org.bukkit.block.data.type.Dispenser
import org.bukkit.entity.*
import org.bukkit.util.Vector
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

class CakeTracker {
    private val earthGravity = Vector(0.0, -9.807, 0.0)
    private val minecraftGravity = earthGravity.clone().multiply(2.9)
    private val gravityPerTick = minecraftGravity.clone().multiply(0.05)

    // Subtract 0.125 from a side for each bite
    private val cakeVolume = (0.5 * 0.875 * 0.875)
    
    private val snowflakeAllocator = ParticleAllocator(4000, 60)
    
    private val cakes: HashSet<ThrownCake> = HashSet()
    var frozen = false
    
    fun tick() {
        snowflakeAllocator.tick()
        cakes.removeIf { !(it.stand.isValid) }

        val particleLimit = 50
        var particleCount = 0

        for(cake in cakes) {
            for(passenger in cake.stand.passengers) {
                if(passenger is FallingBlock) {
                    passenger.ticksLived = 1
                }
            }

            val tracked = cake.trackedEntity
            if(tracked != null) {
                // If attached to an entity
                if(!tracked.entity.isValid) {
                    cake.trackedEntity = null
                } else {
                    tracked.entity.freezeTicks = tracked.entity.maxFreezeTicks - 20

                    // Slide down after a certain delay
                    if(!frozen) tracked.trackedTicks++
                    var downOffset = 0.0
                    if(tracked.trackedTicks > cake.options.slideDownDelayTicks) {
                        downOffset = (tracked.trackedTicks - cake.options.slideDownDelayTicks).toDouble().pow(2.5) *
                                (0.05 * 0.001 * cake.options.slideDownSpeed)

                        if(!frozen && Random.nextInt(0, 3) == 0) {
                            cake.stand.world.playSound(cake.stand.location, Sound.BLOCK_BEEHIVE_DRIP, 0.4F, Random.nextDouble(0.5, 1.5).toFloat())
                        }
                    }

                    // Display falling dust particles
                    if(!frozen && cake.options.dripParticleMaterials != null) {
                        for(i in 1..15) {
                            if(particleCount >= particleLimit) break
                            particleCount++
                            val d = 0.7
                            val particleLoc = cake.stand.location.clone().add(Random.nextDouble(0.0,d)-(d/2), 0.5 - downOffset, Random.nextDouble(0.0,d)-(d/2))
                            cake.stand.world.spawnParticle(Particle.FALLING_DUST, particleLoc, 0, cake.options.dripParticleMaterials.randomItem().createBlockData())
                        }
                    }
                    
                    // Track rotation and relative position
                    val currentAngle = with(tracked.entity.location.direction) { atan2(x, z) }
                    val initialAngle = with(tracked.initialDirection) { atan2(x, z) }

                    val newLoc = tracked.entity.location.clone().add(tracked.relativePosition.clone()
                        .rotateAroundY(currentAngle - initialAngle))
                        .add(0.0, -1 * downOffset, 0.0)
                    cake.stand.teleportWithPassengers(newLoc)

                    // Remove the cake if it slides to the bottom of the entity
                    if(newLoc.y < tracked.entity.location.y) {
                        for (passenger in cake.stand.passengers) {
                            passenger.remove()
                        }
                        cake.stand.remove()
                    }
                }
            } else if(cake.stillTicks != -1) {
                // If the cake is sitting still
                if(!frozen) cake.stillTicks++

                // Remove the cake after some time (to let interp catch up)
                if(cake.stillTicks >= 4) {
                    for (passenger in cake.stand.passengers) {
                        passenger.remove()
                    }
                    cake.stand.remove()
                }

            } else {
                // If the cake is in motion
                if(frozen) {
                    val particleLoc = cake.stand.location.clone().add(cake.velocity.clone().normalize().multiply(-0.5))
                    with(cake.velocity.clone().normalize().multiply(-0.2)) {
                        cake.stand.world.spawnParticle(Particle.SMOKE_NORMAL, particleLoc, 0, x, y, z)
                    }
                    continue
                }
                
                cake.velocity.add(gravityPerTick)
                val diff = cake.velocity.clone().multiply(0.05)
                
                if(cake.options.trailParticleMaterials != null) {
                    val resolution = 0.1
                    val steps = (diff.length() / resolution).roundToInt()
                    val unit = diff.clone().multiply(-1 / steps.toDouble())
                    val origin = cake.stand.location.clone().subtract(diff.clone().normalize().multiply(1.5))
                    for(i in 0..steps) {
                        val loc = origin.clone().add(unit.clone().multiply(i))
                        loc.world?.spawnParticle(Particle.FALLING_DUST, loc, 0, cake.options.trailParticleMaterials.randomItem().createBlockData())
                    }
                }

                // Raytrace for collisions with blocks and entities
                val traceResult = cake.stand.world.rayTrace(
                    cake.stand.location,
                    diff.clone().normalize(),
                    diff.length(),
                    FluidCollisionMode.NEVER,
                    true,
                    0.0
                ) {
                    it is LivingEntity &&
                            !it.isInvisible &&
                            it != cake.thrower &&
                            it != cake.stand &&
                            !cake.stand.passengers.contains(it)
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
                    } else {
                        cake.stand.teleportWithPassengers(loc)
                        cake.stillTicks = 0
                    }

                    // Add particle effects
                    val orthogonal = diff.clone().setY(0).normalize().rotateAroundY(PI / 2)
                    val particleLoc = loc.clone().subtract(diff.clone().normalize().multiply(0.4))
                    for(i in 0..snowflakeAllocator.allocate(500)) {
                        val vel = diff.clone().normalize()
                            .rotateAroundNonUnitAxis(orthogonal, Random.nextDouble(0.0, PI / 4) * -1)
                            .rotateAroundNonUnitAxis(diff, Random.nextDouble(-0.5 * PI, 0.5 * PI))
                            .multiply(Random.nextDouble(0.3, 0.5))
                        with(vel) {
                            cake.stand.world.spawnParticle(Particle.SNOWFLAKE, particleLoc, 0, x, y, z)
                        }
                    }
                    if(cake.options.splashParticleMaterials != null) {
                        for(i in 0..5) {
                            cake.stand.world.spawnParticle(Particle.BLOCK_CRACK, loc, 70, cake.options.splashParticleMaterials.randomItem().createBlockData())
                        }
                    }

                    for(i in 0..4) {
                        cake.stand.world.playSound(cake.stand.location, Sound.BLOCK_MUD_BREAK, 0.5F, Random.nextDouble(0.2, 0.7).toFloat())
                        cake.stand.world.playSound(cake.stand.location, Sound.BLOCK_MUD_FALL, 1.0F, Random.nextDouble(0.9, 1.7).toFloat())
                    }
                } else {
                    cake.stand.teleportWithPassengers(cake.stand.location.add(diff))
                }
            }
        }
    }
    
    private fun <T> List<T>.randomItem(): T { 
        return get(Random.nextInt(size))
    }
    
    fun dispenseCake(dispenser: Block): ThrownCake? {
        val data = dispenser.blockData as Dispenser
        val direction = data.facing.direction
        val velocity = direction.clone().multiply(30)

        return addCake(dispenser.location.add(0.5, 0.5, 0.5).add(direction.clone().multiply(0.5)), velocity,
            CakeOptions(
                CakeOptions.materialsFromColor(DyeColor.LIME),
                CakeOptions.materialsFromColor(DyeColor.LIME),
                CakeOptions.materialsFromColor(DyeColor.LIME)
            ))
    }
    
    fun throwCake(thrower: Player) : ThrownCake? {
        // Raytrace for collisions with blocks and entities
        val distanceFromThrower = 2.5
        val origin = thrower.eyeLocation.clone().add(Vector(0.0, -0.5, 0.0))
        
        val traceResult = thrower.world.rayTrace(
            origin,
            origin.direction,
            distanceFromThrower,
            FluidCollisionMode.NEVER,
            true,
            0.0
        ) {
            it is LivingEntity && !it.isInvisible && it != thrower
        }

        val spawnLoc = if(traceResult != null) {
            with(traceResult.hitPosition) { Location(thrower.world, x, y, z) } 
        } else {
            origin.add(thrower.eyeLocation.direction.normalize().multiply(1.5))
        }
        val velocity = thrower.eyeLocation.direction.multiply(30).add(thrower.velocity)
        
        return addCake(spawnLoc, velocity,
            CakeOptions(
                CakeOptions.materialsFromColor(DyeColor.LIME),
                CakeOptions.materialsFromColor(DyeColor.LIME),
                CakeOptions.materialsFromColor(DyeColor.LIME)
            ), thrower)
    }
        
    private fun addCake(location: Location, velocity: Vector, options: CakeOptions, thrower: Player? = null): ThrownCake? {
        val world = location.world ?: return null
        val stand = world.spawn(location, ArmorStand::class.java) {
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
        cakeData.bites = options.bitesTaken
        val sand = world.spawnFallingBlock(location, cakeData)
        sand.setGravity(false)
        stand.addPassenger(sand)
        
        val cake = ThrownCake(stand, velocity, thrower, options)
        cakes.add(cake)
        world.playSound(location, Sound.ENTITY_SNOWBALL_THROW, 0.5F, 0.35F)
        return cake
    }
 
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

    private fun Vector.addRandom(): Vector {
        val range = 0.05
        this.x += Random.nextDouble(-1 * range, range)
        this.y += Random.nextDouble(-1 * range, range)
        this.z += Random.nextDouble(-1 * range, range)
        return this
    }
}