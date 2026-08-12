package com.skinlens.domain

import android.graphics.Bitmap
import android.graphics.Color
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.tasks.await

class PhotoQualityChecker {

    sealed class QualityResult {
        object Good : QualityResult()
        data class Poor(val reason: String) : QualityResult()
    }

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
    )

    suspend fun checkQuality(bitmap: Bitmap): QualityResult {
        val image = InputImage.fromBitmap(bitmap, 0)

        // 1. Check Face Visibility & Count
        val faces = try {
            detector.process(image).await()
        } catch (e: Exception) {
            return QualityResult.Poor("Could not process image for face detection.")
        }

        if (faces.isEmpty()) {
            return QualityResult.Poor("No face detected. Please ensure your face is clearly visible.")
        }
        if (faces.size > 1) {
            return QualityResult.Poor("Multiple faces detected. Please take a photo of just yourself.")
        }

        val face = faces.first()
        val boundingBox = face.boundingBox

        // 2. Centered Check (roughly)
        val centerX = boundingBox.centerX()
        val imageCenterX = bitmap.width / 2
        val deviationX = Math.abs(centerX - imageCenterX)
        if (deviationX > bitmap.width * 0.3) {
            return QualityResult.Poor("Face is not centered. Please center your face in the frame.")
        }

        // 3. Brightness/Lighting Check
        // Sample a few points to determine overall brightness
        var totalBrightness = 0.0
        var samples = 0
        val sampleStep = 10

        for (x in 0 until bitmap.width step sampleStep) {
            for (y in 0 until bitmap.height step sampleStep) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                val luminance = 0.299 * r + 0.587 * g + 0.114 * b
                totalBrightness += luminance
                samples++
            }
        }
        val avgBrightness = totalBrightness / samples

        if (avgBrightness < 40) {
             return QualityResult.Poor("Image is too dark. Please take another photo in natural, even lighting.")
        }
        if (avgBrightness > 220) {
             return QualityResult.Poor("Image is too bright. Please avoid harsh direct lighting.")
        }

        return QualityResult.Good
    }
}
