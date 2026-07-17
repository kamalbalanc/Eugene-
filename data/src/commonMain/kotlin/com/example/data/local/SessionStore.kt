package com.example.data.local

class SessionStore(private val platformSettings: PlatformSettings) {
    fun getActiveUid(): String? = platformSettings.getString("active_uid")
    fun setActiveUid(uid: String) = platformSettings.setString("active_uid", uid)
    fun clear() = platformSettings.remove("active_uid")
}
