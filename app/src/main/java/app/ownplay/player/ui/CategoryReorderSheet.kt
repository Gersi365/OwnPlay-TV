package app.ownplay.player.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.ownplay.player.live.LiveCategory
import app.ownplay.player.personalization.ManualOrderPlacement

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CategoryReorderSheet(
    categories: List<LiveCategory>,
    onOrderChanged: (List<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    val doneFocusRequester = remember { FocusRequester() }
    var working by remember(categories) { mutableStateOf(categories) }

    LaunchedEffect(Unit) {
        doneFocusRequester.requestFocus()
    }

    fun applyOrder(next: List<LiveCategory>) {
        if (next == working) return
        working = next
        onOrderChanged(next.map(LiveCategory::providerCategoryKey))
    }

    fun moveWithRemote(index: Int, delta: Int) {
        val category = working.getOrNull(index) ?: return
        val anchor = working.getOrNull(index + delta) ?: return
        applyOrder(
            moveRelative(
                categories = working,
                draggedKey = category.providerCategoryKey,
                anchorKey = anchor.providerCategoryKey,
                placement = if (delta < 0) ManualOrderPlacement.BEFORE else ManualOrderPlacement.AFTER,
            ),
        )
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 360.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "Reorder categories",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Use Up / Down with the remote. Press Done when finished.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.focusRequester(doneFocusRequester),
                ) {
                    Text("Done")
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                itemsIndexed(
                    items = working,
                    key = { _, category -> category.providerCategoryKey },
                ) { index, category ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (category.isHidden) {
                                    Text(
                                        text = "Hidden",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            TextButton(
                                enabled = index > 0,
                                onClick = { moveWithRemote(index, -1) },
                            ) {
                                Text("Up")
                            }
                            TextButton(
                                enabled = index < working.lastIndex,
                                onClick = { moveWithRemote(index, 1) },
                            ) {
                                Text("Down")
                            }
                        }
                    }
                }
            }
        }
    }
}

internal fun moveRelative(
    categories: List<LiveCategory>,
    draggedKey: String,
    anchorKey: String,
    placement: ManualOrderPlacement,
): List<LiveCategory> {
    if (draggedKey == anchorKey) return categories
    val dragged = categories.firstOrNull { it.providerCategoryKey == draggedKey } ?: return categories
    if (categories.none { it.providerCategoryKey == anchorKey }) return categories
    val without = categories.filterNot { it.providerCategoryKey == draggedKey }.toMutableList()
    val anchorIndex = without.indexOfFirst { it.providerCategoryKey == anchorKey }
    if (anchorIndex < 0) return categories
    val insertionIndex = if (placement == ManualOrderPlacement.BEFORE) anchorIndex else anchorIndex + 1
    without.add(insertionIndex.coerceIn(0, without.size), dragged)
    return without
}
