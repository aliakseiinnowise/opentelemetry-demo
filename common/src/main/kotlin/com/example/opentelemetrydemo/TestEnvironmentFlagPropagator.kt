package com.example.opentelemetrydemo

import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.TextMapGetter
import io.opentelemetry.context.propagation.TextMapPropagator
import io.opentelemetry.context.propagation.TextMapSetter

object TestEnvironmentFlagPropagator : TextMapPropagator {

    private const val FIELD = "test-environment"
    private val FIELDS = listOf(FIELD)

    private const val PRESENT_VALUE = "true"

    override fun fields() = FIELDS

    override fun <C> inject(context: Context, carrier: C?, setter: TextMapSetter<C>) {
        val inTestEnv = TestEnvironmentFlag.isInTestEnv(context)

        if (!inTestEnv) {
            return
        }

        setter[carrier, FIELD] = PRESENT_VALUE
    }

    override fun <C> extract(context: Context, carrier: C?, getter: TextMapGetter<C>): Context {
        val testEnvHeader = getter[carrier, FIELD]

        if (testEnvHeader == null || testEnvHeader != PRESENT_VALUE) {
            return context
        }

        return context.with(TestEnvironmentFlag(true))
    }
}
