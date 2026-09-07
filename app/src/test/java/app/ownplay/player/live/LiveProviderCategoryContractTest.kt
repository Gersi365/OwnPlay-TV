package app.ownplay.player.live

import app.ownplay.player.testing.sourceText
import org.junit.Assert.assertTrue
import org.junit.Test

class LiveProviderCategoryContractTest {
    @Test
    fun `content categories project directly from provider snapshot`() {
        val source = sourceText("src/main/java/app/ownplay/player/live/LiveBrowseState.kt")

        assertTrue(source.contains("cachedCategories = snapshot.categories"))
        assertTrue(source.contains("categories = cachedCategories"))
        assertTrue(source.contains("customGroups = cachedCustomGroups"))
        assertTrue(source.contains("favoritesOnly = enabled"))
    }
}
