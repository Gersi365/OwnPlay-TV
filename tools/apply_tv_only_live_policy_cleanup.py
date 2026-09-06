from pathlib import Path

ROOT = Path('.')


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding='utf-8')


def write(path: str, text: str) -> None:
    (ROOT / path).write_text(text, encoding='utf-8')


def replace_count(text: str, old: str, new: str, expected: int, label: str) -> str:
    count = text.count(old)
    if count not in (0, expected):
        raise RuntimeError(f'{label}: expected 0 or {expected} matches, found {count}')
    return text.replace(old, new)


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise RuntimeError(f'{label}: expected one match, found {count}')
    return text.replace(old, new, 1)


# Live browse hierarchy is intrinsically TV-only. Preserve the durable remote contract:
# categories at root, Back from channels to categories, and repeated same-channel activation
# promotes Preview to Full View.
write(
    'app/src/main/java/app/ownplay/player/ui/live/LiveBrowseHierarchyPolicy.kt',
    '''package app.ownplay.player.ui.live

internal enum class LiveBrowseHierarchyLevel {
    CATEGORIES,
    CHANNELS,
}

internal enum class LiveBrowseBackAction {
    CLOSE_PREVIEW,
    SHOW_CATEGORIES,
    PROPAGATE,
}

internal enum class LiveChannelActivationAction {
    OPEN_PREVIEW,
    OPEN_FULLSCREEN,
}

internal object LiveBrowseHierarchyPolicy {
    fun initialLevel(
        hasPreview: Boolean = false,
    ): LiveBrowseHierarchyLevel = if (!hasPreview) {
        LiveBrowseHierarchyLevel.CATEGORIES
    } else {
        LiveBrowseHierarchyLevel.CHANNELS
    }

    fun ownsBack(
        hasPreview: Boolean,
        level: LiveBrowseHierarchyLevel,
    ): Boolean = hasPreview || level == LiveBrowseHierarchyLevel.CHANNELS

    fun backAction(
        hasPreview: Boolean,
        level: LiveBrowseHierarchyLevel,
    ): LiveBrowseBackAction = when {
        hasPreview -> LiveBrowseBackAction.CLOSE_PREVIEW
        level == LiveBrowseHierarchyLevel.CHANNELS -> LiveBrowseBackAction.SHOW_CATEGORIES
        else -> LiveBrowseBackAction.PROPAGATE
    }

    fun channelActivationAction(
        activePreviewChannelId: String?,
        activatedChannelId: String,
    ): LiveChannelActivationAction = if (
        activePreviewChannelId == activatedChannelId
    ) {
        LiveChannelActivationAction.OPEN_FULLSCREEN
    } else {
        LiveChannelActivationAction.OPEN_PREVIEW
    }
}
''',
)

route_path = 'app/src/main/java/app/ownplay/player/ui/LiveRoute.kt'
route = read(route_path)
route = replace_count(
    route,
    'isTelevision = true,\n',
    '',
    3,
    'LiveRoute hardcoded TV policy arguments',
)
if 'isTelevision' in route:
    raise RuntimeError('LiveRoute still contains isTelevision after TV-only policy cleanup')
write(route_path, route)

write(
    'app/src/test/java/app/ownplay/player/ui/live/LiveBrowseHierarchyPolicyTest.kt',
    '''package app.ownplay.player.ui.live

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LiveBrowseHierarchyPolicyTest {
    @Test
    fun `browse starts at categories when preview is closed`() {
        assertEquals(
            LiveBrowseHierarchyLevel.CATEGORIES,
            LiveBrowseHierarchyPolicy.initialLevel(),
        )
    }

    @Test
    fun `active preview restores channel hierarchy`() {
        assertEquals(
            LiveBrowseHierarchyLevel.CHANNELS,
            LiveBrowseHierarchyPolicy.initialLevel(hasPreview = true),
        )
    }

    @Test
    fun `preview or channel hierarchy owns back while category root propagates`() {
        assertTrue(
            LiveBrowseHierarchyPolicy.ownsBack(
                hasPreview = true,
                level = LiveBrowseHierarchyLevel.CATEGORIES,
            ),
        )
        assertTrue(
            LiveBrowseHierarchyPolicy.ownsBack(
                hasPreview = false,
                level = LiveBrowseHierarchyLevel.CHANNELS,
            ),
        )
        assertFalse(
            LiveBrowseHierarchyPolicy.ownsBack(
                hasPreview = false,
                level = LiveBrowseHierarchyLevel.CATEGORIES,
            ),
        )
    }

    @Test
    fun `back closes preview before changing browse hierarchy`() {
        assertEquals(
            LiveBrowseBackAction.CLOSE_PREVIEW,
            LiveBrowseHierarchyPolicy.backAction(
                hasPreview = true,
                level = LiveBrowseHierarchyLevel.CHANNELS,
            ),
        )
        assertEquals(
            LiveBrowseBackAction.CLOSE_PREVIEW,
            LiveBrowseHierarchyPolicy.backAction(
                hasPreview = true,
                level = LiveBrowseHierarchyLevel.CATEGORIES,
            ),
        )
    }

    @Test
    fun `back returns channel browsing to categories when preview is closed`() {
        assertEquals(
            LiveBrowseBackAction.SHOW_CATEGORIES,
            LiveBrowseHierarchyPolicy.backAction(
                hasPreview = false,
                level = LiveBrowseHierarchyLevel.CHANNELS,
            ),
        )
    }

    @Test
    fun `back propagates from category root when preview is closed`() {
        assertEquals(
            LiveBrowseBackAction.PROPAGATE,
            LiveBrowseHierarchyPolicy.backAction(
                hasPreview = false,
                level = LiveBrowseHierarchyLevel.CATEGORIES,
            ),
        )
    }

    @Test
    fun `first ok opens preview and second ok on same channel opens fullscreen`() {
        assertEquals(
            LiveChannelActivationAction.OPEN_PREVIEW,
            LiveBrowseHierarchyPolicy.channelActivationAction(
                activePreviewChannelId = null,
                activatedChannelId = "channel-7",
            ),
        )
        assertEquals(
            LiveChannelActivationAction.OPEN_FULLSCREEN,
            LiveBrowseHierarchyPolicy.channelActivationAction(
                activePreviewChannelId = "channel-7",
                activatedChannelId = "channel-7",
            ),
        )
    }

    @Test
    fun `ok on another channel replaces preview instead of opening fullscreen`() {
        assertEquals(
            LiveChannelActivationAction.OPEN_PREVIEW,
            LiveBrowseHierarchyPolicy.channelActivationAction(
                activePreviewChannelId = "channel-7",
                activatedChannelId = "channel-8",
            ),
        )
    }
}
''',
)

# The TV product does not expose phone/offline download presentation in movie or series details.
write(
    'app/src/test/java/app/ownplay/player/ui/DownloadActionConsistencyContractTest.kt',
    '''package app.ownplay.player.ui

import app.ownplay.player.testing.sourceText
import org.junit.Assert.assertFalse
import org.junit.Test

class DownloadActionConsistencyContractTest {
    @Test
    fun `movie details do not expose phone or offline download presentation`() {
        val details = sourceText("src/main/java/app/ownplay/player/ui/vod/MovieDetailsPane.kt")

        assertFalse(details.contains("offlineCopyAvailable"))
        assertFalse(details.contains("Play Offline"))
        assertFalse(details.contains("Resume Offline"))
        assertFalse(details.contains("Phone Downloads"))
        assertFalse(details.contains("Saving to phone Downloads"))
    }

    @Test
    fun `series details do not expose phone or offline download presentation`() {
        val details = sourceText("src/main/java/app/ownplay/player/ui/series/SeriesDetailsPane.kt")

        assertFalse(details.contains("offlineCopyAvailable"))
        assertFalse(details.contains("Play Offline"))
        assertFalse(details.contains("Resume Offline"))
        assertFalse(details.contains("Downloaded · Offline copy"))
    }
}
''',
)

# Source-contract tests must inspect the consolidated single main source set.
for relative, expected in (
    ('app/src/test/java/app/ownplay/player/ui/TvSettingsPresentationContractTest.kt', 2),
    ('app/src/test/java/app/ownplay/player/ui/TvPlaylistAvailabilityIntegrationContractTest.kt', 3),
    ('app/src/test/java/app/ownplay/player/ui/TvPlaylistRemoteActionsContractTest.kt', 1),
):
    text = read(relative)
    text = replace_count(
        text,
        'src/tv/java/app/ownplay/player/ui/',
        'src/main/java/app/ownplay/player/ui/',
        expected,
        f'{relative} consolidated source paths',
    )
    if relative.endswith('TvPlaylistRemoteActionsContractTest.kt'):
        text = replace_count(
            text,
            '        assertFalse(wrapperSource.contains("UI_MODE_TYPE_TELEVISION"))\n',
            '',
            1,
            'TV playlist obsolete runtime-probe assertion',
        )
    write(relative, text)

legacy_test_paths = []
for path in (ROOT / 'app/src/test').rglob('*.kt'):
    if 'src/tv/' in path.read_text(encoding='utf-8'):
        legacy_test_paths.append(path.as_posix())
if legacy_test_paths:
    raise RuntimeError('Tests still reference the removed TV source set: ' + ', '.join(legacy_test_paths))

# Long-press drag is gone, but favorite/manual order remains a remote action distinction.
# Keep that distinction explicit without restoring any pointer gesture state.
browse_path = 'app/src/main/java/app/ownplay/player/ui/live/LiveBrowseScreen.kt'
browse = read(browse_path)
browse = replace_once(
    browse,
    '    val listState = rememberLazyListState()\n\n    Surface(\n',
    '''    val listState = rememberLazyListState()\n    val favoriteOrderActions = editState.isEditing &&\n        state.query.favoritesOnly &&\n        state.query.order == LiveBrowseOrder.FAVORITE_ORDER\n\n    Surface(\n''',
    'LiveBrowse remote favorite order context',
)
browse = replace_once(
    browse,
    '''                    BulkEditBar(\n                        selectedCount = editState.selectedChannelIds.size,\n                        selectedVisibleChannel = selectedVisibleChannel,\n                        groups = state.customGroups,\n''',
    '''                    BulkEditBar(\n                        selectedCount = editState.selectedChannelIds.size,\n                        selectedVisibleChannel = selectedVisibleChannel,\n                        groups = state.customGroups,\n                        favoriteOrderActions = favoriteOrderActions,\n''',
    'LiveBrowse remote order argument',
)
browse = replace_once(
    browse,
    '''private fun BulkEditBar(\n    selectedCount: Int,\n    selectedVisibleChannel: LiveChannelItem?,\n    groups: List<LiveCustomGroup>,\n''',
    '''private fun BulkEditBar(\n    selectedCount: Int,\n    selectedVisibleChannel: LiveChannelItem?,\n    groups: List<LiveCustomGroup>,\n    favoriteOrderActions: Boolean,\n''',
    'BulkEditBar remote order API',
)
if browse.count('favoriteDragEnabled') != 4:
    raise RuntimeError(
        f'LiveBrowse remote favorite action references: expected four matches, found {browse.count("favoriteDragEnabled")}',
    )
browse = browse.replace('favoriteDragEnabled', 'favoriteOrderActions')
for forbidden in ('detectDragGesturesAfterLongPress', 'pointerInput', 'draggedPointerY', 'dragAutoScrollStep', 'Hold a channel, then drag'):
    if forbidden in browse:
        raise RuntimeError(f'LiveBrowse touch drag residue after remote order fix: {forbidden}')
write(browse_path, browse)

print('TV-only Live hierarchy, migration contracts, and remote order cleanup staged successfully.')
