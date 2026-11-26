package com.bplay.bhraspincasino.core

import com.bplay.bhraspincasino.ui.gate.GateViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val gateModule = module {
    viewModelOf(::GateViewModel)
}
