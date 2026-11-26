package com.bplay.bhraspincasino.ui.gate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bplay.bhraspincasino.domain.ResolveOfferLinkUseCase
import com.bplay.bhraspincasino.push.PushLaunch
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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
    private var sentLink: String? = null
    private var shownPage: String? = null
    private var failedRetries = 0
    private var openJob: Job? = null

    fun dispatch(intent: GateIntent) {
        when (intent) {
            is GateIntent.ColdStart -> if (!started) {
                started = true
                open(intent.push)
            } else {
                reattach()
            }

            GateIntent.Restored -> {
                started = true
                pageEverShown = true
            }

            is GateIntent.PushTapped -> {
                started = true
                open(intent.push)
            }

            is GateIntent.PageReady -> {
                pageEverShown = true
                intent.url?.takeIf { it.startsWith("http", ignoreCase = true) }
                    ?.let { shownPage = it }
                failedRetries = 0
                _state.update { it.copy(stage = GateStage.Page) }
            }

            GateIntent.PageUnavailable -> _state.update { it.copy(stage = GateStage.Offline) }
            GateIntent.NetworkBack -> retry()
        }
    }

    private fun open(push: PushLaunch?) {
        lastPush = push
        failedRetries = 0
        openJob?.cancel()
        openJob = viewModelScope.launch { send(GateCommand.Load(linkFor(push))) }
    }

    private fun reattach() {
        val url = shownPage ?: sentLink ?: return
        openJob?.cancel()
        _state.update { it.copy(stage = GateStage.Splash) }
        _commands.trySend(GateCommand.Load(url))
    }

    private fun retry() {
        val wait = retryDelayMillis(failedRetries++)
        _state.update { it.copy(stage = GateStage.Splash) }
        openJob?.cancel()
        openJob = viewModelScope.launch {
            delay(wait)
            val command =
                if (pageEverShown) GateCommand.Reload else GateCommand.Load(linkFor(lastPush))
            send(command)
        }
    }

    private suspend fun send(command: GateCommand) {
        if (command is GateCommand.Load) sentLink = command.url
        _commands.send(command)
    }

    private suspend fun linkFor(push: PushLaunch?): String =
        push?.offerUrl ?: resolveOfferLink(clickId = push?.clickId)

    private companion object {
        const val FIRST_RETRY_DELAY_MILLIS = 1_000L
        const val MAX_RETRY_DELAY_MILLIS = 30_000L

        fun retryDelayMillis(failedRetries: Int): Long =
            (FIRST_RETRY_DELAY_MILLIS shl failedRetries.coerceAtMost(5)).coerceAtMost(
                MAX_RETRY_DELAY_MILLIS
            )
    }
}
