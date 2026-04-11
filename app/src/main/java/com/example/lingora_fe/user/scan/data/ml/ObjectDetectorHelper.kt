package com.example.lingora_fe.user.scan.data.ml

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlinx.coroutines.tasks.await

data class DetectedObject(
    val label: String,
    val confidence: Float,
    val boundingBox: Rect?
)

class ObjectDetectorHelper {

    private val options = ObjectDetectorOptions.Builder()
        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
        .enableMultipleObjects()
        .enableClassification()
        .build()

    private val detector = ObjectDetection.getClient(options)

    suspend fun detect(bitmap: Bitmap): List<DetectedObject> {
        val image = InputImage.fromBitmap(bitmap, 0)
        val result = detector.process(image).await()

        return result.mapNotNull { obj ->
            val label = obj.labels.firstOrNull()
            label?.let {
                DetectedObject(
                    label = it.text,
                    confidence = it.confidence,
                    boundingBox = obj.boundingBox
                )
            }
        }
            .filter { it.confidence > 0.6f }
            .distinctBy { it.label }
    }
}