package com.bplay.bhraspincasino.ui.gate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bplay.bhraspincasino.domain.ResolveOfferLinkUseCase
import com.bplay.bhraspincasino.push.PushLaunch
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GateViewModel(private val resolveOfferLink: ResolveOfferLinkUseCase) : ViewModel() {

    private val _state = MutableStateFlow(GateState())
    val state: StateFlow<GateState> = _state.asStateFlow()

    private val _commands = Channel<GateCommand>(Channel.BUFFERED)
    val commands: Flow<GateCommand> = _commands.receiveAsFlow()

    private var started = false
    private var pageEverShown = false
    private var lastPush: PushLaunch? = null
    private var openJob: Job? = null

    fun dispatch(intent: GateIntent) {
        when (intent) {
            is GateIntent.ColdStart -> if (!started) {
                started = true
                open(intent.push)
            }

            GateIntent.Restored -> {
                started = true
                pageEverShown = true
            }

            is GateIntent.PushTapped -> {
                started = true
                open(intent.push)
            }

            GateIntent.PageReady -> {
                pageEverShown = true
                _state.update { it.copy(stage = GateStage.Page) }
            }

            GateIntent.PageUnavailable -> _state.update { it.copy(stage = GateStage.Offline) }
            GateIntent.NetworkBack -> if (pageEverShown) {
                _commands.trySend(GateCommand.Reload)
            } else {
                open(lastPush)
            }
        }
    }

    private fun open(push: PushLaunch?) {
        lastPush = push
        openJob?.cancel()
        openJob = viewModelScope.launch {
            val url = push?.offerUrl ?: resolveOfferLink(clickId = push?.clickId)
            _commands.send(GateCommand.Load(url))
        }
    }
}
