package app.ownplay.player.ui

import app.ownplay.player.testing.sourceText
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TvSettingsIntegrationContractTest {
    private val settings = sourceText("src/main/java/app/ownplay/player/ui/TvSettingsScreen.kt")
    private val shell = sourceText("src/main/java/app/ownplay/player/ui/TVOwnPlayApp.kt")
    private val playlists = sourceText("src/main/java/app/ownplay/player/ui/SettingsPlaylist.kt")
    private val liveManagement = sourceText("src/main/java/app/ownplay/player/ui/LiveManagementScreen.kt")
    private val backup = sourceText("src/main/java/app/ownplay/player/ui/BackupRestoreSettingsContent.kt")
    private val content = sourceText("src/main/java/app/ownplay/player/ui/SettingsContent.kt")
    private val components = sourceText("src/main/java/app/ownplay/player/ui/SettingsComponents.kt")

    @Test
    fun `settings root remains the four direct TV destinations`() {
        assertTrue(settings.contains("PLAYLISTS,\n    LIVE_MANAGEMENT,\n    BACKUP_RESTORE,\n    ABOUT,"))
        assertFalse(content.contains("ContentSettingsContent("))
        assertFalse(components.contains("CompactSettingsSection("))
    }

    @Test
    fun `settings no longer carries legacy playback and global navigation callbacks`() {
        assertFalse(settings.contains("activeSourceName:"))
        assertFalse(settings.contains("hasActivePlayback:"))
        assertFalse(settings.contains("onOpenLive:"))
        assertFalse(settings.contains("onStopPlayback:"))
        assertFalse(shell.contains("activeSourceName = activeSummary?.name"))
        assertFalse(shell.contains("onStopPlayback ="))
        assertTrue(settings.contains("onOpenSourceInLive: (String) -> Unit"))
    }

    @Test
    fun `settings focus restoration waits for composed TV targets`() {
        assertTrue(settings.contains("withFrameNanos { }"))
        assertTrue(settings.contains("rootFocusRequesters.getValue(lastRootDestination).requestFocus()"))
        assertTrue(playlists.contains("withFrameNanos { }"))
        assertTrue(liveManagement.contains("withFrameNanos { }"))
    }

    @Test
    fun `backup restore exposes direct actions and excludes credentials`() {
        assertTrue(backup.contains("title = \"Create backup\""))
        assertTrue(backup.contains("actionLabel = \"Create\""))
        assertTrue(backup.contains("title = \"Restore backup\""))
        assertTrue(backup.contains("actionLabel = \"Restore\""))
        assertTrue(backup.contains("credentials excluded"))
        assertTrue(backup.contains("PersonalizationBackupService"))
    }
}
