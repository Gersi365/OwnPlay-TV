package app.ownplay.player.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EpgGuideFocusPolicyTest {
    @Test
    fun `focuses current program when available`() {
        val focus = EpgGuideFocusPolicy.initialFocus(
            loading = false,
            failed = false,
            programCount = 5,
            currentIndex = 3,
        )

        assertEquals(EpgGuideFocusTarget.PROGRAM, focus.target)
        assertEquals(3, focus.programIndex)
    }

    @Test
    fun `falls back to first program when current is unavailable`() {
        val focus = EpgGuideFocusPolicy.initialFocus(
            loading = false,
            failed = false,
            programCount = 4,
            currentIndex = null,
        )

        assertEquals(EpgGuideFocusTarget.PROGRAM, focus.target)
        assertEquals(0, focus.programIndex)
    }

    @Test
    fun `falls back to done when guide cannot expose programs`() {
        listOf(
            EpgGuideFocusPolicy.initialFocus(loading = true, failed = false, programCount = 4, currentIndex = 2),
            EpgGuideFocusPolicy.initialFocus(loading = false, failed = true, programCount = 4, currentIndex = 2),
            EpgGuideFocusPolicy.initialFocus(loading = false, failed = false, programCount = 0, currentIndex = null),
        ).forEach { focus ->
            assertEquals(EpgGuideFocusTarget.DONE, focus.target)
            assertNull(focus.programIndex)
        }
    }
}
