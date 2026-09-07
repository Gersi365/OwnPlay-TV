package app.ownplay.player.ui.series

import app.ownplay.player.testing.sourceText
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SeriesTvContractTest {
    @Test
    fun `series use provider categories and a poster grid without All`() {
        val route = sourceText("src/main/java/app/ownplay/player/ui/series/SeriesRoute.kt")

        assertTrue(route.contains("items(catalog.categories"))
        assertTrue(route.contains("category.providerCategoryKey"))
        assertTrue(route.contains("LazyVerticalGrid("))
        assertTrue(route.contains("gridItems(series"))
        assertFalse(route.contains("\"All\""))
    }

    @Test
    fun `continue watching restores the exact episode card after playback`() {
        val route = sourceText("src/main/java/app/ownplay/player/ui/series/SeriesRoute.kt")

        assertTrue(route.contains("\"Continue Watching\""))
        assertTrue(route.contains("SeriesCatalogFocusOrigin.CONTINUE_WATCHING"))
        assertTrue(route.contains("contentId = episode.episodeId"))
        assertTrue(route.contains("restoreCatalogFocusTarget"))
    }

    @Test
    fun `series drilldown Back restores series season and episode origins`() {
        val route = sourceText("src/main/java/app/ownplay/player/ui/series/SeriesRoute.kt")
        val details = sourceText("src/main/java/app/ownplay/player/ui/series/SeriesDetailsPane.kt")

        assertTrue(route.contains("SeriesCatalogFocusTarget(series.seriesId, origin)"))
        assertTrue(route.contains("restoreSeasonFocusNumber = selectedSeasonNumber"))
        assertTrue(route.contains("restoreEpisodeFocusId = selectedEpisodeId"))
        assertTrue(details.contains("restoreSeasonFocusNumber == season.seasonNumber"))
        assertTrue(details.contains("restoreEpisodeFocusId == episode.episodeId"))
    }

    @Test
    fun `episode details expose Continue and Play from beginning`() {
        val details = sourceText("src/main/java/app/ownplay/player/ui/series/SeriesDetailsPane.kt")

        assertTrue(details.contains("Text(if (episode.resumeAvailable) \"Continue\" else \"Play\")"))
        assertTrue(details.contains("Text(\"Play from beginning\")"))
        assertTrue(details.contains("if (onOpen == null)"))
    }

    @Test
    fun `series focus uses fixed border geometry`() {
        val route = sourceText("src/main/java/app/ownplay/player/ui/series/SeriesRoute.kt")
        val details = sourceText("src/main/java/app/ownplay/player/ui/series/SeriesDetailsPane.kt")

        assertTrue(route.contains("onFocusChanged { focused = it.isFocused }"))
        assertTrue(route.contains("width = 2.dp"))
        assertTrue(details.contains("onFocusChanged { focused = it.isFocused }"))
        assertTrue(details.contains("width = 2.dp"))
    }
}
