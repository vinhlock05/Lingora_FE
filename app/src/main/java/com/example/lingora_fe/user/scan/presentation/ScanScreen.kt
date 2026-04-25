package com.example.lingora_fe.user.scan.presentation

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.lingora_fe.user.scan.data.ml.DetectedObject

@Composable
fun ScanScreen(
    bitmap: Bitmap,
    objects: List<DetectedObject>,
    onObjectClick: (DetectedObject) -> Unit,
    onClose: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ObjectDetectionOverlay(
                bitmap = bitmap,
                objects = objects,
                onObjectClick = onObjectClick,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Close
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Close, null, tint = Color.White)
        }
    }
}