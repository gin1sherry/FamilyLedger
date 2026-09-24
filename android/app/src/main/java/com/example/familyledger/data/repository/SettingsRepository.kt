package com.example.familyledger.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.familyledger.domain.util.Money
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val keyMonthly = longPreferencesKey("monthly_budget_cents")

    val monthlyBudgetCents: Flow<Long> = context.dataStore.data.map { it[keyMonthly] ?: 0L }

    suspend fun setMonthlyBudget(yuan: Double) {
        val cents = if (yuan > 0) Money.yuanToCents(yuan) else 0L
        context.dataStore.edit { prefs ->
            prefs[keyMonthly] = cents
        }
    }

    suspend fun clearMonthlyBudget() = setMonthlyBudget(0.0)
}
