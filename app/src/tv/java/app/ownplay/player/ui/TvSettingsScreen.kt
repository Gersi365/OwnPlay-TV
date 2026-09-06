package app.ownplay.player.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.ownplay.player.BuildConfig
import app.ownplay.player.OwnPlayAppRuntime
import app.ownplay.player.persistence.PlaylistSourceSummary
import app.ownplay.player.source.SourceSyncState

private val TV_SETTINGS_ROW_HEIGHT = 72.dp

private enum class TvSettingsDestination {
    PLAYLISTS,
    LIVE_MANAGEMENT,
    BACKUP_RESTORE,
    ABOUT,
}

private enum class TvSettingsPage {
    ROOT,
    PLAYLISTS,
    LIVE_MANAGEMENT,
    BACKUP_RESTORE,
    ABOUT,
}

@Composable
internal fun TvSettingsScreen(
    runtime: OwnPlayAppRuntime,
    summaries: List<PlaylistSourceSummary>,
    syncState: SourceSyncState,
    onOpenSourceInLive: (String) -> Unit,
) {
    var page by remember { mutableStateOf(TvSettingsPage.ROOT) }
    var lastRootDestination by remember { mutableStateOf(TvSettingsDestination.PLAYLISTS) }
    val rootFocusRequesters = remember {
        TvSettingsDestination.entries.associateWith { FocusRequester() }
    }

    BackHandler(enabled = page != TvSettingsPage.ROOT) {
        page = TvSettingsPage.ROOT
    }

    LaunchedEffect(page, lastRootDestination) {
        if (page == TvSettingsPage.ROOT) {
            rootFocusRequesters.getValue(lastRootDestination).requestFocus()
        }
    }

    fun open(destination: TvSettingsDestination) {
        lastRootDestination = destination
        page = when (destination) {
            TvSettingsDestination.PLAYLISTS -> TvSettingsPage.PLAYLISTS
            TvSettingsDestination.LIVE_MANAGEMENT -> TvSettingsPage.LIVE_MANAGEMENT
            TvSettingsDestination.BACKUP_RESTORE -> TvSettingsPage.BACKUP_RESTORE
            TvSettingsDestination.ABOUT -> TvSettingsPage.ABOUT
        }
    }

    when (page) {
        TvSettingsPage.ROOT -> TvSettingsRoot(
            summaries = summaries,
            initialFocusedDestination = lastRootDestination,
            focusRequesters = rootFocusRequesters,
            onOpen = ::open,
        )

        TvSettingsPage.PLAYLISTS -> PlaylistManagementSubscreen(
            runtime = runtime,
            summaries = summaries,
            syncState = syncState,
            onBack = { page = TvSettingsPage.ROOT },
            onOpenInLive = onOpenSourceInLive,
            focusBackOnEntry = true,
        )

        TvSettingsPage.LIVE_MANAGEMENT -> LiveManagementScreen(
            runtime = runtime,
            summaries = summaries.filter { summary -> summary.enabled },
            onBack = { page = TvSettingsPage.ROOT },
            focusBackOnEntry = true,
        )

        TvSettingsPage.BACKUP_RESTORE -> TvSettingsInformationPage(
            title = "Backup & Restore",
            subtitle = "Create or restore supported local personalization.",
            onBack = { page = TvSettingsPage.ROOT },
        ) {
            BackupRestoreSettingsContent()
        }

        TvSettingsPage.ABOUT -> TvSettingsInformationPage(
            title = "About",
            subtitle = "OwnPlay TV product and build information.",
            onBack = { page = TvSettingsPage.ROOT },
        ) {
            AboutSettingsContent()
        }
    }
}

@Composable
private fun TvSettingsRoot(
    summaries: List<PlaylistSourceSummary>,
    initialFocusedDestination: TvSettingsDestination,
    focusRequesters: Map<TvSettingsDestination, FocusRequester>,
    onOpen: (TvSettingsDestination) -> Unit,
) {
    var focusedDestination by remember(initialFocusedDestination) {
        mutableStateOf(initialFocusedDestination)
    }
    val enabledCount = summaries.count { summary -> summary.enabled }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp, vertical = 22.dp),
        horizontalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        Column(
            modifier = Modifier
                .width(360.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            )

            TvSettingsRootRow(
                icon = Icons.Filled.Folder,
                title = "Playlists",
                detail = "$enabledCount enabled",
                focused = focusedDestination == TvSettingsDestination.PLAYLISTS,
                focusRequester = focusRequesters.getValue(TvSettingsDestination.PLAYLISTS),
                onFocused = { focusedDestination = TvSettingsDestination.PLAYLISTS },
                onClick = { onOpen(TvSettingsDestination.PLAYLISTS) },
            )
            TvSettingsRootRow(
                icon = Icons.Filled.Tune,
                title = "Live Management",
                detail = "Categories & channels",
                focused = focusedDestination == TvSettingsDestination.LIVE_MANAGEMENT,
                focusRequester = focusRequesters.getValue(TvSettingsDestination.LIVE_MANAGEMENT),
                onFocused = { focusedDestination = TvSettingsDestination.LIVE_MANAGEMENT },
                onClick = { onOpen(TvSettingsDestination.LIVE_MANAGEMENT) },
            )
            TvSettingsRootRow(
                icon = Icons.Filled.Save,
                title = "Backup & Restore",
                detail = "Personalization",
                focused = focusedDestination == TvSettingsDestination.BACKUP_RESTORE,
                focusRequester = focusRequesters.getValue(TvSettingsDestination.BACKUP_RESTORE),
                onFocused = { focusedDestination = TvSettingsDestination.BACKUP_RESTORE },
                onClick = { onOpen(TvSettingsDestination.BACKUP_RESTORE) },
            )
            TvSettingsRootRow(
                icon = Icons.Filled.Info,
                title = "About",
                detail = BuildConfig.VERSION_NAME,
                focused = focusedDestination == TvSettingsDestination.ABOUT,
                focusRequester = focusRequesters.getValue(TvSettingsDestination.ABOUT),
                onFocused = { focusedDestination = TvSettingsDestination.ABOUT },
                onClick = { onOpen(TvSettingsDestination.ABOUT) },
            )
        }

        TvSettingsContextPanel(
            destination = focusedDestination,
            configuredCount = summaries.size,
            enabledCount = enabledCount,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
    }
}

@Composable
private fun TvSettingsRootRow(
    icon: ImageVector,
    title: String,
    detail: String,
    focused: Boolean,
    focusRequester: FocusRequester,
    onFocused: () -> Unit,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(TV_SETTINGS_ROW_HEIGHT)
            .focusRequester(focusRequester)
            .onFocusChanged { state ->
                if (state.isFocused) onFocused()
            }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (focused) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
        },
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (focused) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (focused) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (focused) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun TvSettingsContextPanel(
    destination: TvSettingsDestination,
    configuredCount: Int,
    enabledCount: Int,
    modifier: Modifier = Modifier,
) {
    val title: String
    val description: String
    val status: String

    when (destination) {
        TvSettingsDestination.PLAYLISTS -> {
            title = "Playlists"
            description = "Add, edit, refresh, enable and open the media sources used by OwnPlay TV."
            status = "$configuredCount configured · $enabledCount enabled"
        }
        TvSettingsDestination.LIVE_MANAGEMENT -> {
            title = "Live Management"
            description = "Organize Live categories and channels, hidden state, ordering and custom groups."
            status = if (enabledCount > 0) "$enabledCount source(s) available" else "Add a playlist first"
        }
        TvSettingsDestination.BACKUP_RESTORE -> {
            title = "Backup & Restore"
            description = "Create or restore supported local personalization without copying provider credentials or secrets."
            status = "Personalization only"
        }
        TvSettingsDestination.ABOUT -> {
            title = "About"
            description = "OwnPlay TV plays and organizes media sources you provide. It does not provide channels or subscriptions."
            status = "Version ${BuildConfig.VERSION_NAME}"
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.14f),
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 30.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = status,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun TvSettingsInformationPage(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    val backFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        backFocusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            TextButton(
                onClick = onBack,
                modifier = Modifier.focusRequester(backFocusRequester),
            ) {
                Text("‹ Settings")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            content()
        }
    }
}
