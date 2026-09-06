package app.ownplay.player.prototype

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AboutBg = Color(0xFF080A0F)
private val AboutSurface = Color(0xFF10131A)
private val AboutSoft = Color(0xFF1E2230)
private val AboutPrimary = Color(0xFF9B7BFF)
private val AboutPrimarySoft = Color(0xFF2B2145)
private val AboutText = Color(0xFFF3F1F7)
private val AboutMuted = Color(0xFFA9A5B4)
private val AboutOutline = Color(0xFF343947)
private val AboutShape = RoundedCornerShape(12.dp)

@Composable
fun TvOnlyAboutPrototype() {
    Column(
        Modifier
            .fillMaxSize()
            .background(AboutBg)
            .padding(horizontal = 54.dp, vertical = 30.dp),
    ) {
        AboutTopBar()
        Spacer(Modifier.height(28.dp))
        Text("About", color = AboutText, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(26.dp))
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(42.dp)) {
            Column(
                Modifier.width(430.dp).padding(top = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier.size(108.dp).background(AboutPrimary, RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(74.dp))
                }
                Spacer(Modifier.height(22.dp))
                Text("OwnPlay TV", color = AboutText, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text("Play your own media sources", color = AboutMuted, fontSize = 12.sp)
            }
            Column(Modifier.weight(1f).padding(top = 28.dp)) {
                AboutPanel("Build", "Prototype presentation · application ID app.ownplay.tv")
                Spacer(Modifier.height(14.dp))
                AboutPanel("Product", "OwnPlay TV is a media player and playlist organizer for user-provided legitimate media sources and credentials.")
                Spacer(Modifier.height(14.dp))
                AboutPanel("Disclaimer", "OwnPlay does not sell subscriptions, provide channels, bundle provider content, or distribute provider media.")
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AboutPill("Android TV")
                    AboutPill("Remote-first")
                    AboutPill("TV-only")
                }
            }
        }
    }
}

@Composable
private fun AboutTopBar() {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(38.dp).background(AboutPrimary, RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(27.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("OwnPlay", color = AboutText, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text("TV", color = AboutMuted, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, letterSpacing = 1.4.sp)
            }
        }
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AboutNav("Live", Icons.Default.LiveTv, false)
            AboutNav("Movies", Icons.Default.Movie, false)
            AboutNav("Series", Icons.Default.VideoLibrary, false)
            AboutNav("Settings", Icons.Default.Settings, true)
        }
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text("20:17", color = AboutText, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text("TV prototype", color = AboutMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun AboutNav(label: String, icon: ImageVector, selected: Boolean) {
    Row(
        Modifier
            .height(42.dp)
            .border(2.dp, if (selected) AboutPrimary else AboutOutline, RoundedCornerShape(10.dp))
            .background(if (selected) AboutPrimarySoft else AboutSurface, RoundedCornerShape(10.dp))
            .padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = if (selected) AboutPrimary else AboutMuted, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = if (selected) AboutText else AboutMuted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AboutPanel(title: String, body: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(AboutSurface, AboutShape)
            .border(1.dp, AboutOutline, AboutShape)
            .padding(20.dp),
    ) {
        Text(title, color = AboutText, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(body, color = AboutMuted, fontSize = 13.sp, lineHeight = 19.sp)
    }
}

@Composable
private fun AboutPill(text: String) {
    Box(
        Modifier
            .background(AboutSoft, RoundedCornerShape(18.dp))
            .padding(horizontal = 11.dp, vertical = 6.dp),
    ) {
        Text(text, color = AboutMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}
