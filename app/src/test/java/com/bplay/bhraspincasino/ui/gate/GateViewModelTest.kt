package com.bplay.bhraspincasino.ui.gate

import com.bplay.bhraspincasino.core.OfferEndpoint
import com.bplay.bhraspincasino.domain.AdvertisingIdSource
import com.bplay.bhraspincasino.domain.OfferLinkComposer
import com.bplay.bhraspincasino.domain.PushTokenSource
import com.bplay.bhraspincasino.domain.ResolveOfferLinkUseCase
import com.bplay.bhraspincasino.push.PushLaunch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GateViewModelTest {

    private val base = "https://host.test/path?track_sr=src&track_gi=aaid-1"
    private lateinit var viewModel: GateViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = GateViewModel(
            ResolveOfferLinkUseCase(
                advertisingIds = object : AdvertisingIdSource {
                    override suspend fun advertisingId(): String = "aaid-1"
                },
                pushTokens = object : PushTokenSource {
                    override suspend fun pushToken(): String = "token-1"
                },
                composer = OfferLinkComposer(
                    OfferEndpoint(
                        baseUrl = "https://host.test/path",
                        source = "src"
                    )
                ),
            ),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cold start loads the standard link and reloads it when the network returns`() = runTest {
        viewModel.dispatch(GateIntent.ColdStart(push = null))

        assertEquals(GateCommand.Load("$base&track_ph=token-1"), viewModel.commands.first())
        viewModel.dispatch(GateIntent.PageReady("https://host.test/lobby"))
        viewModel.dispatch(GateIntent.PageUnavailable)
        viewModel.dispatch(GateIntent.NetworkBack)
        assertEquals(GateCommand.Reload, viewModel.commands.first())
    }

    @Test
    fun `each failed retry goes offline again and waits longer`() = runTest {
        viewModel.dispatch(GateIntent.ColdStart(push = null))
        viewModel.commands.first()
        val link = GateCommand.Load("$base&track_ph=token-1")

        viewModel.dispatch(GateIntent.PageUnavailable)
        viewModel.dispatch(GateIntent.NetworkBack)
        assertEquals(GateStage.Splash, viewModel.state.value.stage)
        var startedAt = testScheduler.currentTime
        assertEquals(link, viewModel.commands.first())
        assertEquals(1_000L, testScheduler.currentTime - startedAt)

        viewModel.dispatch(GateIntent.PageUnavailable)
        assertEquals(GateStage.Offline, viewModel.state.value.stage)
        viewModel.dispatch(GateIntent.NetworkBack)
        startedAt = testScheduler.currentTime
        assertEquals(link, viewModel.commands.first())
        assertEquals(2_000L, testScheduler.currentTime - startedAt)

        viewModel.dispatch(GateIntent.PageReady("https://host.test/lobby"))
        viewModel.dispatch(GateIntent.PageUnavailable)
        viewModel.dispatch(GateIntent.NetworkBack)
        startedAt = testScheduler.currentTime
        assertEquals(GateCommand.Reload, viewModel.commands.first())
        assertEquals(1_000L, testScheduler.currentTime - startedAt)
    }

    @Test
    fun `retention push adds track_cl and offer push opens its own link`() = runTest {
        viewModel.dispatch(GateIntent.ColdStart(PushLaunch(clickId = "pub-42", offerUrl = null)))
        assertEquals(
            GateCommand.Load("$base&track_cl=pub-42&track_ph=token-1"),
            viewModel.commands.first()
        )

        val offer = "https://offer.test/?track_cl=pub-7"
        viewModel.dispatch(GateIntent.PushTapped(PushLaunch(clickId = "pub-7", offerUrl = offer)))
        assertEquals(GateCommand.Load(offer), viewModel.commands.first())
    }

    @Test
    fun `network coming back before any page resolves the link again`() = runTest {
        viewModel.dispatch(GateIntent.ColdStart(PushLaunch(clickId = "pub-42", offerUrl = null)))
        viewModel.commands.first()
        viewModel.dispatch(GateIntent.PageUnavailable)
        assertEquals(GateStage.Offline, viewModel.state.value.stage)

        viewModel.dispatch(GateIntent.NetworkBack)

        assertEquals(
            GateCommand.Load("$base&track_cl=pub-42&track_ph=token-1"),
            viewModel.commands.first()
        )
    }

    @Test
    fun `recreated screen reloads the page it was showing`() = runTest {
        viewModel.dispatch(GateIntent.ColdStart(push = null))
        viewModel.commands.first()
        viewModel.dispatch(GateIntent.PageReady("https://host.test/lobby"))

        viewModel.dispatch(GateIntent.ColdStart(push = null))

        assertEquals(GateStage.Splash, viewModel.state.value.stage)
        assertEquals(GateCommand.Load("https://host.test/lobby"), viewModel.commands.first())
    }

    @Test
    fun `recreated screen before any page reloads the sent link`() = runTest {
        viewModel.dispatch(GateIntent.ColdStart(PushLaunch(clickId = "pub-42", offerUrl = null)))
        val sent = viewModel.commands.first()

        viewModel.dispatch(GateIntent.ColdStart(push = null))

        assertEquals(sent, viewModel.commands.first())
    }
}
