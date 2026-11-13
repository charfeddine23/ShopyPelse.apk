package com.example.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class AppUpdateStatus {
    data object Idle : AppUpdateStatus()
    data object Checking : AppUpdateStatus()
    data class UpToDate(
        val currentVersion: String,
        val directUrl: String = AppUpdateChecker.UPDATE_URL
    ) : AppUpdateStatus()

    data class UpdateAvailable(
        val currentVersion: String,
        val latestVersion: String,
        val releaseNotes: String,
        val downloadUrl: String,
        val publishedAt: String = ""
    ) : AppUpdateStatus()

    data class ReadyDirectLink(
        val currentVersion: String,
        val downloadUrl: String = AppUpdateChecker.UPDATE_URL
    ) : AppUpdateStatus()

    data class Error(
        val messageAr: String,
        val messageEn: String,
        val fallbackUrl: String = AppUpdateChecker.UPDATE_URL
    ) : AppUpdateStatus()
}

object AppUpdateChecker {

    const val UPDATE_URL = "https://github.com/charfeddine23/ShopyPelse.apk"
    private val RELEASE_API_ENDPOINTS = listOf(
        "https://api.github.com/repos/charfeddine23/ShopyPelse.apk/releases/latest",
        "https://api.github.com/repos/charfeddine23/ShopyPelse/releases/latest"
    )

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    suspend fun checkForUpdate(currentVersionName: String): AppUpdateStatus = withContext(Dispatchers.IO) {
        // 1. Try GitHub Releases API to read version tag, release notes, and APK asset URL
        for (apiUrl in RELEASE_API_ENDPOINTS) {
            try {
                val request = Request.Builder()
                    .url(apiUrl)
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "ShopyPulse-Android")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyStr = response.body?.string()
                        if (!bodyStr.isNullOrBlank()) {
                            val json = JSONObject(bodyStr)
                            val tagName = json.optString("tag_name", "").trim()
                            val releaseName = json.optString("name", tagName).trim()
                            val releaseNotes = json.optString("body", "").trim()
                            val htmlUrl = json.optString("html_url", UPDATE_URL).ifBlank { UPDATE_URL }
                            val publishedAt = json.optString("published_at", "").take(10)

                            // Look for an .apk asset in the release, otherwise use UPDATE_URL / html_url
                            var apkDownloadUrl = UPDATE_URL
                            val assets = json.optJSONArray("assets")
                            if (assets != null) {
                                for (i in 0 until assets.length()) {
                                    val asset = assets.getJSONObject(i)
                                    val assetName = asset.optString("name", "")
                                    val browserUrl = asset.optString("browser_download_url", "")
                                    if (assetName.endsWith(".apk", ignoreCase = true) && browserUrl.isNotBlank()) {
                                        apkDownloadUrl = browserUrl
                                        break
                                    }
                                }
                            }
                            if (apkDownloadUrl == UPDATE_URL && htmlUrl.isNotBlank()) {
                                // Prefer direct APK URL if explicitly configured, or release page if no APK asset
                                apkDownloadUrl = UPDATE_URL
                            }

                            val remoteVersion = tagName.ifBlank { releaseName }.ifBlank { currentVersionName }
                            return@withContext if (isNewerVersion(remoteVersion, currentVersionName)) {
                                AppUpdateStatus.UpdateAvailable(
                                    currentVersion = currentVersionName,
                                    latestVersion = remoteVersion,
                                    releaseNotes = releaseNotes,
                                    downloadUrl = apkDownloadUrl,
                                    publishedAt = publishedAt
                                )
                            } else {
                                AppUpdateStatus.UpToDate(
                                    currentVersion = currentVersionName,
                                    directUrl = apkDownloadUrl
                                )
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                // Try next endpoint or direct link probe
            }
        }

        // 2. Fallback: Probe the direct update URL via HEAD request to confirm availability
        try {
            val headReq = Request.Builder()
                .url(UPDATE_URL)
                .head()
                .header("User-Agent", "ShopyPulse-Android")
                .build()

            client.newCall(headReq).execute().use { response ->
                return@withContext if (response.isSuccessful || response.code in 300..399) {
                    val resolvedUrl = response.request.url.toString().ifBlank { UPDATE_URL }
                    AppUpdateStatus.ReadyDirectLink(
                        currentVersion = currentVersionName,
                        downloadUrl = resolvedUrl
                    )
                } else {
                    AppUpdateStatus.ReadyDirectLink(
                        currentVersion = currentVersionName,
                        downloadUrl = UPDATE_URL
                    )
                }
            }
        } catch (e: Exception) {
            return@withContext AppUpdateStatus.Error(
                messageAr = "تعذر الاتصال بخادم التحديثات. يمكنك فتح الرابط مباشرة عبر المتصفح.",
                messageEn = "Unable to reach update server. You can still open the update link in your browser.",
                fallbackUrl = UPDATE_URL
            )
        }
    }

    /**
     * Compares semantic versions like "v1.2.0" vs "1.0" or "1.0.5" vs "1.0.4".
     */
    fun isNewerVersion(remote: String, current: String): Boolean {
        val remoteParts = parseVersionNumbers(remote)
        val currentParts = parseVersionNumbers(current)
        if (remoteParts.isEmpty()) return false

        val maxLen = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }

    private fun parseVersionNumbers(version: String): List<Int> {
        val cleaned = version.trim()
            .removePrefix("v")
            .removePrefix("V")
            .substringBefore("-")
            .substringBefore(" ")
        return cleaned.split(".")
            .mapNotNull { part -> part.filter { it.isDigit() }.toIntOrNull() }
    }
}
