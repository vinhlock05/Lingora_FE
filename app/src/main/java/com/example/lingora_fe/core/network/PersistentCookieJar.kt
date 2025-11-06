package com.example.lingora_fe.core.network

import android.content.SharedPreferences
import android.util.Log
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PersistentCookieJar - Lưu cookies vào SharedPreferences để persist giữa các lần mở app
 * Tự động lưu và load cookies, đặc biệt là refreshToken cookie từ backend
 */
@Singleton
class PersistentCookieJar @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : CookieJar {

    companion object {
        private const val TAG = "PersistentCookieJar"
        private const val PREF_COOKIES = "http_cookies"
        private const val COOKIE_SEPARATOR = "|"
        private const val COOKIE_PAIR_SEPARATOR = "::"
    }

    // In-memory cache để tránh đọc SharedPreferences mỗi lần request
    private val cookieCache = mutableMapOf<String, List<Cookie>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        Log.d(TAG, "Saving ${cookies.size} cookies for ${url.host}")
        
        // Lưu vào cache
        cookieCache[url.host] = cookies
        
        // Lưu vào SharedPreferences
        val editor = sharedPreferences.edit()
        val cookieString = cookies.joinToString(COOKIE_SEPARATOR) { cookie ->
            // Lưu thông tin cookie, hostOnly được xác định từ domain (nếu domain bắt đầu bằng . thì không phải hostOnly)
            val hostOnly = !cookie.domain.startsWith(".")
            "${cookie.name}${COOKIE_PAIR_SEPARATOR}${cookie.value}${COOKIE_PAIR_SEPARATOR}${cookie.domain}${COOKIE_PAIR_SEPARATOR}${cookie.path}${COOKIE_PAIR_SEPARATOR}${cookie.expiresAt}${COOKIE_PAIR_SEPARATOR}${cookie.secure}${COOKIE_PAIR_SEPARATOR}${cookie.httpOnly}${COOKIE_PAIR_SEPARATOR}${hostOnly}${COOKIE_PAIR_SEPARATOR}${cookie.persistent}"
        }
        editor.putString("${PREF_COOKIES}_${url.host}", cookieString)
        editor.apply()
        
        // Log refreshToken cookie nếu có
        cookies.find { it.name == "refreshToken" }?.let {
            Log.d(TAG, "✅ Saved refreshToken cookie")
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        // Kiểm tra cache trước
        cookieCache[url.host]?.let { cachedCookies ->
            // Kiểm tra xem cookies còn valid không (chưa expire)
            val validCookies = cachedCookies.filter { cookie ->
                cookie.expiresAt == Long.MAX_VALUE || cookie.expiresAt > System.currentTimeMillis()
            }
            if (validCookies.isNotEmpty()) {
                Log.d(TAG, "Loading ${validCookies.size} cookies from cache for ${url.host}")
                return validCookies
            }
        }
        
        // Load từ SharedPreferences
        val cookieString = sharedPreferences.getString("${PREF_COOKIES}_${url.host}", null)
        if (cookieString.isNullOrBlank()) {
            Log.d(TAG, "No cookies found for ${url.host}")
            return emptyList()
        }
        
        val cookies = cookieString.split(COOKIE_SEPARATOR).mapNotNull { cookieData ->
            try {
                val parts = cookieData.split(COOKIE_PAIR_SEPARATOR)
                if (parts.size >= 9) {
                    val name = parts[0]
                    val value = parts[1]
                    val domain = parts[2]
                    val path = parts[3]
                    val expiresAt = parts[4].toLong()
                    val secure = parts[5].toBoolean()
                    val httpOnly = parts[6].toBoolean()
                    val hostOnly = parts[7].toBoolean()
                    val persistent = parts[8].toBoolean()
                    
                    // Chỉ trả về cookie nếu chưa expire
                    if (expiresAt == Long.MAX_VALUE || expiresAt > System.currentTimeMillis()) {
                        // OkHttp tự động xác định hostOnly từ domain:
                        // - Nếu domain không bắt đầu bằng "." → hostOnly = true
                        // - Nếu domain bắt đầu bằng "." → hostOnly = false
                        // Vì vậy chỉ cần set domain đúng là được
                        val builder = Cookie.Builder()
                            .name(name)
                            .value(value)
                            .path(path)
                            .expiresAt(expiresAt)
                        
                        // Set domain: nếu hostOnly = true, không thêm leading dot
                        if (hostOnly) {
                            // Host-only cookie: domain không có leading dot
                            builder.domain(domain.removePrefix("."))
                        } else {
                            // Domain cookie: domain có leading dot
                            builder.domain(if (domain.startsWith(".")) domain else ".$domain")
                        }
                        
                        builder.apply {
                            if (secure) secure()
                            if (httpOnly) httpOnly()
                        }.build()
                    } else {
                        null // Cookie đã expire
                    }
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing cookie: ${e.message}")
                null
            }
        }
        
        // Cập nhật cache
        cookieCache[url.host] = cookies
        
        Log.d(TAG, "Loaded ${cookies.size} cookies from SharedPreferences for ${url.host}")
        
        // Log refreshToken cookie nếu có
        cookies.find { it.name == "refreshToken" }?.let {
            Log.d(TAG, "✅ Loaded refreshToken cookie")
        }
        
        return cookies
    }

    /**
     * Xóa tất cả cookies (dùng khi logout)
     */
    fun clearCookies() {
        Log.d(TAG, "Clearing all cookies")
        cookieCache.clear()
        sharedPreferences.edit().clear().apply()
    }

    /**
     * Xóa cookies cho một domain cụ thể
     */
    fun clearCookiesForHost(host: String) {
        Log.d(TAG, "Clearing cookies for host: $host")
        cookieCache.remove(host)
        sharedPreferences.edit().remove("${PREF_COOKIES}_$host").apply()
    }
}

