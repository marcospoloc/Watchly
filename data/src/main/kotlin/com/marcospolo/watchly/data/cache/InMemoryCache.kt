package com.marcospolo.watchly.data.cache

import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Thread-safe in-memory cache whose entries expire [ttl] after being stored.
 *
 * Time comes from an injected [TimeSource]: production uses the monotonic clock
 * (immune to the user changing the device time) and tests use a `TestTimeSource`
 * to check expiration deterministically, without sleeping.
 */
class InMemoryCache<K : Any, V : Any>(
    private val ttl: Duration,
    private val timeSource: TimeSource = TimeSource.Monotonic
) : Cache<K, V> {
    private class Entry<V>(val value: V, val storedAt: TimeMark)

    private val mutex = Mutex()
    private val entries = HashMap<K, Entry<V>>()

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
        mutex.withLock { entries[key] = Entry(value, timeSource.markNow()) }
    }

    private fun Entry<V>.isExpired(): Boolean = storedAt.elapsedNow() >= ttl
}
