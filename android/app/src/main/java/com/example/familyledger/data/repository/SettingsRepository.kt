package com.example.familyledger.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.familyledger.BuildConfig
import com.example.familyledger.domain.util.Money
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

data class AiEndpointConfig(
    val apiKey: String,
    val baseUrl: String,
    val model: String
)

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val keyMonthly = longPreferencesKey("monthly_budget_cents")
    private val keyApiKey = stringPreferencesKey("ai_api_key")
    private val keyBaseUrl = stringPreferencesKey("ai_base_url")
    private val keyModel = stringPreferencesKey("ai_model")

    val monthlyBudgetCents: Flow<Long> = context.dataStore.data.map { it[keyMonthly] ?: 0L }

    val aiApiKey: Flow<String> = context.dataStore.data.map {
        it[keyApiKey]?.takeIf { s -> s.isNotBlank() } ?: BuildConfig.AI_API_KEY
    }

    suspend fun setMonthlyBudget(yuan: Double) {
        val cents = if (yuan > 0) Money.yuanToCents(yuan) else 0L
        context.dataStore.edit { prefs -> prefs[keyMonthly] = cents }
    }

    suspend fun clearMonthlyBudget() = setMonthlyBudget(0.0)

    suspend fun setAiConfig(apiKey: String, baseUrl: String, model: String) {
        context.dataStore.edit { prefs ->
            prefs[keyApiKey] = apiKey.trim()
            prefs[keyBaseUrl] = baseUrl.trim().ifBlank { BuildConfig.AI_BASE_URL }
            prefs[keyModel] = model.trim().ifBlank { BuildConfig.AI_MODEL }
        }
    }

    suspend fun aiConfig(): AiEndpointConfig {
        val p = context.dataStore.data.first()
        return AiEndpointConfig(
            apiKey = p[keyApiKey]?.takeIf { it.isNotBlank() } ?: BuildConfig.AI_API_KEY,
            baseUrl = p[keyBaseUrl]?.takeIf { it.isNotBlank() } ?: BuildConfig.AI_BASE_URL,
            model = p[keyModel]?.takeIf { it.isNotBlank() } ?: BuildConfig.AI_MODEL
        )
    }
}
