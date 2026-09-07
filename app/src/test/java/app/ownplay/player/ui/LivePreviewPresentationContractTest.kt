package app.ownplay.player.ui

import app.ownplay.player.testing.sourceText
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LivePreviewPresentationContractTest {
    @Test
    fun `preview is presentation only and cannot expose playback actions`() {
        val preview = sourceText("src/main/java/app/ownplay/player/ui/LivePreviewPanel.kt")

        assertTrue(preview.contains("useController = false"))
        assertFalse(preview.contains("onPlay:"))
        assertFalse(preview.contains("onPause:"))
        assertFalse(preview.contains("onRetry:"))
        assertFalse(preview.contains("onNavigate:"))
        assertFalse(preview.contains("onOpenFullscreen:"))
        assertFalse(preview.contains("onClose:"))
        assertFalse(preview.contains("TextButton("))
        assertFalse(preview.contains("IconButton("))
    }

    @Test
    fun `live workspace is TV only with browse owned preview focus`() {
        val workspace = sourceText("src/main/java/app/ownplay/player/ui/live/TvLiveWorkspaceAdaptive.kt")

        assertTrue(workspace.contains("TvLiveFocusPolicy"))
        assertTrue(workspace.contains("Preview is presentation-only"))
        assertFalse(workspace.contains("LandscapeLive"))
        assertFalse(workspace.contains("LandscapeBrowseSurface"))
        assertFalse(workspace.contains("touch and TV"))
    }
}
