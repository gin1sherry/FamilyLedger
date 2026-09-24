package com.example.familyledger.domain.budget

/**
 * 预算提醒纯逻辑（与存储无关，便于单测）。
 * 规则见 PRD V1.1 §4.5.2。
 */
object BudgetPolicy {
    const val CATEGORY_ALL = "__all__"
    const val ALERT_RATIO = 0.80

    fun shouldAlert(spentCents: Long, budgetCents: Long): Boolean {
        if (budgetCents <= 0) return false
        return spentCents * 100 >= budgetCents * 80
    }

    fun alertPercent(spentCents: Long, budgetCents: Long): Int {
        if (budgetCents <= 0) return 0
        return ((spentCents.toDouble() / budgetCents) * 100).toInt()
    }

    /** 已低于 80% 时允许再次提醒（编辑/删除后回算） */
    fun shouldResetNotified(spentCents: Long, budgetCents: Long): Boolean {
        if (budgetCents <= 0) return false
        return !shouldAlert(spentCents, budgetCents)
    }

    /** 进度条色阶：<60 绿 / 60–80 橙 / ≥80 红 */
    enum class Level { OK, WARN, CRITICAL }

    fun level(spentCents: Long, budgetCents: Long): Level {
        if (budgetCents <= 0) return Level.OK
        val pct = spentCents.toDouble() / budgetCents
        return when {
            pct >= 0.80 -> Level.CRITICAL
            pct >= 0.60 -> Level.WARN
            else -> Level.OK
        }
    }
}
