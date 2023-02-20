package com.example.opentelemetrydemo

import io.opentelemetry.context.Context
import io.opentelemetry.context.ContextKey
import io.opentelemetry.context.ImplicitContextKeyed

class TestEnvironmentFlag(private val testEnv: Boolean) : ImplicitContextKeyed {

    override fun storeInContext(context: Context): Context = context.with(KEY, this)

    companion object {
        private val KEY = ContextKey.named<TestEnvironmentFlag>("opentelemetry-test-environment-flag")

        fun isInTestEnv(context: Context = Context.current()): Boolean {
            val flag = context.get(KEY)
            return flag?.testEnv ?: false
        }
    }
}
