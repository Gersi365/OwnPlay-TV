package app.ownplay.player.ui.vod

import app.ownplay.player.testing.sourceText
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MoviesTvContractTest {
    @Test
    fun `movies use provider categories without an All destination`() {
        val route = sourceText("src/main/java/app/ownplay/player/ui/vod/VodRoute.kt")

        assertTrue(route.contains("items(catalog.categories"))
        assertTrue(route.contains("category.providerCategoryKey"))
        assertFalse(route.contains("\"All\""))
    }

    @Test
    fun `continue watching stays inside Movies and opens movie details`() {
        val route = sourceText("src/main/java/app/ownplay/player/ui/vod/VodRoute.kt")

        assertTrue(route.contains("text = \"Continue Watching\""))
        assertTrue(route.contains("MovieFocusOrigin.CONTINUE_WATCHING"))
        assertTrue(route.contains("showMovieDetail("))
    }

    @Test
    fun `movie details expose Continue and Play from beginning`() {
        val details = sourceText("src/main/java/app/ownplay/player/ui/vod/MovieDetailsPane.kt")

        assertTrue(details.contains("Text(if (movie.resumeAvailable) \"Continue\" else \"Play\")"))
        assertTrue(details.contains("Text(\"Play from beginning\")"))
        assertTrue(details.contains("onPlayFromBeginning(movie)"))
    }

    @Test
    fun `details Back restores the originating movie item`() {
        val route = sourceText("src/main/java/app/ownplay/player/ui/vod/VodRoute.kt")

        assertTrue(route.contains("MovieFocusTarget(movie.movieId, origin)"))
        assertTrue(route.contains("origin = MovieFocusOrigin.GRID"))
        assertTrue(route.contains("origin = MovieFocusOrigin.CONTINUE_WATCHING"))
        assertTrue(route.contains("focusRequester.requestFocus()"))
        assertTrue(route.contains("onFocusChanged { focused = it.isFocused }"))
        assertTrue(route.contains("width = 2.dp"))
    }
}
