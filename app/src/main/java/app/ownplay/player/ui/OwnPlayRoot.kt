package app.ownplay.player.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import app.ownplay.player.OwnPlayAppRuntime

@Composable
fun OwnPlayRoot(
    runtime: OwnPlayAppRuntime,
    onPlaybackFullscreenChanged: (Boolean) -> Unit = {},
) {
    var contentVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    SideEffect {
        LiveEpgPresentationBridge.bindRuntime(runtime)
    }

    LaunchedEffect(Unit) {
        contentVisible = true
    }

    LaunchedEffect(contentVisible) {
        if (contentVisible) {
            withFrameNanos { }
            if (!focusManager.moveFocus(FocusDirection.Next)) {
                withFrameNanos { }
                focusManager.moveFocus(FocusDirection.Next)
            }
        }
    }

    AnimatedVisibility(
        visible = contentVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = 140)),
    ) {
        TVOwnPlayApp(
            runtime = runtime,
            onPlaybackFullscreenChanged = onPlaybackFullscreenChanged,
        )
    }
}
