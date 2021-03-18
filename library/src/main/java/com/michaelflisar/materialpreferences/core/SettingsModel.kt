package com.michaelflisar.materialpreferences.core

import com.michaelflisar.materialpreferences.core.classes.EnumConverter
import com.michaelflisar.materialpreferences.core.interfaces.Storage
import com.michaelflisar.materialpreferences.core.interfaces.StorageSetting
import com.michaelflisar.materialpreferences.core.settings.*
import com.michaelflisar.materialpreferences.core.settings.AnyStringSetting
import com.michaelflisar.materialpreferences.core.settings.BoolSetting
import com.michaelflisar.materialpreferences.core.settings.FloatSetting
import com.michaelflisar.materialpreferences.core.settings.IntSetting
import com.michaelflisar.materialpreferences.core.settings.LongSetting

abstract class SettingsModel(
        internal val storage: Storage
) {
    internal val internalProperties: MutableMap<String, StorageSetting<*>> = mutableMapOf()

    /**
     * Delegate string property
     * @param default default string value
     * @param key custom storage key
     */
    protected fun stringPref(
            default: String = "",
            key: String? = null
    ): StorageSetting<String> = StringSetting(this, default, key)

    /**
     * Delegate string set property
     * @param default string integer set value
     * @param key custom storage key
     */
    protected fun stringSetPref(
            default: Set<String> = emptySet(),
            key: String? = null
    ): StorageSetting<Set<String>> = StringSetSetting(this, default, key)

    /**
     * Delegate bool property
     * @param default default bool value
     * @param key custom storage key
     */
    protected fun boolPref(
            default: Boolean = false,
            key: String? = null
    ): StorageSetting<Boolean> = BoolSetting(this, default, key)

    /**
     * Delegate integer property
     * @param default default integer value
     * @param key custom storage key
     */
    protected fun intPref(
            default: Int = 0,
            key: String? = null
    ): StorageSetting<Int> = IntSetting(this, default, key)

    /**
     * Delegate integer set property
     * @param default default integer set value
     * @param key custom storage key
     */
    protected fun intSetPref(
            default: Set<Int> = emptySet(),
            key: String? = null
    ): StorageSetting<Set<Int>> = IntSetSetting(this, default, key)

    /**
     * Delegate float property
     * @param default default float value
     * @param key custom storage key
     */
    protected fun floatPref(
            default: Float = 0f,
            key: String? = null
    ): StorageSetting<Float> = FloatSetting(this, default, key)

    /**
     * Delegate long property
     * @param default default long value
     * @param key custom storage key
     */
    protected fun longPref(
            default: Long = 0L,
            key: String? = null
    ): StorageSetting<Long> = LongSetting(this, default, key)

    /**
     * Delegate long property
     * @param default default long value
     * @param key custom storage key
     */
    protected fun longSetPref(
            default: Set<Long> = emptySet(),
            key: String? = null
    ): StorageSetting<Set<Long>> = LongSetSetting(this, default, key)

    /**
     * Delegate enum property
     * @param default default enum value
     * @param key custom storage key
     */
    protected inline fun <reified T : Enum<*>> enumPref(
            default: T,
            key: String? = null
    ): StorageSetting<T> = AnyIntSetting(this, default, key, EnumConverter(T::class.java))

    /**
     * Delegate any property
     * @param default default any value
     * @param key custom storage key
     */
    protected fun <T : Any> anyPref(
            converter: SettingsConverter<T, String>,
            default: T,
            key: String? = null
    ): StorageSetting<T> = AnyStringSetting(this, default, key, converter)
}