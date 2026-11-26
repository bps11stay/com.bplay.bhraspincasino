package com.bplay.bhraspincasino.ui.gate

import com.bplay.bhraspincasino.push.PushLaunch

enum class GateStage { Splash, Page, Offline }

data class GateState(val stage: GateStage = GateStage.Splash)

sealed interface GateIntent {
    data class ColdStart(val push: PushLaunch?) : GateIntent
    data object Restored : GateIntent
    data class PushTapped(val push: PushLaunch) : GateIntent
    data class PageReady(val url: String?) : GateIntent
    data object PageUnavailable : GateIntent
    data object NetworkBack : GateIntent
}

sealed interface GateCommand {
    data class Load(val url: String) : GateCommand
    data object Reload : GateCommand
}
