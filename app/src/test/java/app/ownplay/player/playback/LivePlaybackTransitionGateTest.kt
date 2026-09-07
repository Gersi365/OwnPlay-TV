package app.ownplay.player.playback

import org.junit.Assert.assertEquals
import org.junit.Test

class LivePlaybackTransitionGateTest {
    @Test
    fun `same channel preview to fullscreen transfers surface without restarting playback`() {
        val gate = LivePlaybackTransitionGate()
        val events = mutableListOf<String>()
        gate.reconcileObserved(target(LivePlaybackPresentationSurface.PREVIEW, channelId = "one"))

        val decision = gate.requestHandoff(
            target = target(LivePlaybackPresentationSurface.FULLSCREEN, channelId = "one"),
            detachCurrentSurface = {
                events += "detach"
                true
            },
            stopPlayback = { events += "stop" },
            switchPresentation = { events += "switch" },
            startPlayback = { events += "start" },
        )

        assertEquals(LivePlaybackTransitionDecision.APPLIED, decision)
        assertEquals(listOf("detach", "switch"), events)
    }

    @Test
    fun `different channel handoff uses detach stop switch start ordering`() {
        val gate = LivePlaybackTransitionGate()
        val events = mutableListOf<String>()
        gate.reconcileObserved(target(LivePlaybackPresentationSurface.PREVIEW, channelId = "one"))

        val decision = gate.requestHandoff(
            target = target(LivePlaybackPresentationSurface.FULLSCREEN, channelId = "two"),
            detachCurrentSurface = {
                events += "detach"
                true
            },
            stopPlayback = { events += "stop" },
            switchPresentation = { events += "switch" },
            startPlayback = { events += "start" },
        )

        assertEquals(LivePlaybackTransitionDecision.APPLIED, decision)
        assertEquals(listOf("detach", "stop", "switch", "start"), events)
    }

    @Test
    fun `duplicate pending fullscreen request has no playback or surface side effects`() {
        val gate = LivePlaybackTransitionGate()
        val events = mutableListOf<String>()
        val fullscreen = target(LivePlaybackPresentationSurface.FULLSCREEN, channelId = "one")
        gate.reconcileObserved(target(LivePlaybackPresentationSurface.PREVIEW, channelId = "one"))

        gate.requestHandoff(
            target = fullscreen,
            detachCurrentSurface = {
                events += "detach"
                true
            },
            stopPlayback = { events += "stop" },
            switchPresentation = { events += "switch" },
            startPlayback = { events += "start" },
        )
        val afterFirstRequest = events.toList()

        val duplicate = gate.requestHandoff(
            target = fullscreen,
            detachCurrentSurface = {
                events += "duplicate-detach"
                true
            },
            stopPlayback = { events += "duplicate-stop" },
            switchPresentation = { events += "duplicate-switch" },
            startPlayback = { events += "duplicate-start" },
        )

        assertEquals(LivePlaybackTransitionDecision.DUPLICATE, duplicate)
        assertEquals(afterFirstRequest, events)
    }

    @Test
    fun `rapid reverse request for same channel remains a surface-only handoff`() {
        val gate = LivePlaybackTransitionGate()
        val events = mutableListOf<String>()
        val preview = target(LivePlaybackPresentationSurface.PREVIEW, channelId = "one")
        val fullscreen = target(LivePlaybackPresentationSurface.FULLSCREEN, channelId = "one")
        gate.reconcileObserved(preview)

        gate.requestHandoff(
            target = fullscreen,
            detachCurrentSurface = {
                events += "detach-fullscreen"
                true
            },
            stopPlayback = { events += "stop-fullscreen" },
            switchPresentation = { events += "switch-fullscreen" },
            startPlayback = { events += "start-fullscreen" },
        )

        val reverse = gate.requestHandoff(
            target = preview,
            detachCurrentSurface = {
                events += "detach-preview"
                true
            },
            stopPlayback = { events += "stop-preview" },
            switchPresentation = { events += "switch-preview" },
            startPlayback = { events += "start-preview" },
        )

        assertEquals(LivePlaybackTransitionDecision.APPLIED, reverse)
        assertEquals(
            listOf(
                "detach-fullscreen",
                "switch-fullscreen",
                "detach-preview",
                "switch-preview",
            ),
            events,
        )
    }

    private fun target(
        surface: LivePlaybackPresentationSurface,
        channelId: String,
        sourceId: String = "source",
    ): LivePlaybackTransitionTarget = LivePlaybackTransitionTarget(
        surface = surface,
        sourceId = sourceId,
        channelId = channelId,
    )
}
