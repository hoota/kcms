package kcms.common

import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

interface Caching {
    fun resetCaches()
}

@Component
class Caches(
    private val caches: List<Caching>
) {

    fun reset() {
        caches.forEach { it.resetCaches() }
    }

    @PostConstruct
    fun postContruct() {
        instance = this
    }

    companion object {
        lateinit var instance: Caches
    }
}