package com.example.lingora_fe.user.scan.presentation

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.lingora_fe.user.scan.data.ml.DetectedObject
import kotlin.math.min

@Composable
fun ObjectDetectionOverlay(
    bitmap: Bitmap,
    objects: List<DetectedObject>,
    onObjectClick: (DetectedObject) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier) {
        val containerWidth = constraints.maxWidth.toFloat()   // px
        val containerHeight = constraints.maxHeight.toFloat() // px

        val scale = min(
            containerWidth / bitmap.width,
            containerHeight / bitmap.height
        )

        val scaledWidth = bitmap.width * scale
        val scaledHeight = bitmap.height * scale

        val offsetX = (containerWidth - scaledWidth) / 2f
        val offsetY = (containerHeight - scaledHeight) / 2f

        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        objects.forEach { obj ->
            val rect = obj.boundingBox ?: return@forEach

            // Tính toán bằng px
            val leftPx   = rect.left   * scale + offsetX
            val topPx    = rect.top    * scale + offsetY
            val widthPx  = rect.width()  * scale
            val heightPx = rect.height() * scale

            // Convert px → dp trước khi truyền vào Modifier
            with(density) {
                Box(
                    modifier = Modifier
                        .offset(leftPx.toDp(), topPx.toDp())
                        .size(widthPx.toDp(), heightPx.toDp())
                        .border(2.dp, Color.Red)
                        .clickable { onObjectClick(obj) }
                ) {
                    Text(
                        text = obj.label,
                        color = Color.White,
                        modifier = Modifier
                            .background(Color.Black)
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}