package com.example.lingora_fe.user.scan.data.repository

import android.graphics.Bitmap
import com.example.lingora_fe.user.scan.data.ml.DetectedObject
import com.example.lingora_fe.user.scan.data.ml.ObjectDetectorHelper
import com.example.lingora_fe.user.scan.domain.repository.ScanRepository

import javax.inject.Inject

class ScanRepositoryImpl @Inject constructor(
    private val detector: ObjectDetectorHelper
) : ScanRepository {

    override suspend fun detect(bitmap: Bitmap): List<DetectedObject> {
        return detector.detect(bitmap)
    }
}