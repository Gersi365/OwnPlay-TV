#!/usr/bin/env python3
from pathlib import Path
import re

TARGET = Path("app/src/tvDebug/java/app/ownplay/player/prototype/TvVisualPrototypeScreens.kt")
text = TARGET.read_text()

shell_pattern = re.compile(
    r'''@Composable\nprivate fun Shell\([\s\S]*?\n@Composable\nprivate fun RowItem\(''',
    re.MULTILINE,
)

shell_replacement = '''@Composable
private fun Shell(
    destination: String,
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit,
) {
    Row(Modifier.fillMaxSize().background(Bg)) {
        PrimaryRail(destination)
        Column(
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 34.dp, end = 46.dp, top = 28.dp, bottom = 30.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(title, color = TextMain, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    if (subtitle != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(subtitle, color = Muted, fontSize = 13.sp)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("20:17", color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text("OwnPlay TV", color = Muted, fontSize = 10.sp)
                }
            }
            Spacer(Modifier.height(20.dp))
            Box(Modifier.weight(1f)) { content() }
        }
    }
}

@Composable
private fun PrimaryRail(destination: String) {
    Column(
        Modifier
            .width(82.dp)
            .fillMaxHeight()
            .background(Color(0xFF0C0F15))
            .border(1.dp, Color(0xFF181C25), RoundedCornerShape(0.dp))
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(46.dp).background(Primary, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "OwnPlay", tint = Color.White, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.weight(1f))
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            RailIcon(Icons.Default.LiveTv, "Live", destination == "Live")
            RailIcon(Icons.Default.Movie, "Movies", destination == "Movies")
            RailIcon(Icons.Default.VideoLibrary, "Series", destination == "Series")
            RailIcon(Icons.Default.Settings, "Settings", destination == "Settings")
        }
        Spacer(Modifier.weight(1f))
        Text("TV", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
    }
}

@Composable
private fun RailIcon(icon: ImageVector, description: String, selected: Boolean) {
    Box(
        Modifier
            .size(52.dp)
            .border(2.dp, if (selected) Primary else Outline, RoundedCornerShape(12.dp))
            .background(if (selected) PrimarySoft else Surface, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = description,
            tint = if (selected) Primary else Muted,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun RowItem('''

text, count = shell_pattern.subn(shell_replacement, text, count=1)
if count != 1:
    raise SystemExit(f"Expected exactly one Shell/TopBar/Nav block, replaced {count}")

replacements = {
    'Shell("Live", "Live", "Choose a category. OK opens its channel list.")': 'Shell("Live", "Live", "Categories shown here come from the active provider.")',
    'Section("Categories")': 'Section("Provider categories")',
    'RowItem("Favorites", "9 channels", leading = Icons.Default.Favorite)': 'RowItem("Provider category 01", "9 channels")',
    'RowItem("News", "18 channels")': 'RowItem("Provider category 02", "18 channels")',
    'RowItem("Sports", "24 channels", focused = true)': 'RowItem("Provider category 03", "24 channels", focused = true)',
    'RowItem("Entertainment", "31 channels")': 'RowItem("Provider category 04", "31 channels")',
    'RowItem("Kids", "12 channels")': 'RowItem("Provider category 05", "12 channels")',
    'RowItem("Documentary", "14 channels")': 'RowItem("Provider category 06", "14 channels")',
    'Section("Sports")': 'Section("Provider category 03")',
    'MiniChannel("Arena 1", "Football", true)': 'MiniChannel("Channel 301", "Current program", true)',
    'MiniChannel("Arena 2", "Basketball", false)': 'MiniChannel("Channel 302", "Current program", false)',
    'MiniChannel("Sport Max", "Tennis", false)': 'MiniChannel("Channel 303", "Current program", false)',
    'Shell("Live", "Sports", "Browse channels while Preview remains passive.")': 'Shell("Live", "Provider category 03", "Browse channels while Preview remains passive.")',
    'RowItem("Arena Sport 1 HD", "19:30  Matchday Live", trailing = "LIVE")': 'RowItem("Channel 301 HD", "19:30  Current program", trailing = "LIVE")',
    'RowItem("Arena Sport 2 HD", "20:00  European Football", focused = true, trailing = "PREVIEW")': 'RowItem("Channel 302 HD", "20:00  Current program", focused = true, trailing = "PREVIEW")',
    'RowItem("Sport Max", "20:15  Courtside")': 'RowItem("Channel 303", "20:15  Current program")',
    'RowItem("National Sport", "20:30  Sports Desk")': 'RowItem("Channel 304", "20:30  Current program")',
    'RowItem("Racing TV", "20:00  Night Race")': 'RowItem("Channel 305", "20:00  Current program")',
    'RowItem("Fight Network", "19:45  Main Event")': 'RowItem("Channel 306", "19:45  Current program")',
    '"Arena Sport 2 HD", "Preview · no playback controls"': '"Channel 302 HD", "Preview · no playback controls"',
    'Text("European Football",': 'Text("Current program",',
    'Text("Next  ·  Post Match  21:50",': 'Text("Next  ·  Next program  21:50",',
    'Text("Arena Sport 2 HD",': 'Text("Channel 302 HD",',
    'Timeline(Modifier.width(210.dp), "19:00", "Matchday", false)': 'Timeline(Modifier.width(210.dp), "19:00", "Previous program", false)',
    'Timeline(Modifier.width(370.dp), "20:00", "European Football", true)': 'Timeline(Modifier.width(370.dp), "20:00", "Current program", true)',
    'Timeline(Modifier.width(260.dp), "21:50", "Post Match", false)': 'Timeline(Modifier.width(260.dp), "21:50", "Next program", false)',
    'Timeline(Modifier.width(230.dp), "22:30", "Highlights", false)': 'Timeline(Modifier.width(230.dp), "22:30", "Later program", false)',
    'Shell("Movies", "Movies", "Continue where you stopped, or browse by category.")': 'Shell("Movies", "Movies", "Continue Watching stays here; categories come from the active provider.")',
    'Category("Drama", true)': 'Category("Provider category 01", true)',
    'Category("Action", false)': 'Category("Provider category 02", false)',
    'Category("Comedy", false)': 'Category("Provider category 03", false)',
    'Category("Documentary", false)': 'Category("Provider category 04", false)',
    'Category("Family", false)': 'Category("Provider category 05", false)',
    '"2026 · Drama"': '"2026 · provider metadata"',
    '"2025 · Drama"': '"2025 · provider metadata"',
    '"2024 · Drama"': '"2024 · provider metadata"',
    'Pill("Drama")': 'Pill("Provider metadata")',
    'Shell("Series", "Series", "Resume an episode or browse series by category.")': 'Shell("Series", "Series", "Continue Watching stays here; categories come from the active provider.")',
    'Category("Crime", false)': 'Category("Provider category 02", false)',
    'Category("Sci-Fi", false)': 'Category("Provider category 03", false)',
    'Category("Kids", false)': 'Category("Provider category 05", false)',
    'RowItem("Categories", "Visibility and order", focused = true)': 'RowItem("Provider categories", "Visibility and local order", focused = true)',
    'Section("Category order")': 'Section("Provider category order")',
    'Pill("Landscape")': 'Pill("TV-only")',
}

for old, new in replacements.items():
    if old not in text:
        raise SystemExit(f"Required prototype anchor not found: {old}")
    text = text.replace(old, new)

management_before = '''Section("Provider category order")
                Spacer(Modifier.height(12.dp))
                RowItem("Provider category 02", "18 channels")
                Spacer(Modifier.height(8.dp)); RowItem("Provider category 03", "24 channels", focused = true)
                Spacer(Modifier.height(8.dp)); RowItem("Provider category 04", "31 channels")
                Spacer(Modifier.height(8.dp)); RowItem("Provider category 05", "12 channels")'''
management_after = '''Section("Provider category order")
                Spacer(Modifier.height(12.dp))
                RowItem("Provider category 01", "18 channels")
                Spacer(Modifier.height(8.dp)); RowItem("Provider category 02", "24 channels", focused = true)
                Spacer(Modifier.height(8.dp)); RowItem("Provider category 03", "31 channels")
                Spacer(Modifier.height(8.dp)); RowItem("Provider category 04", "12 channels")'''
if management_before not in text:
    raise SystemExit("Provider category management block not found")
text = text.replace(management_before, management_after, 1)

text = text.replace(
    'RowItem("Custom groups", "Create and organize groups")',
    'RowItem("Custom groups", "Local personalization only")',
)
text = text.replace(
    'Text("Explicit remote actions replace pointer drag. Row geometry never changes with focus.",',
    'Text("Provider categories can be hidden or reordered locally; OwnPlay does not invent replacement content categories. Row geometry never changes with focus.",',
)

for forbidden in ('TopBar(', 'Nav("Live"', 'Nav("Movies"', 'Nav("Series"', 'Nav("Settings"', 'Pill("Landscape")'):
    if forbidden in text:
        raise SystemExit(f"Forbidden legacy prototype token remains: {forbidden}")
if re.search(r'\bAll\b', text):
    raise SystemExit('Forbidden UI label/token All remains in screenshot prototype')

TARGET.write_text(text)
print(f"Applied left icon rail and provider-driven category prototype to {TARGET}")
