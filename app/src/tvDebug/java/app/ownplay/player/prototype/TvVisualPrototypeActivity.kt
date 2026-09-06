package app.ownplay.player.prototype

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class TvVisualPrototypeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        val screen = intent.getStringExtra(EXTRA_SCREEN).orEmpty().ifBlank { "live_categories" }
        setContent {
            if (screen == "about") {
                TvOnlyAboutPrototype()
            } else {
                TvVisualPrototype(screen = screen)
            }
        }
    }

    companion object {
        const val EXTRA_SCREEN = "screen"
    }
}
