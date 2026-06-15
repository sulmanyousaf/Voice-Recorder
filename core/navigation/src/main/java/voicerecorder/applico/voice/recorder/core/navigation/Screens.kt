package voicerecorder.applico.voice.recorder.core.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Record : Screen

    @Serializable
    data object Library : Screen

    @Serializable
    data object Settings : Screen
}
