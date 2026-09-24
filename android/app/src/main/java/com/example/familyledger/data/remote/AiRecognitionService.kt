package com.example.familyledger.data.remote

import com.example.familyledger.BuildConfig
import com.example.familyledger.data.model.ReceiptItem
import com.example.familyledger.data.model.RecognitionResult
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

enum class RecognitionErrorKind {
    OFFLINE, TIMEOUT, HTTP, BAD_JSON, REFUSED, NO_KEY, UNKNOWN
}

class RecognitionException(
    val kind: RecognitionErrorKind,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * 多模态识别 API — 仅此处联网。
 * Key 来自本机 local.properties → BuildConfig。
 */
@Singleton
class AiRecognitionService @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    fun recognize(
        imageBase64: String,
        imageType: String,
        forceReceipt: Boolean
    ): RecognitionResult {
        if (BuildConfig.AI_API_KEY.isBlank()) {
            throw RecognitionException(RecognitionErrorKind.NO_KEY, "未配置 AI_API_KEY")
        }
        val prompt = buildPrompt(imageType, forceReceipt)
        val body = JSONObject()
            .put("model", BuildConfig.AI_MODEL)
            .put(
                "messages",
                JSONArray().put(
                    JSONObject()
                        .put("role", "user")
                        .put(
                            "content",
                            JSONArray()
                                .put(
                                    JSONObject()
                                        .put("type", "image_url")
                                        .put(
                                            "image_url",
                                            JSONObject().put("url", "data:image/jpeg;base64,$imageBase64")
                                        )
                                )
                                .put(JSONObject().put("type", "text").put("text", prompt))
                        )
                )
            )
            .put("temperature", 0.1)
            .toString()

        val request = Request.Builder()
            .url(BuildConfig.AI_BASE_URL.trimEnd('/') + "/chat/completions")
            .addHeader("Authorization", "Bearer " + BuildConfig.AI_API_KEY)
            .addHeader("Content-Type", "application/json")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        try {
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) {
                    throw RecognitionException(RecognitionErrorKind.HTTP, "HTTP ${resp.code}")
                }
                val raw = resp.body?.string()
                    ?: throw RecognitionException(RecognitionErrorKind.BAD_JSON, "empty body")
                return parseChat(raw, imageType)
            }
        } catch (e: RecognitionException) {
            throw e
        } catch (io: IOException) {
            throw RecognitionException(RecognitionErrorKind.TIMEOUT, io.message ?: "network", io)
        } catch (t: Throwable) {
            throw RecognitionException(RecognitionErrorKind.UNKNOWN, t.message ?: "unknown", t)
        }
    }

    private fun buildPrompt(imageType: String, forceReceipt: Boolean): String = buildString {
        appendLine("你是消费单据信息提取器。只输出严格 JSON，不要 Markdown。")
        appendLine("字段：name, price, date, category, brand, spec, quantity, unitPrice, discount, store, paymentMethod, orderNo, expiryDate, note, confidence(0-1), extras(object)。")
        appendLine("category 仅限：餐饮/日用/数码/服饰/交通/医疗/教育/娱乐/家居/其他。")
        appendLine("price 必须是实付价。找不到用 null，禁止编造。未预定义信息放入 extras。")
        if (forceReceipt || imageType != "product") {
            appendLine("多条模式：mode=receipt, total, items[{name,price,category,brand,quantity}]，并识别 store/date/paymentMethod/orderNo。")
        } else {
            appendLine("单品模式：mode=product。")
        }
    }

    private fun parseChat(chatJson: String, imageType: String): RecognitionResult {
        val root = try {
            JSONObject(chatJson)
        } catch (t: Throwable) {
            throw RecognitionException(RecognitionErrorKind.BAD_JSON, "chat envelope", t)
        }
        val content = try {
            root.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
        } catch (t: Throwable) {
            throw RecognitionException(RecognitionErrorKind.BAD_JSON, "choices", t)
        }
        val cleaned = content.trim()
            .removePrefix("```json").removePrefix("```")
            .removeSuffix("```").trim()
        val obj = try {
            JSONObject(cleaned)
        } catch (t: Throwable) {
            throw RecognitionException(RecognitionErrorKind.BAD_JSON, "content json", t)
        }

        val items = mutableListOf<ReceiptItem>()
        if (obj.has("items") && !obj.isNull("items")) {
            val arr = obj.getJSONArray("items")
            for (i in 0 until arr.length()) {
                val it = arr.getJSONObject(i)
                items.add(
                    ReceiptItem(
                        name = it.str("name"),
                        price = it.num("price"),
                        category = it.str("category"),
                        brand = it.str("brand"),
                        quantity = it.num("quantity")
                    )
                )
            }
        }
        val extras = mutableMapOf<String, String>()
        if (obj.has("extras") && !obj.isNull("extras")) {
            val ex = obj.getJSONObject("extras")
            for (k in ex.keys()) extras[k] = ex.optString(k, "")
        }

        val dateMs = parseDate(obj.str("date"))
        val expiry = parseDate(obj.str("expiryDate"))

        return RecognitionResult(
            mode = obj.optString("mode", if (forceReceiptMode(imageType)) "receipt" else "product"),
            name = obj.str("name"),
            price = obj.num("price"),
            date = dateMs,
            category = obj.str("category"),
            brand = obj.str("brand"),
            spec = obj.str("spec"),
            quantity = obj.num("quantity"),
            unitPrice = obj.num("unitPrice"),
            discount = obj.num("discount"),
            store = obj.str("store"),
            paymentMethod = obj.str("paymentMethod"),
            orderNo = obj.str("orderNo"),
            expiryDate = expiry,
            note = obj.str("note"),
            confidence = obj.num("confidence"),
            extras = extras,
            items = items,
            total = obj.num("total"),
            source = when (imageType) {
                "receipt" -> "receipt"
                "order" -> "screenshot"
                else -> "camera"
            }
        )
    }

    private fun forceReceiptMode(imageType: String) = imageType != "product"

    private fun parseDate(s: String?): Long? {
        if (s.isNullOrBlank()) return null
        return try {
            if (s.all { it.isDigit() } && s.length >= 10) {
                val v = s.toLong()
                if (v < 1_000_000_000_000L) v * 1000 else v
            } else {
                java.time.LocalDate.parse(s.take(10))
                    .atStartOfDay(java.time.ZoneId.systemDefault())
                    .toInstant().toEpochMilli()
            }
        } catch (_: Exception) {
            null
        }
    }
}

private fun JSONObject.str(key: String): String? =
    if (has(key) && !isNull(key)) optString(key).takeIf { it.isNotBlank() } else null

private fun JSONObject.num(key: String): Double? =
    if (has(key) && !isNull(key)) optDouble(key, Double.NaN).takeIf { !it.isNaN() } else null
