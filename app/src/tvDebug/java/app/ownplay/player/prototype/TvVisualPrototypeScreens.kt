package app.ownplay.player.prototype

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
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

private val Bg = Color(0xFF080A0F)
private val Surface = Color(0xFF10131A)
private val SurfaceRaised = Color(0xFF171B24)
private val SurfaceSoft = Color(0xFF1E2230)
private val Primary = Color(0xFF9B7BFF)
private val PrimarySoft = Color(0xFF2B2145)
private val TextMain = Color(0xFFF3F1F7)
private val Muted = Color(0xFFA9A5B4)
private val Outline = Color(0xFF343947)
private val Success = Color(0xFF7ED7B4)
private val Shape = RoundedCornerShape(12.dp)

private val Colors = darkColorScheme(
    primary = Primary,
    onPrimary = Color(0xFF170E2A),
    primaryContainer = PrimarySoft,
    onPrimaryContainer = TextMain,
    background = Bg,
    onBackground = TextMain,
    surface = Surface,
    onSurface = TextMain,
    surfaceVariant = SurfaceRaised,
    onSurfaceVariant = Muted,
    outline = Outline,
)

@Composable
fun TvVisualPrototype(screen: String) {
    MaterialTheme(colorScheme = Colors) {
        when (screen) {
            "live_categories" -> LiveCategories()
            "live_channels_preview" -> LiveChannelsPreview()
            "live_full" -> LiveFull(showEpg = false)
            "live_full_epg" -> LiveFull(showEpg = true)
            "movies" -> Movies()
            "movie_details" -> MovieDetails()
            "movie_playback" -> DemandPlayback("Northbound", null)
            "series" -> Series()
            "series_details" -> SeriesDetails()
            "episodes" -> Episodes()
            "series_playback" -> DemandPlayback("Arcline", "S2 · E4  The Crossing")
            "settings" -> SettingsRoot()
            "playlists" -> Playlists()
            "live_management" -> LiveManagement()
            "backup_restore" -> BackupRestore()
            "about" -> About()
            else -> LiveCategories()
        }
    }
}

@Composable
private fun Shell(
    destination: String,
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(horizontal = 54.dp, vertical = 30.dp),
    ) {
        TopBar(destination)
        Spacer(Modifier.height(28.dp))
        Text(title, color = TextMain, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = Muted, fontSize = 14.sp)
        }
        Spacer(Modifier.height(20.dp))
        Box(Modifier.weight(1f)) { content() }
    }
}

@Composable
private fun TopBar(destination: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(38.dp).background(Primary, RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(27.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("OwnPlay", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text("TV", color = Muted, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, letterSpacing = 1.4.sp)
            }
        }
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Nav("Live", Icons.Default.LiveTv, destination == "Live")
            Nav("Movies", Icons.Default.Movie, destination == "Movies")
            Nav("Series", Icons.Default.VideoLibrary, destination == "Series")
            Nav("Settings", Icons.Default.Settings, destination == "Settings")
        }
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text("20:17", color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text("TV prototype", color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun Nav(label: String, icon: ImageVector, selected: Boolean) {
    Row(
        Modifier
            .height(42.dp)
            .border(2.dp, if (selected) Primary else Outline, RoundedCornerShape(10.dp))
            .background(if (selected) PrimarySoft else Surface, RoundedCornerShape(10.dp))
            .padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = if (selected) Primary else Muted, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = if (selected) TextMain else Muted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun RowItem(
    title: String,
    secondary: String? = null,
    focused: Boolean = false,
    trailing: String? = null,
    leading: ImageVector? = null,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(62.dp)
            .border(2.dp, if (focused) Primary else Outline, Shape)
            .background(if (focused) PrimarySoft else Surface, Shape)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            Icon(leading, null, tint = if (focused) Primary else Muted, modifier = Modifier.size(23.dp))
            Spacer(Modifier.width(14.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (secondary != null) Text(secondary, color = Muted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (trailing != null) Text(trailing, color = if (focused) Primary else Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Section(text: String) {
    Text(text.uppercase(), color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
}

@Composable
private fun Panel(modifier: Modifier = Modifier, title: String, body: String) {
    Column(
        modifier
            .background(Surface, Shape)
            .border(1.dp, Outline, Shape)
            .padding(20.dp),
    ) {
        Text(title, color = TextMain, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(body, color = Muted, fontSize = 13.sp, lineHeight = 19.sp)
    }
}

@Composable
private fun Pill(text: String, positive: Boolean = false) {
    Box(
        Modifier
            .background(if (positive) Color(0xFF17372F) else SurfaceSoft, RoundedCornerShape(18.dp))
            .padding(horizontal = 11.dp, vertical = 6.dp),
    ) {
        Text(text, color = if (positive) Success else Muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun VideoFrame(modifier: Modifier = Modifier, title: String, subtitle: String) {
    Box(
        modifier
            .background(Color(0xFF08111B), Shape)
            .border(1.dp, Outline, Shape),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Tv, null, tint = Color(0xFF48566A), modifier = Modifier.size(60.dp))
            Spacer(Modifier.height(10.dp))
            Text(title, color = Color(0xFFB8C4D4), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color(0xFF6F7A8C), fontSize = 11.sp)
        }
    }
}

@Composable
private fun Progress(progress: Float) {
    Box(Modifier.fillMaxWidth().height(4.dp).background(Outline, RoundedCornerShape(2.dp))) {
        Box(Modifier.fillMaxWidth(progress).height(4.dp).background(Primary, RoundedCornerShape(2.dp)))
    }
}

@Composable
private fun Poster(title: String, subtitle: String, focused: Boolean = false, progress: Float? = null) {
    Column(Modifier.width(164.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(208.dp)
                .border(2.dp, if (focused) Primary else Outline, Shape)
                .background(if (focused) Color(0xFF2C2340) else SurfaceRaised, Shape),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(12.dp)) {
                Icon(Icons.Default.Movie, null, tint = if (focused) Primary else Color(0xFF626979), modifier = Modifier.size(42.dp))
                Spacer(Modifier.height(10.dp))
                Text(title, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
        if (progress != null) {
            Spacer(Modifier.height(7.dp))
            Progress(progress)
        }
        Spacer(Modifier.height(7.dp))
        Text(title, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(subtitle, color = Muted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun Action(text: String, focused: Boolean = false, icon: ImageVector = Icons.Default.PlayArrow) {
    Row(
        Modifier
            .height(50.dp)
            .border(2.dp, if (focused) Primary else Outline, Shape)
            .background(if (focused) PrimarySoft else SurfaceRaised, Shape)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = if (focused) Primary else Muted, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun Category(label: String, focused: Boolean) {
    Box(
        Modifier
            .height(38.dp)
            .border(2.dp, if (focused) Primary else Outline, RoundedCornerShape(19.dp))
            .background(if (focused) PrimarySoft else Surface, RoundedCornerShape(19.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = if (focused) TextMain else Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LiveCategories() {
    Shell("Live", "Live", "Choose a category. OK opens its channel list.") {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            Column(Modifier.width(420.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Section("Categories")
                Spacer(Modifier.height(4.dp))
                RowItem("Favorites", "9 channels", leading = Icons.Default.Favorite)
                RowItem("News", "18 channels")
                RowItem("Sports", "24 channels", focused = true)
                RowItem("Entertainment", "31 channels")
                RowItem("Kids", "12 channels")
                RowItem("Documentary", "14 channels")
            }
            Column(Modifier.weight(1f)) {
                Section("Sports")
                Spacer(Modifier.height(12.dp))
                Panel(Modifier.fillMaxWidth(), "24 channels", "A simple category entry point. Focus stays in the left list; OK moves one logical level deeper into channels.")
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MiniChannel("Arena 1", "Football", true)
                    MiniChannel("Arena 2", "Basketball", false)
                    MiniChannel("Sport Max", "Tennis", false)
                }
                Spacer(Modifier.height(18.dp))
                Panel(Modifier.fillMaxWidth(), "Remote", "Up / Down changes category · OK opens channels · Back returns to the previous product area")
            }
        }
    }
}

@Composable
private fun MiniChannel(name: String, program: String, focused: Boolean) {
    Column(
        Modifier
            .width(190.dp)
            .height(118.dp)
            .border(2.dp, if (focused) Primary else Outline, Shape)
            .background(if (focused) PrimarySoft else Surface, Shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Default.LiveTv, null, tint = if (focused) Primary else Muted, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(8.dp))
        Text(name, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(program, color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun LiveChannelsPreview() {
    Shell("Live", "Sports", "Browse channels while Preview remains passive.") {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Column(Modifier.width(500.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Section("Channels")
                Spacer(Modifier.height(4.dp))
                RowItem("Arena Sport 1 HD", "19:30  Matchday Live", trailing = "LIVE")
                RowItem("Arena Sport 2 HD", "20:00  European Football", focused = true, trailing = "PREVIEW")
                RowItem("Sport Max", "20:15  Courtside")
                RowItem("National Sport", "20:30  Sports Desk")
                RowItem("Racing TV", "20:00  Night Race")
                RowItem("Fight Network", "19:45  Main Event")
            }
            Column(Modifier.weight(1f)) {
                VideoFrame(Modifier.fillMaxWidth().height(286.dp), "Arena Sport 2 HD", "Preview · no playback controls")
                Spacer(Modifier.height(15.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("European Football", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("20:00 — 21:50", color = Muted, fontSize = 12.sp)
                    }
                    Pill("On now", positive = true)
                }
                Spacer(Modifier.height(9.dp))
                Progress(0.36f)
                Spacer(Modifier.height(12.dp))
                Text("Next  ·  Post Match  21:50", color = Muted, fontSize = 13.sp)
                Spacer(Modifier.height(13.dp))
                Text("OK again on the same previewed channel opens Full View.", color = Primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun LiveFull(showEpg: Boolean) {
    Box(Modifier.fillMaxSize().background(Color(0xFF07101A))) {
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.LiveTv, null, tint = Color(0xFF33445A), modifier = Modifier.size(86.dp))
            Spacer(Modifier.height(12.dp))
            Text("Arena Sport 2 HD", color = Color(0xFF7F8EA3), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text("Full View video surface", color = Color(0xFF526075), fontSize = 12.sp)
        }
        if (showEpg) {
            FullEpg(Modifier.align(Alignment.BottomCenter))
        } else {
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(48.dp)
                    .width(520.dp)
                    .background(Color(0xE611141B), Shape)
                    .border(1.dp, Outline, Shape)
                    .padding(20.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Arena Sport 2 HD", color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Pill("LIVE", positive = true)
                }
                Spacer(Modifier.height(7.dp))
                Text("European Football", color = TextMain, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("20:00 — 21:50", color = Muted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun FullEpg(modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .background(Color(0xF211141B))
            .padding(horizontal = 48.dp, vertical = 22.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Arena Sport 2 HD", color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("EPG timeline", color = Muted, fontSize = 11.sp)
            }
            Spacer(Modifier.weight(1f))
            Text("OK  EPG   ·   ↓  Timeline   ·   ← →  Programs   ·   ↑  Leave timeline", color = Muted, fontSize = 11.sp)
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Timeline(Modifier.width(210.dp), "19:00", "Matchday", false)
            Timeline(Modifier.width(370.dp), "20:00", "European Football", true)
            Timeline(Modifier.width(260.dp), "21:50", "Post Match", false)
            Timeline(Modifier.width(230.dp), "22:30", "Highlights", false)
        }
    }
}

@Composable
private fun Timeline(modifier: Modifier, time: String, title: String, focused: Boolean) {
    Column(
        modifier
            .height(90.dp)
            .border(2.dp, if (focused) Primary else Outline, Shape)
            .background(if (focused) PrimarySoft else SurfaceRaised, Shape)
            .padding(14.dp),
    ) {
        Text(time, color = if (focused) Primary else Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(7.dp))
        Text(title, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun Movies() {
    Shell("Movies", "Movies", "Continue where you stopped, or browse by category.") {
        Column(Modifier.fillMaxSize()) {
            Section("Continue Watching")
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Poster("Northbound", "47 min remaining", focused = true, progress = 0.58f)
                Poster("Quiet Harbor", "1 h 12 min remaining", progress = 0.31f)
                Poster("Signal Lost", "36 min remaining", progress = 0.72f)
            }
            Spacer(Modifier.height(22.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Section("Categories")
                Spacer(Modifier.width(18.dp))
                Category("Drama", true)
                Spacer(Modifier.width(8.dp)); Category("Action", false)
                Spacer(Modifier.width(8.dp)); Category("Comedy", false)
                Spacer(Modifier.width(8.dp)); Category("Documentary", false)
                Spacer(Modifier.width(8.dp)); Category("Family", false)
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Poster("Northbound", "2026 · Drama")
                Poster("Paper City", "2025 · Drama")
                Poster("Horizon Line", "2026 · Drama")
                Poster("Silent Lake", "2024 · Drama")
                Poster("The Long Route", "2025 · Drama")
                Poster("Winter Glass", "2026 · Drama")
            }
        }
    }
}

@Composable
private fun MovieDetails() {
    Shell("Movies", "Movie details") {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(36.dp)) {
            Artwork(Modifier.width(300.dp).fillMaxHeight(), "NORTHBOUND", Icons.Default.Movie)
            Column(Modifier.weight(1f).padding(top = 8.dp)) {
                Text("Northbound", color = TextMain, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pill("2026"); Pill("1 h 52 min"); Pill("Drama"); Pill("16+")
                }
                Spacer(Modifier.height(22.dp))
                Text(
                    "A remote research crew heads north after an unexpected transmission changes the route home. The details page keeps one decision path: resume or start over.",
                    color = Muted,
                    fontSize = 15.sp,
                    lineHeight = 23.sp,
                    modifier = Modifier.width(760.dp),
                )
                Spacer(Modifier.height(23.dp))
                Text("You stopped at 1:05:14", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(9.dp))
                Box(Modifier.width(540.dp)) { Progress(0.58f) }
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Action("Continue · 47 min left", focused = true)
                    Action("Play from beginning")
                }
                Spacer(Modifier.height(26.dp))
                Panel(Modifier.fillMaxWidth(), "Back behavior", "Back returns to the originating Movies context and restores focus to Northbound.")
            }
        }
    }
}

@Composable
private fun Artwork(modifier: Modifier, label: String, icon: ImageVector) {
    Box(
        modifier.border(1.dp, Outline, Shape).background(SurfaceRaised, Shape),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = Color(0xFF626979), modifier = Modifier.size(70.dp))
            Spacer(Modifier.height(12.dp))
            Text(label, color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DemandPlayback(title: String, episode: String?) {
    Box(Modifier.fillMaxSize().background(Color(0xFF061019))) {
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.PlayArrow, null, tint = Color(0xFF35465B), modifier = Modifier.size(92.dp))
            Text("On-demand video surface", color = Color(0xFF66768C), fontSize = 14.sp)
        }
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0xE910131A))
                .padding(horizontal = 54.dp, vertical = 24.dp),
        ) {
            Text(title, color = TextMain, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            if (episode != null) Text(episode, color = Muted, fontSize = 12.sp)
            Spacer(Modifier.height(13.dp))
            Progress(if (episode == null) 0.58f else 0.34f)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (episode == null) "1:05:14" else "17:42", color = Muted, fontSize = 11.sp)
                Spacer(Modifier.weight(1f))
                Action("Pause", focused = true)
                Spacer(Modifier.width(10.dp)); Action("Audio")
                Spacer(Modifier.width(10.dp)); Action("Subtitles")
                Spacer(Modifier.weight(1f))
                Text(if (episode == null) "1:52:00" else "51:10", color = Muted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun Series() {
    Shell("Series", "Series", "Resume an episode or browse series by category.") {
        Column(Modifier.fillMaxSize()) {
            Section("Continue Watching")
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Poster("Arcline", "S2 · E4 · 33 min left", focused = true, progress = 0.34f)
                Poster("River Station", "S1 · E7 · 18 min left", progress = 0.63f)
            }
            Spacer(Modifier.height(22.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Section("Categories")
                Spacer(Modifier.width(18.dp))
                Category("Drama", true)
                Spacer(Modifier.width(8.dp)); Category("Crime", false)
                Spacer(Modifier.width(8.dp)); Category("Sci-Fi", false)
                Spacer(Modifier.width(8.dp)); Category("Comedy", false)
                Spacer(Modifier.width(8.dp)); Category("Kids", false)
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Poster("Arcline", "3 seasons")
                Poster("River Station", "2 seasons")
                Poster("After Signal", "1 season")
                Poster("The Divide", "4 seasons")
                Poster("Night Office", "2 seasons")
                Poster("Outer District", "3 seasons")
            }
        }
    }
}

@Composable
private fun SeriesDetails() {
    Shell("Series", "Series details") {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(36.dp)) {
            Artwork(Modifier.width(300.dp).fillMaxHeight(), "ARCLINE", Icons.Default.VideoLibrary)
            Column(Modifier.weight(1f).padding(top = 8.dp)) {
                Text("Arcline", color = TextMain, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pill("2024–2026"); Pill("3 seasons"); Pill("Drama")
                }
                Spacer(Modifier.height(22.dp))
                Text(
                    "A transport network team discovers that a routine systems failure is connected to a much larger pattern. Series details lead directly to seasons and episodes.",
                    color = Muted,
                    fontSize = 15.sp,
                    lineHeight = 23.sp,
                    modifier = Modifier.width(760.dp),
                )
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Action("Continue S2 · E4", focused = true)
                    Action("Episodes", icon = Icons.Default.VideoLibrary)
                }
                Spacer(Modifier.height(28.dp))
                Section("Seasons")
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Category("Season 1", false); Category("Season 2", true); Category("Season 3", false)
                }
            }
        }
    }
}

@Composable
private fun Episodes() {
    Shell("Series", "Arcline", "Season 2 · episodes") {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(26.dp)) {
            Column(Modifier.width(270.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Section("Seasons")
                Spacer(Modifier.height(4.dp))
                RowItem("Season 1", "8 episodes")
                RowItem("Season 2", "10 episodes", focused = true)
                RowItem("Season 3", "8 episodes")
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Section("Season 2")
                Spacer(Modifier.height(4.dp))
                Episode("E1", "Restart", "48 min", null, false)
                Episode("E2", "Closed Loop", "51 min", null, false)
                Episode("E3", "Crossing Point", "49 min", null, false)
                Episode("E4", "The Crossing", "51 min", 0.34f, true)
                Episode("E5", "North Relay", "46 min", null, false)
                Episode("E6", "Late Signal", "52 min", null, false)
            }
        }
    }
}

@Composable
private fun Episode(code: String, title: String, duration: String, progress: Float?, focused: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(70.dp)
            .border(2.dp, if (focused) Primary else Outline, Shape)
            .background(if (focused) PrimarySoft else Surface, Shape)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(92.dp).height(46.dp).background(SurfaceSoft, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
            Text(code, color = if (focused) Primary else Muted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            if (progress != null) {
                Spacer(Modifier.height(6.dp))
                Box(Modifier.width(260.dp)) { Progress(progress) }
            }
        }
        Text(if (progress != null) "33 min left" else duration, color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun SettingsRoot() {
    Shell("Settings", "Settings", "Four destinations. No extra category layer.") {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(30.dp)) {
            Column(Modifier.width(480.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                RowItem("Playlists", "Add, edit, refresh and enable sources", focused = true, leading = Icons.Default.Bookmark)
                RowItem("Live Management", "Categories, channels, groups and order", leading = Icons.Default.LiveTv)
                RowItem("Backup & Restore", "Supported local personalization", leading = Icons.Default.Backup)
                RowItem("About", "OwnPlay, build information and disclaimer", leading = Icons.Default.Tv)
            }
            Column(Modifier.weight(1f)) {
                Section("Playlists")
                Spacer(Modifier.height(12.dp))
                Panel(Modifier.fillMaxWidth(), "Manage media sources", "Add a source through a step-by-step TV flow, edit its display information, refresh content, or enable and disable it without dense phone-style forms.")
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { Pill("2 sources"); Pill("2 enabled", positive = true) }
                Spacer(Modifier.height(23.dp))
                Text("OK opens · Back returns one level · returning restores focus to Playlists", color = Muted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun Playlists() {
    Shell("Settings", "Playlists", "Source management designed for D-pad use.") {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            Column(Modifier.width(500.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Section("Sources")
                Spacer(Modifier.height(4.dp))
                RowItem("Family TV", "Xtream-compatible · enabled", focused = true, trailing = "READY")
                RowItem("Local Channels", "M3U · enabled", trailing = "READY")
                Spacer(Modifier.height(6.dp))
                Action("Add source", icon = Icons.Default.Bookmark)
            }
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Family TV", color = TextMain, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                        Text("Xtream-compatible source", color = Muted, fontSize = 12.sp)
                    }
                    Pill("Enabled", positive = true)
                }
                Spacer(Modifier.height(20.dp))
                Panel(Modifier.fillMaxWidth(), "Source status", "Last refresh completed successfully. Provider credentials are never displayed on this management surface.")
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Action("Open", focused = true); Action("Refresh", icon = Icons.Default.Restore); Action("Edit", icon = Icons.Default.Settings); Action("Disable", icon = Icons.Default.CheckCircle)
                }
            }
        }
    }
}

@Composable
private fun LiveManagement() {
    Shell("Settings", "Live Management", "Local personalization without drag gestures.") {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            Column(Modifier.width(360.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Section("Manage")
                Spacer(Modifier.height(4.dp))
                RowItem("Categories", "Visibility and order", focused = true)
                RowItem("Channels", "Visibility, names and logos")
                RowItem("Favorites", "Membership and order")
                RowItem("Custom groups", "Create and organize groups")
            }
            Column(Modifier.weight(1f)) {
                Section("Category order")
                Spacer(Modifier.height(12.dp))
                RowItem("News", "18 channels")
                Spacer(Modifier.height(8.dp)); RowItem("Sports", "24 channels", focused = true)
                Spacer(Modifier.height(8.dp)); RowItem("Entertainment", "31 channels")
                Spacer(Modifier.height(8.dp)); RowItem("Kids", "12 channels")
                Spacer(Modifier.height(17.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { Action("Move up", focused = true); Action("Move down"); Action("Hide") }
                Spacer(Modifier.height(14.dp))
                Text("Explicit remote actions replace pointer drag. Row geometry never changes with focus.", color = Muted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun BackupRestore() {
    Shell("Settings", "Backup & Restore", "Backup supported personalization without provider secrets.") {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(22.dp)) {
            BackupCard(
                Modifier.weight(1f),
                "Create backup",
                "Save supported local personalization such as favorites, hidden content, ordering and custom groups.",
                Icons.Default.Backup,
                true,
            )
            BackupCard(
                Modifier.weight(1f),
                "Restore backup",
                "Review and restore supported personalization. Provider credentials and secrets remain outside this backup format.",
                Icons.Default.Restore,
                false,
            )
        }
    }
}

@Composable
private fun BackupCard(modifier: Modifier, title: String, body: String, icon: ImageVector, focused: Boolean) {
    Column(
        modifier
            .fillMaxHeight()
            .border(2.dp, if (focused) Primary else Outline, Shape)
            .background(if (focused) PrimarySoft else Surface, Shape)
            .padding(28.dp),
    ) {
        Icon(icon, null, tint = if (focused) Primary else Muted, modifier = Modifier.size(42.dp))
        Spacer(Modifier.height(18.dp))
        Text(title, color = TextMain, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(body, color = Muted, fontSize = 14.sp, lineHeight = 21.sp)
        Spacer(Modifier.weight(1f))
        Pill(if (focused) "Focused action" else "No provider secrets")
    }
}

@Composable
private fun About() {
    Shell("Settings", "About") {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(36.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.width(430.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(130.dp).background(Primary, RoundedCornerShape(28.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(88.dp))
                }
                Spacer(Modifier.height(20.dp))
                Text("OwnPlay TV", color = TextMain, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Text("Play your own media sources", color = Muted, fontSize = 13.sp)
            }
            Column(Modifier.weight(1f)) {
                Panel(Modifier.fillMaxWidth(), "Build", "Prototype presentation · application ID app.ownplay.tv")
                Spacer(Modifier.height(14.dp))
                Panel(Modifier.fillMaxWidth(), "Product", "OwnPlay TV is a media player and playlist organizer for user-provided legitimate media sources and credentials.")
                Spacer(Modifier.height(14.dp))
                Panel(Modifier.fillMaxWidth(), "Disclaimer", "OwnPlay does not sell subscriptions, provide channels, bundle provider content, or distribute provider media.")
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { Pill("Android TV"); Pill("Remote-first"); Pill("Landscape") }
            }
        }
    }
}
