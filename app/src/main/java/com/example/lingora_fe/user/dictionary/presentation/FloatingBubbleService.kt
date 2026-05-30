package com.example.lingora_fe.user.dictionary.presentation

import android.annotation.SuppressLint
import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.lingora_fe.R

class FloatingBubbleService : Service() {

    private lateinit var windowManager: WindowManager
    private var bubbleView: View? = null
    private var closeZoneView: View? = null
    private var isBubbleShowing = false
    private var lastCopiedWord = ""

    private var originalX = 0
    private var originalY = 300
    private var isLookupOpen = false
    private var isDraggingToClose = false
    private var isOverCloseZone = false

    private val clipListener = ClipboardManager.OnPrimaryClipChangedListener {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboard.primaryClip
        if (clipData != null && clipData.itemCount > 0) {
            val copiedText = clipData.getItemAt(0).text?.toString()?.trim() ?: ""
            if (isEligibleWord(copiedText)) {
                lastCopiedWord = copiedText
                showBubble()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        currentService = this
        isRunning = true
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.addPrimaryClipChangedListener(clipListener)

        showBubble()
    }

    private fun isEligibleWord(text: String): Boolean {
        val clean = text.trim()
        if (clean.isEmpty() || clean.contains(" ") || clean.contains("\n")) return false
        if (clean.length !in 2..20) return false
        return clean.all { it.isLetter() }
    }

    private fun overlayFlag(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    private fun showBubble() {
        if (isBubbleShowing) return

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayFlag(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 300
        }

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        bubbleView = inflater.inflate(R.layout.layout_floating_bubble, null)

        val bubbleImage = bubbleView?.findViewById<ImageView>(R.id.bubble_image_view)

        bubbleImage?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isDragMove = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isDragMove = false
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()

                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            if (!isDragMove) {
                                isDragMove = true
                                // Show trash zone when drag starts
                                showCloseZone()
                                if (isLookupOpen) {
                                    isDraggingToClose = true
                                    FloatingLookupActivity.instance?.finish()
                                }
                            }
                        }

                        params.x = initialX + dx
                        params.y = initialY + dy
                        bubbleView?.let { windowManager.updateViewLayout(it, params) }

                        // Update trash zone highlight state
                        if (isDragMove) {
                            updateCloseZoneState(isBubbleOverCloseZone(params))
                        }
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        if (!isDragMove) {
                            // Tap: open or close lookup panel
                            if (isLookupOpen) {
                                FloatingLookupActivity.instance?.finish()
                            } else {
                                launchLookupActivity()
                            }
                        } else if (isOverCloseZone) {
                            // Dropped on trash zone → dismiss the service
                            removeCloseZone()
                            stopSelf()
                            return true
                        } else {
                            // Normal drag end → hide zone, snap to edge
                            removeCloseZone()
                            val metrics = DisplayMetrics()
                            @Suppress("DEPRECATION")
                            windowManager.defaultDisplay.getMetrics(metrics)
                            val screenWidth = metrics.widthPixels
                            params.x = if (params.x < screenWidth / 2) 0
                            else screenWidth - (bubbleView?.width ?: dpToPx(76))
                            bubbleView?.let { windowManager.updateViewLayout(it, params) }
                        }
                        isDraggingToClose = false
                        return true
                    }
                }
                return false
            }
        })

        windowManager.addView(bubbleView, params)
        isBubbleShowing = true
    }

    // ── Close zone ────────────────────────────────────────────────────────────

    @SuppressLint("InflateParams")
    private fun showCloseZone() {
        if (closeZoneView != null) return

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        closeZoneView = inflater.inflate(R.layout.layout_bubble_close_zone, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayFlag(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = dpToPx(48)
        }

        closeZoneView?.alpha = 0f
        windowManager.addView(closeZoneView, params)
        closeZoneView?.animate()?.alpha(1f)?.setDuration(200)?.start()
    }

    private fun removeCloseZone() {
        closeZoneView?.let {
            try { windowManager.removeView(it) } catch (_: Exception) {}
            closeZoneView = null
        }
        // Restore bubble scale if it was shrunk
        bubbleView?.animate()?.scaleX(1f)?.scaleY(1f)?.setDuration(150)?.start()
        isOverCloseZone = false
    }

    private fun updateCloseZoneState(over: Boolean) {
        if (isOverCloseZone == over) return
        isOverCloseZone = over

        val circle = closeZoneView?.findViewById<View>(R.id.close_zone_circle) ?: return
        if (over) {
            circle.setBackgroundResource(R.drawable.bg_close_zone_active)
            bubbleView?.animate()?.scaleX(0.8f)?.scaleY(0.8f)?.setDuration(150)?.start()
        } else {
            circle.setBackgroundResource(R.drawable.bg_close_zone)
            bubbleView?.animate()?.scaleX(1f)?.scaleY(1f)?.setDuration(150)?.start()
        }
    }

    private fun isBubbleOverCloseZone(bubbleParams: WindowManager.LayoutParams): Boolean {
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(metrics)

        // Zone center: horizontally centered, 48dp from the bottom + half zone height
        val zoneCenterX = metrics.widthPixels / 2
        val zoneCenterY = metrics.heightPixels - dpToPx(48) - dpToPx(40) // 40 = half of 80dp zone

        val bubbleW = bubbleView?.width ?: dpToPx(76)
        val bubbleH = bubbleView?.height ?: dpToPx(76)
        val bubbleCenterX = bubbleParams.x + bubbleW / 2
        val bubbleCenterY = bubbleParams.y + bubbleH / 2

        val dx = (bubbleCenterX - zoneCenterX).toDouble()
        val dy = (bubbleCenterY - zoneCenterY).toDouble()
        return Math.sqrt(dx * dx + dy * dy) < dpToPx(72)
    }

    // ── Lookup panel callbacks ─────────────────────────────────────────────────

    private fun launchLookupActivity() {
        val intent = Intent(this, FloatingLookupActivity::class.java).apply {
            putExtra("query", lastCopiedWord)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }

    fun onLookupOpened() {
        isLookupOpen = true
        bubbleView?.let { view ->
            val params = view.layoutParams as WindowManager.LayoutParams
            originalX = params.x
            originalY = params.y
            view.visibility = View.INVISIBLE
        }
    }

    fun updateBubblePositionForCard(cardTopY: Int) {
        // Bubble is hidden while panel is open — nothing to do
    }

    fun onLookupClosed() {
        isLookupOpen = false
        bubbleView?.let { view ->
            view.visibility = View.VISIBLE

            if (isDraggingToClose) return

            val params = view.layoutParams as WindowManager.LayoutParams
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(metrics)
            val screenWidth = metrics.widthPixels

            params.x = if (originalX < screenWidth / 2) 0 else screenWidth - view.width
            params.y = originalY
            windowManager.updateViewLayout(view, params)
        }
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    private fun removeBubble() {
        bubbleView?.let {
            try { windowManager.removeView(it) } catch (e: Exception) { e.printStackTrace() }
            bubbleView = null
        }
        isBubbleShowing = false
    }

    override fun onDestroy() {
        super.onDestroy()
        currentService = null
        isRunning = false
        removeBubble()
        removeCloseZone()
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.removePrimaryClipChangedListener(clipListener)
    }

    companion object {
        var currentService: FloatingBubbleService? = null
        var isRunning by mutableStateOf(false)
    }
}
