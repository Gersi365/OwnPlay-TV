from pathlib import Path
import shutil

ROOT = Path('.')


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding='utf-8')


def write(path: str, text: str) -> None:
    p = ROOT / path
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(text, encoding='utf-8')


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise RuntimeError(f'{label}: expected one match, found {count}')
    return text.replace(old, new, 1)


def matching_brace(text: str, open_index: int) -> int:
    depth = 0
    in_string = False
    escaped = False
    for i in range(open_index, len(text)):
        c = text[i]
        if in_string:
            if escaped:
                escaped = False
            elif c == '\\':
                escaped = True
            elif c == '"':
                in_string = False
            continue
        if c == '"':
            in_string = True
        elif c == '{':
            depth += 1
        elif c == '}':
            depth -= 1
            if depth == 0:
                return i
    raise RuntimeError('Unmatched brace')


def remove_block(text: str, marker: str, label: str) -> str:
    start = text.find(marker)
    if start < 0:
        raise RuntimeError(f'{label}: marker not found')
    open_index = text.find('{', start)
    end = matching_brace(text, open_index)
    while end + 1 < len(text) and text[end + 1] in ' \t':
        end += 1
    if end + 1 < len(text) and text[end + 1] == '\n':
        end += 1
    return text[:start] + text[end + 1:]


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


def delete(path: str) -> None:
    p = ROOT / path
    if p.exists():
        p.unlink()


# Single TV build target.
build = read('app/build.gradle.kts')
build = replace_once(
    build,
    '''    defaultConfig {\n        minSdk = 26\n        targetSdk = 36\n        versionCode = 15\n        versionName = "1.0.13-product-polish-update"\n\n        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"\n    }\n\n    flavorDimensions += "device"\n    productFlavors {\n        create("mobile") {\n            dimension = "device"\n            applicationId = "app.ownplay.mobile"\n            manifestPlaceholders["appLabel"] = "OwnPlay"\n            buildConfigField("boolean", "IS_TV_BUILD", "false")\n            buildConfigField("String", "TARGET_DEVICE", "\\\"mobile\\\"")\n        }\n        create("tv") {\n            dimension = "device"\n            applicationId = "app.ownplay.tv"\n            manifestPlaceholders["appLabel"] = "OwnPlay"\n            buildConfigField("boolean", "IS_TV_BUILD", "true")\n            buildConfigField("String", "TARGET_DEVICE", "\\\"tv\\\"")\n        }\n    }\n''',
    '''    defaultConfig {\n        applicationId = "app.ownplay.tv"\n        minSdk = 26\n        targetSdk = 36\n        versionCode = 15\n        versionName = "1.0.13-product-polish-update"\n        manifestPlaceholders["appLabel"] = "OwnPlay"\n\n        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"\n    }\n''',
    'single TV defaultConfig',
)
write('app/build.gradle.kts', build)

# Consolidate the TV manifest into the single main manifest. Landscape is a static TV invariant,
# not a runtime/user-selectable orientation mode.
write('app/src/main/AndroidManifest.xml', '''<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <uses-feature
        android:name="android.hardware.touchscreen"
        android:required="false" />
    <uses-feature
        android:name="android.software.leanback"
        android:required="true" />

    <application
        android:name="app.ownplay.player.OwnPlayApplication"
        android:allowBackup="false"
        android:banner="@drawable/tv_banner"
        android:icon="@mipmap/ic_launcher"
        android:label="${appLabel}"
        android:networkSecurityConfig="@xml/network_security_config"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.Material.NoActionBar">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:screenOrientation="landscape">
            <property
                android:name="android.window.PROPERTY_COMPAT_ALLOW_RESTRICTED_RESIZABILITY"
                android:value="true" />
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LEANBACK_LAUNCHER" />
            </intent-filter>
        </activity>

        <service
            android:name="androidx.work.impl.foreground.SystemForegroundService"
            android:foregroundServiceType="dataSync"
            tools:node="merge" />
    </application>
</manifest>
''')

# Move the former TV flavor sources into the single main source set.
move_files = {
    'app/src/tv/java/app/ownplay/player/ui/LiveRoute.kt': 'app/src/main/java/app/ownplay/player/ui/LiveRoute.kt',
    'app/src/tv/java/app/ownplay/player/ui/TVOwnPlayApp.kt': 'app/src/main/java/app/ownplay/player/ui/TVOwnPlayApp.kt',
    'app/src/tv/java/app/ownplay/player/ui/TvPlaylistAvailabilityStore.kt': 'app/src/main/java/app/ownplay/player/ui/TvPlaylistAvailabilityStore.kt',
    'app/src/tv/java/app/ownplay/player/ui/TvSettingsScreen.kt': 'app/src/main/java/app/ownplay/player/ui/TvSettingsScreen.kt',
}
for src, dst in move_files.items():
    if not (ROOT / src).is_file():
        raise RuntimeError(f'missing source to move: {src}')
    if (ROOT / dst).exists():
        raise RuntimeError(f'target already exists: {dst}')
    (ROOT / dst).parent.mkdir(parents=True, exist_ok=True)
    shutil.move(src, dst)
delete('app/src/tv/java/app/ownplay/player/ui/TargetOwnPlayApp.kt')
delete('app/src/tv/AndroidManifest.xml')
shutil.rmtree(ROOT / 'app/src/tv', ignore_errors=True)

# TV-only Activity: no smartphone profile, touch gestures, PiP, rotation controller, or offline bridge.
write('app/src/main/java/app/ownplay/player/MainActivity.kt', '''package app.ownplay.player

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
''')

# Root is always TV and always owns initial D-pad focus routing.
write('app/src/main/java/app/ownplay/player/ui/OwnPlayRoot.kt', '''package app.ownplay.player.ui

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
''')

# Theme is permanently remote-first; no runtime device profile or configuration injection.
theme = read('app/src/main/java/app/ownplay/player/ui/theme/Theme.kt')
for old in [
    'import android.content.res.Configuration\n',
    'import androidx.compose.ui.platform.LocalConfiguration\n',
    'import app.ownplay.player.personalization.AppDeviceProfile\n',
]:
    theme = theme.replace(old, '')
start = theme.find('@Composable\nfun OwnPlayTheme(')
if start < 0:
    raise RuntimeError('OwnPlayTheme function not found')
theme = theme[:start] + '''@Composable
fun OwnPlayTheme(content: @Composable () -> Unit) {
    val tvIndication = remember {
        TvRemoteIndication(
            focusColor = OwnPlayDarkColors.primary,
            pressedColor = Color.White,
        )
    }

    MaterialTheme(
        colorScheme = OwnPlayDarkColors,
        typography = OwnPlayTypography,
        shapes = OwnPlayShapes,
    ) {
        CompositionLocalProvider(LocalIndication provides tvIndication) {
            content()
        }
    }
}
'''
write('app/src/main/java/app/ownplay/player/ui/theme/Theme.kt', theme)

# Collapse TV shell wrapper/configuration emulation; Android TV is the only runtime target.
tv_app_path = 'app/src/main/java/app/ownplay/player/ui/TVOwnPlayApp.kt'
tv_app = read(tv_app_path)
for old in [
    'import android.content.res.Configuration\n',
    'import androidx.compose.runtime.CompositionLocalProvider\n',
    'import androidx.compose.ui.platform.LocalConfiguration\n',
]:
    tv_app = tv_app.replace(old, '')
tv_app = replace_once(
    tv_app,
    '''@Composable\ninternal fun TVOwnPlayApp(\n    runtime: OwnPlayAppRuntime,\n    onPlaybackFullscreenChanged: (Boolean) -> Unit,\n    onPlaybackSurfaceActiveChanged: (Boolean) -> Unit,\n    onLivePreviewActiveChanged: (Boolean) -> Unit,\n) {\n    TVConfigurationBoundary {\n        TVOwnPlayAppContent(\n            runtime = runtime,\n            onPlaybackFullscreenChanged = onPlaybackFullscreenChanged,\n            onPlaybackSurfaceActiveChanged = onPlaybackSurfaceActiveChanged,\n            onLivePreviewActiveChanged = onLivePreviewActiveChanged,\n        )\n    }\n}\n\n@Composable\nprivate fun TVOwnPlayAppContent(\n    runtime: OwnPlayAppRuntime,\n    onPlaybackFullscreenChanged: (Boolean) -> Unit,\n    onPlaybackSurfaceActiveChanged: (Boolean) -> Unit,\n    onLivePreviewActiveChanged: (Boolean) -> Unit,\n) {\n''',
    '''@Composable\ninternal fun TVOwnPlayApp(\n    runtime: OwnPlayAppRuntime,\n    onPlaybackFullscreenChanged: (Boolean) -> Unit,\n) {\n''',
    'collapse TV app wrapper',
)
tv_app = replace_once(
    tv_app,
    '''    val playbackSurfaceActive =\n        previewActive ||\n            fullscreenSelection != null ||\n            vodFullscreen ||\n            seriesFullscreen ||\n            libraryFullscreen\n''',
    '',
    'remove playback surface callback state',
)
tv_app = replace_once(
    tv_app,
    '''    LaunchedEffect(playbackSurfaceActive) {\n        onPlaybackSurfaceActiveChanged(playbackSurfaceActive)\n    }\n    LaunchedEffect(previewActive) {\n        // TV never opts into rotation-driven fullscreen; keep the activity callback explicitly off.\n        onLivePreviewActiveChanged(false)\n    }\n''',
    '',
    'remove form factor callbacks',
)
boundary = tv_app.find('\n@Composable\nprivate fun TVConfigurationBoundary(')
if boundary < 0:
    raise RuntimeError('TVConfigurationBoundary not found')
tv_app = tv_app[:boundary].rstrip() + '\n'
write(tv_app_path, tv_app)

# Live route is a single TV workspace; no portrait/landscape or TV/non-TV branches.
live_path = 'app/src/main/java/app/ownplay/player/ui/LiveRoute.kt'
live = read(live_path)
for old in [
    'import android.content.res.Configuration\n',
    'import androidx.compose.ui.platform.LocalConfiguration\n',
    'import app.ownplay.player.ui.live.PortraitLiveBrowseWithViewModes\n',
]:
    live = live.replace(old, '')
live = live.replace(
    'import app.ownplay.player.ui.live.LandscapeLiveWorkspaceAdaptive\n',
    'import app.ownplay.player.ui.live.TvLiveWorkspaceAdaptive\n',
)
live = replace_once(
    live,
    '''    val configuration = LocalConfiguration.current\n    val context = LocalContext.current\n    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE\n    val isTelevision =\n        configuration.uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_TELEVISION\n''',
    '    val context = LocalContext.current\n',
    'remove Live form factor detection',
)
live = live.replace('remember(sourceId, isTelevision)', 'remember(sourceId)')
live = live.replace('isTelevision = isTelevision', 'isTelevision = true')
live = replace_once(
    live,
    '''    val effectiveHierarchyLevel = if (isTelevision) {\n        hierarchyLevel\n    } else {\n        LiveBrowseHierarchyLevel.CHANNELS\n    }\n''',
    '    val effectiveHierarchyLevel = hierarchyLevel\n',
    'single Live hierarchy',
)
live = replace_once(
    live,
    '''    fun selectCategory(categoryKey: String?) {\n        browseSession.selectCategory(categoryKey)\n        if (isTelevision) {\n            hierarchyLevel = LiveBrowseHierarchyLevel.CHANNELS\n        }\n    }\n''',
    '''    fun selectCategory(categoryKey: String?) {\n        browseSession.selectCategory(categoryKey)\n        hierarchyLevel = LiveBrowseHierarchyLevel.CHANNELS\n    }\n''',
    'TV category selection',
)
live = replace_if_else_with_then(live, '    if (isLandscape) {', 'Live TV workspace')
live = live.replace('LandscapeLiveWorkspaceAdaptive(', 'TvLiveWorkspaceAdaptive(')
live = replace_once(
    live,
    '''        onCategorySelected = if (isTelevision) {\n            ::selectCategory\n        } else {\n            browseSession::selectCategory\n        },\n''',
    '        onCategorySelected = ::selectCategory,\n',
    'TV Live category callback',
)
if 'isTelevision' in live or 'isLandscape' in live or 'Portrait' in live:
    raise RuntimeError('LiveRoute still contains form-factor branching')
write(live_path, live)

# Rename the live workspace to describe its actual TV role.
old_workspace = ROOT / 'app/src/main/java/app/ownplay/player/ui/live/LandscapeLiveWorkspaceAdaptive.kt'
new_workspace = ROOT / 'app/src/main/java/app/ownplay/player/ui/live/TvLiveWorkspaceAdaptive.kt'
if not old_workspace.is_file() or new_workspace.exists():
    raise RuntimeError('Live workspace rename precondition failed')
workspace = old_workspace.read_text(encoding='utf-8')
workspace = workspace.replace('LandscapeLiveWorkspaceAdaptive', 'TvLiveWorkspaceAdaptive')
workspace = workspace.replace('landscape workspace', 'TV workspace')
new_workspace.write_text(workspace, encoding='utf-8')
old_workspace.unlink()
delete('app/src/main/java/app/ownplay/player/ui/live/PortraitLiveViewModes.kt')

# Movies: keep the existing split TV catalog/detail presentation only.
vod_path = 'app/src/main/java/app/ownplay/player/ui/vod/VodRoute.kt'
vod = read(vod_path)
vod = vod.replace('import androidx.compose.ui.platform.LocalConfiguration\n', '')
vod = replace_once(
    vod,
    '''    val context = LocalContext.current\n    val configuration = LocalConfiguration.current\n    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE\n''',
    '    val context = LocalContext.current\n',
    'remove VOD orientation detection',
)
vod = remove_block(vod, '    if (!isLandscape) {', 'remove portrait VOD details')
vod = replace_if_else_with_then(vod, '    if (isLandscape) {', 'VOD TV split layout')
if 'isLandscape' in vod or 'isPortrait' in vod:
    raise RuntimeError('VodRoute still contains orientation branching')
write(vod_path, vod)

# Series: keep the TV split catalog/detail presentation only.
series_path = 'app/src/main/java/app/ownplay/player/ui/series/SeriesRoute.kt'
series = read(series_path)
series = series.replace('import android.content.res.Configuration\n', '')
series = series.replace('import androidx.compose.ui.platform.LocalConfiguration\n', '')
series = replace_once(
    series,
    '''    val context = LocalContext.current\n    val configuration = LocalConfiguration.current\n    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE\n''',
    '    val context = LocalContext.current\n',
    'remove Series orientation detection',
)
series = replace_once(series, '    val portraitSelection = selectedSeries\n', '', 'remove portrait selection alias')
series = remove_block(series, '    if (!isLandscape && portraitSelection != null) {', 'remove portrait Series details')
if 'isLandscape' in series or 'portraitSelection' in series:
    raise RuntimeError('SeriesRoute still contains orientation branching')
write(series_path, series)

# Keep TV Live handoff/continuity logic, remove rotation-only presentation policy.
live_presentation_path = 'app/src/main/java/app/ownplay/player/playback/LivePlaybackPresentation.kt'
live_presentation = read(live_presentation_path)
marker = '/**\n * Executes Live handoffs between independent Preview and Fullscreen surfaces.'
pos = live_presentation.find(marker)
if pos < 0:
    raise RuntimeError('Live handoff marker not found')
live_presentation = 'package app.ownplay.player.playback\n\n' + live_presentation[pos:]
live_presentation = live_presentation.replace(' * VOD, Series, and PiP do not use this helper.\n', ' * VOD and Series do not use this helper.\n')
write(live_presentation_path, live_presentation)

# Delete presentation/runtime files that only existed for mobile/profile/orientation/PiP paths.
for path in [
    'app/src/main/java/app/ownplay/player/personalization/AppOrientationStore.kt',
    'app/src/main/java/app/ownplay/player/playback/LiveActivityLifecyclePolicy.kt',
    'app/src/main/java/app/ownplay/player/ui/OrientationSetupScreen.kt',
    'app/src/main/java/app/ownplay/player/ui/PlaybackWindowController.kt',
    'app/src/main/java/app/ownplay/player/ui/PictureInPicturePlaybackSurface.kt',
    'app/src/main/java/app/ownplay/player/ui/PictureInPictureSurfaceHandoffPolicy.kt',
    'app/src/main/java/app/ownplay/player/ui/OwnPlayApp.kt',
    'app/src/main/java/app/ownplay/player/ui/SettingsInterface.kt',
    'app/src/main/java/app/ownplay/player/ui/SettingsLandscape.kt',
    'app/src/main/java/app/ownplay/player/ui/SettingsLandscapeRail.kt',
    'app/src/main/java/app/ownplay/player/ui/SettingsScreen.kt',
    'app/src/main/java/app/ownplay/player/ui/DownloadsSettingsScreen.kt',
    'app/src/main/java/app/ownplay/player/ui/DownloadPlaybackBridge.kt',
    'app/src/main/java/app/ownplay/player/ui/PlaybackOriginBadge.kt',
    'app/src/test/java/app/ownplay/player/personalization/AppOrientationStoreTest.kt',
    'app/src/test/java/app/ownplay/player/LiveRotationPresentationGateTest.kt',
    'app/src/test/java/app/ownplay/player/playback/LiveActivityLifecyclePolicyTest.kt',
    'app/src/test/java/app/ownplay/player/playback/LivePlaybackPresentationPolicyTest.kt',
    'app/src/test/java/app/ownplay/player/ui/PlaybackWindowPolicyTest.kt',
    'app/src/test/java/app/ownplay/player/ui/PictureInPictureSurfaceHandoffPolicyTest.kt',
    'app/src/test/java/app/ownplay/player/ui/LivePipLifecycleOrderingRegressionTest.kt',
    'app/src/test/java/app/ownplay/player/ui/LiveSurfaceOwnershipRegressionHarnessTest.kt',
    'app/src/test/java/app/ownplay/player/ui/DownloadPlaybackBridgeTest.kt',
]:
    delete(path)

# Remove the obsolete orientation-only Settings component.
components_path = 'app/src/main/java/app/ownplay/player/ui/SettingsComponents.kt'
components = read(components_path)
components = remove_block(components, '@Composable\ninternal fun OrientationButton(', 'remove OrientationButton')
write(components_path, components)

# Tests now read TV-specific source from the single main source set.
for test_path in (ROOT / 'app/src/test').rglob('*.kt'):
    text = test_path.read_text(encoding='utf-8')
    updated = text.replace(
        'src/tv/java/app/ownplay/player/ui/',
        'src/main/java/app/ownplay/player/ui/',
    ).replace('app.ownplay.mobile', 'app.ownplay.tv')
    if updated != text:
        test_path.write_text(updated, encoding='utf-8')

# Durable contract: one TV target, no mobile flavor/profile, no runtime orientation mode.
source_path = 'OwnPlay_TV_SOURCE.md'
source = read(source_path)
source = replace_once(
    source,
    '''Target devices:\n\n- Android TV;\n- Google TV;\n- compatible Android TV boxes.\n''',
    '''Target devices:\n\n- Android TV;\n- Google TV;\n- compatible Android TV boxes.\n\n## TV-Only Build & Form-Factor Invariant\n\n`OwnPlay-TV` is a single-target TV project. It must not contain a mobile application flavor, mobile package identity, smartphone runtime profile, touch-first fallback shell, or a separate mobile source set.\n\nThe application has no user-selectable or runtime portrait/landscape mode. The TV activity is statically fixed to landscape in the manifest because that is a device invariant, not a presentation choice. Product UI must not branch between portrait and landscape layouts.\n''',
    'source TV-only invariant',
)
source = source.replace(
    'Settings is a dedicated TV-first experience and must not be treated as a generic landscape settings screen.',
    'Settings is a dedicated TV-first experience and must not be treated as a generic non-TV settings screen.',
)
write(source_path, source)

workflow_path = 'OwnPlay_TV_ENGINEERING_WORKFLOW.md'
workflow_doc = read(workflow_path)
workflow_doc = replace_once(
    workflow_doc,
    '''Do not refactor unrelated modules merely for consistency.\n\nDo not modify inactive legacy code solely because it contains outdated visual patterns.\n''',
    '''Do not refactor unrelated modules merely for consistency.\n\nThis repository is TV-only. Do not preserve mobile flavors, mobile source sets, smartphone profiles, touch-first fallback shells, PiP/mobile window policy, or portrait/landscape presentation branches as inactive legacy. Remove such form-factor legacy when it is found, while respecting separate safety boundaries such as destructive database migrations.\n''',
    'engineering TV-only scope',
)
workflow_doc = workflow_doc.replace(
    'TV variant compilation is mandatory for changes affecting shared UI/data contracts used by TV.',
    'The single TV application variant must compile for changes affecting UI/data contracts.',
)
write(workflow_path, workflow_doc)

ui_contract_path = 'OwnPlay_TV_UI_INTERACTION_CONTRACT.md'
ui_contract = read(ui_contract_path)
ui_contract = replace_once(
    ui_contract,
    '''Update this file only when intended TV interaction changes, not when implementation checkpoints change.\n\n---\n\n# 1. Visual Direction\n''',
    '''Update this file only when intended TV interaction changes, not when implementation checkpoints change.\n\n## TV-Only Form-Factor Invariant\n\nOwnPlay TV has one remote-first TV presentation. There is no phone/touch fallback presentation and no runtime orientation mode. The activity is fixed to landscape as a TV device invariant; interaction and layout logic must not branch between portrait and landscape variants.\n\n---\n\n# 1. Visual Direction\n''',
    'UI contract TV-only invariant',
)
write(ui_contract_path, ui_contract)

# Validation now targets the single Debug variant and rejects reintroduction of mobile/form-factor code.
validation_path = '.github/workflows/android-validation-no-apk.yml'
validation = read(validation_path)
validation = validation.replace('test -d app/src/tv\n', 'test ! -d app/src/tv\n')
validation = validation.replace('test -f app/src/tv/java/app/ownplay/player/ui/TvSettingsScreen.kt\n', 'test -f app/src/main/java/app/ownplay/player/ui/TvSettingsScreen.kt\n')
validation = validation.replace(':app:kspTvDebugKotlin', ':app:kspDebugKotlin')
validation = validation.replace(':app:testTvDebugUnitTest', ':app:testDebugUnitTest')
validation = validation.replace(':app:lintTvDebug', ':app:lintDebug')
validation = validation.replace(':app:compileTvDebugAndroidTestKotlin', ':app:compileDebugAndroidTestKotlin')
needle = '''          grep -Fq 'applicationId = "app.ownplay.tv"' app/build.gradle.kts\n          test -f app/src/main/java/app/ownplay/player/ui/TvSettingsScreen.kt\n'''
replacement = needle + '''          test "$(grep -RIlE 'create\\("mobile"\\)|app\\.ownplay\\.mobile|IS_TV_BUILD|TARGET_DEVICE|AppDeviceProfile|AppOrientation|SMARTPHONE|TOUCHSCREEN|PORTRAIT|PortraitLive|SettingsLandscape|PictureInPicture|rotationFullscreen|GestureDetector|dispatchTouchEvent' app/src app/build.gradle.kts 2>/dev/null | wc -l)" -eq 0\n          test "$(grep -RIlE 'Configuration\\.ORIENTATION_(LANDSCAPE|PORTRAIT)|isLandscape|isPortrait' app/src/main/java 2>/dev/null | wc -l)" -eq 0\n'''
validation = replace_once(validation, needle, replacement, 'validation form-factor gate')
write(validation_path, validation)

# Remove temporary audit workflow. The apply workflow and this script are removed by the runner.
delete('.github/workflows/audit-tv-only-form-factor.yml')

print('TV-only form-factor cleanup staged successfully.')
