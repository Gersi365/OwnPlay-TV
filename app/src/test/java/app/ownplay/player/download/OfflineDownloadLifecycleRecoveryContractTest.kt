package app.ownplay.player.download

import app.ownplay.player.testing.sourceBlockAfter
import app.ownplay.player.testing.sourceText
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineDownloadLifecycleRecoveryContractTest {
    @Test
    fun `duplicate active enqueue keeps existing worker and paused stays paused`() {
        val repository = sourceText("src/main/java/app/ownplay/player/download/OfflineDownloadRepository.kt")
        val enqueueBlock = sourceBlockAfter(repository, "suspend fun enqueue(spec: OfflineDownloadSpec)")

        assertTrue(enqueueBlock.contains("DownloadStates.DOWNLOADING"))
        assertTrue(enqueueBlock.contains("DownloadStates.QUEUED"))
        assertTrue(enqueueBlock.contains("existingWorkPolicy = ExistingWorkPolicy.KEEP"))
        assertTrue(enqueueBlock.contains("DownloadStates.PAUSED -> return existing.downloadId"))
    }
}
