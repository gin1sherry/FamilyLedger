package com.example.familyledger.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ProductMatcherTest {
    @Test
    fun strips_spec_and_case() {
        assertEquals(
            ProductMatcher.normalize("伊利纯牛奶250ml"),
            ProductMatcher.normalize("伊利纯牛奶 250ML")
        )
    }

    @Test
    fun brand_alias_apple() {
        assertEquals(
            ProductMatcher.keyFor("苹果16 Pro"),
            ProductMatcher.keyFor("iPhone 16 Pro")
        )
    }

    @Test
    fun different_products_differ() {
        assertNotEquals(
            ProductMatcher.keyFor("星巴克拿铁"),
            ProductMatcher.keyFor("农夫山泉")
        )
    }
}
