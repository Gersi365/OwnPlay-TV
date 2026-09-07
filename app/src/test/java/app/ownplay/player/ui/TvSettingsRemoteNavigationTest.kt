package app.ownplay.player.ui

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w960dp-h540dp-land-mdpi", application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TvSettingsRemoteNavigationTest {
    @get:Rule
    val compose = createComposeRule()

    private val titles = listOf("Playlists", "Live Management", "Backup & Restore", "About")
    private val opened = mutableListOf<TvSettingsDestination>()

    @Test
    fun dpadTraversesFourDestinationsAndOkOpensWithoutChangingRowGeometry() {
        showRoot()
        val originalBounds = titles.associateWith { row(it).fetchSemanticsNode().boundsInRoot }

        row(titles.first()).performKeyInput { pressKey(Key.DirectionUp) }
        row(titles.first()).assertIsFocused()
        titles.forEachIndexed { index, title ->
            row(title).assertIsFocused().assertIsDisplayed()
            assertEquals(originalBounds.getValue(title), row(title).fetchSemanticsNode().boundsInRoot)
            row(title).performKeyInput { pressKey(Key.DirectionCenter) }
            if (index < titles.lastIndex) {
                row(title).performKeyInput { pressKey(Key.DirectionDown) }
            }
        }
        assertEquals(TvSettingsDestination.entries.toList(), opened)
        row(titles.last()).performKeyInput { pressKey(Key.DirectionDown) }
        row(titles.last()).assertIsFocused()

        titles.asReversed().dropLast(1).forEach { title ->
            row(title).performKeyInput { pressKey(Key.DirectionUp) }
        }
        row(titles.first()).assertIsFocused()
    }

    @Test
    fun leavingForTheRailClearsRowHighlightWhileKeepingContext() {
        showRoot()
        val row = row("Playlists")
        val focusedColor = playlistTitleColor()
        val bounds = row.fetchSemanticsNode().boundsInRoot

        row.performKeyInput { pressKey(Key.DirectionLeft) }
        compose.onNodeWithText("Rail").assertIsFocused()
        row.assertIsNotFocused()
        compose.onNodeWithText("0 configured · 0 ready · 0 available").assertIsDisplayed()
        assertNotEquals(focusedColor, playlistTitleColor())
        assertEquals(bounds, row.fetchSemanticsNode().boundsInRoot)
    }

    @Test
    fun dpadScrollsEveryDestinationIntoViewInAShortViewport() {
        showRoot(height = 280.dp)
        titles.forEachIndexed { index, title ->
            row(title).assertIsFocused().assertIsDisplayed()
            if (index < titles.lastIndex) {
                row(title).performKeyInput { pressKey(Key.DirectionDown) }
            }
        }
        titles.asReversed().dropLast(1).forEach { title ->
            row(title).performKeyInput { pressKey(Key.DirectionUp) }
        }
        row("Playlists").assertIsFocused().assertIsDisplayed()
    }

    private fun row(title: String) = compose.onNode(hasText(title) and hasClickAction())

    private fun playlistTitleColor() = mutableListOf<TextLayoutResult>().let { results ->
        compose.onNode(
            hasText("Playlists") and hasAnyAncestor(hasClickAction()),
            useUnmergedTree = true,
        ).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
        results.single().layoutInput.style.color
    }

    private fun showRoot(height: Dp = 500.dp) {
        val focusRequesters = TvSettingsDestination.entries.associateWith { FocusRequester() }
        val railFocusRequester = FocusRequester()
        compose.setContent {
            val inputModeManager = LocalInputModeManager.current
            MaterialTheme {
                Row(modifier = Modifier.height(height)) {
                    TextButton(
                        onClick = {},
                        modifier = Modifier.width(82.dp).focusRequester(railFocusRequester),
                    ) { Text("Rail") }
                    Box(modifier = Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.TopStart) {
                        TvSettingsRoot(
                            summaries = emptyList(),
                            disabledSourceIds = emptySet(),
                            initialFocusedDestination = TvSettingsDestination.PLAYLISTS,
                            focusRequesters = focusRequesters,
                            onOpen = { opened += it },
                        )
                    }
                }
                LaunchedEffect(Unit) {
                    inputModeManager.requestInputMode(InputMode.Keyboard)
                    withFrameNanos { }
                    focusRequesters.getValue(TvSettingsDestination.PLAYLISTS).requestFocus()
                }
            }
        }
    }
}
