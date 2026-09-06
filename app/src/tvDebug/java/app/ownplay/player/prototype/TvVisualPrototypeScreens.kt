package app.ownplay.player.prototype

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PrototypeBg = Color(0xFF080A0F)
private val PrototypeSurface = Color(0xFF10131A)
private val PrototypeSurfaceRaised = Color(0xFF171B24)
private val PrototypeSurfaceSoft = Color(0xFF1E2230)
private val PrototypePrimary = Color(0xFF9B7BFF)
private val PrototypePrimarySoft = Color(0xFF2B2145)
private val PrototypeText = Color(0xFFF3F1F7)
private val PrototypeMuted = Color(0xFFA9A5B4)
private val PrototypeOutline = Color(0xFF343947)
private val PrototypeSuccess = Color(0xFF7ED7B4)
private val PrototypeWarning = Color(0xFFFFD38A)
private val Radius = RoundedCornerShape(12.dp)

private val PrototypeColors = darkColorScheme(
    primary = PrototypePrimary,
    onPrimary = Color(0xFF170E2A),
    primaryContainer = PrototypePrimarySoft,
    onPrimaryContainer = PrototypeText,
    background = PrototypeBg,
    onBackground = PrototypeText,
    surface = PrototypeSurface,
    onSurface = PrototypeText,
    surfaceVariant = PrototypeSurfaceRaised,
    onSurfaceVariant = PrototypeMuted,
    outline = PrototypeOutline,
)

@Composable
fun TvVisualPrototype(screen: String) {
    MaterialTheme(colorScheme = PrototypeColors) {
        when (screen) {
            "live_categories" -> LiveCategoriesScreen()
            "live_channels_preview" -> LiveChannelsPreviewScreen()
            "live_full" -> LiveFullViewScreen(showEpg = false)
            "live_full_epg" -> LiveFullViewScreen(showEpg = true)
            "movies" -> MoviesScreen()
            "movie_details" -> MovieDetailsScreen()
            "movie_playback" -> OnDemandPlaybackScreen(title = "Northbound", episode = null)
            "series" -> SeriesScreen()
            "series_details" -> SeriesDetailsScreen()
            "episodes" -> EpisodesScreen()
            "series_playback" -> OnDemandPlaybackScreen(title = "Arcline", episode = "S2 · E4  The Crossing")
            "settings" -> SettingsScreen()
            "playlists" -> PlaylistsScreen()
            "live_management" -> LiveManagementScreen()
            "backup_restore" -> BackupRestoreScreen()
            "about" -> AboutScreen()
            else -> LiveCategoriesScreen()
        }
    }
}

@Composable
private fun PrototypeShell(
    destination: String,
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrototypeBg)
            .padding(horizontal = 54.dp, vertical = 30.dp),
    ) {
        TopBar(destination)
        Spacer(Modifier.height(30.dp))
        Text(title, color = PrototypeText, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        if (subtitle != null) {
            Spacer(Modifier.height(5.dp))
            Text(subtitle, color = PrototypeMuted, fontSize = 15.sp)
        }
        Spacer(Modifier.height(22.dp))
        Box(Modifier.weight(1f)) { content() }
    }
}

@Composable
private fun TopBar(destination: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(PrototypePrimary, RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(27.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("OwnPlay", color = PrototypeText, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text("TV", color = PrototypeMuted, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, letterSpacing = 1.4.sp)
            }
        }
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NavChip("Live", Icons.Default.LiveTv, destination == "Live")
            NavChip("Movies", Icons.Default.Movie, destination == "Movies")
            NavChip("Series", Icons.Default.VideoLibrary, destination == "Series")
            NavChip("Settings", Icons.Default.Settings, destination == "Settings")
        }
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text("20:17", color = PrototypeText, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text("TV prototype", color = PrototypeMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun NavChip(label: String, icon: ImageVector, selected: Boolean) {
    Row(
        modifier = Modifier
            .height(42.dp)
            .border(2.dp, if (selected) PrototypePrimary else PrototypeOutline, RoundedCornerShape(10.dp))
            .background(if (selected) PrototypePrimarySoft else PrototypeSurface, RoundedCornerShape(10.dp))
            .padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = if (selected) PrototypePrimary else PrototypeMuted, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = if (selected) PrototypeText else PrototypeMuted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun FocusRow(
    title: String,
    secondary: String? = null,
    focused: Boolean = false,
    trailing: String? = null,
    leading: ImageVector? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .border(2.dp, if (focused) PrototypePrimary else PrototypeOutline, Radius)
            .background(if (focused) PrototypePrimarySoft else PrototypeSurface, Radius)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            Icon(leading, null, tint = if (focused) PrototypePrimary else PrototypeMuted, modifier = Modifier.size(23.dp))
            Spacer(Modifier.width(14.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, color = PrototypeText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (secondary != null) {
                Spacer(Modifier.height(2.dp))
                Text(secondary, color = PrototypeMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        if (trailing != null) {
            Text(trailing, color = if (focused) PrototypePrimary else PrototypeMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text.uppercase(), color = PrototypeMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
}

@Composable
private fun InfoPanel(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PrototypeSurface, Radius)
            .border(1.dp, PrototypeOutline, Radius)
            .padding(20.dp),
    ) {
        Text(title, color = PrototypeText, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(body, color = PrototypeMuted, fontSize = 13.sp, lineHeight = 19.sp)
    }
}

@Composable
private fun StatusPill(text: String, positive: Boolean = false) {
    Box(
        modifier = Modifier
            .background(if (positive) Color(0xFF17372F) else PrototypeSurfaceSoft, RoundedCornerShape(20.dp))
            .padding(horizontal = 11.dp, vertical = 6.dp),
    ) {
        Text(text, color = if (positive) PrototypeSuccess else PrototypeMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun VideoFrame(title: String, subtitle: String? = null, compact: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (compact) Modifier.height(290.dp) else Modifier.fillMaxHeight())
            .background(Color(0xFF0A111A), Radius)
            .border(1.dp, PrototypeOutline, Radius),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Tv, null, tint = Color(0xFF48566A), modifier = Modifier.size(if (compact) 58.dp else 76.dp))
            Spacer(Modifier.height(12.dp))
            Text(title, color = Color(0xFFB8C4D4), fontSize = if (compact) 15.sp else 18.sp, fontWeight = FontWeight.SemiBold)
            if (subtitle != null) {
                Spacer(Modifier.height(4.dp))
                Text(subtitle, color = Color(0xFF6F7A8C), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun ProgressLine(progress: Float) {
    Box(Modifier.fillMaxWidth().height(4.dp).background(PrototypeOutline, RoundedCornerShape(2.dp))) {
        Box(Modifier.fillMaxWidth(progress).height(4.dp).background(PrototypePrimary, RoundedCornerShape(2.dp)))
    }
}

@Composable
private fun PosterCard(title: String, subtitle: String, focused: Boolean = false, progress: Float? = null) {
    Column(modifier = Modifier.width(164.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(214.dp)
                .border(2.dp, if (focused) PrototypePrimary else PrototypeOutline, Radius)
                .background(if (focused) Color(0xFF2C2340) else PrototypeSurfaceRaised, Radius),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(12.dp)) {
                Icon(Icons.Default.Movie, null, tint = if (focused) PrototypePrimary else Color(0xFF626979), modifier = Modifier.size(42.dp))
                Spacer(Modifier.height(10.dp))
                Text(title, color = PrototypeText, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
        if (progress != null) {
            Spacer(Modifier.height(7.dp))
            ProgressLine(progress)
        }
        Spacer(Modifier.height(7.dp))
        Text(title, color = PrototypeText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(subtitle, color = PrototypeMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun ActionButton(text: String, focused: Boolean = false, icon: ImageVector = Icons.Default.PlayArrow) {
    Row(
        modifier = Modifier
            .height(50.dp)
            .border(2.dp, if (focused) PrototypePrimary else PrototypeOutline, Radius)
            .background(if (focused) PrototypePrimarySoft else PrototypeSurfaceRaised, Radius)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = if (focused) PrototypePrimary else PrototypeMuted, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = PrototypeText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LiveCategoriesScreen() {
    PrototypeShell("Live", "Live", "Choose a category. OK opens its channel list.") {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            Column(Modifier.width(420.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("Categories")
                Spacer(Modifier.height(5.dp))
                FocusRow("Favorites", "9 channels", leading = Icons.Default.Favorite)
                FocusRow("News", "18 channels")
                FocusRow("Sports", "24 channels", focused = true)
                FocusRow("Entertainment", "31 channels")
                FocusRow("Kids", "12 channels")
                FocusRow("Documentary", "14 channels")
            }
            Column(Modifier.weight(1f)) {
                SectionLabel("Sports")
                Spacer(Modifier.height(13.dp))
                InfoPanel("24 channels", "A simple category entry point. Focus stays in the left list; OK moves one logical level deeper into channels.")
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MiniChannel("Arena 1", "Football", true)
                    MiniChannel("Arena 2", "Basketball", false)
                    MiniChannel("Sport Max", "Tennis", false)
                }
                Spacer(Modifier.height(18.dp))
                InfoPanel("Remote", "Up / Down changes category · OK opens channels · Back returns to the previous product area")
            }
        }
    }
}

@Composable
private fun MiniChannel(name: String, program: String, focused: Boolean) {
    Column(
        modifier = Modifier
            .width(190.dp)
            .height(120.dp)
            .border(2.dp, if (focused) PrototypePrimary else PrototypeOutline, Radius)
            .background(if (focused) PrototypePrimarySoft else PrototypeSurface, Radius)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Default.LiveTv, null, tint = if (focused) PrototypePrimary else PrototypeMuted, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(10.dp))
        Text(name, color = PrototypeText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(program, color = PrototypeMuted, fontSize = 11.sp)
    }
}

@Composable
private fun LiveChannelsPreviewScreen() {
    PrototypeShell("Live", "Sports", "Browse channels while Preview remains passive.") {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Column(Modifier.width(500.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("Channels")
                Spacer(Modifier.height(5.dp))
                FocusRow("Arena Sport 1 HD", "19:30  Matchday Live", trailing = "LIVE")
                FocusRow("Arena Sport 2 HD", "20:00  European Football", focused = true, trailing = "PREVIEW")
                FocusRow("Sport Max", "20:15  Courtside")
                FocusRow("National Sport", "20:30  Sports Desk")
                FocusRow("Racing TV", "20:00  Night Race")
                FocusRow("Fight Network", "19:45  Main Event")
            }
            Column(Modifier.weight(1f)) {
                VideoFrame("Arena Sport 2 HD", "Preview · no playback controls", compact = true)
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("European Football", color = PrototypeText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(3.dp))
                        Text("20:00 — 21:50", color = PrototypeMuted, fontSize = 12.sp)
                    }
                    StatusPill("On now", positive = true)
                }
                Spacer(Modifier.height(10.dp))
                ProgressLine(0.36f)
                Spacer(Modifier.height(13.dp))
                Text("Next  ·  Post Match  21:50", color = PrototypeMuted, fontSize = 13.sp)
                Spacer(Modifier.height(15.dp))
                Text("OK again on the same previewed channel opens Full View.", color = PrototypePrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun LiveFullViewScreen(showEpg: Boolean) {
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Box(Modifier.fillMaxSize().background(Color(0xFF07101A)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.LiveTv, null, tint = Color(0xFF33445A), modifier = Modifier.size(86.dp))
                Spacer(Modifier.height(12.dp))
                Text("Arena Sport 2 HD", color = Color(0xFF7F8EA3), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text("Full View video surface", color = Color(0xFF526075), fontSize = 12.sp)
            }
        }
        if (!showEpg) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(48.dp)
                    .width(520.dp)
                    .background(Color(0xE611141B), Radius)
                    .border(1.dp, PrototypeOutline, Radius)
                    .padding(20.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Arena Sport 2 HD", color = PrototypeText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    StatusPill("LIVE", positive = true)
                }
                Spacer(Modifier.height(7.dp))
                Text("European Football", color = PrototypeText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("20:00 — 21:50", color = PrototypeMuted, fontSize = 12.sp)
            }
        } else {
            FullViewEpgOverlay()
        }
    }
}

@Composable
private fun FullViewEpgOverlay() {
    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .background(Color(0xF211141B))
            .padding(horizontal = 48.dp, vertical = 22.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Arena Sport 2 HD", color = PrototypeText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("EPG timeline", color = PrototypeMuted, fontSize = 11.sp)
            }
            Spacer(Modifier.weight(1f))
            Text("OK  EPG   ·   ↓  Timeline   ·   ← →  Programs   ·   ↑  Leave timeline", color = PrototypeMuted, fontSize = 11.sp)
        }
        Spacer(Modifier.height(15.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TimelineCard("19:00", "Matchday", 0.8f, false)
            TimelineCard("20:00", "European Football", 1.45f, true)
            TimelineCard("21:50", "Post Match", 1.0f, false)
            TimelineCard("22:30", "Highlights", 0.9f, false)
        }
    }
}

@Composable
private fun TimelineCard(time: String, title: String, weight: Float, focused: Boolean) {
    Column(
        modifier = Modifier
            .weight(weight)
            .height(92.dp)
            .border(2.dp, if (focused) PrototypePrimary else PrototypeOutline, Radius)
            .background(if (focused) PrototypePrimarySoft else PrototypeSurfaceRaised, Radius)
            .padding(14.dp),
    ) {
        Text(time, color = if (focused) PrototypePrimary else PrototypeMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(title, color = PrototypeText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun MoviesScreen() {
    PrototypeShell("Movies", "Movies", "Continue where you stopped, or browse by category.") {
        Column(Modifier.fillMaxSize()) {
            SectionLabel("Continue Watching")
            Spacer(Modifier.height(11.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PosterCard("Northbound", "47 min remaining", focused = true, progress = 0.58f)
                PosterCard("Quiet Harbor", "1 h 12 min remaining", progress = 0.31f)
                PosterCard("Signal Lost", "36 min remaining", progress = 0.72f)
            }
            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionLabel("Categories")
                Spacer(Modifier.width(18.dp))
                CategoryChip("Drama", true)
                Spacer(Modifier.width(8.dp))
                CategoryChip("Action", false)
                Spacer(Modifier.width(8.dp))
                CategoryChip("Comedy", false)
                Spacer(Modifier.width(8.dp))
                CategoryChip("Documentary", false)
                Spacer(Modifier.width(8.dp))
                CategoryChip("Family", false)
            }
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PosterCard("Northbound", "2026 · Drama")
                PosterCard("Paper City", "2025 · Drama")
                PosterCard("Horizon Line", "2026 · Drama")
                PosterCard("Silent Lake", "2024 · Drama")
                PosterCard("The Long Route", "2025 · Drama")
                PosterCard("Winter Glass", "2026 · Drama")
            }
        }
    }
}

@Composable
private fun CategoryChip(label: String, focused: Boolean) {
    Box(
        modifier = Modifier
            .height(38.dp)
            .border(2.dp, if (focused) PrototypePrimary else PrototypeOutline, RoundedCornerShape(19.dp))
            .background(if (focused) PrototypePrimarySoft else PrototypeSurface, RoundedCornerShape(19.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = if (focused) PrototypeText else PrototypeMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MovieDetailsScreen() {
    PrototypeShell("Movies", "Movie details") {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(36.dp)) {
            Box(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
                    .border(1.dp, PrototypeOutline, Radius)
                    .background(PrototypeSurfaceRaised, Radius),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Movie, null, tint = Color(0xFF626979), modifier = Modifier.size(70.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("NORTHBOUND", color = PrototypeText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            Column(Modifier.weight(1f).padding(top = 8.dp)) {
                Text("Northbound", color = PrototypeText, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusPill("2026")
                    StatusPill("1 h 52 min")
                    StatusPill("Drama")
                    StatusPill("16+")
                }
                Spacer(Modifier.height(22.dp))
                Text(
                    "A remote research crew heads north after an unexpected transmission changes the route home. The details page keeps one decision path: resume or start over.",
                    color = PrototypeMuted,
                    fontSize = 15.sp,
                    lineHeight = 23.sp,
                    modifier = Modifier.width(760.dp),
                )
                Spacer(Modifier.height(24.dp))
                Text("You stopped at 1:05:14", color = PrototypeText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(9.dp))
                Box(Modifier.width(540.dp)) { ProgressLine(0.58f) }
                Spacer(Modifier.height(25.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionButton("Continue · 47 min left", focused = true)
                    ActionButton("Play from beginning")
                }
                Spacer(Modifier.height(28.dp))
                InfoPanel("Back behavior", "Back returns to the originating Movies context and restores focus to Northbound.")
            }
        }
    }
}

@Composable
private fun OnDemandPlaybackScreen(title: String, episode: String?) {
    Box(Modifier.fillMaxSize().background(Color(0xFF061019))) {
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.PlayArrow, null, tint = Color(0xFF35465B), modifier = Modifier.size(92.dp))
            Text("On-demand video surface", color = Color(0xFF66768C), fontSize = 14.sp)
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0xE910131A))
                .padding(horizontal = 54.dp, vertical = 24.dp),
        ) {
            Text(title, color = PrototypeText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            if (episode != null) Text(episode, color = PrototypeMuted, fontSize = 12.sp)
            Spacer(Modifier.height(14.dp))
            ProgressLine(if (episode == null) 0.58f else 0.34f)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (episode == null) "1:05:14" else "17:42", color = PrototypeMuted, fontSize = 11.sp)
                Spacer(Modifier.weight(1f))
                ActionButton("Pause", focused = true)
                Spacer(Modifier.width(10.dp))
                ActionButton("Audio")
                Spacer(Modifier.width(10.dp))
                ActionButton("Subtitles")
                Spacer(Modifier.weight(1f))
                Text(if (episode == null) "1:52:00" else "51:10", color = PrototypeMuted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun SeriesScreen() {
    PrototypeShell("Series", "Series", "Resume an episode or browse series by category.") {
        Column(Modifier.fillMaxSize()) {
            SectionLabel("Continue Watching")
            Spacer(Modifier.height(11.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PosterCard("Arcline", "S2 · E4 · 33 min left", focused = true, progress = 0.34f)
                PosterCard("River Station", "S1 · E7 · 18 min left", progress = 0.63f)
            }
            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionLabel("Categories")
                Spacer(Modifier.width(18.dp))
                CategoryChip("Drama", true)
                Spacer(Modifier.width(8.dp))
                CategoryChip("Crime", false)
                Spacer(Modifier.width(8.dp))
                CategoryChip("Sci-Fi", false)
                Spacer(Modifier.width(8.dp))
                CategoryChip("Comedy", false)
                Spacer(Modifier.width(8.dp))
                CategoryChip("Kids", false)
            }
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PosterCard("Arcline", "3 seasons")
                PosterCard("River Station", "2 seasons")
                PosterCard("After Signal", "1 season")
                PosterCard("The Divide", "4 seasons")
                PosterCard("Night Office", "2 seasons")
                PosterCard("Outer District", "3 seasons")
            }
        }
    }
}

@Composable
private fun SeriesDetailsScreen() {
    PrototypeShell("Series", "Series details") {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(36.dp)) {
            Box(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
                    .border(1.dp, PrototypeOutline, Radius)
                    .background(PrototypeSurfaceRaised, Radius),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.VideoLibrary, null, tint = Color(0xFF626979), modifier = Modifier.size(70.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("ARCLINE", color = PrototypeText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            Column(Modifier.weight(1f).padding(top = 8.dp)) {
                Text("Arcline", color = PrototypeText, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusPill("2024–2026")
                    StatusPill("3 seasons")
                    StatusPill("Drama")
                }
                Spacer(Modifier.height(22.dp))
                Text(
                    "A transport network team discovers that a routine systems failure is connected to a much larger pattern. Series details lead directly to seasons and episodes.",
                    color = PrototypeMuted,
                    fontSize = 15.sp,
                    lineHeight = 23.sp,
                    modifier = Modifier.width(760.dp),
                )
                Spacer(Modifier.height(25.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionButton("Continue S2 · E4", focused = true)
                    ActionButton("Episodes", icon = Icons.Default.VideoLibrary)
                }
                Spacer(Modifier.height(30.dp))
                SectionLabel("Seasons")
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CategoryChip("Season 1", false)
                    CategoryChip("Season 2", true)
                    CategoryChip("Season 3", false)
                }
            }
        }
    }
}

@Composable
private fun EpisodesScreen() {
    PrototypeShell("Series", "Arcline", "Season 2 · episodes") {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(26.dp)) {
            Column(Modifier.width(270.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("Seasons")
                Spacer(Modifier.height(5.dp))
                FocusRow("Season 1", "8 episodes")
                FocusRow("Season 2", "10 episodes", focused = true)
                FocusRow("Season 3", "8 episodes")
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("Season 2")
                Spacer(Modifier.height(5.dp))
                EpisodeRow("E1", "Restart", "48 min", null, false)
                EpisodeRow("E2", "Closed Loop", "51 min", null, false)
                EpisodeRow("E3", "Crossing Point", "49 min", null, false)
                EpisodeRow("E4", "The Crossing", "51 min", 0.34f, true)
                EpisodeRow("E5", "North Relay", "46 min", null, false)
                EpisodeRow("E6", "Late Signal", "52 min", null, false)
            }
        }
    }
}

@Composable
private fun EpisodeRow(code: String, title: String, duration: String, progress: Float?, focused: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .border(2.dp, if (focused) PrototypePrimary else PrototypeOutline, Radius)
            .background(if (focused) PrototypePrimarySoft else PrototypeSurface, Radius)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(92.dp)
                .height(48.dp)
                .background(PrototypeSurfaceSoft, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(code, color = if (focused) PrototypePrimary else PrototypeMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = PrototypeText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            if (progress != null) {
                Spacer(Modifier.height(6.dp))
                Box(Modifier.width(260.dp)) { ProgressLine(progress) }
            }
        }
        Text(if (progress != null) "33 min left" else duration, color = PrototypeMuted, fontSize = 11.sp)
    }
}

@Composable
private fun SettingsScreen() {
    PrototypeShell("Settings", "Settings", "Four destinations. No extra category layer.") {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(30.dp)) {
            Column(Modifier.width(480.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FocusRow("Playlists", "Add, edit, refresh and enable sources", focused = true, leading = Icons.Default.Bookmark)
                FocusRow("Live Management", "Categories, channels, groups and order", leading = Icons.Default.LiveTv)
                FocusRow("Backup & Restore", "Supported local personalization", leading = Icons.Default.Backup)
                FocusRow("About", "OwnPlay, build information and disclaimer", leading = Icons.Default.Tv)
            }
            Column(Modifier.weight(1f)) {
                SectionLabel("Playlists")
                Spacer(Modifier.height(13.dp))
                InfoPanel("Manage media sources", "Add a source through a step-by-step TV flow, edit its display information, refresh content, or enable and disable it without dense phone-style forms.")
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatusPill("2 sources")
                    StatusPill("2 enabled", positive = true)
                }
                Spacer(Modifier.height(24.dp))
                Text("OK opens · Back returns one level · returning restores focus to Playlists", color = PrototypeMuted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun PlaylistsScreen() {
    PrototypeShell("Settings", "Playlists", "Source management designed for D-pad use.") {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            Column(Modifier.width(500.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                SectionLabel("Sources")
                Spacer(Modifier.height(4.dp))
                FocusRow("Family TV", "Xtream-compatible · enabled", focused = true, trailing = "READY")
                FocusRow("Local Channels", "M3U · enabled", trailing = "READY")
                Spacer(Modifier.height(6.dp))
                ActionButton("Add source", icon = Icons.Default.Bookmark)
            }
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Family TV", color = PrototypeText, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                        Text("Xtream-compatible source", color = PrototypeMuted, fontSize = 12.sp)
                    }
                    StatusPill("Enabled", positive = true)
                }
                Spacer(Modifier.height(20.dp))
                InfoPanel("Source status", "Last refresh completed successfully. Provider credentials are never displayed on this management surface.")
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionButton("Open", focused = true)
                    ActionButton("Refresh", icon = Icons.Default.Restore)
                    ActionButton("Edit", icon = Icons.Default.Settings)
                    ActionButton("Disable", icon = Icons.Default.CheckCircle)
                }
            }
        }
    }
}

@Composable
private fun LiveManagementScreen() {
    PrototypeShell("Settings", "Live Management", "Local personalization without drag gestures.") {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            Column(Modifier.width(360.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                SectionLabel("Manage")
                Spacer(Modifier.height(4.dp))
                FocusRow("Categories", "Visibility and order", focused = true)
                FocusRow("Channels", "Visibility, names and logos")
                FocusRow("Favorites", "Membership and order")
                FocusRow("Custom groups", "Create and organize groups")
            }
            Column(Modifier.weight(1f)) {
                SectionLabel("Category order")
                Spacer(Modifier.height(12.dp))
                ManagementRow("News", "18 channels", false)
                Spacer(Modifier.height(8.dp))
                ManagementRow("Sports", "24 channels", true)
                Spacer(Modifier.height(8.dp))
                ManagementRow("Entertainment", "31 channels", false)
                Spacer(Modifier.height(8.dp))
                ManagementRow("Kids", "12 channels", false)
                Spacer(Modifier.height(17.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionButton("Move up", focused = true)
                    ActionButton("Move down")
                    ActionButton("Hide")
                }
                Spacer(Modifier.height(14.dp))
                Text("Explicit remote actions replace pointer drag. Row geometry never changes with focus.", color = PrototypeMuted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun ManagementRow(title: String, secondary: String, focused: Boolean) {
    FocusRow(title, secondary, focused = focused, trailing = if (focused) "FOCUS" else null)
}

@Composable
private fun BackupRestoreScreen() {
    PrototypeShell("Settings", "Backup & Restore", "Backup supported personalization without provider secrets.") {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(22.dp)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .border(2.dp, PrototypePrimary, Radius)
                    .background(PrototypePrimarySoft, Radius)
                    .padding(28.dp),
            ) {
                Icon(Icons.Default.Backup, null, tint = PrototypePrimary, modifier = Modifier.size(42.dp))
                Spacer(Modifier.height(18.dp))
                Text("Create backup", color = PrototypeText, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Save supported local personalization such as favorites, hidden content, ordering and custom groups.", color = PrototypeMuted, fontSize = 14.sp, lineHeight = 21.sp)
                Spacer(Modifier.weight(1f))
                StatusPill("Focused action")
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .border(2.dp, PrototypeOutline, Radius)
                    .background(PrototypeSurface, Radius)
                    .padding(28.dp),
            ) {
                Icon(Icons.Default.Restore, null, tint = PrototypeMuted, modifier = Modifier.size(42.dp))
                Spacer(Modifier.height(18.dp))
                Text("Restore backup", color = PrototypeText, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Review and restore supported personalization. Provider credentials and secrets remain outside this backup format.", color = PrototypeMuted, fontSize = 14.sp, lineHeight = 21.sp)
                Spacer(Modifier.weight(1f))
                StatusPill("No provider secrets")
            }
        }
    }
}

@Composable
private fun AboutScreen() {
    PrototypeShell("Settings", "About") {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(36.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.width(430.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .background(PrototypePrimary, RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(88.dp))
                }
                Spacer(Modifier.height(20.dp))
                Text("OwnPlay TV", color = PrototypeText, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Text("Play your own media sources", color = PrototypeMuted, fontSize = 13.sp)
            }
            Column(Modifier.weight(1f)) {
                InfoPanel("Build", "Prototype presentation · application ID app.ownplay.tv")
                Spacer(Modifier.height(14.dp))
                InfoPanel("Product", "OwnPlay TV is a media player and playlist organizer for user-provided legitimate media sources and credentials.")
                Spacer(Modifier.height(14.dp))
                InfoPanel("Disclaimer", "OwnPlay does not sell subscriptions, provide channels, bundle provider content, or distribute provider media.")
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatusPill("Android TV")
                    StatusPill("Remote-first")
                    StatusPill("Landscape")
                }
            }
        }
    }
}
