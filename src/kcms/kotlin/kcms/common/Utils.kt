package kcms.common

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException
import java.util.*

fun <T: Collection<*>> T?.nullIfEmpty(): T? {
    return if(isNullOrEmpty()) null else this
}

inline fun <I, C : Collection<I>, R> C?.ifNotEmpty(delegate: (C) -> R): R? {
    return if(!this.isNullOrEmpty()) delegate(this) else null
}

inline fun <K, V, M : Map<K, V>, R> M?.ifNotEmpty(delegate: (M) -> R): R? {
    return if(!this.isNullOrEmpty()) delegate(this) else null
}

inline fun String?.isNotNullNorBlank(): Boolean = !this.isNullOrBlank()

fun String?.toUUIDOrNull(): UUID? {
    return try {
        this.nullIfBlank()?.let { UUID.fromString(it) }
    } catch(@Suppress("SwallowedException") e: IllegalArgumentException) {
        null
    }
}

inline fun <reified T : Any> String?.toObjectOrNull(objectMapper: ObjectMapper): T? = this.nullIfBlank()?.let {
    try {
        objectMapper.readValue(this, T::class.java)
    } catch(@Suppress("SwallowedException") e: IOException) {
        null
    }
}

fun JsonNode?.nullIfNull(): JsonNode? = if(this == null || this.isNull) null else this

fun String?.toBooleanOrNull(): Boolean? = when {
    this == null -> null
    "true".equals(this, ignoreCase = true) -> true
    "false".equals(this, ignoreCase = true) -> false
    else -> null
}

fun <T> List<T>.asReversed(reverse: Boolean): List<T> = if(reverse) this.asReversed() else this
fun <T> Collection<T>.asMutable(): MutableList<T> = this as? MutableList<T> ?: ArrayList(this)
fun <T> Set<T>.asMutable(): MutableSet<T> = this as? MutableSet<T> ?: this.toMutableSet()
fun <K, V> Map<K, V>.asMutable(): MutableMap<K, V> = this as? MutableMap<K, V> ?: LinkedHashMap(this)
inline fun <T, R : Comparable<R>> Iterable<T>.sortedBy(reverse: Boolean, crossinline selector: (T) -> R?): List<T> =
    if(reverse) this.sortedByDescending(selector) else this.sortedBy(selector)

inline fun <reified T : Enum<T>> String?.toEnumValueOrNull(ignoreCase: Boolean = false): T? =
    T::class.java.enumConstants.firstOrNull { it.name.equals(this, ignoreCase) }

fun String?.nullIfBlank(): String? = this?.trim()?.let {
    it.ifEmpty { null }
}

inline fun <T> ifNot(b: Boolean, body: () -> T) = if(b) null else body()

fun <T> Collection<T>.containsAny(other: Collection<T>): Boolean =
    other.any { this.contains(it) }

fun Double.nullIfNotPositive(): Double? = if(this > 0.0) this else null
fun Double.nullIfNaN(): Double? = if(this.isNaN()) null else this

inline fun <T> Boolean.ifTrue(value: () -> T?) = if(this) value() else null

inline fun <T> Boolean.ifFalse(value: () -> T?) = if(!this) value() else null
inline fun <T> Boolean?.ifNullOrFalse(value: () -> T?) = if(this != true) value() else null
inline fun <T> Any?.ifNull(value: () -> T?) = if(this == null) value() else null

inline fun <T> Double?.ifPositive(value: (x: Double) -> T?) = this?.let { x ->
    if(x > 0) value(x) else null
}

inline fun <T, R> T?.ifPresent(value: (T) -> R) = this?.let { value(it) }

fun <T> Optional<T>.orNull(): T? = this.orElse(null)
