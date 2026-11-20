package com.marcospolo.watchly.data.cache

import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Enough for a browsing session; keys never asked for again would otherwise stay in memory forever. */
internal const val DEFAULT_MAX_CACHE_ENTRIES = 100

/**
 * Thread-safe in-memory cache whose entries expire [ttl] after being stored and
 * that never holds more than [maxEntries] entries.
 *
 * Time comes from an injected [TimeSource]: production uses the monotonic clock
 * (immune to the user changing the device time) and tests use a `TestTimeSource`
 * to check expiration deterministically, without sleeping.
 *
 * When full, expired entries are dropped first and then the least recently used
 * one, so a key that is still being read is not evicted by one-off searches.
 */
class InMemoryCache<K : Any, V : Any>(
    private val ttl: Duration,
    private val timeSource: TimeSource = TimeSource.Monotonic,
    private val maxEntries: Int = DEFAULT_MAX_CACHE_ENTRIES
) : Cache<K, V> {
    init {
        require(maxEntries > 0) { "maxEntries must be positive, was $maxEntries" }
    }

    private class Entry<V>(val value: V, val storedAt: TimeMark)

    private val mutex = Mutex()

    // accessOrder = true: iteration goes from the least to the most recently used entry.
    private val entries = LinkedHashMap<K, Entry<V>>(maxEntries, LOAD_FACTOR, true)

    override suspend fun get(key: K): V? = mutex.withLock {
        val entry = entries[key] ?: return@withLock null
        if (entry.isExpired()) {
            entries.remove(key)
            null
        } else {
            entry.value
        }
    }

    override suspend fun put(key: K, value: V) {
        mutex.withLock {
            entries.remove(key)
            if (entries.size >= maxEntries) entries.values.removeAll { it.isExpired() }
            if (entries.size >= maxEntries) entries.remove(entries.keys.first())
            entries[key] = Entry(value, timeSource.markNow())
        }
    }

    private fun Entry<V>.isExpired(): Boolean = storedAt.elapsedNow() >= ttl

    private companion object {
        const val LOAD_FACTOR = 0.75f
    }
}
