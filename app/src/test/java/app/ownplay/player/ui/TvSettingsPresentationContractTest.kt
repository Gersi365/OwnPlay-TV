package app.ownplay.player.ui

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TvSettingsPresentationContractTest {
    private val tvSettingsSource = sourceText(
        "src/tv/java/app/ownplay/player/ui/TvSettingsScreen.kt",
    )
    private val tvShellSource = sourceText(
        "src/tv/java/app/ownplay/player/ui/TVOwnPlayApp.kt",
    )

    @Test
    fun tvRootContainsOnlyApprovedDestinationsInOrder() {
        val playlists = tvSettingsSource.indexOf("title = \"Playlists\"")
        val liveManagement = tvSettingsSource.indexOf("title = \"Live Management\"")
        val backupRestore = tvSettingsSource.indexOf("title = \"Backup & Restore\"")
        val about = tvSettingsSource.indexOf("title = \"About\"")

        assertTrue(playlists >= 0)
        assertTrue(playlists < liveManagement)
        assertTrue(liveManagement < backupRestore)
        assertTrue(backupRestore < about)
        assertFalse(tvSettingsSource.contains("title = \"Interface\""))
        assertFalse(tvSettingsSource.contains("title = \"Downloads\""))
    }

    @Test
    fun tvShellRoutesSettingsToDedicatedPresentation() {
        assertTrue(
            tvShellSource.contains("TVSection.SETTINGS -> TvSettingsScreen("),
        )
        assertFalse(
            tvShellSource.contains("TVSection.SETTINGS -> SettingsScreen("),
        )
    }

    @Test
    fun subpageBackReturnsToRootAndRootRestoresOriginFocus() {
        assertTrue(
            tvSettingsSource.contains(
                "BackHandler(enabled = page != TvSettingsPage.ROOT)",
            ),
        )
        assertTrue(
            tvSettingsSource.contains("lastRootDestination = destination"),
        )
        assertTrue(
            tvSettingsSource.contains("page = TvSettingsPage.ROOT"),
        )
        assertTrue(
            tvSettingsSource.contains(
                "rootFocusRequesters.getValue(lastRootDestination).requestFocus()",
            ),
        )
    }

    @Test
    fun rootFocusTreatmentKeepsGeometryStable() {
        assertTrue(
            tvSettingsSource.contains(".height(TV_SETTINGS_ROW_HEIGHT)"),
        )
        assertTrue(
            tvSettingsSource.contains("shape = RoundedCornerShape(14.dp)"),
        )
        assertTrue(
            tvSettingsSource.contains("color = if (focused)"),
        )
        assertFalse(tvSettingsSource.contains(".scale("))
    }

    @Test
    fun rootUsesDedicatedSubpagesForAllApprovedDestinations() {
        assertTrue(tvSettingsSource.contains("TvSettingsPage.PLAYLISTS -> PlaylistManagementSubscreen("))
        assertTrue(tvSettingsSource.contains("TvSettingsPage.LIVE_MANAGEMENT -> LiveManagementScreen("))
        assertTrue(tvSettingsSource.contains("TvSettingsPage.BACKUP_RESTORE -> TvSettingsInformationPage("))
        assertTrue(tvSettingsSource.contains("TvSettingsPage.ABOUT -> TvSettingsInformationPage("))
    }

    private fun sourceText(relativeToApp: String): String {
        val candidates = listOf(
            Path.of(relativeToApp),
            Path.of("app").resolve(relativeToApp),
        )
        val source = candidates.firstOrNull(Files::exists)
            ?: error("Source file not found: $relativeToApp")
        return source.readText()
    }
}
