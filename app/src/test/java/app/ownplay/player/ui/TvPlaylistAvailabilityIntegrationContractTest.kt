package app.ownplay.player.ui

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TvPlaylistAvailabilityIntegrationContractTest {
    private val storeSource = sourceText(
        "src/main/java/app/ownplay/player/ui/TvPlaylistAvailabilityStore.kt",
    )
    private val tvShellSource = sourceText(
        "src/main/java/app/ownplay/player/ui/TVOwnPlayApp.kt",
    )
    private val tvSettingsSource = sourceText(
        "src/main/java/app/ownplay/player/ui/TvSettingsScreen.kt",
    )
    private val playlistSource = sourceText(
        "src/main/java/app/ownplay/player/ui/PlaylistSettingsScreen.kt",
    )

    @Test
    fun userAvailabilityRemainsSeparateFromSourceReadiness() {
        assertTrue(storeSource.contains("stringSetPreferencesKey(\"disabled_source_ids\")"))
        assertTrue(
            storeSource.contains(
                ".filter { summary -> summary.enabled && summary.sourceId !in disabledSourceIds }",
            ),
        )
        assertFalse(storeSource.contains("Room"))
        assertFalse(storeSource.contains("password"))
    }

    @Test
    fun tvShellResolvesOnlyAvailableSourcesAndRoutesAvailabilityToSettings() {
        assertTrue(tvShellSource.contains("TvPlaylistAvailabilityStore(context.applicationContext)"))
        assertTrue(tvShellSource.contains("resolveTvAvailableSourceIds("))
        assertTrue(tvShellSource.contains("enabledSourceIds = availableSourceIds"))
        assertTrue(tvShellSource.contains("disabledSourceIds = disabledSourceIds"))
        assertTrue(tvShellSource.contains("onSetSourceEnabled = { sourceId, enabled ->"))
    }

    @Test
    fun settingsAndPlaylistUiExposeEnableDisableWithoutChangingProviderData() {
        assertTrue(
            tvSettingsSource.contains(
                "summary.enabled && summary.sourceId !in disabledSourceIds",
            ),
        )
        assertTrue(playlistSource.contains("val userEnabled = summary.sourceId !in disabledSourceIds"))
        assertTrue(playlistSource.contains("onSetUserEnabled(!userEnabled)"))
        assertTrue(playlistSource.contains("Text(if (userEnabled) \"Disable\" else \"Enable\")"))
        assertTrue(playlistSource.contains("enabled = summary.enabled && userEnabled"))
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
