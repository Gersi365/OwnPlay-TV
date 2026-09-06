from pathlib import Path

ROOT = Path('.')


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding='utf-8')


def write(path: str, text: str) -> None:
    (ROOT / path).write_text(text, encoding='utf-8')


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise RuntimeError(f'{label}: expected one match, found {count}')
    return text.replace(old, new, 1)


def remove_once(text: str, old: str, label: str) -> str:
    return replace_once(text, old, '', label)


def matching_brace(text: str, open_index: int) -> int:
    depth = 0
    in_string = False
    escaped = False
    for index in range(open_index, len(text)):
        char = text[index]
        if in_string:
            if escaped:
                escaped = False
            elif char == '\\':
                escaped = True
            elif char == '"':
                in_string = False
            continue
        if char == '"':
            in_string = True
        elif char == '{':
            depth += 1
        elif char == '}':
            depth -= 1
            if depth == 0:
                return index
    raise RuntimeError('unmatched brace')


def remove_block(text: str, marker: str, label: str) -> str:
    start = text.find(marker)
    if start < 0:
        raise RuntimeError(f'{label}: marker not found')
    open_index = text.find('{', start)
    if open_index < 0:
        raise RuntimeError(f'{label}: opening brace not found')
    end = matching_brace(text, open_index)
    cursor = end + 1
    while cursor < len(text) and text[cursor] in ' \t':
        cursor += 1
    if cursor < len(text) and text[cursor] == '\n':
        cursor += 1
    return text[:start] + text[cursor:]


def unwrap_if_block(text: str, marker: str, label: str) -> str:
    start = text.find(marker)
    if start < 0:
        raise RuntimeError(f'{label}: marker not found')
    open_index = text.find('{', start)
    end = matching_brace(text, open_index)
    inner = text[open_index + 1:end]
    lines = inner.splitlines(True)
    inner = ''.join(line[4:] if line.startswith('    ') else line for line in lines)
    return text[:start] + inner.lstrip('\n') + text[end + 1:]


def replace_if_else_with_then(text: str, marker: str, label: str) -> str:
    start = text.find(marker)
    if start < 0:
        raise RuntimeError(f'{label}: marker not found')
    open_index = text.find('{', start)
    then_end = matching_brace(text, open_index)
    cursor = then_end + 1
    while cursor < len(text) and text[cursor].isspace():
        cursor += 1
    if not text.startswith('else', cursor):
        raise RuntimeError(f'{label}: else not found')
    else_open = text.find('{', cursor)
    else_end = matching_brace(text, else_open)
    inner = text[open_index + 1:then_end]
    lines = inner.splitlines(True)
    inner = ''.join(line[4:] if line.startswith('    ') else line for line in lines)
    return text[:start] + inner.lstrip('\n') + text[else_end + 1:]


def remove_between(text: str, start_marker: str, end_marker: str, label: str) -> str:
    start = text.find(start_marker)
    if start < 0:
        raise RuntimeError(f'{label}: start marker not found')
    end = text.find(end_marker, start)
    if end < 0:
        raise RuntimeError(f'{label}: end marker not found')
    return text[:start] + text[end:]


def remove_named_lambda_argument(text: str, marker: str, label: str) -> str:
    start = text.find(marker)
    if start < 0:
        raise RuntimeError(f'{label}: marker not found')
    open_index = text.find('{', start)
    end = matching_brace(text, open_index)
    cursor = end + 1
    while cursor < len(text) and text[cursor] in ' \t':
        cursor += 1
    if cursor < len(text) and text[cursor] == ',':
        cursor += 1
    if cursor < len(text) and text[cursor] == '\n':
        cursor += 1
    return text[:start] + text[cursor:]


def assert_absent(text: str, tokens: tuple[str, ...], label: str) -> None:
    leftovers = [token for token in tokens if token in text]
    if leftovers:
        raise RuntimeError(f'{label}: forbidden residue {leftovers}')


# Unified Library: keep only the TV presentation. Offline/download backend can remain shared,
# but TV never exposes the Offline filter or mobile loading/search chrome.
path = 'app/src/main/java/app/ownplay/player/ui/library/UnifiedLibraryRoute.kt'
text = read(path)
text = text.replace('import android.content.res.Configuration\n', '')
text = text.replace('import androidx.compose.ui.platform.LocalConfiguration\n', '')
text = remove_once(
    text,
    '''    val configuration = LocalConfiguration.current\n    val isTelevision =\n        configuration.uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_TELEVISION\n''',
    'Library root TV probe',
)
text = replace_once(
    text,
    '    val presentationDownloads = if (isTelevision) emptyList() else downloads\n',
    '    val presentationDownloads = emptyList<OfflineDownload>()\n',
    'Library TV download presentation',
)
text = replace_once(
    text,
    '''    var filter by remember(isTelevision) {\n        mutableStateOf(UnifiedLibraryFilter.MOVIES)\n    }\n''',
    '    var filter by remember { mutableStateOf(UnifiedLibraryFilter.MOVIES) }\n',
    'Library TV filter state',
)
text = replace_once(
    text,
    '    var offlineOnly by remember(isTelevision) { mutableStateOf(false) }\n',
    '    var offlineOnly by remember { mutableStateOf(false) }\n',
    'Library TV offline state',
)
text = remove_once(
    text,
    '    var searchExpanded by remember(isTelevision) { mutableStateOf(isTelevision) }\n',
    'Library mobile search state',
)
text = remove_block(text, '    LaunchedEffect(isTelevision) {\n', 'Library form-factor setup effect')
text = remove_once(
    text,
    '''    val showInitialMobileLoading = shouldShowMobileLibraryInitialLoading(\n        isTelevision = isTelevision,\n        offlineOnly = offlineOnly,\n        hasItems = hasItems,\n        refreshing = refreshing,\n        initialRefreshPending = initialCatalogRefreshPending,\n    )\n''',
    'Library mobile loading state',
)
text = replace_once(
    text,
    '''    LaunchedEffect(isTelevision, visibleFocusKeys, libraryViewMode) {\n        if (!isTelevision || visibleFocusKeys.isEmpty()) return@LaunchedEffect\n''',
    '''    LaunchedEffect(visibleFocusKeys, libraryViewMode) {\n        if (visibleFocusKeys.isEmpty()) return@LaunchedEffect\n''',
    'Library TV focus effect',
)
text = replace_once(
    text,
    '''            .padding(\n                horizontal = if (isTelevision) 20.dp else 10.dp,\n                vertical = if (isTelevision) 12.dp else 4.dp,\n            ),\n        verticalArrangement = Arrangement.spacedBy(if (isTelevision) 10.dp else 4.dp),\n''',
    '''            .padding(horizontal = 20.dp, vertical = 12.dp),\n        verticalArrangement = Arrangement.spacedBy(10.dp),\n''',
    'Library TV geometry',
)
text = replace_if_else_with_then(
    text,
    '        if (isTelevision) {\n            Row(\n',
    'Library TV-only header',
)
text = text.replace(
    '''                        text = if (offlineOnly) {\n                            "Local files on this device · playback works without internet"\n                        } else {\n                            "Movies and Series from your active playlist"\n                        },\n''',
    '                        text = "Movies and Series from your active playlist",\n',
)
text = text.replace('                showLabel = isTelevision,\n', '                showLabel = true,\n')
text = unwrap_if_block(
    text,
    '        if (isTelevision || searchExpanded || query.isNotBlank()) {\n',
    'Library always-visible TV search',
)
text = remove_block(text, '        if (showInitialMobileLoading) {\n', 'Library mobile loading surface')
text = remove_once(
    text,
    '''    val configuration = LocalConfiguration.current\n    val isTelevision =\n        configuration.uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_TELEVISION\n''',
    'Library catalog TV probe',
)
text = replace_once(text, '    val cardMinSize = if (isTelevision) 172.dp else 150.dp\n', '    val cardMinSize = 172.dp\n', 'Library card size')
text = replace_once(text, '    val compactMinSize = if (isTelevision) 120.dp else 108.dp\n', '    val compactMinSize = 120.dp\n', 'Library compact size')
text = remove_between(
    text,
    'internal fun shouldShowMobileLibraryInitialLoading(',
    'private suspend fun enqueueSeriesEpisode(',
    'Library mobile loading policy',
)
assert_absent(text, ('isTelevision', 'LocalConfiguration', 'shouldShowMobileLibraryInitialLoading', 'searchExpanded'), 'UnifiedLibraryRoute')
write(path, text)

presentation_test = ROOT / 'app/src/test/java/app/ownplay/player/ui/library/UnifiedLibraryPresentationTest.kt'
if not presentation_test.is_file():
    raise RuntimeError('UnifiedLibraryPresentationTest missing before TV-only deletion')
presentation_test.unlink()

# Continue Watching: fixed TV geometry, subtitle and progress presentation.
path = 'app/src/main/java/app/ownplay/player/ui/library/LibraryContinueWatching.kt'
text = read(path)
text = text.replace('import android.content.res.Configuration\n', '')
text = text.replace('import androidx.compose.ui.platform.LocalConfiguration\n', '')
text = text.replace('        hideSubtitleOnMobile = true,\n', '')
text = text.replace('        hideSubtitleOnMobile = false,\n', '')
text = remove_once(text, '    hideSubtitleOnMobile: Boolean,\n', 'Continue Watching mobile subtitle API')
text = remove_once(
    text,
    '''    val configuration = LocalConfiguration.current\n    val isTelevision =\n        configuration.uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_TELEVISION\n''',
    'Continue Watching TV probe',
)
text = replace_once(text, '    val cardWidth = if (isTelevision) 172.dp else 138.dp\n', '    val cardWidth = 172.dp\n', 'Continue Watching card width')
text = replace_once(
    text,
    '''            style = if (isTelevision) {\n                MaterialTheme.typography.titleMedium\n            } else {\n                MaterialTheme.typography.titleSmall\n            },\n''',
    '            style = MaterialTheme.typography.titleMedium,\n',
    'Continue Watching title style',
)
text = replace_once(text, '            horizontalArrangement = Arrangement.spacedBy(if (isTelevision) 10.dp else 8.dp),\n', '            horizontalArrangement = Arrangement.spacedBy(10.dp),\n', 'Continue Watching spacing')
text = remove_block(text, '                        if (!isTelevision) {\n                            ContinueWatchingProgressSlot', 'Continue Watching mobile progress slot')
text = unwrap_if_block(text, '                        if (isTelevision || !hideSubtitleOnMobile) {\n', 'Continue Watching TV subtitle')
text = replace_if_else_with_then(text, '                        if (isTelevision) {\n                            progress?.let', 'Continue Watching TV progress')
text = remove_block(text, '@Composable\nprivate fun ContinueWatchingProgressSlot', 'Continue Watching mobile progress composable')
assert_absent(text, ('isTelevision', 'hideSubtitleOnMobile', 'LocalConfiguration'), 'LibraryContinueWatching')
write(path, text)

# Offline-series shared component is unreachable from TV catalog, but its focus behavior remains TV-first.
path = 'app/src/main/java/app/ownplay/player/ui/library/LibrarySeriesComponents.kt'
text = read(path)
text = text.replace('import android.content.res.Configuration\n', '')
text = text.replace('import androidx.compose.ui.platform.LocalConfiguration\n', '')
text = remove_once(
    text,
    '''    val configuration = LocalConfiguration.current\n    val isTelevision =\n        configuration.uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_TELEVISION\n''',
    'Library series TV probe',
)
text = replace_once(
    text,
    '''    LaunchedEffect(isTelevision, group.key, selectedSeasonNumber, selectedEpisodeId) {\n        if (isTelevision) {\n            detailBackFocusRequester.requestFocus()\n        }\n    }\n''',
    '''    LaunchedEffect(group.key, selectedSeasonNumber, selectedEpisodeId) {\n        detailBackFocusRequester.requestFocus()\n    }\n''',
    'Library series TV entry focus',
)
text = replace_once(
    text,
    '''    LaunchedEffect(\n        isTelevision,\n        returnFocusEpisodeId,\n        returnFocusGeneration,\n        selectedEpisodeId,\n    ) {\n        if (\n            isTelevision &&\n            returnFocusGeneration > 0 &&\n''',
    '''    LaunchedEffect(\n        returnFocusEpisodeId,\n        returnFocusGeneration,\n        selectedEpisodeId,\n    ) {\n        if (\n            returnFocusGeneration > 0 &&\n''',
    'Library series TV focus restoration',
)
text = replace_once(
    text,
    '''                    .takeIf {\n                        isTelevision &&\n                            returnFocusGeneration > 0 &&\n''',
    '''                    .takeIf {\n                        returnFocusGeneration > 0 &&\n''',
    'Library series TV primary requester',
)
assert_absent(text, ('isTelevision', 'LocalConfiguration', 'UI_MODE_TYPE_TELEVISION'), 'LibrarySeriesComponents')
write(path, text)

# EPG guide focus policy is now intrinsically TV-only.
path = 'app/src/main/java/app/ownplay/player/ui/EpgGuideFocusPolicy.kt'
text = read(path)
text = remove_once(text, '        isTelevision: Boolean,\n', 'EPG guide device parameter')
text = remove_once(
    text,
    '''        if (!isTelevision) {\n            return EpgGuideInitialFocus(EpgGuideFocusTarget.NONE)\n        }\n''',
    'EPG guide non-TV branch',
)
assert_absent(text, ('isTelevision',), 'EpgGuideFocusPolicy')
write(path, text)

path = 'app/src/main/java/app/ownplay/player/ui/EpgGuideSheet.kt'
text = read(path)
text = text.replace('import android.content.res.Configuration\n', '')
text = text.replace('import androidx.compose.ui.platform.LocalConfiguration\n', '')
text = remove_once(
    text,
    '''    val configuration = LocalConfiguration.current\n    val isTelevision =\n        configuration.uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_TELEVISION\n''',
    'EPG guide TV probe',
)
text = remove_once(text, '        isTelevision = isTelevision,\n', 'EPG guide TV policy argument')
text = remove_once(text, '        isTelevision,\n', 'EPG guide focus effect device key')
text = unwrap_if_block(text, '                if (isTelevision) {\n                    TextButton(', 'EPG guide Done action')
assert_absent(text, ('isTelevision', 'LocalConfiguration', 'UI_MODE_TYPE_TELEVISION'), 'EpgGuideSheet')
write(path, text)

write(
    'app/src/test/java/app/ownplay/player/ui/EpgGuideFocusPolicyTest.kt',
    '''package app.ownplay.player.ui\n\nimport org.junit.Assert.assertEquals\nimport org.junit.Assert.assertNull\nimport org.junit.Test\n\nclass EpgGuideFocusPolicyTest {\n    @Test\n    fun `focuses current program when available`() {\n        val focus = EpgGuideFocusPolicy.initialFocus(\n            loading = false,\n            failed = false,\n            programCount = 5,\n            currentIndex = 3,\n        )\n\n        assertEquals(EpgGuideFocusTarget.PROGRAM, focus.target)\n        assertEquals(3, focus.programIndex)\n    }\n\n    @Test\n    fun `falls back to first program when current is unavailable`() {\n        val focus = EpgGuideFocusPolicy.initialFocus(\n            loading = false,\n            failed = false,\n            programCount = 4,\n            currentIndex = null,\n        )\n\n        assertEquals(EpgGuideFocusTarget.PROGRAM, focus.target)\n        assertEquals(0, focus.programIndex)\n    }\n\n    @Test\n    fun `falls back to done when guide cannot expose programs`() {\n        listOf(\n            EpgGuideFocusPolicy.initialFocus(loading = true, failed = false, programCount = 4, currentIndex = 2),\n            EpgGuideFocusPolicy.initialFocus(loading = false, failed = true, programCount = 4, currentIndex = 2),\n            EpgGuideFocusPolicy.initialFocus(loading = false, failed = false, programCount = 0, currentIndex = null),\n        ).forEach { focus ->\n            assertEquals(EpgGuideFocusTarget.DONE, focus.target)\n            assertNull(focus.programIndex)\n        }\n    }\n}\n''',
)

# Base cleanup renames the Landscape workspace to the TV workspace; remove its now-redundant TV probe.
path = 'app/src/main/java/app/ownplay/player/ui/live/TvLiveWorkspaceAdaptive.kt'
text = read(path)
text = text.replace('import android.content.res.Configuration\n', '')
text = text.replace('import androidx.compose.ui.platform.LocalConfiguration\n', '')
text = remove_once(
    text,
    '''    val configuration = LocalConfiguration.current\n    val isTelevision =\n        configuration.uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_TELEVISION\n''',
    'TV Live workspace device probe',
)
assert_absent(text, ('isTelevision', 'LocalConfiguration', 'UI_MODE_TYPE_TELEVISION'), 'TvLiveWorkspaceAdaptive')
write(path, text)

# Series details: preserve remote focus; remove all phone download/offline controls.
path = 'app/src/main/java/app/ownplay/player/ui/series/SeriesDetailsPane.kt'
text = read(path)
text = text.replace('import android.content.res.Configuration\n', '')
text = text.replace('import androidx.compose.ui.platform.LocalConfiguration\n', '')
probe = '''    val configuration = LocalConfiguration.current\n    val isTelevision =\n        configuration.uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_TELEVISION\n'''
if text.count(probe) != 2:
    raise RuntimeError(f'Series details TV probes: expected two matches, found {text.count(probe)}')
text = text.replace(probe, '', 2)
text = replace_once(
    text,
    '''    LaunchedEffect(\n        isTelevision,\n        focusBackOnEntry,\n''',
    '''    LaunchedEffect(\n        focusBackOnEntry,\n''',
    'Series details focus effect',
)
text = remove_once(text, '        if (!isTelevision) return@LaunchedEffect\n', 'Series details non-TV focus guard')
text = remove_once(text, '    val offlineCopyAvailable = !isTelevision && download?.state == DownloadStates.COMPLETED\n', 'Series episode offline flag')
text = replace_once(
    text,
    '''                    Text(\n                        when {\n                            offlineCopyAvailable && episode.resumeAvailable -> "Resume Offline"\n                            offlineCopyAvailable -> "Play Offline"\n                            episode.resumeAvailable -> "Resume"\n                            else -> "Play"\n                        },\n                    )\n''',
    '                    Text(if (episode.resumeAvailable) "Resume" else "Play")\n',
    'Series TV play label',
)
for marker, label in [
    ('                if (!isTelevision && !offlineCopyAvailable) {\n', 'Series download action'),
    ('                if (!isTelevision && download != null) {\n', 'Series remove download action'),
    ('            if (!isTelevision && offlineCopyAvailable) {\n', 'Series offline badge'),
    ('            if (\n                !isTelevision &&\n                (download?.state == DownloadStates.DOWNLOADING ||\n', 'Series download progress'),
    ('            if (!isTelevision) {\n', 'Series download failure'),
]:
    text = remove_block(text, marker, label)
assert_absent(text, ('isTelevision', 'LocalConfiguration', 'UI_MODE_TYPE_TELEVISION', 'Resume Offline', 'Play Offline', 'Downloaded · Offline copy'), 'SeriesDetailsPane')
write(path, text)

# Movie details: preserve remote primary focus; remove all phone download/offline surfaces.
path = 'app/src/main/java/app/ownplay/player/ui/vod/MovieDetailsPane.kt'
text = read(path)
text = text.replace('import androidx.compose.ui.platform.LocalConfiguration\n', '')
text = remove_once(
    text,
    '''    val configuration = LocalConfiguration.current\n    val isTelevision =\n        configuration.uiMode and android.content.res.Configuration.UI_MODE_TYPE_MASK ==\n            android.content.res.Configuration.UI_MODE_TYPE_TELEVISION\n''',
    'Movie details TV probe',
)
text = remove_once(text, '    val offlineCopyAvailable = !isTelevision && download?.state == DownloadStates.COMPLETED\n', 'Movie details offline flag')
text = replace_once(
    text,
    '''    LaunchedEffect(isTelevision, focusBackOnEntry, movie.movieId) {\n        if (isTelevision) {\n            withFrameNanos { }\n            detailPrimaryFocusRequester.requestFocus()\n        }\n    }\n''',
    '''    LaunchedEffect(focusBackOnEntry, movie.movieId) {\n        withFrameNanos { }\n        detailPrimaryFocusRequester.requestFocus()\n    }\n''',
    'Movie details TV focus',
)
text = replace_once(
    text,
    '''                    Text(\n                        when {\n                            offlineCopyAvailable && movie.resumeAvailable -> "Resume Offline"\n                            offlineCopyAvailable -> "Play Offline"\n                            movie.resumeAvailable -> "Resume"\n                            else -> "Play"\n                        },\n                    )\n''',
    '                    Text(if (movie.resumeAvailable) "Resume" else "Play")\n',
    'Movie TV play label',
)
text = remove_block(text, '            if (!isTelevision) {\n                val target = details?.movie ?: movie\n', 'Movie phone download UI')
assert_absent(text, ('isTelevision', 'LocalConfiguration', 'UI_MODE_TYPE_TELEVISION', 'Resume Offline', 'Play Offline', 'Phone Downloads', 'Saving to phone Downloads'), 'MovieDetailsPane')
write(path, text)

# Poster presentation is always the accessible TV version.
path = 'app/src/main/java/app/ownplay/player/ui/vod/RemotePoster.kt'
text = read(path)
text = text.replace('import android.content.res.Configuration\n', '')
text = text.replace('import androidx.compose.ui.platform.LocalConfiguration\n', '')
text = remove_once(
    text,
    '''    val configuration = LocalConfiguration.current\n    val isTelevision =\n        configuration.uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_TELEVISION\n''',
    'Remote poster TV probe',
)
text = replace_once(text, '                contentDescription = if (isTelevision) title else null,\n', '                contentDescription = title,\n', 'Remote poster accessibility')
text = replace_once(text, '            RemotePosterState.Loading -> if (isTelevision) {\n                PosterFallbackLabel(title = title)\n            }\n', '            RemotePosterState.Loading -> PosterFallbackLabel(title = title)\n', 'Remote poster loading fallback')
assert_absent(text, ('isTelevision', 'LocalConfiguration', 'UI_MODE_TYPE_TELEVISION'), 'RemotePoster')
write(path, text)

# Live Management: remote focus and explicit reorder actions are the only active reorder interaction.
path = 'app/src/main/java/app/ownplay/player/ui/LiveManagementScreen.kt'
text = read(path)
text = text.replace('import android.content.res.Configuration\n', '')
text = text.replace('import androidx.compose.ui.platform.LocalConfiguration\n', '')
text = remove_once(
    text,
    '''    val configuration = LocalConfiguration.current\n    val isTelevision =\n        configuration.uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_TELEVISION\n''',
    'Live Management TV probe',
)
text = replace_once(
    text,
    '''    LaunchedEffect(isTelevision, focusPrimaryOnEntry, selectedSourceId) {\n        if (isTelevision && focusPrimaryOnEntry) {\n''',
    '''    LaunchedEffect(focusPrimaryOnEntry, selectedSourceId) {\n        if (focusPrimaryOnEntry) {\n''',
    'Live Management TV focus',
)
text = replace_once(text, '        if (isTelevision && selectedChannelId != null) {\n', '        if (selectedChannelId != null) {\n', 'Live Management remote order')
text = remove_block(text, '    fun persistDraggedChannelMove(\n', 'Live Management pointer-drag persistence')
text = remove_named_lambda_argument(text, '            onManualMoveRelative = { channelId, anchorChannelId, placement ->\n', 'Live Management manual drag callback')
text = remove_named_lambda_argument(text, '            onFavoriteMoveRelative = { channelId, anchorChannelId, placement ->\n', 'Live Management favorite drag callback')
assert_absent(text, ('isTelevision', 'LocalConfiguration', 'UI_MODE_TYPE_TELEVISION', 'persistDraggedChannelMove'), 'LiveManagementScreen')
write(path, text)

# Preview remains presentation-only; fullscreen activation stays in channel browsing.
path = 'app/src/main/java/app/ownplay/player/ui/LivePreviewPanel.kt'
text = read(path)
for old in [
    'import android.content.res.Configuration\n',
    'import androidx.compose.foundation.clickable\n',
    'import androidx.compose.foundation.interaction.MutableInteractionSource\n',
    'import androidx.compose.runtime.remember\n',
    'import androidx.compose.ui.platform.LocalConfiguration\n',
    'import androidx.compose.ui.semantics.Role\n',
    'import androidx.compose.ui.semantics.contentDescription\n',
    'import androidx.compose.ui.semantics.semantics\n',
]:
    text = text.replace(old, '')
text = text.replace(
    ''' * No playback/navigation/fullscreen/close buttons are rendered on either mobile or TV. TV keeps\n * focus in the channel browser so a second OK on the selected channel can open fullscreen. Mobile\n * gets a transparent tap target above PlayerView so tapping video opens fullscreen without adding a\n * visible control layer. Back/ESC ownership remains in LiveRoute so Preview closes first.\n''',
    ''' * No playback/navigation/fullscreen/close buttons are rendered. Focus stays in the channel\n * browser so a second OK on the selected channel can open fullscreen. Back/ESC ownership remains\n * in LiveRoute so Preview closes first.\n''',
)
text = remove_once(
    text,
    '''    val configuration = LocalConfiguration.current\n    val isTelevision =\n        configuration.uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_TELEVISION\n''',
    'Live Preview TV probe',
)
text = remove_once(text, '    val interactionSource = remember { MutableInteractionSource() }\n', 'Live Preview touch interaction source')
text = remove_block(text, '            if (!isTelevision) {\n                Box(\n', 'Live Preview touch activation overlay')
assert_absent(text, ('isTelevision', 'LocalConfiguration', 'UI_MODE_TYPE_TELEVISION', '.semantics', 'Role.Button', 'interactionSource'), 'LivePreviewPanel')
write(path, text)

# Shared on-demand playback: remote wake/focus only; no tap surface.
path = 'app/src/main/java/app/ownplay/player/ui/OnDemandPlaybackSurface.kt'
text = read(path)
for old in [
    'import android.content.res.Configuration\n',
    'import androidx.compose.foundation.gestures.detectTapGestures\n',
    'import androidx.compose.ui.input.pointer.pointerInput\n',
    'import androidx.compose.ui.platform.LocalConfiguration\n',
]:
    text = text.replace(old, '')
text = remove_once(
    text,
    '''    val configuration = LocalConfiguration.current\n    val isTelevision =\n        configuration.uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_TELEVISION\n''',
    'On-demand TV probe',
)
text = replace_once(
    text,
    '''    LaunchedEffect(isTelevision, controlsVisible, playbackState, contentKey) {\n        if (!isTelevision) return@LaunchedEffect\n''',
    '    LaunchedEffect(controlsVisible, playbackState, contentKey) {\n',
    'On-demand remote focus',
)
text = replace_once(text, '    val remoteWakeModifier = if (isTelevision && !controlsVisible) {\n', '    val remoteWakeModifier = if (!controlsVisible) {\n', 'On-demand remote wake')
text = replace_once(
    text,
    '''                    if (\n                        isTelevision &&\n                        controlsVisible &&\n                        event.nativeKeyEvent.isOnDemandRemoteNavigationKeyDown()\n                    ) {\n''',
    '''                    if (\n                        controlsVisible &&\n                        event.nativeKeyEvent.isOnDemandRemoteNavigationKeyDown()\n                    ) {\n''',
    'On-demand remote interaction',
)
text = replace_once(
    text,
    '''            Box(\n                modifier = Modifier\n                    .fillMaxSize()\n                    .pointerInput(contentKey, controlsVisible) {\n                        detectTapGestures {\n                            if (controlsVisible) {\n                                controlsVisible = false\n                            } else {\n                                revealControls()\n                            }\n                        }\n                    }\n                    .then(remoteWakeModifier),\n            )\n''',
    '''            Box(\n                modifier = Modifier\n                    .fillMaxSize()\n                    .then(remoteWakeModifier),\n            )\n''',
    'On-demand touch wake surface',
)
assert_absent(text, ('isTelevision', 'LocalConfiguration', 'UI_MODE_TYPE_TELEVISION', 'detectTapGestures', 'pointerInput'), 'OnDemandPlaybackSurface')
write(path, text)

# Fullscreen Live: retain only the durable remote/EPG contract; remove mobile gestures and feedback.
path = 'app/src/main/java/app/ownplay/player/ui/PlaybackScreen.kt'
text = read(path)
for old in [
    'import android.content.res.Configuration\n',
    'import android.media.AudioManager\n',
    'import android.provider.Settings\n',
    'import androidx.compose.foundation.gestures.detectDragGestures\n',
    'import androidx.compose.ui.input.pointer.pointerInput\n',
    'import androidx.compose.ui.platform.LocalConfiguration\n',
    'import kotlin.math.abs\n',
    'import kotlin.math.max\n',
    'import kotlin.math.roundToInt\n',
]:
    text = text.replace(old, '')
text = remove_once(text, 'private const val MOBILE_CHANNEL_SWIPE_TRIGGER_FRACTION = 0.12f\n', 'Live mobile swipe constant')
text = remove_once(text, 'private const val MOBILE_GESTURE_FEEDBACK_HIDE_MILLIS = 700L\n', 'Live mobile feedback constant')
text = remove_block(text, 'private enum class MobileFullscreenGestureAxis', 'Live mobile gesture axis')
text = remove_between(text, 'private data class MobileFullscreenGestureFeedback(', '/**\n * Live fullscreen presentation.', 'Live mobile gesture feedback model')
text = text.replace(
    ''' * The final timeline card opens the full guide over Full View without tearing down playback. Mobile\n * uses tap to show/hide EPG, horizontal swipe to change channel, left-side vertical swipe for\n * brightness, and right-side vertical swipe for media volume. Category gestures belong only to the\n * channel browser and never to Full View.\n''',
    ''' * The final timeline card opens the full guide over Full View without tearing down playback.\n''',
)
text = remove_once(text, '    val configuration = LocalConfiguration.current\n', 'Live fullscreen configuration')
text = remove_once(text, '    val context = LocalContext.current\n', 'Live fullscreen local context')
text = remove_once(
    text,
    '''    val isTelevision =\n        configuration.uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_TELEVISION\n''',
    'Live fullscreen TV probe',
)
text = remove_once(text, '    val hostActivity = remember(context) { context.findActivity() }\n', 'Live fullscreen touch activity')
text = remove_once(
    text,
    '''    val audioManager = remember(context) {\n        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager\n    }\n''',
    'Live fullscreen touch audio manager',
)
text = remove_once(text, '    val touchInteractionSource = remember { MutableInteractionSource() }\n', 'Live fullscreen touch interaction source')
text = remove_once(
    text,
    '''    var mobileGestureFeedback by remember {\n        mutableStateOf<MobileFullscreenGestureFeedback?>(null)\n    }\n    var mobileGestureFeedbackGeneration by remember { mutableIntStateOf(0) }\n''',
    'Live mobile gesture feedback state',
)
text = remove_block(text, '    fun toggleEpg() {\n', 'Live touch EPG toggle')
text = remove_block(text, '    fun publishMobileGestureFeedback(', 'Live touch gesture feedback publisher')
text = replace_once(
    text,
    '''    LaunchedEffect(selection.request.channelId, isTelevision) {\n        epgVisible = true\n        epgFocused = false\n        showFullGuide = false\n        selectedProgramIndex = currentProgramIndex\n        interactionGeneration += 1\n        if (isTelevision) {\n            withFrameNanos { }\n            rootFocusRequester.requestFocus()\n        }\n    }\n''',
    '''    LaunchedEffect(selection.request.channelId) {\n        epgVisible = true\n        epgFocused = false\n        showFullGuide = false\n        selectedProgramIndex = currentProgramIndex\n        interactionGeneration += 1\n        withFrameNanos { }\n        rootFocusRequester.requestFocus()\n    }\n''',
    'Live fullscreen entry focus',
)
text = replace_once(
    text,
    '''    LaunchedEffect(showFullGuide, isTelevision) {\n        if (isTelevision && !showFullGuide) {\n            withFrameNanos { }\n            rootFocusRequester.requestFocus()\n        }\n    }\n''',
    '''    LaunchedEffect(showFullGuide) {\n        if (!showFullGuide) {\n            withFrameNanos { }\n            rootFocusRequester.requestFocus()\n        }\n    }\n''',
    'Live guide return focus',
)
text = remove_block(text, '    LaunchedEffect(mobileGestureFeedbackGeneration) {\n', 'Live mobile feedback timeout')
text = replace_if_else_with_then(text, '                    if (isTelevision) {\n                        Modifier\n', 'Live fullscreen remote root modifier')
text = remove_between(
    text,
    '''            Box(\n                modifier = Modifier\n                    .fillMaxSize()\n                    .pointerInput(\n''',
    '            val controls = PlaybackPresentationPolicy.controlsFor(state)\n',
    'Live fullscreen touch gesture surface',
)
text = remove_between(
    text,
    '            mobileGestureFeedback?.let { feedback ->\n',
    '            AnimatedVisibility(\n',
    'Live mobile gesture feedback UI',
)
text = remove_between(
    text,
    'private fun currentWindowBrightness(',
    '@Composable\nprivate fun FullscreenSystemBarsEffect(',
    'Live touch brightness helpers',
)
assert_absent(
    text,
    ('isTelevision', 'LocalConfiguration', 'UI_MODE_TYPE_TELEVISION', 'MobileFullscreen', 'MOBILE_', 'pointerInput', 'detectDragGestures', 'mobileGestureFeedback', 'currentWindowBrightness', 'setWindowBrightness'),
    'PlaybackScreen',
)
write(path, text)

# Category ordering: remote-only Up/Down controls. Keep pure moveRelative policy for regression tests.
path = 'app/src/main/java/app/ownplay/player/ui/CategoryReorderSheet.kt'
original = read(path)
for required in ('detectDragGesturesAfterLongPress', 'pointerInput', 'moveRelative(', 'Text("Up")', 'Text("Down")'):
    if required not in original:
        raise RuntimeError(f'CategoryReorderSheet expected marker missing: {required}')
write(
    path,
    '''package app.ownplay.player.ui\n\nimport androidx.compose.foundation.layout.Arrangement\nimport androidx.compose.foundation.layout.Column\nimport androidx.compose.foundation.layout.PaddingValues\nimport androidx.compose.foundation.layout.Row\nimport androidx.compose.foundation.layout.fillMaxWidth\nimport androidx.compose.foundation.layout.heightIn\nimport androidx.compose.foundation.layout.padding\nimport androidx.compose.foundation.lazy.LazyColumn\nimport androidx.compose.foundation.lazy.itemsIndexed\nimport androidx.compose.foundation.shape.RoundedCornerShape\nimport androidx.compose.material3.ExperimentalMaterial3Api\nimport androidx.compose.material3.MaterialTheme\nimport androidx.compose.material3.ModalBottomSheet\nimport androidx.compose.material3.Surface\nimport androidx.compose.material3.Text\nimport androidx.compose.material3.TextButton\nimport androidx.compose.runtime.Composable\nimport androidx.compose.runtime.LaunchedEffect\nimport androidx.compose.runtime.getValue\nimport androidx.compose.runtime.mutableStateOf\nimport androidx.compose.runtime.remember\nimport androidx.compose.runtime.setValue\nimport androidx.compose.ui.Alignment\nimport androidx.compose.ui.Modifier\nimport androidx.compose.ui.focus.FocusRequester\nimport androidx.compose.ui.focus.focusRequester\nimport androidx.compose.ui.text.font.FontWeight\nimport androidx.compose.ui.text.style.TextOverflow\nimport androidx.compose.ui.unit.dp\nimport app.ownplay.player.live.LiveCategory\nimport app.ownplay.player.personalization.ManualOrderPlacement\n\n@OptIn(ExperimentalMaterial3Api::class)\n@Composable\ninternal fun CategoryReorderSheet(\n    categories: List<LiveCategory>,\n    onOrderChanged: (List<String>) -> Unit,\n    onDismiss: () -> Unit,\n) {\n    val doneFocusRequester = remember { FocusRequester() }\n    var working by remember(categories) { mutableStateOf(categories) }\n\n    LaunchedEffect(Unit) {\n        doneFocusRequester.requestFocus()\n    }\n\n    fun applyOrder(next: List<LiveCategory>) {\n        if (next == working) return\n        working = next\n        onOrderChanged(next.map(LiveCategory::providerCategoryKey))\n    }\n\n    fun moveWithRemote(index: Int, delta: Int) {\n        val category = working.getOrNull(index) ?: return\n        val anchor = working.getOrNull(index + delta) ?: return\n        applyOrder(\n            moveRelative(\n                categories = working,\n                draggedKey = category.providerCategoryKey,\n                anchorKey = anchor.providerCategoryKey,\n                placement = if (delta < 0) ManualOrderPlacement.BEFORE else ManualOrderPlacement.AFTER,\n            ),\n        )\n    }\n\n    ModalBottomSheet(onDismissRequest = onDismiss) {\n        Column(\n            modifier = Modifier\n                .fillMaxWidth()\n                .heightIn(min = 360.dp),\n        ) {\n            Row(\n                modifier = Modifier\n                    .fillMaxWidth()\n                    .padding(horizontal = 20.dp, vertical = 4.dp),\n                verticalAlignment = Alignment.CenterVertically,\n                horizontalArrangement = Arrangement.spacedBy(8.dp),\n            ) {\n                Column(\n                    modifier = Modifier.weight(1f),\n                    verticalArrangement = Arrangement.spacedBy(2.dp),\n                ) {\n                    Text(\n                        text = "Reorder categories",\n                        style = MaterialTheme.typography.headlineSmall,\n                        fontWeight = FontWeight.SemiBold,\n                    )\n                    Text(\n                        text = "Use Up / Down with the remote. Press Done when finished.",\n                        style = MaterialTheme.typography.bodySmall,\n                        color = MaterialTheme.colorScheme.onSurfaceVariant,\n                    )\n                }\n                TextButton(\n                    onClick = onDismiss,\n                    modifier = Modifier.focusRequester(doneFocusRequester),\n                ) {\n                    Text("Done")\n                }\n            }\n\n            LazyColumn(\n                modifier = Modifier.fillMaxWidth(),\n                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),\n                verticalArrangement = Arrangement.spacedBy(4.dp),\n            ) {\n                itemsIndexed(\n                    items = working,\n                    key = { _, category -> category.providerCategoryKey },\n                ) { index, category ->\n                    Surface(\n                        modifier = Modifier.fillMaxWidth(),\n                        shape = RoundedCornerShape(12.dp),\n                        color = MaterialTheme.colorScheme.surface,\n                        tonalElevation = 0.dp,\n                    ) {\n                        Row(\n                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),\n                            verticalAlignment = Alignment.CenterVertically,\n                            horizontalArrangement = Arrangement.spacedBy(12.dp),\n                        ) {\n                            Column(modifier = Modifier.weight(1f)) {\n                                Text(\n                                    text = category.name,\n                                    style = MaterialTheme.typography.bodyLarge,\n                                    fontWeight = FontWeight.Medium,\n                                    maxLines = 1,\n                                    overflow = TextOverflow.Ellipsis,\n                                )\n                                if (category.isHidden) {\n                                    Text(\n                                        text = "Hidden",\n                                        style = MaterialTheme.typography.labelSmall,\n                                        color = MaterialTheme.colorScheme.onSurfaceVariant,\n                                    )\n                                }\n                            }\n                            TextButton(\n                                enabled = index > 0,\n                                onClick = { moveWithRemote(index, -1) },\n                            ) {\n                                Text("Up")\n                            }\n                            TextButton(\n                                enabled = index < working.lastIndex,\n                                onClick = { moveWithRemote(index, 1) },\n                            ) {\n                                Text("Down")\n                            }\n                        }\n                    }\n                }\n            }\n        }\n    }\n}\n\ninternal fun moveRelative(\n    categories: List<LiveCategory>,\n    draggedKey: String,\n    anchorKey: String,\n    placement: ManualOrderPlacement,\n): List<LiveCategory> {\n    if (draggedKey == anchorKey) return categories\n    val dragged = categories.firstOrNull { it.providerCategoryKey == draggedKey } ?: return categories\n    if (categories.none { it.providerCategoryKey == anchorKey }) return categories\n    val without = categories.filterNot { it.providerCategoryKey == draggedKey }.toMutableList()\n    val anchorIndex = without.indexOfFirst { it.providerCategoryKey == anchorKey }\n    if (anchorIndex < 0) return categories\n    val insertionIndex = if (placement == ManualOrderPlacement.BEFORE) anchorIndex else anchorIndex + 1\n    without.add(insertionIndex.coerceIn(0, without.size), dragged)\n    return without\n}\n''',
)

# Live management browser: eliminate long-press pointer reordering; explicit management actions own order changes.
path = 'app/src/main/java/app/ownplay/player/ui/live/LiveBrowseScreen.kt'
text = read(path)
for old in [
    'import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress\n',
    'import androidx.compose.foundation.gestures.scrollBy\n',
    'import androidx.compose.runtime.withFrameNanos\n',
    'import androidx.compose.ui.input.pointer.pointerInput\n',
    'import app.ownplay.player.personalization.ChannelDragTarget\n',
    'import app.ownplay.player.personalization.ChannelDragTargetResolver\n',
    'import app.ownplay.player.personalization.VisibleChannelBounds\n',
]:
    text = text.replace(old, '')
text = remove_between(
    text,
    '    var draggedChannelId by remember { mutableStateOf<String?>(null) }\n',
    '    Surface(\n',
    'LiveBrowse pointer drag state',
)
text = replace_once(
    text,
    '''        LazyColumn(\n            state = listState,\n            modifier = Modifier\n                .fillMaxSize()\n                .then(channelDragModifier),\n        ) {\n''',
    '''        LazyColumn(\n            state = listState,\n            modifier = Modifier.fillMaxSize(),\n        ) {\n''',
    'LiveBrowse pointer drag modifier',
)
text = remove_once(text, '                        if (!editing) clearDragState()\n', 'LiveBrowse drag cleanup')
text = remove_once(text, '                        dragEnabled = dragEnabled,\n', 'LiveBrowse drag enabled UI argument')
text = remove_once(text, '                        favoriteDragEnabled = favoriteDragEnabled,\n', 'LiveBrowse favorite drag UI argument')
text = replace_once(text, '                    val isDropAnchor = dragTarget?.anchorChannelId == channel.channelId\n\n', '', 'LiveBrowse drop anchor')
text = replace_once(text, '                        isDragging = draggedChannelId == channel.channelId,\n', '                        isDragging = false,\n', 'LiveBrowse dragging row state')
text = replace_once(text, '                        dropPlacement = if (isDropAnchor) dragTarget?.placement else null,\n', '                        dropPlacement = null,\n', 'LiveBrowse drop placement')
text = replace_once(text, '                        showDragHandle = dragEnabled,\n', '                        showDragHandle = false,\n', 'LiveBrowse drag handle')
text = remove_between(text, 'private fun dragAutoScrollStepForPointer(', '@Composable\nprivate fun LiveBrowseHeader(', 'LiveBrowse pointer drag helpers')
text = remove_once(text, '    dragEnabled: Boolean,\n', 'Bulk edit drag API')
text = remove_once(text, '    favoriteDragEnabled: Boolean,\n', 'Bulk edit favorite drag API')
text = remove_block(text, '        if (dragEnabled) {\n            Text(\n                text = if (favoriteDragEnabled) {\n', 'Bulk edit touch instruction')
assert_absent(text, ('detectDragGesturesAfterLongPress', 'pointerInput', 'draggedPointerY', 'dragAutoScrollStep', 'ChannelDragTargetResolver', 'Hold a channel, then drag'), 'LiveBrowseScreen')
write(path, text)

print('TV-only shared cleanup staged successfully.')
