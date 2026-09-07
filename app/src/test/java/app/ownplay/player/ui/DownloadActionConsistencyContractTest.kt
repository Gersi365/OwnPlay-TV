package app.ownplay.player.ui

import app.ownplay.player.testing.sourceText
import org.junit.Assert.assertFalse
import org.junit.Test

class DownloadActionConsistencyContractTest {
    @Test
    fun `movie details do not expose phone or offline download presentation`() {
        val details = sourceText("src/main/java/app/ownplay/player/ui/vod/MovieDetailsPane.kt")

        assertFalse(details.contains("offlineCopyAvailable"))
        assertFalse(details.contains("Play Offline"))
        assertFalse(details.contains("Resume Offline"))
        assertFalse(details.contains("Phone Downloads"))
        assertFalse(details.contains("Saving to phone Downloads"))
    }

    @Test
    fun `series details do not expose phone or offline download presentation`() {
        val details = sourceText("src/main/java/app/ownplay/player/ui/series/SeriesDetailsPane.kt")

        assertFalse(details.contains("offlineCopyAvailable"))
        assertFalse(details.contains("Play Offline"))
        assertFalse(details.contains("Resume Offline"))
        assertFalse(details.contains("Downloaded · Offline copy"))
    }
}
