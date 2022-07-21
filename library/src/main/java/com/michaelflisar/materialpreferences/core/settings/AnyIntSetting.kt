package com.michaelflisar.materialpreferences.core.settings

import com.michaelflisar.materialpreferences.core.SettingsConverter
import com.michaelflisar.materialpreferences.core.SettingsModel
import com.michaelflisar.materialpreferences.core.initialisation.SettingSetup
import com.michaelflisar.materialpreferences.core.interfaces.Storage
import com.michaelflisar.materialpreferences.core.interfaces.StorageSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.reflect.KProperty

class AnyIntSetting<T : Any>(
        private val model: SettingsModel,
        override val defaultValue: T,
        override val customKey: String?,
        val converter: SettingsConverter<T, Int>,
        override val cache: Boolean
) : AbstractSetting<T>() {

    private var name: String? = null
    override val key: String by lazy { customKey ?: name!! }

    override val storage: Storage
        get() = model.storage

    override fun createFlow() = model.storage.getInt(key, converter.to(defaultValue)).map { converter.from(it) }

    override suspend fun persistValue(value: T) {
        model.storage.setInt(key, converter.to(value))
    }

    private fun init(name: String) {
        if (this.name == null) {
            this.name = name
            model.internalProperties[key] = this
        }
    }

    /* Delegate */
    override fun getValue(
            thisRef: SettingsModel,
            property: KProperty<*>
    ): StorageSetting<T> {
        init(property.name)
        return this
    }
}