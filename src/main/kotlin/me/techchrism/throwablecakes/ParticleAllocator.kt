package me.techchrism.throwablecakes

import me.techchrism.throwablecakes.data.AllocatedParticles
import kotlin.math.roundToInt

class ParticleAllocator(private val maxCount: Int, private val expirationTicks: Int) {
    private var tick = 0
    private var count = 0
    private val allocations = HashSet<AllocatedParticles>()
    
    fun tick() {
        tick++
        allocations.removeIf { 
            if(it.expires <= tick) {
                count -= it.count
                true
            } else {
                false
            }
        }
    }
    
    fun allocate(amount: Int): Int {
        val allocation = ((1 - (count / maxCount.toDouble())) * amount).roundToInt()
        allocations.add(AllocatedParticles(allocation, (tick + expirationTicks).toLong()))
        count += allocation
        return allocation
    }
}