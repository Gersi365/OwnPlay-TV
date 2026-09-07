package app.ownplay.player.playback

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Transient Live presentation state whose lifetime matches the process-scoped app runtime.
 *
 * This is intentionally not persisted to disk. It lets a recreated Activity reattach to the
 * existing Preview/Fullscreen presentation while the process and playback runtime are still alive,
 * without turning a cold process restart into implicit Live autoplay.
 */
data class LivePlaybackPresentationSessionState(
    val selection: LivePlaybackSelection? = null,
    val surface: LivePlaybackPresentationSurface? = null,
) {
    init {
        require((selection == null) == (surface == null)) {
            "Live presentation selection and surface must be present together"
        }
    }

    val fullscreenSelection: LivePlaybackSelection?
        get() = selection.takeIf { surface == LivePlaybackPresentationSurface.FULLSCREEN }
}

class LivePlaybackPresentationSession {
    private val _state = MutableStateFlow(LivePlaybackPresentationSessionState())
    val state: StateFlow<LivePlaybackPresentationSessionState> = _state.asStateFlow()

    fun showPreview(selection: LivePlaybackSelection) {
        _state.value = LivePlaybackPresentationSessionState(
            selection = selection,
            surface = LivePlaybackPresentationSurface.PREVIEW,
        )
    }

    fun showFullscreen(selection: LivePlaybackSelection) {
        _state.value = LivePlaybackPresentationSessionState(
            selection = selection,
            surface = LivePlaybackPresentationSurface.FULLSCREEN,
        )
    }

    fun replaceSelection(selection: LivePlaybackSelection) {
        val current = _state.value
        val surface = current.surface ?: return
        _state.value = current.copy(
            selection = selection,
            surface = surface,
        )
    }

    fun clear() {
        _state.value = LivePlaybackPresentationSessionState()
    }
}
