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


store_path = Path("app/src/tv/java/app/ownplay/player/ui/TvPlaylistAvailabilityStore.kt")
if store_path.exists():
    raise SystemExit("TV playlist availability store already exists")
store_path.write_text(
    '''package app.ownplay.player.ui

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.ownplay.player.persistence.PlaylistSourceSummary
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.ownPlayTvPlaylistAvailabilityPreferences: DataStore<Preferences> by preferencesDataStore(
    name = "ownplay_tv_playlist_availability",
)

internal class TvPlaylistAvailabilityStore(
    context: Context,
) {
    private val dataStore = context.applicationContext.ownPlayTvPlaylistAvailabilityPreferences

    fun observeDisabledSourceIds(): Flow<Set<String>> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences ->
            preferences[DISABLED_SOURCE_IDS_KEY]?.toSet().orEmpty()
        }

    suspend fun setEnabled(sourceId: String, enabled: Boolean): Boolean {
        val normalizedSourceId = sourceId.trim()
        if (normalizedSourceId.isEmpty()) return false

        return try {
            dataStore.edit { preferences ->
                val disabled = preferences[DISABLED_SOURCE_IDS_KEY]?.toMutableSet() ?: mutableSetOf()
                if (enabled) {
                    disabled.remove(normalizedSourceId)
                } else {
                    disabled.add(normalizedSourceId)
                }
                if (disabled.isEmpty()) {
                    preferences.remove(DISABLED_SOURCE_IDS_KEY)
                } else {
                    preferences[DISABLED_SOURCE_IDS_KEY] = disabled
                }
            }
            true
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            false
        }
    }
}

internal fun resolveTvAvailableSourceIds(
    summaries: List<PlaylistSourceSummary>,
    disabledSourceIds: Set<String>,
): List<String> = summaries
    .asSequence()
    .filter { summary -> summary.enabled && summary.sourceId !in disabledSourceIds }
    .map { summary -> summary.sourceId }
    .toList()

private val DISABLED_SOURCE_IDS_KEY = stringSetPreferencesKey("disabled_source_ids")
''',
    encoding="utf-8",
)

test_path = Path("app/src/test/java/app/ownplay/player/ui/TvPlaylistAvailabilityPolicyTest.kt")
if test_path.exists():
    raise SystemExit("TV playlist availability policy test already exists")
test_path.write_text(
    '''package app.ownplay.player.ui

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
''',
    encoding="utf-8",
)

path = "app/src/tv/java/app/ownplay/player/ui/TVOwnPlayApp.kt"
text = read(path)
text = replace_once(
    text,
    '''    val activePlaylistStore = remember(context) {
        ActivePlaylistStore(context.applicationContext)
    }
    val activePlaylistSelection by activePlaylistStore.observe().collectAsState(
        initial = ActivePlaylistSelection.Loading,
    )
    val activePlaylistScope = rememberCoroutineScope()
''',
    '''    val activePlaylistStore = remember(context) {
        ActivePlaylistStore(context.applicationContext)
    }
    val playlistAvailabilityStore = remember(context) {
        TvPlaylistAvailabilityStore(context.applicationContext)
    }
    val activePlaylistSelection by activePlaylistStore.observe().collectAsState(
        initial = ActivePlaylistSelection.Loading,
    )
    val disabledSourceIds by playlistAvailabilityStore.observeDisabledSourceIds().collectAsState(
        initial = emptySet(),
    )
    val activePlaylistScope = rememberCoroutineScope()
''',
    "TV shell availability store",
)
text = replace_once(
    text,
    "    LaunchedEffect(summaries, activePlaylistSelection) {\n",
    "    LaunchedEffect(summaries, activePlaylistSelection, disabledSourceIds) {\n",
    "TV shell availability effect key",
)
text = replace_once(
    text,
    '''        val enabledSourceIds = summaries
            .asSequence()
            .filter { summary -> summary.enabled }
            .map { summary -> summary.sourceId }
            .toList()
''',
    '''        val availableSourceIds = resolveTvAvailableSourceIds(
            summaries = summaries,
            disabledSourceIds = disabledSourceIds,
        )
''',
    "TV shell available IDs",
)
count = text.count("enabledSourceIds")
if count != 4:
    raise SystemExit(f"TV shell enabledSourceIds expected 4 remaining references, found {count}")
text = text.replace("enabledSourceIds", "availableSourceIds")
text = replace_once(
    text,
    "    val activeSummary = summaries.firstOrNull { it.sourceId == activeSourceId && it.enabled }\n",
    '''    val activeSummary = summaries.firstOrNull { summary ->
        summary.sourceId == activeSourceId &&
            summary.enabled &&
            summary.sourceId !in disabledSourceIds
    }
''',
    "TV shell active summary availability",
)
text = replace_once(
    text,
    '''                TVSection.SETTINGS -> TvSettingsScreen(
                    runtime = runtime,
                    summaries = summaries,
                    syncState = syncState,
''',
    '''                TVSection.SETTINGS -> TvSettingsScreen(
                    runtime = runtime,
                    summaries = summaries,
                    disabledSourceIds = disabledSourceIds,
                    onSetSourceEnabled = { sourceId, enabled ->
                        activePlaylistScope.launch {
                            playlistAvailabilityStore.setEnabled(sourceId, enabled)
                        }
                    },
                    syncState = syncState,
''',
    "TV shell Settings availability routing",
)
write(path, text)

path = "app/src/tv/java/app/ownplay/player/ui/TvSettingsScreen.kt"
text = read(path)
text = replace_once(
    text,
    '''    runtime: OwnPlayAppRuntime,
    summaries: List<PlaylistSourceSummary>,
    syncState: SourceSyncState,
''',
    '''    runtime: OwnPlayAppRuntime,
    summaries: List<PlaylistSourceSummary>,
    disabledSourceIds: Set<String>,
    onSetSourceEnabled: (String, Boolean) -> Unit,
    syncState: SourceSyncState,
''',
    "TV Settings availability signature",
)
text = replace_once(
    text,
    '''        TvSettingsPage.ROOT -> TvSettingsRoot(
            summaries = summaries,
            initialFocusedDestination = lastRootDestination,
''',
    '''        TvSettingsPage.ROOT -> TvSettingsRoot(
            summaries = summaries,
            disabledSourceIds = disabledSourceIds,
            initialFocusedDestination = lastRootDestination,
''',
    "TV Settings root availability args",
)
text = replace_once(
    text,
    '''            onBack = { page = TvSettingsPage.ROOT },
            onOpenInLive = onOpenSourceInLive,
            focusPrimaryOnEntry = true,
''',
    '''            onBack = { page = TvSettingsPage.ROOT },
            onOpenInLive = onOpenSourceInLive,
            disabledSourceIds = disabledSourceIds,
            onSetSourceEnabled = onSetSourceEnabled,
            focusPrimaryOnEntry = true,
''',
    "TV Settings playlists availability args",
)
text = replace_once(
    text,
    "            summaries = summaries.filter { summary -> summary.enabled },\n",
    '''            summaries = summaries.filter { summary ->
                summary.enabled && summary.sourceId !in disabledSourceIds
            },
''',
    "TV Settings Live availability filter",
)
text = replace_once(
    text,
    '''private fun TvSettingsRoot(
    summaries: List<PlaylistSourceSummary>,
    initialFocusedDestination: TvSettingsDestination,
''',
    '''private fun TvSettingsRoot(
    summaries: List<PlaylistSourceSummary>,
    disabledSourceIds: Set<String>,
    initialFocusedDestination: TvSettingsDestination,
''',
    "TV Settings root signature",
)
text = replace_once(
    text,
    "    val readyCount = summaries.count { summary -> summary.enabled }\n",
    "    val readyCount = summaries.count { summary -> summary.enabled }\n    val availableCount = resolveTvAvailableSourceIds(summaries, disabledSourceIds).size\n",
    "TV Settings availability count",
)
text = replace_once(
    text,
    '                detail = "$readyCount ready",\n',
    '                detail = "$availableCount available",\n',
    "TV Settings playlist root detail",
)
text = replace_once(
    text,
    '''            configuredCount = summaries.size,
            readyCount = readyCount,
            modifier = Modifier
''',
    '''            configuredCount = summaries.size,
            readyCount = readyCount,
            availableCount = availableCount,
            modifier = Modifier
''',
    "TV Settings context counts",
)
text = replace_once(
    text,
    '''    configuredCount: Int,
    readyCount: Int,
    modifier: Modifier = Modifier,
''',
    '''    configuredCount: Int,
    readyCount: Int,
    availableCount: Int,
    modifier: Modifier = Modifier,
''',
    "TV Settings context signature",
)
text = replace_once(
    text,
    '            status = "$configuredCount configured · $readyCount ready"\n',
    '            status = "$configuredCount configured · $readyCount ready · $availableCount available"\n',
    "TV Settings playlist status",
)
text = replace_once(
    text,
    '            status = if (readyCount > 0) "$readyCount source(s) ready" else "Add a playlist first"\n',
    '            status = if (availableCount > 0) "$availableCount source(s) available" else "Enable or add a playlist"\n',
    "TV Settings Live status",
)
write(path, text)

path = "app/src/main/java/app/ownplay/player/ui/SettingsPlaylist.kt"
text = read(path)
text = replace_once(
    text,
    '''    onBack: () -> Unit,
    onOpenInLive: (String) -> Unit,
    focusPrimaryOnEntry: Boolean = false,
''',
    '''    onBack: () -> Unit,
    onOpenInLive: (String) -> Unit,
    disabledSourceIds: Set<String> = emptySet(),
    onSetSourceEnabled: (String, Boolean) -> Unit = { _, _ -> },
    focusPrimaryOnEntry: Boolean = false,
''',
    "playlist subpage availability signature",
)
text = replace_once(
    text,
    '''                syncState = syncState,
                onOpenInLive = onOpenInLive,
                initialFocusRequester = contentFocusRequester,
''',
    '''                syncState = syncState,
                onOpenInLive = onOpenInLive,
                disabledSourceIds = disabledSourceIds,
                onSetSourceEnabled = onSetSourceEnabled,
                initialFocusRequester = contentFocusRequester,
''',
    "playlist subpage availability routing",
)
write(path, text)

path = "app/src/main/java/app/ownplay/player/ui/PlaylistSettingsScreen.kt"
text = read(path)
text = replace_once(
    text,
    '''    syncState: SourceSyncState,
    onOpenInLive: (String) -> Unit,
    initialFocusRequester: FocusRequester? = null,
''',
    '''    syncState: SourceSyncState,
    onOpenInLive: (String) -> Unit,
    disabledSourceIds: Set<String> = emptySet(),
    onSetSourceEnabled: (String, Boolean) -> Unit = { _, _ -> },
    initialFocusRequester: FocusRequester? = null,
''',
    "playlist screen availability signature",
)
text = replace_once(
    text,
    '''        summaries.forEach { summary ->
            PlaylistCard(
                summary = summary,
''',
    '''        summaries.forEach { summary ->
            val userEnabled = summary.sourceId !in disabledSourceIds
            PlaylistCard(
                summary = summary,
''',
    "playlist card user availability",
)
text = replace_once(
    text,
    '''                importQueued = summary.sourceId in pendingImportExecution.queuedSourceIds,
                importActive = summary.sourceId in pendingImportExecution.activeSourceIds,
                isActive = summary.enabled && summary.sourceId == activeSourceId,
                onSetActive = {
''',
    '''                importQueued = summary.sourceId in pendingImportExecution.queuedSourceIds,
                importActive = summary.sourceId in pendingImportExecution.activeSourceIds,
                userEnabled = userEnabled,
                isActive = summary.enabled && userEnabled && summary.sourceId == activeSourceId,
                onSetActive = {
''',
    "playlist card availability inputs",
)
text = replace_once(
    text,
    "                onOpen = { onOpenInLive(summary.sourceId) },\n",
    "                onOpen = { onOpenInLive(summary.sourceId) },\n                onSetUserEnabled = { enabled -> onSetSourceEnabled(summary.sourceId, enabled) },\n",
    "playlist card availability callback",
)
text = replace_once(
    text,
    '''    importQueued: Boolean,
    importActive: Boolean,
    isActive: Boolean,
    onSetActive: () -> Unit,
    onOpen: () -> Unit,
''',
    '''    importQueued: Boolean,
    importActive: Boolean,
    userEnabled: Boolean,
    isActive: Boolean,
    onSetActive: () -> Unit,
    onOpen: () -> Unit,
    onSetUserEnabled: (Boolean) -> Unit,
''',
    "playlist card availability signature",
)
text = replace_once(
    text,
    '''                            importFailed -> "${sourceKindLabel(summary.sourceKind)} • Import failed"
                            importing -> "${sourceKindLabel(summary.sourceKind)} • Waiting to import…"
                            else -> "${sourceKindLabel(summary.sourceKind)} • ${summary.channelCount} channels"
''',
    '''                            importFailed -> "${sourceKindLabel(summary.sourceKind)} • Import failed"
                            importing -> "${sourceKindLabel(summary.sourceKind)} • Waiting to import…"
                            !userEnabled -> "${sourceKindLabel(summary.sourceKind)} • Disabled on TV"
                            else -> "${sourceKindLabel(summary.sourceKind)} • ${summary.channelCount} channels"
''',
    "playlist card disabled status",
)
text = replace_once(
    text,
    "                        enabled = summary.enabled,\n                        role = Role.RadioButton,\n",
    "                        enabled = summary.enabled && userEnabled,\n                        role = Role.RadioButton,\n",
    "playlist active selector availability",
)
text = replace_once(
    text,
    "                    enabled = summary.enabled,\n                )\n",
    "                    enabled = summary.enabled && userEnabled,\n                )\n",
    "playlist radio availability",
)
text = replace_once(
    text,
    '''                        importFailed -> "Retry import before activating"
                        importing -> "Available after import"
                        else -> "Use as active playlist"
''',
    '''                        importFailed -> "Retry import before activating"
                        importing -> "Available after import"
                        !userEnabled -> "Enable to use this playlist"
                        else -> "Use as active playlist"
''',
    "playlist active label availability",
)
text = replace_once(
    text,
    "                    color = if (summary.enabled) {\n",
    "                    color = if (summary.enabled && userEnabled) {\n",
    "playlist active label color",
)
text = replace_once(
    text,
    '                TextButton(onClick = onOpen, enabled = summary.enabled) { Text("Live") }\n',
    '                TextButton(onClick = onOpen, enabled = summary.enabled && userEnabled) { Text("Live") }\n',
    "playlist Live availability",
)
text = replace_once(
    text,
    '''                TextButton(onClick = onEdit, enabled = summary.enabled && !syncing) { Text("Edit") }
                TextButton(onClick = onDelete) { Text("Delete") }
''',
    '''                TextButton(onClick = onEdit, enabled = summary.enabled && !syncing) { Text("Edit") }
                TextButton(
                    onClick = { onSetUserEnabled(!userEnabled) },
                    enabled = summary.enabled && !syncing,
                ) {
                    Text(if (userEnabled) "Disable" else "Enable")
                }
                TextButton(onClick = onDelete) { Text("Delete") }
''',
    "playlist availability action",
)
write(path, text)
