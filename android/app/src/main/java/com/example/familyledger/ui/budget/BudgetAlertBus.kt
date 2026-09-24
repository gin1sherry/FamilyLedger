package com.example.familyledger.ui.budget

import com.example.familyledger.domain.usecase.BudgetAlert
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/** 进程内预算横幅事件（记账/编辑后触发展示） */
@Singleton
class BudgetAlertBus @Inject constructor() {
    private val _alerts = MutableSharedFlow<BudgetAlert>(extraBufferCapacity = 4)
    val alerts: SharedFlow<BudgetAlert> = _alerts.asSharedFlow()

    fun publish(alert: BudgetAlert) {
        _alerts.tryEmit(alert)
    }
}
