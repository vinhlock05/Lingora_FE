package com.example.lingora_fe.user.studyset.presentation

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity

class PaymentWebViewActivity : ComponentActivity() {
    private lateinit var webView: WebView
    
    companion object {
        const val EXTRA_PAYMENT_URL = "payment_url"
        const val EXTRA_STUDY_SET_ID = "study_set_id"
        const val RESULT_SUCCESS = "payment_success"
        const val RESULT_CANCELLED = "payment_cancelled"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val paymentUrl = intent.getStringExtra(EXTRA_PAYMENT_URL) ?: run {
            Log.e("PaymentWebView", "No payment URL provided")
            finish()
            return
        }
        
        webView = WebView(this)
        setContentView(webView)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportMultipleWindows(true)
            allowFileAccess = true
            allowContentAccess = true
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        
        // Enable cookies
        android.webkit.CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }
        
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                url?.let {
                    Log.d("PaymentWebView", "URL loading: $it")
                    
                    // Check for VNPay return URL
                    if (it.contains("/vnpay/return")) {
                        val uri = Uri.parse(it)
                        
                        Log.d("PaymentWebView", "VNPay return URL detected")
                        
                        // Extract all VNPay params
                        val resultIntent = Intent().apply {
                            // Add all query parameters
                            uri.queryParameterNames.forEach { paramName ->
                                uri.getQueryParameter(paramName)?.let { value ->
                                    putExtra(paramName, value)
                                    Log.d("PaymentWebView", "Param: $paramName = $value")
                                }
                            }
                            
                            // Flag to indicate this needs backend verification
                            putExtra("needVerification", true)
                            
                            // Also add response code for quick check
                            val responseCode = uri.getQueryParameter("vnp_ResponseCode")
                            putExtra("vnp_ResponseCode", responseCode)
                            putExtra("isSuccess", responseCode == "00")
                        }
                        
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                        return true
                    }
                }
                return false
            }
        }
        
        Log.d("PaymentWebView", "Loading payment URL: $paymentUrl")
        webView.loadUrl(paymentUrl)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Bạn có chắc chắn muốn hủy thanh toán?")
                .setPositiveButton("Có") { _, _ ->
                    val resultIntent = Intent().apply {
                        putExtra("action", RESULT_CANCELLED)
                        putExtra("isSuccess", false)
                    }
                    setResult(Activity.RESULT_CANCELED, resultIntent)
                    finish()
                }
                .setNegativeButton("Không", null)
                .show()
        }
    }
}
