package app.ownplay.player.ui

import app.ownplay.player.testing.sourceText
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TvPrimaryNavigationContractTest {
    private val shell = sourceText("src/main/java/app/ownplay/player/ui/TVOwnPlayApp.kt")

    @Test
    fun `global destinations are Live Movies Series Settings with no Library destination`() {
        assertTrue(shell.contains("LIVE,\n    MOVIES,\n    SERIES,\n    SETTINGS,"))
        assertFalse(shell.contains("TVSection.LIBRARY"))
        assertFalse(shell.contains("onOpenLibrary"))
        assertFalse(shell.contains("label = \"Library\""))
    }

    @Test
    fun `primary navigation is a fixed icon only left rail`() {
        assertTrue(shell.contains("private fun TVPrimaryNavigationRail("))
        assertTrue(shell.contains(".width(82.dp)"))
        assertTrue(shell.contains(".size(52.dp)"))
        assertTrue(shell.contains(".border(2.dp, borderColor, shape)"))
        assertTrue(shell.contains("contentDescription = label"))
        assertFalse(shell.contains("Text(text = label"))
        assertFalse(shell.contains("TVPrimaryNavigationBar"))
        assertFalse(shell.contains("Scaffold("))
    }

    @Test
    fun `rail order matches the durable TV contract`() {
        val live = shell.indexOf("label = \"Live\"")
        val movies = shell.indexOf("label = \"Movies\"")
        val series = shell.indexOf("label = \"Series\"")
        val settings = shell.indexOf("label = \"Settings\"")

        assertTrue(live >= 0)
        assertTrue(live < movies)
        assertTrue(movies < series)
        assertTrue(series < settings)
    }

    @Test
    fun `top level Back never routes through Library`() {
        assertTrue(shell.contains("TVSection.SETTINGS,\n            -> openSection(TVSection.LIVE)"))
        assertFalse(shell.contains("openSection(TVSection.LIBRARY)"))
    }
}
