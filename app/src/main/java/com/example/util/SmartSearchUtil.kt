package com.example.util

import java.text.Normalizer
import kotlin.math.abs

/**
 * Intelligent bilingual (Arabic / English / French) search and relevance-ranking engine.
 * Ranks exact, starts-with, word-prefix, substring, multi-token, and typo-tolerant fuzzy matches
 * strictly in descending order of relevance so the closest products always appear first.
 */
object SmartSearchUtil {

    private val DIACRITICS_REGEX = Regex("\\p{InCombiningDiacriticalMarks}+")
    private val ARABIC_DIACRITICS_REGEX = Regex("[\u064B-\u065F\u0670\u0640]")
    private val TOKEN_SPLIT_REGEX = Regex("[\\s\\-_/,().+]+")

    fun normalizeText(input: String): String {
        if (input.isBlank()) return ""
        val trimmed = input.trim().lowercase()
        val noArabicMarks = ARABIC_DIACRITICS_REGEX.replace(trimmed, "")
        val arabicUnified = buildString(noArabicMarks.length) {
            for (ch in noArabicMarks) {
                when (ch) {
                    'أ', 'إ', 'آ', 'ٱ' -> append('ا')
                    'ة' -> append('ه')
                    'ى' -> append('ي')
                    'ؤ' -> append('و')
                    'ئ' -> append('ي')
                    else -> append(ch)
                }
            }
        }
        val nfd = Normalizer.normalize(arabicUnified, Normalizer.Form.NFD)
        return DIACRITICS_REGEX.replace(nfd, "").trim()
    }

    /**
     * Filters and sorts [ProductGroup] items by search relevance, also ordering each group's
     * variants so the best-matching variant appears first when searching.
     */
    fun searchAndRankGroups(
        groups: List<ProductGroup>,
        query: String,
        categoryFilter: String = "All"
    ): List<ProductGroup> {
        val categoryFiltered = if (categoryFilter == "All" || categoryFilter.isBlank()) {
            groups
        } else {
            groups.filter { it.category == categoryFilter }
        }

        val qNorm = normalizeText(query)
        if (qNorm.isEmpty()) return categoryFiltered

        val qTokens = qNorm.split(TOKEN_SPLIT_REGEX).filter { it.isNotEmpty() }

        val scored = ArrayList<Pair<Int, ProductGroup>>(categoryFiltered.size)
        for (group in categoryFiltered) {
            val groupScore = scoreGroupHeader(group, qNorm, qTokens)

            var bestVariantScore = 0
            var matchingVariantCount = 0
            val scoredVariants = ArrayList<Pair<Int, ProductVariant>>(group.variants.size)
            for (variant in group.variants) {
                val vScore = scoreVariant(variant, qNorm, qTokens)
                if (vScore > 0) {
                    matchingVariantCount++
                    if (vScore > bestVariantScore) bestVariantScore = vScore
                }
                scoredVariants.add(vScore to variant)
            }

            val totalScore = maxOf(groupScore, bestVariantScore) +
                (if (groupScore > 0 && bestVariantScore > 0) 300 else 0) +
                (matchingVariantCount.coerceAtMost(10) * 15)

            if (totalScore > 0) {
                val orderedVariants = if (bestVariantScore > 0) {
                    scoredVariants
                        .sortedWith(compareByDescending<Pair<Int, ProductVariant>> { it.first }
                            .thenBy { it.second.priceDzd })
                        .map { it.second }
                } else {
                    group.variants
                }
                scored.add(totalScore to group.copy(variants = orderedVariants))
            }
        }

        return scored
            .sortedWith(compareByDescending<Pair<Int, ProductGroup>> { it.first }
                .thenBy { it.second.minPrice })
            .map { it.second }
    }

    /**
     * Ranks variants inside a [ProductGroup] for a query. Returns matching variants ordered
     * from highest relevance to lowest; falls back to original list if the match came from the group title.
     */
    fun rankVariants(variants: List<ProductVariant>, query: String): List<ProductVariant> {
        val qNorm = normalizeText(query)
        if (qNorm.isEmpty()) return variants
        val qTokens = qNorm.split(TOKEN_SPLIT_REGEX).filter { it.isNotEmpty() }

        val matching = variants.mapNotNull { variant ->
            val score = scoreVariant(variant, qNorm, qTokens)
            if (score > 0) score to variant else null
        }
        return if (matching.isEmpty()) {
            variants
        } else {
            matching.sortedWith(
                compareByDescending<Pair<Int, ProductVariant>> { it.first }
                    .thenBy { it.second.priceDzd }
            ).map { it.second }
        }
    }

    fun scoreVariant(variant: ProductVariant, rawQuery: String): Int {
        val qNorm = normalizeText(rawQuery)
        if (qNorm.isEmpty()) return 0
        val qTokens = qNorm.split(TOKEN_SPLIT_REGEX).filter { it.isNotEmpty() }
        return scoreVariant(variant, qNorm, qTokens)
    }

    private fun scoreGroupHeader(
        group: ProductGroup,
        qNorm: String,
        qTokens: List<String>
    ): Int {
        val catAr = LocalizationUtil.getCategoryLabel(group.category, isArabic = true)
        val catEn = LocalizationUtil.getCategoryLabel(group.category, isArabic = false)
        val nameScore = maxOf(
            scoreField(normalizeText(group.nameAr), qNorm, qTokens),
            scoreField(normalizeText(group.nameEn), qNorm, qTokens)
        )
        val categoryScore = maxOf(
            scoreField(normalizeText(catAr), qNorm, qTokens),
            scoreField(normalizeText(catEn), qNorm, qTokens),
            scoreField(normalizeText(group.category), qNorm, qTokens)
        ) / 2
        return maxOf(nameScore, categoryScore)
    }

    private fun scoreVariant(
        variant: ProductVariant,
        qNorm: String,
        qTokens: List<String>
    ): Int {
        val barcodeNorm = normalizeText(variant.barcode)
        if (barcodeNorm.isNotEmpty()) {
            if (barcodeNorm == qNorm) return 10_000
            if (barcodeNorm.startsWith(qNorm) && qNorm.length >= 3) return 6_500
            if (barcodeNorm.contains(qNorm) && qNorm.length >= 4) return 4_000
        }

        val arNorm = normalizeText(variant.itemNameAr)
        val enNorm = normalizeText(variant.itemName)
        val combinedNorm = "$arNorm $enNorm".trim()

        val directBest = maxOf(
            scoreField(arNorm, qNorm, qTokens),
            scoreField(enNorm, qNorm, qTokens)
        )
        if (directBest > 0) return directBest

        // Cross-field multi-token check (e.g. Arabic name + Latin unit or brand)
        if (qTokens.size > 1) {
            return scoreMultiTokenAcross(combinedNorm, qTokens)
        }
        return 0
    }

    internal fun scoreField(
        fieldNorm: String,
        qNorm: String,
        qTokens: List<String>
    ): Int {
        if (fieldNorm.isEmpty() || qNorm.isEmpty()) return 0

        if (fieldNorm == qNorm) return 5_500
        if (fieldNorm.startsWith(qNorm)) {
            val tightnessBonus = (200 - abs(fieldNorm.length - qNorm.length)).coerceAtLeast(0)
            return 3_400 + tightnessBonus
        }

        val fieldWords = fieldNorm.split(TOKEN_SPLIT_REGEX).filter { it.isNotEmpty() }

        // Exact word match inside product name (e.g. "Candia" in "Candia Choco 1L")
        if (fieldWords.any { it == qNorm }) {
            return 3_100
        }

        // Word-boundary prefix match (e.g. "cand" matching word "Candia")
        if (fieldWords.any { it.startsWith(qNorm) }) {
            return 2_500
        }

        // Direct substring match
        val subIdx = fieldNorm.indexOf(qNorm)
        if (subIdx >= 0) {
            val earlyBonus = (150 - subIdx * 3).coerceAtLeast(0)
            return 1_600 + earlyBonus
        }

        // Multi-word token matching (all words present anywhere in field, e.g. "حليب 1لتر" or "oil 5l")
        if (qTokens.size > 1) {
            val multiScore = scoreMultiTokenAcross(fieldNorm, qTokens)
            if (multiScore > 0) return multiScore
        }

        // Fuzzy typo tolerance for queries of length >= 3
        if (qNorm.length >= 3) {
            var bestFuzzy = 0
            // Check whole field similarity
            bestFuzzy = maxOf(bestFuzzy, scoreFuzzyWord(qNorm, fieldNorm))

            // Check each individual word (plus 2-word joined forms for spacing typos like "زيتمائدة")
            for (word in fieldWords) {
                bestFuzzy = maxOf(bestFuzzy, scoreFuzzyWord(qNorm, word))
                if (qNorm.length >= 5 && word.length >= qNorm.length) {
                    bestFuzzy = maxOf(bestFuzzy, scoreFuzzyWord(qNorm, word.take(qNorm.length)))
                }
            }
            return bestFuzzy
        }

        return 0
    }

    private fun scoreMultiTokenAcross(targetNorm: String, qTokens: List<String>): Int {
        if (targetNorm.isEmpty() || qTokens.isEmpty()) return 0
        val targetWords = targetNorm.split(TOKEN_SPLIT_REGEX).filter { it.isNotEmpty() }
        var matchedCount = 0
        var tokenQualitySum = 0

        for (token in qTokens) {
            val tokenScore = when {
                targetWords.any { it == token } -> 320
                targetWords.any { it.startsWith(token) } -> 250
                targetNorm.contains(token) -> 180
                token.length >= 3 && targetWords.any { scoreFuzzyWord(token, it) > 0 } -> 110
                else -> 0
            }
            if (tokenScore > 0) {
                matchedCount++
                tokenQualitySum += tokenScore
            }
        }

        return when {
            matchedCount == qTokens.size -> 1_300 + tokenQualitySum
            qTokens.size >= 3 && matchedCount >= qTokens.size - 1 -> 550 + tokenQualitySum
            else -> 0
        }
    }

    private fun scoreFuzzyWord(queryWord: String, candidateWord: String): Int {
        if (queryWord.length < 3 || candidateWord.length < 3) return 0
        val maxEdits = if (maxOf(queryWord.length, candidateWord.length) <= 4) 1 else 2
        if (abs(queryWord.length - candidateWord.length) > maxEdits) return 0

        val dist = damerauLevenshteinDistance(queryWord, candidateWord)
        if (dist > maxEdits) return 0

        val maxLen = maxOf(queryWord.length, candidateWord.length).toFloat()
        val similarity = 1f - (dist / maxLen)
        if (similarity < 0.60f) return 0

        val firstCharBonus = if (queryWord.first() == candidateWord.first()) 120 else 0
        return when (dist) {
            0 -> 2_800
            1 -> 820 + firstCharBonus + (similarity * 100).toInt()
            else -> 460 + firstCharBonus + (similarity * 100).toInt()
        }
    }

    /**
     * Optimal String Alignment (restricted Damerau-Levenshtein) distance treating adjacent character
     * transpositions (e.g., "canida" -> "candia") as a single edit.
     */
    internal fun damerauLevenshteinDistance(a: String, b: String): Int {
        val lenA = a.length
        val lenB = b.length
        if (lenA == 0) return lenB
        if (lenB == 0) return lenA

        val dp = Array(lenA + 1) { IntArray(lenB + 1) }
        for (i in 0..lenA) dp[i][0] = i
        for (j in 0..lenB) dp[0][j] = j

        for (i in 1..lenA) {
            for (j in 1..lenB) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                var best = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
                if (i > 1 && j > 1 && a[i - 1] == b[j - 2] && a[i - 2] == b[j - 1]) {
                    best = minOf(best, dp[i - 2][j - 2] + 1)
                }
                dp[i][j] = best
            }
        }
        return dp[lenA][lenB]
    }
}
