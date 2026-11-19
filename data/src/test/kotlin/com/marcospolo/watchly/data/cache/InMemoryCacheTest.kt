package com.marcospolo.watchly.data.cache

import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InMemoryCacheTest {
    private val timeSource = TestTimeSource()
    private val cache = InMemoryCache<String, String>(ttl = 3.minutes, timeSource = timeSource)

    @Test
    fun `given a stored value when read within the ttl then returns it`() = runTest {
        cache.put("lost", "value")
        timeSource += 2.minutes + 59.seconds

        assertEquals("value", cache.get("lost"))
    }

    @Test
    fun `given a stored value when the ttl has exactly elapsed then it is expired`() = runTest {
        cache.put("lost", "value")
        timeSource += 3.minutes

        assertNull(cache.get("lost"))
    }

    @Test
    fun `given an expired value when read again after a new put then returns the new value`() = runTest {
        cache.put("lost", "old")
        timeSource += 5.minutes
        assertNull(cache.get("lost"))

        cache.put("lost", "new")

        assertEquals("new", cache.get("lost"))
    }

    @Test
    fun `given a missing key when read then returns null`() = runTest {
        assertNull(cache.get("unknown"))
    }

    @Test
    fun `given different keys when stored then each keeps its own value`() = runTest {
        cache.put("lost", "a")
        cache.put("house", "b")

        assertEquals("a", cache.get("lost"))
        assertEquals("b", cache.get("house"))
    }

    @Test
    fun `given an existing key when put again then overwrites the value and renews its ttl`() = runTest {
        cache.put("lost", "old")
        timeSource += 2.minutes
        cache.put("lost", "new")
        timeSource += 2.minutes

        assertEquals("new", cache.get("lost"))
    }
}
