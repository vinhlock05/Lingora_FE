package com.example.lingora_fe.user.scan.domain.repository

import android.graphics.Bitmap
import com.example.lingora_fe.user.scan.data.ml.DetectedObject

interface ScanRepository {
    suspend fun detect(bitmap: Bitmap): List<DetectedObject>
}