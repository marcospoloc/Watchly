package com.marcospolo.watchly.data.mapper

import com.marcospolo.watchly.data.remote.dto.ChannelDto
import com.marcospolo.watchly.data.remote.dto.ImageDto
import com.marcospolo.watchly.data.remote.dto.RatingDto
import com.marcospolo.watchly.data.remote.dto.ShowDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShowMapperTest {
    private val fullDto = ShowDto(
        id = 1,
        name = "Under the Dome",
        genres = listOf("Drama", "Science-Fiction"),
        premiered = "2013-06-24",
        language = "English",
        status = "Ended",
        runtime = 60,
        summary = "<p><b>Under the Dome</b> is a town &amp; its people.</p><p>Second paragraph.</p>",
        rating = RatingDto(average = 6.5),
        image = ImageDto(medium = "https://img/medium.jpg", original = "https://img/original.jpg"),
        network = ChannelDto(name = "CBS")
    )

    @Test
    fun `given a complete dto when mapped to show then keeps the list fields and extracts the year`() {
        val show = fullDto.toShow()

        assertEquals(1, show.id)
        assertEquals("Under the Dome", show.name)
        assertEquals("https://img/medium.jpg", show.posterUrl)
        assertEquals(2013, show.premieredYear)
        assertEquals(listOf("Drama", "Science-Fiction"), show.genres)
        assertEquals(6.5, show.rating)
    }

    @Test
    fun `given a dto with null optional fields when mapped then the fields are null`() {
        val dto = ShowDto(id = 2, name = "Unknown")

        val show = dto.toShow()
        val detail = dto.toShowDetail()

        assertNull(show.posterUrl)
        assertNull(show.premieredYear)
        assertNull(show.rating)
        assertEquals(emptyList<String>(), show.genres)
        assertNull(detail.summary)
        assertNull(detail.network)
    }

    @Test
    fun `given an html summary when mapped to detail then returns plain text with paragraphs`() {
        val detail = fullDto.toShowDetail()

        assertEquals("Under the Dome is a town & its people.\n\nSecond paragraph.", detail.summary)
    }

    @Test
    fun `given a detail dto when mapped then prefers the original image and the network name`() {
        val detail = fullDto.toShowDetail()

        assertEquals("https://img/original.jpg", detail.imageUrl)
        assertEquals("CBS", detail.network)
        assertEquals(60, detail.runtimeMinutes)
    }

    @Test
    fun `given a streaming show without network when mapped then uses the web channel name`() {
        val dto = ShowDto(id = 3, name = "Stranger Things", webChannel = ChannelDto(name = "Netflix"))

        assertEquals("Netflix", dto.toShowDetail().network)
    }
}
