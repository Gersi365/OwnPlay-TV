package app.ownplay.player.ui.live

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TvLiveFocusPolicyTest {
    @Test
    fun `browser arrows stay native so cards keeps two dimensional dpad navigation`() {
        listOf(
            TvLiveFocusAction.LEFT,
            TvLiveFocusAction.RIGHT,
            TvLiveFocusAction.UP,
            TvLiveFocusAction.DOWN,
        ).forEach { action ->
            assertNull(
                TvLiveFocusPolicy.destination(
                    current = TvLiveFocusZone.BROWSER,
                    action = action,
                ),
            )
        }
    }

    @Test
    fun `epg left returns focus to browser`() {
        assertEquals(
            TvLiveFocusZone.BROWSER,
            TvLiveFocusPolicy.destination(
                current = TvLiveFocusZone.EPG,
                action = TvLiveFocusAction.LEFT,
            ),
        )
    }

    @Test
    fun `epg non left directions remain available to native focus handling`() {
        listOf(
            TvLiveFocusAction.RIGHT,
            TvLiveFocusAction.UP,
            TvLiveFocusAction.DOWN,
            TvLiveFocusAction.BACK,
        ).forEach { action ->
            assertNull(
                TvLiveFocusPolicy.destination(
                    current = TvLiveFocusZone.EPG,
                    action = action,
                ),
            )
        }
    }
}
