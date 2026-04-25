package com.example.lingora_fe.user.scan.data.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import kotlinx.coroutines.tasks.await

data class DetectedObject(
    val label: String,
    val confidence: Float,
    val boundingBox: Rect?
)

class ObjectDetectorHelper(private val context: Context) {

    private val localModel = LocalModel.Builder()
        .setAssetFilePath("object_labeler.tflite")
        .build()

    private val options = CustomObjectDetectorOptions.Builder(localModel)
        .setDetectorMode(CustomObjectDetectorOptions.SINGLE_IMAGE_MODE)
        .enableMultipleObjects()
        .enableClassification()
        .setClassificationConfidenceThreshold(0.5f)
        .setMaxPerObjectLabelCount(1)
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
            .filter { it.confidence > 0.5f }
    }
}