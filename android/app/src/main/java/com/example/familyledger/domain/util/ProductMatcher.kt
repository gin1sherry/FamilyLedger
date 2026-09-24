package com.example.familyledger.domain.util

/**
 * 商品聚合键 — PRD P0-05
 * 小写、全半角统一、去空白标点、去数量规格后缀、品牌别名
 */
object ProductMatcher {
    private val brandAliases = listOf(
        Regex("iphone\\s*16\\s*pro\\s*max", RegexOption.IGNORE_CASE) to "iphone16promax",
        Regex("iphone\\s*16\\s*pro", RegexOption.IGNORE_CASE) to "iphone16pro",
        Regex("iphone\\s*16", RegexOption.IGNORE_CASE) to "iphone16",
        Regex("苹果\\s*16\\s*pro\\s*max", RegexOption.IGNORE_CASE) to "iphone16promax",
        Regex("苹果\\s*16\\s*pro", RegexOption.IGNORE_CASE) to "iphone16pro",
        Regex("苹果\\s*16", RegexOption.IGNORE_CASE) to "iphone16",
        Regex("苹果|apple", RegexOption.IGNORE_CASE) to "apple"
    )

    private val specSuffix = Regex(
        """\s*\d+(\.\d+)?\s*(ml|g|kg|l|升|毫升|克|千克|斤|件|片|枚|双|瓶|包|盒|罐|袋|支|管|粒)|\d+\s*[x×]\s*\d+|【[^】]*】|\([^)]*\)|（[^）]*）""",
        RegexOption.IGNORE_CASE
    )

    fun normalize(name: String): String {
        var s = name.trim().lowercase()
        s = s.map { ch ->
            if (ch.code in 0xFF01..0xFF5E) (ch.code - 0xFEE0).toChar() else ch
        }.joinToString("")
        s = s.replace(Regex("\\s+"), "")
        brandAliases.forEach { (re, rep) -> s = s.replace(re, rep) }
        s = s.replace(specSuffix, "")
        s = s.replace(Regex("[，,。.、·\\-_/|]"), "")
        return s
    }

    fun keyFor(name: String): String = normalize(name).ifEmpty { "id:" + name.hashCode() }
}
