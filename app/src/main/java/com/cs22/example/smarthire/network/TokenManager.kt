package com.cs22.example.smarthire.network

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("smarthire_prefs", Context.MODE_PRIVATE)

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit().apply {
            putString("ACCESS_TOKEN", accessToken)
            putString("REFRESH_TOKEN", refreshToken)
            apply()
        }
    }

    fun saveRole(role: String) {
        prefs.edit().putString("USER_ROLE", role).apply()
    }

    fun getAccessToken(): String? = prefs.getString("ACCESS_TOKEN", null)
    fun getRole(): String? = prefs.getString("USER_ROLE", null)

    fun clear() {
        prefs.edit().clear().apply()
    }
}
