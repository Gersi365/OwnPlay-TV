package app.ownplay.player.ui.series

import app.ownplay.player.series.SeriesEpisode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class SeriesPlaybackStartPolicyTest {
    @Test
    fun `continue keeps the saved episode checkpoint`() {
        val episode = episode()

        assertSame(episode, seriesPlaybackSnapshot(episode, fromBeginning = false))
    }

    @Test
    fun `play from beginning clears only the playback snapshot checkpoint`() {
        val episode = episode()

        val playback = seriesPlaybackSnapshot(episode, fromBeginning = true)

        assertNull(playback.positionMs)
        assertEquals(false, playback.progressCompleted)
        assertNull(playback.progressUpdatedAtEpochMillis)
        assertEquals(45_000L, episode.positionMs)
        assertEquals(300_000L, episode.durationMs)
    }

    private fun episode() = SeriesEpisode(
        episodeId = "episode-1",
        seriesId = "series-1",
        seriesTitle = "Series",
        providerEpisodeId = 11,
        seasonNumber = 2,
        episodeNumber = 3,
        title = "Episode",
        containerExtension = "mp4",
        durationSeconds = null,
        posterUrl = null,
        positionMs = 45_000L,
        durationMs = 300_000L,
        progressCompleted = false,
        progressUpdatedAtEpochMillis = 123L,
    )
}
