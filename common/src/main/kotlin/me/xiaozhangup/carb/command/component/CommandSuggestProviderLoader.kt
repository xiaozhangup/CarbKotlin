/*
 * Derived from TabooLib 6.3.0-test-6-23-1 (MIT), Copyright (c) 2018 Bkm016.
 * Adapted for CarbKotlin shared commands and platform backends, 2026-09-25.
 * See src/main/resources/META-INF/licenses/TabooLib-command-LICENSE.txt.
 */
package me.xiaozhangup.carb.command.component

import java.util.*

/**
 * 命令建议提供者加载器
 *
 * 使用 Java ServiceLoader 机制加载自定义的建议提供者
 */
object CommandSuggestProviderLoader {

    private var provider: CommandSuggestProvider? = null

    /**
     * 获取当前的建议提供者
     */
    fun getProvider(): CommandSuggestProvider {
        if (provider == null) {
            provider = loadProvider()
        }
        return provider!!
    }

    /**
     * 手动设置建议提供者
     */
    fun setProvider(provider: CommandSuggestProvider) {
        this.provider = provider
    }

    /**
     * 重新加载提供者
     */
    fun reload() {
        provider = loadProvider()
    }

    private fun loadProvider(): CommandSuggestProvider {
        return try {
            val loader = ServiceLoader.load(
                CommandSuggestProvider::class.java,
                CommandSuggestProvider::class.java.classLoader
            )
            // 获取第一个自定义实现，如果没有则使用默认实现
            loader.firstOrNull() ?: createDefaultProvider()
        } catch (_: Exception) {
            // 如果加载失败，使用默认实现
            createDefaultProvider()
        }
    }

    private fun createDefaultProvider(): CommandSuggestProvider {
        return DefaultCommandSuggestProvider()
    }
}
