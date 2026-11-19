package com.marcospolo.watchly.data.cache

/** Minimal key-value cache abstraction, so the storage strategy can change without touching the repository. */
interface Cache<K : Any, V : Any> {
    /** Returns the cached value for [key], or `null` when it is missing or no longer valid. */
    suspend fun get(key: K): V?

    suspend fun put(key: K, value: V)
}
