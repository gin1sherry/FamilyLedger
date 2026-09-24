package com.example.familyledger.data.model

/** 分类枚举 — PRD 4.2.11 */
object Categories {
    val ALL = listOf(
        "餐饮", "日用", "数码", "服饰", "交通",
        "医疗", "教育", "娱乐", "家居", "其他"
    )
    const val DEFAULT = "其他"

    fun isValid(c: String) = c in ALL
}

/** 图片类型 */
enum class ImageType(val apiValue: String) {
    PRODUCT("product"),
    RECEIPT("receipt"),
    ORDER("order"),
    OTHER("other");

    /** 压缩参数 — PRD 4.2.4 */
    val longEdge: Int get() = if (this == PRODUCT) 1024 else 1536
    val initialQuality: Int get() = if (this == PRODUCT) 75 else 80
    val targetKb: Int get() = if (this == PRODUCT) 100 else 300
}
