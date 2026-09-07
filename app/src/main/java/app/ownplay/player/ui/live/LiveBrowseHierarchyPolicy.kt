package app.ownplay.player.ui.live

internal enum class LiveBrowseHierarchyLevel {
    CATEGORIES,
    CHANNELS,
}

internal enum class LiveBrowseBackAction {
    CLOSE_PREVIEW,
    SHOW_CATEGORIES,
    PROPAGATE,
}

internal enum class LiveChannelActivationAction {
    OPEN_PREVIEW,
    OPEN_FULLSCREEN,
}

internal object LiveBrowseHierarchyPolicy {
    fun initialLevel(
        hasPreview: Boolean = false,
    ): LiveBrowseHierarchyLevel = if (!hasPreview) {
        LiveBrowseHierarchyLevel.CATEGORIES
    } else {
        LiveBrowseHierarchyLevel.CHANNELS
    }

    fun ownsBack(
        hasPreview: Boolean,
        level: LiveBrowseHierarchyLevel,
    ): Boolean = hasPreview || level == LiveBrowseHierarchyLevel.CHANNELS

    fun backAction(
        hasPreview: Boolean,
        level: LiveBrowseHierarchyLevel,
    ): LiveBrowseBackAction = when {
        hasPreview -> LiveBrowseBackAction.CLOSE_PREVIEW
        level == LiveBrowseHierarchyLevel.CHANNELS -> LiveBrowseBackAction.SHOW_CATEGORIES
        else -> LiveBrowseBackAction.PROPAGATE
    }

    fun channelActivationAction(
        activePreviewChannelId: String?,
        activatedChannelId: String,
    ): LiveChannelActivationAction = if (
        activePreviewChannelId == activatedChannelId
    ) {
        LiveChannelActivationAction.OPEN_FULLSCREEN
    } else {
        LiveChannelActivationAction.OPEN_PREVIEW
    }
}
