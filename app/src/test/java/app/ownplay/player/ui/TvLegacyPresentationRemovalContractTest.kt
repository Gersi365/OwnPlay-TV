package app.ownplay.player.ui

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TvLegacyPresentationRemovalContractTest {
    @Test
    fun `superseded library and prototype presentation sources are absent`() {
        assertFalse(exists("src/main/java/app/ownplay/player/ui/library"))
        assertFalse(exists("src/main/java/app/ownplay/player/ui/OfflineMediaTvFocusPolicy.kt"))
        assertFalse(exists("src/debug"))
    }

    @Test
    fun `active TV shell contains no Library route`() {
        val shell = text("src/main/java/app/ownplay/player/ui/TVOwnPlayApp.kt")
        assertFalse(shell.contains("TVSection.LIBRARY"))
        assertFalse(shell.contains("UnifiedLibraryRoute"))
        assertFalse(shell.contains("onOpenLibrary"))
        assertTrue(shell.contains("TVSection.MOVIES"))
        assertTrue(shell.contains("TVSection.SERIES"))
    }

    private fun exists(relativeToApp: String): Boolean = candidates(relativeToApp).any(Files::exists)

    private fun text(relativeToApp: String): String {
        val source = candidates(relativeToApp).firstOrNull(Files::exists)
            ?: error("Source file not found: $relativeToApp")
        return source.readText()
    }

    private fun candidates(relativeToApp: String): List<Path> = listOf(
        Path.of(relativeToApp),
        Path.of("app").resolve(relativeToApp),
    )
}
