package app.ownplay.player.ui

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.ownplay.player.persistence.PlaylistSourceSummary
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.ownPlayTvPlaylistAvailabilityPreferences: DataStore<Preferences> by preferencesDataStore(
    name = "ownplay_tv_playlist_availability",
)

internal class TvPlaylistAvailabilityStore(
    context: Context,
) {
    private val dataStore = context.applicationContext.ownPlayTvPlaylistAvailabilityPreferences

    fun observeDisabledSourceIds(): Flow<Set<String>> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences ->
            preferences[DISABLED_SOURCE_IDS_KEY]?.toSet().orEmpty()
        }

    suspend fun setEnabled(sourceId: String, enabled: Boolean): Boolean {
        val normalizedSourceId = sourceId.trim()
        if (normalizedSourceId.isEmpty()) return false

        return try {
            dataStore.edit { preferences ->
                val disabled = preferences[DISABLED_SOURCE_IDS_KEY]?.toMutableSet() ?: mutableSetOf()
                if (enabled) {
                    disabled.remove(normalizedSourceId)
                } else {
                    disabled.add(normalizedSourceId)
                }
                if (disabled.isEmpty()) {
                    preferences.remove(DISABLED_SOURCE_IDS_KEY)
                } else {
                    preferences[DISABLED_SOURCE_IDS_KEY] = disabled
                }
            }
            true
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            false
        }
    }
}

internal fun resolveTvAvailableSourceIds(
    summaries: List<PlaylistSourceSummary>,
    disabledSourceIds: Set<String>,
): List<String> = summaries
    .asSequence()
    .filter { summary -> summary.enabled && summary.sourceId !in disabledSourceIds }
    .map { summary -> summary.sourceId }
    .toList()

private val DISABLED_SOURCE_IDS_KEY = stringSetPreferencesKey("disabled_source_ids")
