package app.ownplay.player.ui

import app.ownplay.player.persistence.PlaylistSourceSummary
import org.junit.Assert.assertEquals
import org.junit.Test

class TvPlaylistAvailabilityPolicyTest {
    @Test
    fun availabilityRequiresReadySourceAndNoUserDisable() {
        val summaries = listOf(
            summary("ready-a", ready = true),
            summary("pending", ready = false),
            summary("ready-b", ready = true),
        )

        assertEquals(
            listOf("ready-b"),
            resolveTvAvailableSourceIds(
                summaries = summaries,
                disabledSourceIds = setOf("ready-a"),
            ),
        )
    }

    @Test
    fun enablingAgainRestoresSourceWithoutChangingReadiness() {
        val summaries = listOf(summary("ready-a", ready = true))

        assertEquals(
            emptyList<String>(),
            resolveTvAvailableSourceIds(summaries, setOf("ready-a")),
        )
        assertEquals(
            listOf("ready-a"),
            resolveTvAvailableSourceIds(summaries, emptySet()),
        )
    }

    private fun summary(sourceId: String, ready: Boolean) = PlaylistSourceSummary(
        sourceId = sourceId,
        name = sourceId,
        sourceKind = "test",
        enabled = ready,
        channelCount = 0,
        createdAtEpochMillis = 0L,
        updatedAtEpochMillis = 0L,
    )
}
