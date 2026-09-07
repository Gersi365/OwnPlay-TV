package app.ownplay.player.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import app.ownplay.player.OwnPlayAppRuntime
import app.ownplay.player.livePlaybackPresentationSession
import app.ownplay.player.onDemandPresentationSession
import app.ownplay.player.playback.LivePlaybackSelection
import app.ownplay.player.playback.LivePlaybackSurfaceTeardown
import app.ownplay.player.playback.LivePlaybackTransitionGate
import app.ownplay.player.playback.LivePlaybackTransitionTarget
import app.ownplay.player.playback.OnDemandContentKind
import app.ownplay.player.playback.PlaybackInteractionBridge
import app.ownplay.player.source.SourceSyncState
import app.ownplay.player.source.selection.ActivePlaylistSelection
import app.ownplay.player.source.selection.ActivePlaylistStore
import app.ownplay.player.source.selection.resolveActivePlaylistId
import app.ownplay.player.ui.series.SeriesRoute
import app.ownplay.player.ui.vod.VodRoute
import kotlinx.coroutines.launch

private enum class TVSection {
    LIVE,
    MOVIES,
    SERIES,
    SETTINGS,
}

/**
 * TV-only OwnPlay presentation shell.
 *
 * Primary navigation exposes Live / Movies / Series / Settings as a fixed icon-only rail.
 * The shell keeps global navigation geometry stable while page-specific routes retain ownership of
 * their own nested Back/focus restoration behavior.
 */
@Composable
internal fun TVOwnPlayApp(
    runtime: OwnPlayAppRuntime,
    onPlaybackFullscreenChanged: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val activePlaylistStore = remember(context) {
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
    val summaries by runtime.observeSourceSummaries().collectAsState(initial = emptyList())
    val syncState by runtime.sourceSyncState.collectAsState()
    val playbackState by runtime.playbackController.state.collectAsState()
    val playbackTrackState by runtime.playbackTrackController.state.collectAsState()
    val livePresentation by runtime.livePlaybackPresentationSession.state.collectAsState()
    val onDemandPresentation by runtime.onDemandPresentationSession.state.collectAsState()

    var section by remember {
        mutableStateOf(
            when (onDemandPresentation.kind) {
                OnDemandContentKind.MOVIE -> TVSection.MOVIES
                OnDemandContentKind.SERIES -> TVSection.SERIES
                null -> TVSection.LIVE
            },
        )
    }
    var activeSourceId by remember { mutableStateOf(onDemandPresentation.sourceId) }
    var requestedVodMovieId by remember {
        mutableStateOf(
            onDemandPresentation.itemId.takeIf {
                onDemandPresentation.kind == OnDemandContentKind.MOVIE
            },
        )
    }
    var requestedSeriesId by remember {
        mutableStateOf(
            onDemandPresentation.itemId.takeIf {
                onDemandPresentation.kind == OnDemandContentKind.SERIES
            },
        )
    }
    val vodFullscreen = onDemandPresentation.isMoviePlayback
    val seriesFullscreen = onDemandPresentation.isSeriesPlayback
    val activeSelection = livePresentation.selection
    val fullscreenSelection = livePresentation.fullscreenSelection
    val liveTransitionGate = remember { LivePlaybackTransitionGate() }

    fun rememberActiveSource(sourceId: String?) {
        activeSourceId = sourceId
        activePlaylistScope.launch {
            activePlaylistStore.set(sourceId)
        }
    }

    fun stopLivePresentation(clearPresentation: () -> Unit) {
        LivePlaybackSurfaceTeardown.stopAfterDetaching(
            detachCurrentSurface = {
                PlaybackInteractionBridge.detachCurrent(runtime.playbackVideoOutput)
            },
            stopPlayback = runtime.playbackController::stop,
            clearPresentation = clearPresentation,
        )
    }

    fun openLiveFullscreen(selection: LivePlaybackSelection) {
        liveTransitionGate.requestHandoff(
            target = LivePlaybackTransitionTarget.fullscreen(selection),
            detachCurrentSurface = {
                PlaybackInteractionBridge.detachCurrent(runtime.playbackVideoOutput)
            },
            stopPlayback = runtime.playbackController::stop,
            switchPresentation = {
                runtime.livePlaybackPresentationSession.showFullscreen(selection)
            },
            startPlayback = { runtime.playbackController.start(selection.request) },
        )
    }

    fun returnLiveToPreview(selection: LivePlaybackSelection) {
        liveTransitionGate.requestHandoff(
            target = LivePlaybackTransitionTarget.preview(selection),
            detachCurrentSurface = {
                PlaybackInteractionBridge.detachCurrent(runtime.playbackVideoOutput)
            },
            stopPlayback = runtime.playbackController::stop,
            switchPresentation = {
                rememberActiveSource(selection.request.sourceId)
                section = TVSection.LIVE
                runtime.livePlaybackPresentationSession.showPreview(selection)
            },
            startPlayback = { runtime.playbackController.start(selection.request) },
        )
    }

    fun openSection(target: TVSection) {
        if (target != TVSection.LIVE && activeSelection != null) {
            stopLivePresentation {
                runtime.livePlaybackPresentationSession.clear()
            }
        }

        val onDemandCurrent = runtime.onDemandPresentationSession.current
        when (target) {
            TVSection.MOVIES -> {
                if (onDemandCurrent.kind != OnDemandContentKind.MOVIE) {
                    activeSourceId?.let(runtime.onDemandPresentationSession::showMovieCatalog)
                }
            }
            TVSection.SERIES -> {
                if (onDemandCurrent.kind != OnDemandContentKind.SERIES) {
                    activeSourceId?.let(runtime.onDemandPresentationSession::showSeriesCatalog)
                }
            }
            else -> if (onDemandCurrent.kind != null) {
                runtime.onDemandPresentationSession.clear()
            }
        }

        if (target != TVSection.MOVIES) {
            requestedVodMovieId = null
        }
        if (target != TVSection.SERIES) {
            requestedSeriesId = null
        }
        section = target
    }

    BackHandler(enabled = section != TVSection.LIVE) {
        val interactionHandled = when (section) {
            TVSection.MOVIES,
            TVSection.SERIES,
            -> PlaybackInteractionBridge.handleBack()
            TVSection.LIVE,
            TVSection.SETTINGS,
            -> false
        }
        if (interactionHandled) return@BackHandler

        when (section) {
            TVSection.MOVIES,
            TVSection.SERIES,
            TVSection.SETTINGS,
            -> openSection(TVSection.LIVE)
            TVSection.LIVE -> Unit
        }
    }

    LaunchedEffect(summaries, activePlaylistSelection, disabledSourceIds) {
        val persistedSelection = activePlaylistSelection as? ActivePlaylistSelection.Ready
            ?: return@LaunchedEffect
        val availableSourceIds = resolveTvAvailableSourceIds(
            summaries = summaries,
            disabledSourceIds = disabledSourceIds,
        )
        val previousSourceId = activeSourceId
        val resolvedSourceId = resolveActivePlaylistId(
            persistedSourceId = persistedSelection.sourceId,
            currentSourceId = activeSourceId,
            enabledSourceIds = availableSourceIds,
        )
        activeSourceId = resolvedSourceId

        if (availableSourceIds.isNotEmpty() && persistedSelection.sourceId != resolvedSourceId) {
            activePlaylistStore.set(resolvedSourceId)
        }
        if (resolvedSourceId != null && previousSourceId != resolvedSourceId) {
            runtime.onActiveSourceSelected(resolvedSourceId)
        }

        val selectionSourceId = activeSelection?.request?.sourceId
        if (selectionSourceId != null && selectionSourceId != resolvedSourceId) {
            stopLivePresentation {
                runtime.livePlaybackPresentationSession.clear()
            }
        }
        val onDemandSourceId = runtime.onDemandPresentationSession.current.sourceId
        if (
            availableSourceIds.isNotEmpty() &&
            resolvedSourceId != null &&
            onDemandSourceId != null &&
            onDemandSourceId != resolvedSourceId
        ) {
            runtime.onDemandPresentationSession.clear()
        }
        if (resolvedSourceId == null) {
            requestedVodMovieId = null
            requestedSeriesId = null
        }
    }

    val previewActive =
        section == TVSection.LIVE &&
            activeSelection != null &&
            fullscreenSelection == null
    val observedLiveTransitionTarget =
        fullscreenSelection?.let(LivePlaybackTransitionTarget::fullscreen)
            ?: if (previewActive) {
                activeSelection?.let(LivePlaybackTransitionTarget::preview)
            } else {
                null
            }

    SideEffect {
        liveTransitionGate.reconcileObserved(observedLiveTransitionTarget)
    }

    LaunchedEffect(fullscreenSelection != null) {
        onPlaybackFullscreenChanged(fullscreenSelection != null)
    }

    val openedFullscreen = fullscreenSelection
    if (openedFullscreen != null) {
        PlaybackScreen(
            selection = openedFullscreen,
            state = playbackState,
            trackState = playbackTrackState,
            videoOutput = runtime.playbackVideoOutput,
            onPlay = runtime.playbackController::play,
            onPause = runtime.playbackController::pause,
            onRetry = runtime.playbackController::retry,
            onAudioSelection = runtime.playbackTrackController::selectAudio,
            onSubtitleSelection = runtime.playbackTrackController::selectSubtitle,
            onNavigate = { direction ->
                (fullscreenSelection ?: openedFullscreen)
                    .navigate(direction)
                    ?.let { target ->
                        runtime.livePlaybackPresentationSession.replaceSelection(target)
                        runtime.playbackController.start(target.request)
                    }
            },
            onReturnToChannels = {
                returnLiveToPreview(
                    fullscreenSelection ?: activeSelection ?: openedFullscreen,
                )
            },
            onFullscreenStateChanged = {},
        )
        return
    }

    val activeSummary = summaries.firstOrNull { summary ->
        summary.sourceId == activeSourceId &&
            summary.enabled &&
            summary.sourceId !in disabledSourceIds
    }
    val hidePrimaryNavigation = vodFullscreen || seriesFullscreen

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = if (hidePrimaryNavigation) 0.dp else 82.dp),
        ) {
            when (section) {
                TVSection.LIVE -> {
                    val sourceId = activeSourceId
                    if (sourceId == null) {
                        TVNoSourceScreen(
                            syncState = syncState,
                            onAddPlaylist = { openSection(TVSection.SETTINGS) },
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        LiveRoute(
                            runtime = runtime,
                            sourceId = sourceId,
                            activeSelection = activeSelection,
                            playbackState = playbackState,
                            videoOutput = runtime.playbackVideoOutput,
                            syncState = syncState,
                            onOpenMovies = { openSection(TVSection.MOVIES) },
                            onOpenSeries = { openSection(TVSection.SERIES) },
                            onOpenSettings = { openSection(TVSection.SETTINGS) },
                            onPreviewRequested = { selection ->
                                runtime.livePlaybackPresentationSession.showPreview(selection)
                                runtime.playbackController.start(selection.request)
                                liveTransitionGate.reconcileObserved(
                                    LivePlaybackTransitionTarget.preview(selection),
                                )
                            },
                            onPreviewClosed = {
                                stopLivePresentation {
                                    runtime.livePlaybackPresentationSession.clear()
                                }
                            },
                            onOpenFullscreen = { selection ->
                                openLiveFullscreen(activeSelection ?: selection)
                            },
                        )
                    }
                }

                TVSection.MOVIES -> VodRoute(
                    runtime = runtime,
                    sourceId = activeSourceId,
                    sourceKind = activeSummary?.sourceKind,
                    requestedMovieId = requestedVodMovieId,
                    onRequestedMovieConsumed = { requestedVodMovieId = null },
                    onOpenLive = { openSection(TVSection.LIVE) },
                    onOpenSeries = { openSection(TVSection.SERIES) },
                    onOpenSettings = { openSection(TVSection.SETTINGS) },
                    onFullscreenStateChanged = onPlaybackFullscreenChanged,
                )

                TVSection.SERIES -> SeriesRoute(
                    runtime = runtime,
                    sourceId = activeSourceId,
                    sourceKind = activeSummary?.sourceKind,
                    requestedSeriesId = requestedSeriesId,
                    onRequestedSeriesConsumed = { requestedSeriesId = null },
                    onOpenSettings = { openSection(TVSection.SETTINGS) },
                    onFullscreenStateChanged = onPlaybackFullscreenChanged,
                )

                TVSection.SETTINGS -> TvSettingsScreen(
                    runtime = runtime,
                    summaries = summaries,
                    disabledSourceIds = disabledSourceIds,
                    onSetSourceEnabled = { sourceId, enabled ->
                        activePlaylistScope.launch {
                            playlistAvailabilityStore.setEnabled(sourceId, enabled)
                        }
                    },
                    syncState = syncState,
                    onOpenSourceInLive = { sourceId ->
                        if (sourceId != activeSourceId && activeSelection != null) {
                            stopLivePresentation {
                                runtime.livePlaybackPresentationSession.clear()
                            }
                        }
                        rememberActiveSource(sourceId)
                        runtime.onDemandPresentationSession.clear()
                        section = TVSection.LIVE
                    },
                )
            }
        }

        if (!hidePrimaryNavigation) {
            TVPrimaryNavigationRail(
                selected = section,
                onOpenLive = { openSection(TVSection.LIVE) },
                onOpenMovies = { openSection(TVSection.MOVIES) },
                onOpenSeries = { openSection(TVSection.SERIES) },
                onOpenSettings = { openSection(TVSection.SETTINGS) },
            )
        }
    }
}

@Composable
private fun TVPrimaryNavigationRail(
    selected: TVSection,
    onOpenLive: () -> Unit,
    onOpenMovies: () -> Unit,
    onOpenSeries: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(82.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.42f))
            .padding(vertical = 24.dp)
            .focusGroup(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(12.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "OwnPlay",
                tint = Color.White,
                modifier = Modifier.size(30.dp),
            )
        }

        Spacer(Modifier.weight(1f))

        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TVPrimaryNavigationItem(
                label = "Live",
                icon = Icons.Filled.LiveTv,
                selected = selected == TVSection.LIVE,
                onClick = onOpenLive,
            )
            TVPrimaryNavigationItem(
                label = "Movies",
                icon = Icons.Filled.Movie,
                selected = selected == TVSection.MOVIES,
                onClick = onOpenMovies,
            )
            TVPrimaryNavigationItem(
                label = "Series",
                icon = Icons.Filled.VideoLibrary,
                selected = selected == TVSection.SERIES,
                onClick = onOpenSeries,
            )
            TVPrimaryNavigationItem(
                label = "Settings",
                icon = Icons.Filled.Settings,
                selected = selected == TVSection.SETTINGS,
                onClick = onOpenSettings,
            )
        }

        Spacer(Modifier.weight(1f))

        Text(
            text = "TV",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TVPrimaryNavigationItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    var focused by remember(label) { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val shape = RoundedCornerShape(12.dp)
    val borderColor = when {
        focused -> MaterialTheme.colorScheme.primary
        selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.72f)
        else -> MaterialTheme.colorScheme.outline
    }
    val backgroundColor = when {
        focused -> MaterialTheme.colorScheme.primaryContainer
        selected -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = Modifier
            .size(52.dp)
            .border(2.dp, borderColor, shape)
            .background(backgroundColor, shape)
            .onFocusChanged { focused = it.isFocused }
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) {
                    false
                } else {
                    when (event.key) {
                        Key.DirectionLeft -> true
                        Key.DirectionRight -> focusManager.moveFocus(FocusDirection.Right)
                        else -> false
                    }
                }
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = when {
                focused -> MaterialTheme.colorScheme.onPrimaryContainer
                selected -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun TVNoSourceScreen(
    syncState: SourceSyncState,
    onAddPlaylist: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = if (syncState.sourceId != null) "Loading Live TV…" else "No playlist configured",
            style = MaterialTheme.typography.titleLarge,
        )
        TextButton(onClick = onAddPlaylist) {
            Text("Open Settings")
        }
    }
}
