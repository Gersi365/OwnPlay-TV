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
    private val settingsComponentsSource = sourceText(
        "src/main/java/app/ownplay/player/ui/SettingsComponents.kt",
    )
    private val playlistSubpageSource = sourceText(
        "src/main/java/app/ownplay/player/ui/SettingsPlaylist.kt",
    )
    private val liveManagementSource = sourceText(
        "src/main/java/app/ownplay/player/ui/LiveManagementScreen.kt",
    )

    @Test
    fun tvRootContainsOnlyApprovedDestinationsInOrder() {
        assertTrue(
            tvSettingsSource.contains(
                "private enum class TvSettingsDestination {\n" +
                    "    PLAYLISTS,\n" +
                    "    LIVE_MANAGEMENT,\n" +
                    "    BACKUP_RESTORE,\n" +
                    "    ABOUT,\n" +
                    "}",
            ),
        )
        assertTrue(tvSettingsSource.contains("title = \"Playlists\""))
        assertTrue(tvSettingsSource.contains("title = \"Live Management\""))
        assertTrue(tvSettingsSource.contains("title = \"Backup & Restore\""))
        assertTrue(tvSettingsSource.contains("title = \"About\""))
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
    fun tvSubpagesFocusMeaningfulContentInsteadOfBackOnEntry() {
        assertTrue(tvSettingsSource.contains("focusPrimaryOnEntry = true"))
        assertTrue(
            tvSettingsSource.contains(
                "BackupRestoreSettingsContent(initialFocusRequester = backupFocusRequester)",
            ),
        )
        assertTrue(
            tvSettingsSource.contains(
                "(initialContentFocusRequester ?: backFocusRequester).requestFocus()",
            ),
        )
        assertTrue(
            playlistSubpageSource.contains(
                "initialFocusRequester = contentFocusRequester",
            ),
        )
        assertTrue(
            liveManagementSource.contains(
                "primaryFocusRequester.requestFocus()",
            ),
        )
        assertFalse(playlistSubpageSource.contains("focusBackOnEntry"))
        assertFalse(liveManagementSource.contains("focusBackOnEntry"))
    }

    @Test
    fun sharedSettingsActionRowIsAStableWholeRowRemoteTarget() {
        assertTrue(settingsComponentsSource.contains(".height(64.dp)"))
        assertTrue(settingsComponentsSource.contains(".clickable(onClick = onClick)"))
        assertTrue(settingsComponentsSource.contains("shape = RoundedCornerShape(14.dp)"))
        assertTrue(settingsComponentsSource.contains("color = if (focused)"))
        assertFalse(settingsComponentsSource.contains("IconButton("))
        assertFalse(settingsComponentsSource.contains(".scale("))
    }

    @Test
    fun rootUsesDedicatedSubpagesForAllApprovedDestinations() {
        assertTrue(tvSettingsSource.contains("TvSettingsPage.PLAYLISTS -> PlaylistManagementSubscreen("))
        assertTrue(tvSettingsSource.contains("TvSettingsPage.LIVE_MANAGEMENT -> LiveManagementScreen("))
        assertTrue(tvSettingsSource.contains("TvSettingsPage.BACKUP_RESTORE ->"))
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
