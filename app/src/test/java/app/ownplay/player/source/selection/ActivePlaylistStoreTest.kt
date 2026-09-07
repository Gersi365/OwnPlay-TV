package app.ownplay.player.source.selection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ActivePlaylistStoreTest {
    @Test
    fun persistedEnabledSourceWins() {
        assertEquals(
            "source-b",
            resolveActivePlaylistId(
                persistedSourceId = "source-b",
                currentSourceId = "source-a",
                enabledSourceIds = listOf("source-a", "source-b"),
            ),
        )
    }

    @Test
    fun invalidPersistedSourceFallsBackToCurrentEnabledSource() {
        assertEquals(
            "source-a",
            resolveActivePlaylistId(
                persistedSourceId = "deleted-source",
                currentSourceId = "source-a",
                enabledSourceIds = listOf("source-a", "source-b"),
            ),
        )
    }

    @Test
    fun missingSelectionFallsBackToFirstEnabledSource() {
        assertEquals(
            "source-a",
            resolveActivePlaylistId(
                persistedSourceId = null,
                currentSourceId = null,
                enabledSourceIds = listOf("source-a", "source-b"),
            ),
        )
    }

    @Test
    fun noEnabledSourcesReturnsNull() {
        assertNull(
            resolveActivePlaylistId(
                persistedSourceId = "pending-source",
                currentSourceId = "pending-source",
                enabledSourceIds = emptyList(),
            ),
        )
    }

    @Test
    fun currentManagementSourceSurvivesCatalogReordering() {
        for (sourceIds in listOf(
            listOf("source-a", "source-b"),
            listOf("new-source", "source-a", "source-b"),
            listOf("source-b", "source-a"),
        )) {
            assertEquals(
                "source-b",
                resolveActivePlaylistId(null, "source-b", sourceIds),
            )
        }
    }

    @Test
    fun removedManagementSourceFallsBackWithoutReselectingItWhenItReturns() {
        val fallback = resolveActivePlaylistId(null, "source-b", listOf("source-a"))
        assertEquals("source-a", fallback)
        assertEquals(
            "source-a",
            resolveActivePlaylistId(null, fallback, listOf("source-b", "source-a")),
        )
        assertNull(resolveActivePlaylistId(null, fallback, emptyList()))
    }
}
