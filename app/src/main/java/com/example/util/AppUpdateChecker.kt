package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
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

sealed class ApkDownloadState {
    data object Idle : ApkDownloadState()
    data class Downloading(
        val progressPercent: Int,
        val downloadedBytes: Long,
        val totalBytes: Long
    ) : ApkDownloadState()

    data class Downloaded(val apkFile: File) : ApkDownloadState()
    data class RequiresInstallPermission(val apkFile: File) : ApkDownloadState()
    data class Failed(
        val messageAr: String,
        val messageEn: String,
        val fallbackUrl: String
    ) : ApkDownloadState()
}

object AppUpdateChecker {

    const val UPDATE_URL = "https://github.com/charfeddine23/ShopyPelse.apk"
    private val REPO_SLUGS = listOf(
        "charfeddine23/ShopyPelse.apk",
        "charfeddine23/ShopyPelse"
    )
    private val RELEASE_API_ENDPOINTS = REPO_SLUGS.map {
        "https://api.github.com/repos/$it/releases/latest"
    }

    private const val MAX_APK_SIZE_BYTES = 200L * 1024L * 1024L

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    /**
     * Validates that update/download URLs strictly use HTTPS and belong to GitHub-owned hosts.
     * Prevents HTTP downgrade, arbitrary third-party redirects, or non-HTTPS scheme injection.
     */
    fun isTrustedUpdateUrl(url: String): Boolean {
        val trimmed = url.trim()
        if (!trimmed.startsWith("https://", ignoreCase = true)) return false
        val host = try {
            java.net.URI(trimmed).host?.lowercase() ?: return false
        } catch (_: Exception) {
            return false
        }
        return host == "github.com" ||
            host == "api.github.com" ||
            host.endsWith(".github.com") ||
            host == "githubusercontent.com" ||
            host.endsWith(".githubusercontent.com")
    }

    suspend fun checkForUpdate(currentVersionName: String): AppUpdateStatus = withContext(Dispatchers.IO) {
        // 1. Try GitHub Releases API to read version tag, release notes, and direct .apk asset URL
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
                            val publishedAt = json.optString("published_at", "").take(10)

                            val apkDownloadUrl = extractApkAssetUrl(json)
                                ?: resolveApkUrlFromGithub()
                                ?: UPDATE_URL

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
                // Try next endpoint
            }
        }

        // 2. Fallback: Check GitHub Tags API + resolve APK asset URL
        for (slug in REPO_SLUGS) {
            try {
                val tagReq = Request.Builder()
                    .url("https://api.github.com/repos/$slug/tags")
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "ShopyPulse-Android")
                    .build()

                client.newCall(tagReq).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyStr = response.body?.string()
                        if (!bodyStr.isNullOrBlank() && bodyStr.trim().startsWith("[")) {
                            val tagsArray = JSONArray(bodyStr)
                            if (tagsArray.length() > 0) {
                                val latestTag = tagsArray.getJSONObject(0).optString("name", "").trim()
                                if (latestTag.isNotBlank()) {
                                    val resolvedApk = resolveApkUrlFromGithub() ?: UPDATE_URL
                                    return@withContext if (isNewerVersion(latestTag, currentVersionName)) {
                                        AppUpdateStatus.UpdateAvailable(
                                            currentVersion = currentVersionName,
                                            latestVersion = latestTag,
                                            releaseNotes = "",
                                            downloadUrl = resolvedApk
                                        )
                                    } else {
                                        AppUpdateStatus.UpToDate(
                                            currentVersion = currentVersionName,
                                            directUrl = resolvedApk
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                // Continue to direct probe
            }
        }

        // 3. Fallback: Resolve direct APK binary from repo releases/contents or direct endpoint
        try {
            val resolvedApk = resolveApkUrlFromGithub()
            if (!resolvedApk.isNullOrBlank()) {
                return@withContext AppUpdateStatus.ReadyDirectLink(
                    currentVersion = currentVersionName,
                    downloadUrl = resolvedApk
                )
            }
            val headReq = Request.Builder()
                .url(UPDATE_URL)
                .head()
                .header("User-Agent", "ShopyPulse-Android")
                .build()

            client.newCall(headReq).execute().use { response ->
                val rawResolved = response.request.url.toString()
                val resolvedUrl = if (isTrustedUpdateUrl(rawResolved)) rawResolved else UPDATE_URL
                return@withContext AppUpdateStatus.ReadyDirectLink(
                    currentVersion = currentVersionName,
                    downloadUrl = resolvedUrl
                )
            }
        } catch (_: Exception) {
            return@withContext AppUpdateStatus.Error(
                messageAr = "تعذر الاتصال بخادم التحديثات. تحقق من اتصال الإنترنت وحاول مجدداً.",
                messageEn = "Unable to reach update server. Check your connection and try again.",
                fallbackUrl = UPDATE_URL
            )
        }
    }

    private fun extractApkAssetUrl(releaseJson: JSONObject): String? {
        val assets = releaseJson.optJSONArray("assets") ?: return null
        for (i in 0 until assets.length()) {
            val asset = assets.optJSONObject(i) ?: continue
            val assetName = asset.optString("name", "")
            val browserUrl = asset.optString("browser_download_url", "")
            if (assetName.endsWith(".apk", ignoreCase = true) && isTrustedUpdateUrl(browserUrl)) {
                return browserUrl
            }
        }
        return null
    }

    /**
     * Resolves a direct downloadable .apk binary URL from GitHub Releases or repo contents
     * so downloading happens strictly inside the app without ever opening a browser.
     */
    private fun resolveApkUrlFromGithub(): String? {
        // Try recent releases list
        for (slug in REPO_SLUGS) {
            try {
                val req = Request.Builder()
                    .url("https://api.github.com/repos/$slug/releases")
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "ShopyPulse-Android")
                    .build()
                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val body = resp.body?.string()
                        if (!body.isNullOrBlank() && body.trim().startsWith("[")) {
                            val arr = JSONArray(body)
                            for (i in 0 until arr.length()) {
                                val rel = arr.optJSONObject(i) ?: continue
                                val apkUrl = extractApkAssetUrl(rel)
                                if (!apkUrl.isNullOrBlank()) return apkUrl
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        // Try root repository contents for any committed .apk binary
        for (slug in REPO_SLUGS) {
            try {
                val req = Request.Builder()
                    .url("https://api.github.com/repos/$slug/contents")
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "ShopyPulse-Android")
                    .build()
                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val body = resp.body?.string()
                        if (!body.isNullOrBlank() && body.trim().startsWith("[")) {
                            val arr = JSONArray(body)
                            for (i in 0 until arr.length()) {
                                val item = arr.optJSONObject(i) ?: continue
                                val name = item.optString("name", "")
                                val rawUrl = item.optString("download_url", "")
                                if (name.endsWith(".apk", ignoreCase = true) && isTrustedUpdateUrl(rawUrl)) {
                                    return rawUrl
                                }
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }
        return null
    }

    /**
     * Streams the APK directly inside the app into cache/updates/ with real-time progress.
     */
    fun downloadApkInApp(
        context: Context,
        downloadUrl: String,
        versionLabel: String
    ): Flow<ApkDownloadState> = flow {
        emit(ApkDownloadState.Downloading(progressPercent = 0, downloadedBytes = 0L, totalBytes = -1L))

        val updatesDir = File(context.cacheDir, "updates").apply {
            if (!exists()) mkdirs()
            // Clean previous cached APKs
            listFiles()?.forEach { oldFile -> oldFile.delete() }
        }

        val safeVersion = versionLabel.replace(Regex("[^a-zA-Z0-9._-]"), "_").ifBlank { "latest" }
        val targetFile = File(updatesDir, "Shopy.Pulse.$safeVersion.apk")

        try {
            if (!targetFile.canonicalPath.startsWith(updatesDir.canonicalPath + File.separator)) {
                emit(
                    ApkDownloadState.Failed(
                        messageAr = "مسار حفظ ملف التحديث غير صالح.",
                        messageEn = "Invalid update destination path.",
                        fallbackUrl = UPDATE_URL
                    )
                )
                return@flow
            }

            // If URL is the repository homepage or not yet a direct binary link, resolve the real APK asset inside the app
            val effectiveUrl = if (
                downloadUrl.equals(UPDATE_URL, ignoreCase = true) ||
                downloadUrl.equals("$UPDATE_URL/", ignoreCase = true)
            ) {
                resolveApkUrlFromGithub() ?: downloadUrl
            } else {
                downloadUrl
            }

            if (!isTrustedUpdateUrl(effectiveUrl)) {
                emit(
                    ApkDownloadState.Failed(
                        messageAr = "رابط التحديث غير موثوق أو لا يستخدم HTTPS.",
                        messageEn = "Untrusted or non-HTTPS update URL rejected.",
                        fallbackUrl = UPDATE_URL
                    )
                )
                return@flow
            }

            val request = Request.Builder()
                .url(effectiveUrl)
                .header("User-Agent", "ShopyPulse-Android")
                .header("Accept", "application/vnd.android.package-archive, application/octet-stream, */*")
                .build()

            client.newCall(request).execute().use { response ->
                val finalUrl = response.request.url.toString()
                if (!isTrustedUpdateUrl(finalUrl)) {
                    emit(
                        ApkDownloadState.Failed(
                            messageAr = "تم حظر إعادة توجيه التحديث إلى مصدر غير موثوق.",
                            messageEn = "Blocked redirect to untrusted update host.",
                            fallbackUrl = UPDATE_URL
                        )
                    )
                    return@flow
                }

                if (!response.isSuccessful) {
                    emit(
                        ApkDownloadState.Failed(
                            messageAr = "تعذر تحميل ملف التحديث (رمز ${response.code})",
                            messageEn = "Failed to download update (HTTP ${response.code})",
                            fallbackUrl = effectiveUrl
                        )
                    )
                    return@flow
                }

                val body = response.body
                if (body == null) {
                    emit(
                        ApkDownloadState.Failed(
                            messageAr = "ملف التحديث فارغ أو غير متاح",
                            messageEn = "Update package is empty or unavailable",
                            fallbackUrl = effectiveUrl
                        )
                    )
                    return@flow
                }

                val contentType = response.header("Content-Type") ?: ""
                if (contentType.contains("text/html", ignoreCase = true)) {
                    emit(
                        ApkDownloadState.Failed(
                            messageAr = "لم يتم العثور على ملف APK مباشر في هذا الإصدار بعد.",
                            messageEn = "No direct APK release asset was found yet.",
                            fallbackUrl = effectiveUrl
                        )
                    )
                    return@flow
                }

                val totalBytes = body.contentLength()
                if (totalBytes > MAX_APK_SIZE_BYTES) {
                    emit(
                        ApkDownloadState.Failed(
                            messageAr = "حجم ملف التحديث يتجاوز الحد الآمن المسموح.",
                            messageEn = "Update file exceeds safe maximum size.",
                            fallbackUrl = effectiveUrl
                        )
                    )
                    return@flow
                }

                var downloadedBytes = 0L
                var lastEmittedPercent = -1
                var lastEmittedBytes = 0L
                val buffer = ByteArray(16 * 1024)
                var checkedMagic = false

                body.byteStream().use { input ->
                    FileOutputStream(targetFile).use { output ->
                        while (true) {
                            val read = input.read(buffer)
                            if (read == -1) break

                            if (downloadedBytes + read > MAX_APK_SIZE_BYTES) {
                                output.close()
                                targetFile.delete()
                                emit(
                                    ApkDownloadState.Failed(
                                        messageAr = "تجاوز التحميل الحد الأقصى المسموح به.",
                                        messageEn = "Download exceeded maximum allowed APK size.",
                                        fallbackUrl = effectiveUrl
                                    )
                                )
                                return@flow
                            }

                            // Validate APK/ZIP magic bytes ('P', 'K') on first chunk
                            if (!checkedMagic && read >= 2) {
                                checkedMagic = true
                                if (buffer[0] != 0x50.toByte() || buffer[1] != 0x4B.toByte()) {
                                    output.close()
                                    targetFile.delete()
                                    emit(
                                        ApkDownloadState.Failed(
                                            messageAr = "الملف المستلم ليس حزمة APK صالحة.",
                                            messageEn = "Downloaded file is not a valid APK package.",
                                            fallbackUrl = effectiveUrl
                                        )
                                    )
                                    return@flow
                                }
                            }

                            output.write(buffer, 0, read)
                            downloadedBytes += read
                            val percent = if (totalBytes > 0) {
                                ((downloadedBytes * 100L) / totalBytes).toInt().coerceIn(0, 100)
                            } else {
                                -1
                            }

                            val shouldEmit = if (percent >= 0) {
                                percent != lastEmittedPercent
                            } else {
                                downloadedBytes - lastEmittedBytes >= 131_072L
                            }

                            if (shouldEmit) {
                                lastEmittedPercent = percent
                                lastEmittedBytes = downloadedBytes
                                emit(
                                    ApkDownloadState.Downloading(
                                        progressPercent = percent,
                                        downloadedBytes = downloadedBytes,
                                        totalBytes = totalBytes
                                    )
                                )
                            }
                        }
                        output.flush()
                    }
                }

                // Validate archive packageName matches current applicationId before offering install
                val archiveInfo = context.packageManager.getPackageArchiveInfo(targetFile.absolutePath, 0)
                if (archiveInfo == null || archiveInfo.packageName != context.packageName) {
                    targetFile.delete()
                    emit(
                        ApkDownloadState.Failed(
                            messageAr = "حزمة التحديث لا تطابق هوية التطبيق وتم رفضها أمنياً.",
                            messageEn = "Downloaded APK does not match app package ID.",
                            fallbackUrl = effectiveUrl
                        )
                    )
                    return@flow
                }

                emit(
                    ApkDownloadState.Downloading(
                        progressPercent = 100,
                        downloadedBytes = downloadedBytes,
                        totalBytes = if (totalBytes > 0) totalBytes else downloadedBytes
                    )
                )
                emit(ApkDownloadState.Downloaded(targetFile))
            }
        } catch (e: Exception) {
            if (targetFile.exists()) targetFile.delete()
            emit(
                ApkDownloadState.Failed(
                    messageAr = "تعذر إكمال التحميل: ${e.localizedMessage ?: "خطأ في الشبكة"}",
                    messageEn = "Download failed: ${e.localizedMessage ?: "Network error"}",
                    fallbackUrl = if (isTrustedUpdateUrl(downloadUrl)) downloadUrl else UPDATE_URL
                )
            )
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Triggers Android's package installer for the downloaded APK using FileProvider.
     * Returns true if installer intent was launched, false if user needs to grant install permission first.
     */
    fun triggerApkInstall(context: Context, apkFile: File): Boolean {
        val updatesDir = File(context.cacheDir, "updates")
        val isInsideUpdatesDir = try {
            apkFile.exists() &&
                apkFile.isFile &&
                apkFile.canonicalPath.startsWith(updatesDir.canonicalPath + File.separator)
        } catch (_: Exception) {
            false
        }
        if (!isInsideUpdatesDir) return false

        val archiveInfo = context.packageManager.getPackageArchiveInfo(apkFile.absolutePath, 0)
        if (archiveInfo == null || archiveInfo.packageName != context.packageName) {
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
            val permIntent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(permIntent)
            } catch (_: Exception) {}
            return false
        }

        return try {
            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(installIntent)
            true
        } catch (_: Exception) {
            false
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
