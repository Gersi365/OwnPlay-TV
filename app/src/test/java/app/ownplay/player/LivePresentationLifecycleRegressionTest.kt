package app.ownplay.player

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LivePresentationLifecycleRegressionTest {
    @Test
    fun tvReadsLivePresentationFromProcessScopedSession() {
        shellPaths.forEach { path ->
            val source = sourceText(path)

            assertTrue(
                "$path must collect Live presentation from the process-scoped runtime session",
                source.contains("runtime.livePlaybackPresentationSession.state.collectAsState()"),
            )
            assertFalse(
                "$path must not own Preview selection in Activity-local Compose memory",
                source.contains("var activeSelection by remember"),
            )
            assertFalse(
                "$path must not own Fullscreen selection in Activity-local Compose memory",
                source.contains("var fullscreenSelection by remember"),
            )
            assertTrue(
                "$path must clear transient Live presentation on explicit teardown",
                source.contains("runtime.livePlaybackPresentationSession.clear()"),
            )
        }
    }

    @Test
    fun rapidPreviewToFullscreenAcknowledgesPreviewBeforeSecondActivation() {
        val source = sourceText("src/main/java/app/ownplay/player/ui/TVOwnPlayApp.kt")
        val callback = source
            .substringAfter("onPreviewRequested = { selection ->")
            .substringBefore("onPreviewClosed =")

        val showPreview = callback.indexOf("runtime.livePlaybackPresentationSession.showPreview(selection)")
        val startPlayback = callback.indexOf("runtime.playbackController.start(selection.request)")
        val acknowledgePreview = callback.indexOf("liveTransitionGate.reconcileObserved(")
        val previewTarget = callback.indexOf("LivePlaybackTransitionTarget.preview(selection)")

        assertTrue(showPreview >= 0)
        assertTrue(showPreview < startPlayback)
        assertTrue(startPlayback < acknowledgePreview)
        assertTrue(acknowledgePreview < previewTarget)
    }

    @Test
    fun transientSessionIsNotDiskBacked() {
        val source = sourceText(
            "src/main/java/app/ownplay/player/playback/LivePlaybackPresentationSession.kt",
        )

        assertFalse(source.contains("DataStore"))
        assertFalse(source.contains("Room"))
        assertFalse(source.contains("SharedPreferences"))
        assertTrue(source.contains("MutableStateFlow"))
    }

    private fun sourceText(relativePath: String): String {
        val candidates = listOf(
            File(relativePath),
            File("app/$relativePath"),
        )
        val source = candidates.firstOrNull(File::isFile)
            ?: error("Could not locate source file: $relativePath")
        return source.readText()
    }

    private companion object {
        val shellPaths = listOf(
            "src/main/java/app/ownplay/player/ui/TVOwnPlayApp.kt",
        )
    }
}
