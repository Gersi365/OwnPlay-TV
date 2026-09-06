package app.ownplay.player.ui

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TvPlaylistRemoteActionsContractTest {
    private val tvSettingsSource = sourceText(
        "src/tv/java/app/ownplay/player/ui/TvSettingsScreen.kt",
    )
    private val wrapperSource = sourceText(
        "src/main/java/app/ownplay/player/ui/SettingsPlaylist.kt",
    )
    private val playlistSource = sourceText(
        "src/main/java/app/ownplay/player/ui/PlaylistSettingsScreen.kt",
    )

    @Test
    fun tvSettingsExplicitlyUsesRemoteFirstPlaylistActions() {
        assertTrue(tvSettingsSource.contains("remoteFirstActions = true"))
        assertTrue(wrapperSource.contains("remoteFirstActions: Boolean = false"))
        assertTrue(wrapperSource.contains("remoteFirstActions = remoteFirstActions"))
        assertFalse(wrapperSource.contains("UI_MODE_TYPE_TELEVISION"))
        assertFalse(wrapperSource.contains("LocalConfiguration"))
    }

    @Test
    fun remoteFirstPlaylistActionsUseFixedVerticalTargets() {
        assertTrue(playlistSource.contains("private fun PlaylistRemoteActionRow("))
        assertTrue(playlistSource.contains(".height(60.dp)"))
        assertTrue(playlistSource.contains(".onFocusChanged { focused = it.isFocused }"))
        assertTrue(playlistSource.contains("var focused by remember { mutableStateOf(false) }"))
        assertFalse(playlistSource.contains("remember(title, enabled)"))
        assertTrue(playlistSource.contains(".clickable(enabled = enabled, onClick = onClick)"))
        assertTrue(playlistSource.contains("title = \"Open in Live\""))
        assertTrue(playlistSource.contains("title = \"Edit source\""))
        assertTrue(playlistSource.contains("\"Disable on TV\" else \"Enable on TV\""))
        assertTrue(playlistSource.contains("title = \"Delete source\""))
        assertFalse(playlistSource.contains("scale("))
    }

    @Test
    fun remoteFirstAddSourceTypesAreVerticalAndSharedDialogsRemainInUse() {
        assertTrue(playlistSource.contains("title = \"Xtream\""))
        assertTrue(playlistSource.contains("title = \"M3U URL\""))
        assertTrue(playlistSource.contains("title = \"Local M3U file\""))
        assertTrue(playlistSource.contains("AddPlaylistDialog("))
        assertTrue(playlistSource.contains("EditPlaylistDialog("))
        assertTrue(playlistSource.contains("Delete playlist?"))
    }

    @Test
    fun sharedNonTvPlaylistPresentationRemainsAvailable() {
        assertTrue(playlistSource.contains("if (remoteFirstActions) {"))
        assertTrue(playlistSource.contains("OutlinedButton("))
        assertTrue(playlistSource.contains("TextButton(onClick = onOpen"))
        assertTrue(playlistSource.contains("remoteFirstActions: Boolean = false"))
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
