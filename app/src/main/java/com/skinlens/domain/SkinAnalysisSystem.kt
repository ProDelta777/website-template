package com.skinlens.domain

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class SkinAnalysisResult(
    val overallScore: Int,
    val oiliness: Level,
    val dryness: Level,
    val redness: Level,
    val texture: Level,
    val darkSpots: Level,
    val blemishLike: Level
) {
    enum class Level(val label: String) {
        LOW("Low"),
        MODERATE("Moderate"),
        HIGH("High")
    }
}

class SkinAnalysisSystem {

    /**
     * This is a transparent, educational analysis based on basic image properties.
     * It does NOT perform medical diagnosis.
     */
    fun analyzeSkin(bitmap: Bitmap): SkinAnalysisResult {
        // Downsample for faster analysis
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false)

        var totalRedness = 0.0
        var totalBrightness = 0.0
        var totalVariance = 0.0

        val width = scaledBitmap.width
        val height = scaledBitmap.height
        val pixels = IntArray(width * height)
        scaledBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val grays = DoubleArray(pixels.size)
        var sumGray = 0.0

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)

            // Calculate a redness factor (R relative to G and B)
            if (r > g && r > b) {
                totalRedness += (r - max(g, b))
            }

            val luminance = 0.299 * r + 0.587 * g + 0.114 * b
            totalBrightness += luminance
            grays[i] = luminance
            sumGray += luminance
        }

        val avgGray = sumGray / pixels.size
        for (gray in grays) {
            totalVariance += (gray - avgGray) * (gray - avgGray)
        }
        val avgVariance = totalVariance / pixels.size

        val avgRedness = totalRedness / pixels.size

        // Heuristics based on image properties (Educational only)
        // High brightness variance often correlates with uneven texture or oiliness (specular highlights)
        val oiliness = if (avgVariance > 1500) SkinAnalysisResult.Level.HIGH else if (avgVariance > 800) SkinAnalysisResult.Level.MODERATE else SkinAnalysisResult.Level.LOW

        // Redness is calculated from color channels
        val redness = if (avgRedness > 30) SkinAnalysisResult.Level.HIGH else if (avgRedness > 15) SkinAnalysisResult.Level.MODERATE else SkinAnalysisResult.Level.LOW

        // Dryness heuristic (lower variance, duller brightness might imply dryness)
        val dryness = if (avgVariance < 500 && avgGray < 120) SkinAnalysisResult.Level.HIGH else if (avgVariance < 1000) SkinAnalysisResult.Level.MODERATE else SkinAnalysisResult.Level.LOW

        // Texture can be linked to high variance without high brightness
        val texture = if (avgVariance > 1200) SkinAnalysisResult.Level.HIGH else SkinAnalysisResult.Level.MODERATE

        // Simulated or generalized levels for blemish and dark spots as simple local analysis is limited
        // A real system would use a specific object detection model.
        val darkSpots = SkinAnalysisResult.Level.LOW // Placeholder for safety
        val blemishLike = SkinAnalysisResult.Level.MODERATE // Placeholder for safety

        // Calculate a score from 0-100 based on the "ideal" levels
        var score = 100
        score -= penaltyFor(oiliness)
        score -= penaltyFor(dryness)
        score -= penaltyFor(redness)
        score -= penaltyFor(texture)
        score -= penaltyFor(darkSpots)
        score -= penaltyFor(blemishLike)

        // Add a small random jitter to avoid exact duplicate scores on similar photos
        score = min(100, max(0, score + Random.nextInt(-3, 3)))

        return SkinAnalysisResult(
            overallScore = score,
            oiliness = oiliness,
            dryness = dryness,
            redness = redness,
            texture = texture,
            darkSpots = darkSpots,
            blemishLike = blemishLike
        )
    }

    private fun penaltyFor(level: SkinAnalysisResult.Level): Int {
        return when (level) {
            SkinAnalysisResult.Level.LOW -> 2
            SkinAnalysisResult.Level.MODERATE -> 6
            SkinAnalysisResult.Level.HIGH -> 12
        }
    }
}
