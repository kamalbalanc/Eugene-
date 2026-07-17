package com.example.data.local

class PlatformSettings {
    private val memoryStore = mutableMapOf<String, Any>()
    private var delegate: PlatformSettingsDelegate? = null

    fun setDelegate(delegate: PlatformSettingsDelegate) {
        this.delegate = delegate
    }

    fun getBoolean(key: String, defaultValue: Boolean? = null): Boolean? {
        delegate?.let { return it.getBoolean(key, defaultValue) }
        return memoryStore[key] as? Boolean ?: defaultValue
    }

    fun setBoolean(key: String, value: Boolean) {
        delegate?.let { it.setBoolean(key, value); return }
        memoryStore[key] = value
    }

    fun getString(key: String, defaultValue: String? = null): String? {
        delegate?.let { return it.getString(key, defaultValue) }
        return memoryStore[key] as? String ?: defaultValue
    }

    fun setString(key: String, value: String) {
        delegate?.let { it.setString(key, value); return }
        memoryStore[key] = value
    }

    fun remove(key: String) {
        delegate?.let { it.remove(key); return }
        memoryStore.remove(key)
    }
}

interface PlatformSettingsDelegate {
    fun getBoolean(key: String, defaultValue: Boolean?): Boolean?
    fun setBoolean(key: String, value: Boolean)
    fun getString(key: String, defaultValue: String?): String?
    fun setString(key: String, value: String)
    fun remove(key: String)
}
