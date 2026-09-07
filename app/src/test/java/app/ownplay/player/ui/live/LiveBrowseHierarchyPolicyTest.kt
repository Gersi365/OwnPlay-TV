package app.ownplay.player.ui.live

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
