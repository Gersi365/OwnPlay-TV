from pathlib import Path


def read(path: str) -> str:
    return Path(path).read_text(encoding="utf-8")


def write(path: str, text: str) -> None:
    Path(path).write_text(text, encoding="utf-8")


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected exactly one match, found {count}")
    return text.replace(old, new, 1)


# Shared playlist wrapper: remove device detection and make remote-first presentation explicit.
path = "app/src/main/java/app/ownplay/player/ui/SettingsPlaylist.kt"
text = read(path)
text = text.replace("import android.content.res.Configuration\n", "")
text = text.replace("import androidx.compose.ui.platform.LocalConfiguration\n", "")
text = replace_once(
    text,
    """    disabledSourceIds: Set<String> = emptySet(),
    onSetSourceEnabled: (String, Boolean) -> Unit = { _, _ -> },
    focusPrimaryOnEntry: Boolean = false,
""",
    """    disabledSourceIds: Set<String> = emptySet(),
    onSetSourceEnabled: (String, Boolean) -> Unit = { _, _ -> },
    remoteFirstActions: Boolean = false,
    focusPrimaryOnEntry: Boolean = false,
""",
    "playlist wrapper remote-first signature",
)
text = replace_once(
    text,
    """    val configuration = LocalConfiguration.current
    val isTelevision =
        configuration.uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_TELEVISION
    val contentFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isTelevision, focusPrimaryOnEntry) {
        if (isTelevision && focusPrimaryOnEntry) {
            contentFocusRequester.requestFocus()
        }
    }
""",
    """    val contentFocusRequester = remember { FocusRequester() }

    LaunchedEffect(focusPrimaryOnEntry) {
        if (focusPrimaryOnEntry) {
            contentFocusRequester.requestFocus()
        }
    }
""",
    "playlist wrapper focus ownership",
)
text = replace_once(
    text,
    """                disabledSourceIds = disabledSourceIds,
                onSetSourceEnabled = onSetSourceEnabled,
                initialFocusRequester = contentFocusRequester,
""",
    """                disabledSourceIds = disabledSourceIds,
                onSetSourceEnabled = onSetSourceEnabled,
                remoteFirstActions = remoteFirstActions,
                initialFocusRequester = contentFocusRequester,
""",
    "playlist wrapper presentation routing",
)
write(path, text)


# TV Settings explicitly opts into the remote-first playlist action presentation.
path = "app/src/tv/java/app/ownplay/player/ui/TvSettingsScreen.kt"
text = read(path)
text = replace_once(
    text,
    """            disabledSourceIds = disabledSourceIds,
            onSetSourceEnabled = onSetSourceEnabled,
            focusPrimaryOnEntry = true,
""",
    """            disabledSourceIds = disabledSourceIds,
            onSetSourceEnabled = onSetSourceEnabled,
            remoteFirstActions = true,
            focusPrimaryOnEntry = true,
""",
    "TV Settings remote-first playlist routing",
)
write(path, text)


# Playlist presentation: retain shared business logic/dialogs while providing large vertical TV actions.
path = "app/src/main/java/app/ownplay/player/ui/PlaylistSettingsScreen.kt"
text = read(path)
text = text.replace(
    "import androidx.compose.foundation.layout.fillMaxWidth\n",
    "import androidx.compose.foundation.layout.fillMaxWidth\nimport androidx.compose.foundation.layout.height\n",
    1,
)
text = text.replace(
    "import androidx.compose.foundation.rememberScrollState\n",
    "import androidx.compose.foundation.clickable\nimport androidx.compose.foundation.rememberScrollState\n",
    1,
)
text = text.replace(
    "import androidx.compose.ui.focus.focusRequester\n",
    "import androidx.compose.ui.focus.focusRequester\nimport androidx.compose.ui.focus.onFocusChanged\n",
    1,
)
text = replace_once(
    text,
    """    disabledSourceIds: Set<String> = emptySet(),
    onSetSourceEnabled: (String, Boolean) -> Unit = { _, _ -> },
    initialFocusRequester: FocusRequester? = null,
""",
    """    disabledSourceIds: Set<String> = emptySet(),
    onSetSourceEnabled: (String, Boolean) -> Unit = { _, _ -> },
    remoteFirstActions: Boolean = false,
    initialFocusRequester: FocusRequester? = null,
""",
    "playlist screen remote-first signature",
)
text = replace_once(
    text,
    """                importActive = summary.sourceId in pendingImportExecution.activeSourceIds,
                userEnabled = userEnabled,
                isActive = summary.enabled && userEnabled && summary.sourceId == activeSourceId,
""",
    """                importActive = summary.sourceId in pendingImportExecution.activeSourceIds,
                userEnabled = userEnabled,
                remoteFirstActions = remoteFirstActions,
                isActive = summary.enabled && userEnabled && summary.sourceId == activeSourceId,
""",
    "playlist card remote-first input",
)
old_add_types = """        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = { addMode = AddPlaylistMode.XTREAM },
                modifier = Modifier.weight(1f),
            ) { Text("Xtream") }
            OutlinedButton(
                onClick = { addMode = AddPlaylistMode.REMOTE_M3U },
                modifier = Modifier.weight(1f),
            ) { Text("M3U URL") }
            OutlinedButton(
                onClick = { addMode = AddPlaylistMode.LOCAL_M3U },
                modifier = Modifier.weight(1f),
            ) { Text("File") }
        }
"""
new_add_types = """        if (remoteFirstActions) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PlaylistRemoteActionRow(
                    title = "Xtream",
                    detail = "Server URL, username and password.",
                    onClick = { addMode = AddPlaylistMode.XTREAM },
                )
                PlaylistRemoteActionRow(
                    title = "M3U URL",
                    detail = "Remote M3U or M3U8 playlist URL.",
                    onClick = { addMode = AddPlaylistMode.REMOTE_M3U },
                )
                PlaylistRemoteActionRow(
                    title = "Local M3U file",
                    detail = "Choose a playlist with the Android document picker.",
                    onClick = { addMode = AddPlaylistMode.LOCAL_M3U },
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = { addMode = AddPlaylistMode.XTREAM },
                    modifier = Modifier.weight(1f),
                ) { Text("Xtream") }
                OutlinedButton(
                    onClick = { addMode = AddPlaylistMode.REMOTE_M3U },
                    modifier = Modifier.weight(1f),
                ) { Text("M3U URL") }
                OutlinedButton(
                    onClick = { addMode = AddPlaylistMode.LOCAL_M3U },
                    modifier = Modifier.weight(1f),
                ) { Text("File") }
            }
        }
"""
text = replace_once(text, old_add_types, new_add_types, "playlist add-source TV actions")
text = replace_once(
    text,
    """    importActive: Boolean,
    userEnabled: Boolean,
    isActive: Boolean,
""",
    """    importActive: Boolean,
    userEnabled: Boolean,
    remoteFirstActions: Boolean,
    isActive: Boolean,
""",
    "playlist card remote-first signature",
)
old_actions = """            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                TextButton(onClick = onOpen, enabled = summary.enabled && userEnabled) { Text("Live") }
                TextButton(onClick = onRefresh, enabled = !busy) {
                    Text(if (summary.enabled) "Refresh" else "Retry")
                }
                TextButton(onClick = onEdit, enabled = summary.enabled && !syncing) { Text("Edit") }
                TextButton(
                    onClick = { onSetUserEnabled(!userEnabled) },
                    enabled = summary.enabled && !syncing,
                ) {
                    Text(if (userEnabled) "Disable" else "Enable")
                }
                TextButton(onClick = onDelete) { Text("Delete") }
            }
"""
new_actions = """            if (remoteFirstActions) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    PlaylistRemoteActionRow(
                        title = "Open in Live",
                        detail = "Open this source in Live browsing.",
                        enabled = summary.enabled && userEnabled,
                        onClick = onOpen,
                    )
                    PlaylistRemoteActionRow(
                        title = if (summary.enabled) "Refresh" else "Retry import",
                        detail = if (summary.enabled) {
                            "Refresh the imported catalog for this source."
                        } else {
                            "Retry the pending source import."
                        },
                        enabled = !busy,
                        onClick = onRefresh,
                    )
                    PlaylistRemoteActionRow(
                        title = "Edit source",
                        detail = "Edit source name and connection details.",
                        enabled = summary.enabled && !syncing,
                        onClick = onEdit,
                    )
                    PlaylistRemoteActionRow(
                        title = if (userEnabled) "Disable on TV" else "Enable on TV",
                        detail = if (userEnabled) {
                            "Keep source data but exclude it from TV use."
                        } else {
                            "Make this ready source available on TV again."
                        },
                        enabled = summary.enabled && !syncing,
                        onClick = { onSetUserEnabled(!userEnabled) },
                    )
                    PlaylistRemoteActionRow(
                        title = "Delete source",
                        detail = "Open the existing delete confirmation for this source.",
                        onClick = onDelete,
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    TextButton(onClick = onOpen, enabled = summary.enabled && userEnabled) { Text("Live") }
                    TextButton(onClick = onRefresh, enabled = !busy) {
                        Text(if (summary.enabled) "Refresh" else "Retry")
                    }
                    TextButton(onClick = onEdit, enabled = summary.enabled && !syncing) { Text("Edit") }
                    TextButton(
                        onClick = { onSetUserEnabled(!userEnabled) },
                        enabled = summary.enabled && !syncing,
                    ) {
                        Text(if (userEnabled) "Disable" else "Enable")
                    }
                    TextButton(onClick = onDelete) { Text("Delete") }
                }
            }
"""
text = replace_once(text, old_actions, new_actions, "playlist TV action stack")
marker = """@Composable
private fun AddPlaylistDialog(
"""
helper = """@Composable
private fun PlaylistRemoteActionRow(
    title: String,
    detail: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    var focused by remember(title, enabled) { mutableStateOf(false) }
    val highlighted = focused && enabled

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .onFocusChanged { focused = it.isFocused }
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 0.dp,
        color = when {
            highlighted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.64f)
            enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f)
        },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = when {
                    highlighted -> MaterialTheme.colorScheme.onPrimaryContainer
                    enabled -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f)
                },
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    highlighted -> MaterialTheme.colorScheme.primary
                    enabled -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.46f)
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AddPlaylistDialog(
"""
text = replace_once(text, marker, helper, "playlist remote action helper")
write(path, text)


# Source-contract regression coverage for the TV-specific presentation selection.
test_path = Path("app/src/test/java/app/ownplay/player/ui/TvPlaylistRemoteActionsContractTest.kt")
if test_path.exists():
    raise SystemExit("TV playlist remote actions contract test already exists")
test_path.write_text(
    '''package app.ownplay.player.ui

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
''',
    encoding="utf-8",
)
