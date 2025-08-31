package kcms.common

import java.util.concurrent.TimeUnit

class OneValueCache<T>(
    private val timeout: Long = 30,
    private val timeunit: TimeUnit = TimeUnit.SECONDS,
    private val getter: () -> T
) {

    @Volatile
    private var cached: T? = null

    @Volatile
    private var updatedAt: Long = 0L

    val value: T? get() {
        val now = System.currentTimeMillis()

        return if(cached == null || now - updatedAt > timeunit.toMillis(timeout)) {
            synchronized(this) {
                if(cached == null || now - updatedAt > timeunit.toMillis(timeout)) {
                    cached = getter()
                    updatedAt = now
                }

                cached
            }
        } else cached
    }

    fun reset() {
        cached = null
    }
}