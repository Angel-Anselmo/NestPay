package com.icescream.nestpay.ui.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.net.Uri
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentAuthorizationWebView(
    showDialog: Boolean,
    authorizationUrl: String,
    onDismiss: () -> Unit,
    onAuthorizationComplete: (interactRef: String) -> Unit,
    onAuthorizationError: (error: String) -> Unit
) {
    if (showDialog && authorizationUrl.isNotEmpty()) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Autorizar Pago",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        IconButton(
                            onClick = onDismiss
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.Gray
                            )
                        }
                    }

                    Divider(color = Color.Gray.copy(alpha = 0.3f))

                    // WebView
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = true
                                settings.setSupportZoom(true)

                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        url: String?
                                    ): Boolean {
                                        Log.d("PaymentWebView", "URL intercepted: $url")

                                        // Detectar callback de autorización exitosa
                                        if (url != null) {
                                            when {
                                                // URL de éxito con interact_ref
                                                url.contains("interact_ref=") -> {
                                                    val uri = Uri.parse(url)
                                                    val interactRef =
                                                        uri.getQueryParameter("interact_ref")

                                                    if (interactRef != null) {
                                                        Log.d(
                                                            "PaymentWebView",
                                                            "Authorization successful: $interactRef"
                                                        )
                                                        onAuthorizationComplete(interactRef)
                                                        return true
                                                    }
                                                }

                                                // URL de cancelación
                                                url.contains("cancel") || url.contains("error") -> {
                                                    Log.d(
                                                        "PaymentWebView",
                                                        "Authorization cancelled or error"
                                                    )
                                                    onAuthorizationError("Usuario canceló la autorización")
                                                    return true
                                                }

                                                // URL de finalización exitosa
                                                url.contains("finish") || url.contains("success") -> {
                                                    Log.d(
                                                        "PaymentWebView",
                                                        "Authorization finished successfully"
                                                    )
                                                    // Si no hay interact_ref en la URL, usar un placeholder
                                                    val uri = Uri.parse(url)
                                                    val interactRef =
                                                        uri.getQueryParameter("interact_ref")
                                                            ?: "auth_completed"
                                                    onAuthorizationComplete(interactRef)
                                                    return true
                                                }
                                            }
                                        }

                                        return false
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        Log.d("PaymentWebView", "Page loaded: $url")
                                    }

                                    override fun onReceivedError(
                                        view: WebView?,
                                        errorCode: Int,
                                        description: String?,
                                        failingUrl: String?
                                    ) {
                                        super.onReceivedError(
                                            view,
                                            errorCode,
                                            description,
                                            failingUrl
                                        )
                                        Log.e("PaymentWebView", "WebView error: $description")
                                        onAuthorizationError("Error cargando página de autorización: $description")
                                    }
                                }

                                loadUrl(authorizationUrl)
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}