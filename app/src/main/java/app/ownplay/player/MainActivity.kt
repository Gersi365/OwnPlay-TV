package app.ownplay.player

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.os.SystemClock
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import app.ownplay.player.playback.PlaybackInteractionBridge
import app.ownplay.player.ui.OwnPlayRoot
import app.ownplay.player.ui.theme.OwnPlayTheme
import app.ownplay.player.ui.tv.TvBackgroundPlaybackAction
import app.ownplay.player.ui.tv.TvPlaybackLifecyclePolicy
import app.ownplay.player.ui.tv.TvRemoteActionGuard
import app.ownplay.player.ui.tv.TvRemoteActionKind
import app.ownplay.player.ui.tv.TvRemoteKeySuppression

class MainActivity : ComponentActivity() {
    private lateinit var runtime: OwnPlayAppRuntime
    private val tvRemoteActionGuard = TvRemoteActionGuard()
    private val tvRemoteKeySuppression = TvRemoteKeySuppression()
    private var exitConfirmationDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runtime = (application as OwnPlayApplication).runtime
        PlaybackInteractionBridge.setDpadMode(true)
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (PlaybackInteractionBridge.handleBack()) return
                    showExitConfirmation()
                }
            },
        )
        enableEdgeToEdge()
        hideStatusBar()
        setContent {
            OwnPlayTheme {
                OwnPlayRoot(
                    runtime = runtime,
                    onPlaybackFullscreenChanged = { isFullscreen ->
                        holdTvRemoteTransitionLock()
                        if (!isFullscreen) hideStatusBar()
                    },
                )
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_ESCAPE) {
            if (event.action == KeyEvent.ACTION_UP) {
                onBackPressedDispatcher.onBackPressed()
            }
            return true
        }
        if (event.isRemoteActivationKey()) {
            when (event.action) {
                KeyEvent.ACTION_DOWN -> {
                    if (event.repeatCount > 0) {
                        tvRemoteKeySuppression.suppress(event.keyCode)
                        return true
                    }
                    if (
                        !tvRemoteActionGuard.tryAcquire(
                            nowMillis = SystemClock.elapsedRealtime(),
                            actionId = event.keyCode,
                        )
                    ) {
                        tvRemoteKeySuppression.suppress(event.keyCode)
                        return true
                    }
                    tvRemoteKeySuppression.allow(event.keyCode)
                }
                KeyEvent.ACTION_UP -> {
                    if (tvRemoteActionGuard.isGloballyBlocked(SystemClock.elapsedRealtime())) {
                        tvRemoteKeySuppression.consumeRelease(event.keyCode)
                        return true
                    }
                    if (tvRemoteKeySuppression.consumeRelease(event.keyCode)) return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onResume() {
        super.onResume()
        if (::runtime.isInitialized) {
            PlaybackInteractionBridge.resumeLifecycleSuspended(runtime.playbackVideoOutput)
            runtime.playbackController.resumeAfterBackground()
        }
        hideStatusBar()
    }

    override fun onStop() {
        if (::runtime.isInitialized && !isChangingConfigurations) {
            when (TvPlaybackLifecyclePolicy.backgroundAction(runtime.playbackController.state.value)) {
                TvBackgroundPlaybackAction.NONE -> Unit
                TvBackgroundPlaybackAction.SUSPEND -> {
                    PlaybackInteractionBridge.suspendCurrentForLifecycle(runtime.playbackVideoOutput)
                    runtime.playbackController.suspendForBackground()
                }
            }
        }
        super.onStop()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideStatusBar()
    }

    override fun onDestroy() {
        exitConfirmationDialog?.dismiss()
        exitConfirmationDialog = null
        if (isFinishing && ::runtime.isInitialized) {
            runtime.playbackController.stop()
        }
        PlaybackInteractionBridge.discardLifecycleSuspendedSurface()
        super.onDestroy()
    }

    private fun holdTvRemoteTransitionLock() {
        tvRemoteActionGuard.extendBlock(
            nowMillis = SystemClock.elapsedRealtime(),
            kind = TvRemoteActionKind.TRANSITION,
        )
    }

    private fun showExitConfirmation() {
        if (isFinishing || exitConfirmationDialog?.isShowing == true) return
        exitConfirmationDialog = AlertDialog.Builder(this)
            .setTitle("Exit OwnPlay?")
            .setMessage("Are you sure you want to close the app?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Exit") { _, _ -> finish() }
            .setOnDismissListener { exitConfirmationDialog = null }
            .show()
    }

    private fun hideStatusBar() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.statusBars())
        }
    }
}

private fun KeyEvent.isRemoteActivationKey(): Boolean =
    keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
        keyCode == KeyEvent.KEYCODE_ENTER ||
        keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER ||
        keyCode == KeyEvent.KEYCODE_BUTTON_A ||
        keyCode == KeyEvent.KEYCODE_BUTTON_SELECT ||
        keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE ||
        keyCode == KeyEvent.KEYCODE_MEDIA_PLAY ||
        keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE ||
        keyCode == KeyEvent.KEYCODE_BACK
