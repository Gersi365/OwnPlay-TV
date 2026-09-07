package app.ownplay.player.ui.vod

import app.ownplay.player.vod.VodMovie
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class MoviePlaybackStartPolicyTest {
    @Test
    fun `continue keeps the saved playback checkpoint`() {
        val movie = movie()

        assertSame(movie, moviePlaybackSnapshot(movie, fromBeginning = false))
    }

    @Test
    fun `play from beginning clears only the playback snapshot checkpoint`() {
        val movie = movie()

        val playback = moviePlaybackSnapshot(movie, fromBeginning = true)

        assertNull(playback.positionMs)
        assertEquals(false, playback.progressCompleted)
        assertNull(playback.progressUpdatedAtEpochMillis)
        assertEquals(45_000L, movie.positionMs)
        assertEquals(300_000L, movie.durationMs)
    }

    private fun movie() = VodMovie(
        movieId = "movie-1",
        providerStreamId = 11,
        categoryKey = "provider-category",
        name = "Movie",
        posterUrl = null,
        containerExtension = "mp4",
        rating = null,
        addedAtEpochSeconds = null,
        isFavorite = false,
        positionMs = 45_000L,
        durationMs = 300_000L,
        progressCompleted = false,
        progressUpdatedAtEpochMillis = 123L,
    )
}
