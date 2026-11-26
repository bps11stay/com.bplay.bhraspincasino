package com.bplay.bhraspincasino.ui.gate

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebSettings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bplay.bhraspincasino.BuildConfig
import com.bplay.bhraspincasino.databinding.ActivityGateBinding
import com.bplay.bhraspincasino.push.NotificationPermissionRequest
import com.bplay.bhraspincasino.push.PushLaunch
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class GateActivity : ComponentActivity() {

    private val viewModel: GateViewModel by viewModel()
    private val notificationPermission = NotificationPermissionRequest(this)
    private val chromeClient = GateChromeClient(openPicker = ::openFilePicker)
    private val filePicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            chromeClient.onPickerResult(it.resultCode, it.data)
        }
    private val networkWatcher by lazy { NetworkReturnWatcher(this) { viewModel.dispatch(GateIntent.NetworkBack) } }

    private lateinit var binding: ActivityGateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        binding = ActivityGateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpWebView()
        padForSystemBars()
        observe()

        val pageState = savedInstanceState?.getBundle(KEY_PAGE_STATE)
        if (pageState != null && binding.gateWeb.restoreState(pageState) != null) {
            viewModel.dispatch(GateIntent.Restored)
        } else {
            viewModel.dispatch(GateIntent.ColdStart(pushFrom(intent)))
        }
        if (savedInstanceState == null) notificationPermission.askIfNeeded()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pushFrom(intent)?.let { viewModel.dispatch(GateIntent.PushTapped(it)) }
    }

    override fun onResume() {
        super.onResume()
        binding.gateWeb.onResume()
    }

    override fun onPause() {
        binding.gateWeb.onPause()
        CookieManager.getInstance().flush()
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(KEY_PAGE_STATE, Bundle().also { binding.gateWeb.saveState(it) })
    }

    override fun onDestroy() {
        networkWatcher.stop()
        chromeClient.dropPending()
        binding.gateWeb.destroy()
        super.onDestroy()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpWebView() = with(binding.gateWeb) {
        setBackgroundColor(Color.TRANSPARENT)
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(false)
            loadWithOverviewMode = true
            useWideViewPort = true
            mediaPlaybackRequiresUserGesture = false
            cacheMode = WebSettings.LOAD_DEFAULT
        }
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(this@with, true)
        }
        webViewClient = GateWebClient(
            onPageReady = { viewModel.dispatch(GateIntent.PageReady) },
            onPageUnavailable = { viewModel.dispatch(GateIntent.PageUnavailable) },
        )
        webChromeClient = chromeClient
    }

    private fun padForSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.gateWeb) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                        WindowInsetsCompat.Type.displayCutout() or
                        WindowInsetsCompat.Type.ime(),
            )
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.state.collect(::render) }
            }
        }
        lifecycleScope.launch {
            viewModel.commands.collect { command ->
                when (command) {
                    is GateCommand.Load -> {
                        if (BuildConfig.DEBUG) Log.d(TAG, "load ${command.url}")
                        binding.gateWeb.loadUrl(command.url)
                    }

                    GateCommand.Reload -> binding.gateWeb.reload()
                }
            }
        }
    }

    private fun render(state: GateState) {
        val pageVisible = state.stage == GateStage.Page
        binding.gateWeb.visibility = if (pageVisible) View.VISIBLE else View.INVISIBLE
        binding.gateBanner.visibility = if (pageVisible) View.GONE else View.VISIBLE
        if (state.stage == GateStage.Offline) networkWatcher.watch() else networkWatcher.stop()
    }

    private fun openFilePicker(picker: Intent): Boolean = try {
        filePicker.launch(picker)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }

    private fun pushFrom(intent: Intent?): PushLaunch? {
        val fromHistory =
            intent != null && (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0
        return if (fromHistory) null else PushLaunch.from(intent)
    }

    private companion object {
        const val TAG = "BplayGate"
        const val KEY_PAGE_STATE = "gate_page_state"
    }
}
