package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.GeolocationPermissions
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.HudColorTheme
import com.example.model.NavStep
import com.example.model.RoutePreset

/**
 * Layer modes for the built-in real Google Maps engine
 */
enum class GoogleMapLayer(val title: String, val icon: ImageVector) {
    DIRECTIONS("Lộ trình", Icons.Default.Directions),
    STREETS("Bản đồ", Icons.Default.Map),
    SATELLITE("Vệ tinh", Icons.Default.Satellite),
    NIGHT_HUD("HUD Đêm", Icons.Default.DarkMode)
}

/**
 * Built-in real Google Maps HUD component with 100% clean, unobstructed map view.
 * All UI clutter (search boxes, route input fields, banners, buttons) has been completely removed.
 * Google Maps is embedded cleanly without input boxes or promo cards.
 */
@Composable
fun GoogleMapHudView(
    route: RoutePreset,
    currentStep: NavStep,
    stepIndex: Int,
    totalSteps: Int,
    remainingMeters: Int,
    compassHeading: Float,
    speedKmh: Float,
    currentLocation: Location?,
    isGpsActive: Boolean,
    gpsAccuracy: Float,
    colorTheme: HudColorTheme,
    onOpenSearch: () -> Unit,
    onSelectStep: ((Int) -> Unit)? = null,
    onOpenSos: (() -> Unit)? = null,
    isExpanded: Boolean = false,
    onToggleExpand: (() -> Unit)? = null,
    isMirrored: Boolean = false,
    modifier: Modifier = Modifier
) {
    var selectedLayer by remember { mutableStateOf(GoogleMapLayer.DIRECTIONS) }
    var zoomLevel by remember { mutableIntStateOf(16) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isMapLoading by remember { mutableStateOf(true) }

    // Coordinates: prioritize live GPS coordinates if active, otherwise route preset coordinates
    val lat = currentLocation?.latitude ?: route.startLat
    val lng = currentLocation?.longitude ?: route.startLng

    Box(
        modifier = modifier
            .testTag("google_map_hud_container")
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF000000))
            .border(1.2.dp, colorTheme.primaryColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .then(if (isMirrored) Modifier.scale(scaleX = -1f, scaleY = 1f) else Modifier)
    ) {
        // =========================================================================================
        // 1. 100% PURE REAL GOOGLE MAPS (ZERO UI CLUTTER, ZERO INPUT BOXES, ZERO PROMO BANNERS)
        // =========================================================================================
        RealGoogleMapsWebView(
            layer = selectedLayer,
            latitude = lat,
            longitude = lng,
            destination = route.destination,
            zoom = zoomLevel,
            onLoadingChanged = { isMapLoading = it },
            onWebViewCreated = { webViewInstance = it },
            modifier = Modifier.fillMaxSize()
        )

        // =========================================================================================
        // 2. DISCREET LOADING PILL (Only while tiles load)
        // =========================================================================================
        if (isMapLoading) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xD9050B18))
                    .border(1.dp, colorTheme.primaryColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = colorTheme.primaryColor,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Đang tải bản đồ...",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // =========================================================================================
        // 3. LAYER TOGGLE CHIP (Minimalist transparent pill at top-right corner)
        // =========================================================================================
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xCC050B18))
                .border(1.dp, colorTheme.primaryColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                .clickable {
                    val nextLayer = when (selectedLayer) {
                        GoogleMapLayer.DIRECTIONS -> GoogleMapLayer.STREETS
                        GoogleMapLayer.STREETS -> GoogleMapLayer.SATELLITE
                        GoogleMapLayer.SATELLITE -> GoogleMapLayer.NIGHT_HUD
                        GoogleMapLayer.NIGHT_HUD -> GoogleMapLayer.DIRECTIONS
                    }
                    selectedLayer = nextLayer
                    webViewInstance?.let { wv ->
                        val newUrl = buildGoogleMapsUrl(nextLayer, lat, lng, route.destination, zoomLevel)
                        wv.loadUrl(newUrl)
                    }
                }
                .padding(horizontal = 7.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = selectedLayer.icon,
                    contentDescription = selectedLayer.title,
                    tint = colorTheme.primaryColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = selectedLayer.title,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // =========================================================================================
        // 4. DISCREET MY LOCATION RECENTER BUTTON (Bottom-Right corner)
        // =========================================================================================
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xDD0A1124))
                .border(1.dp, colorTheme.primaryColor.copy(alpha = 0.7f), CircleShape)
                .clickable {
                    webViewInstance?.let { wv ->
                        val targetUrl = buildGoogleMapsUrl(selectedLayer, lat, lng, route.destination, zoomLevel)
                        wv.loadUrl(targetUrl)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Vị trí GPS",
                tint = colorTheme.primaryColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Builds authentic clean Google Maps URLs using embed mode to ensure NO input boxes,
 * NO mobile promo banners, and NO white directions sheets appear over the map.
 */
fun buildGoogleMapsUrl(
    layer: GoogleMapLayer,
    lat: Double,
    lng: Double,
    destination: String,
    zoom: Int
): String {
    return when (layer) {
        GoogleMapLayer.DIRECTIONS -> {
            // Clean Embedded Google Maps Route from GPS origin to destination
            "https://maps.google.com/maps?saddr=$lat,$lng&daddr=${Uri.encode(destination)}&output=embed"
        }
        GoogleMapLayer.STREETS -> {
            // Clean Embedded Street Map centered on GPS location
            "https://maps.google.com/maps?q=$lat,$lng&z=$zoom&output=embed"
        }
        GoogleMapLayer.SATELLITE -> {
            // Clean Embedded Satellite Imagery centered on GPS location
            "https://maps.google.com/maps?q=$lat,$lng&t=k&z=$zoom&output=embed"
        }
        GoogleMapLayer.NIGHT_HUD -> {
            // Clean Embedded Map with night contrast filter applied to canvas
            "https://maps.google.com/maps?saddr=$lat,$lng&daddr=${Uri.encode(destination)}&output=embed"
        }
    }
}

/**
 * Embedded real Google Maps WebView with aggressive cleaning of Google promotional banners,
 * app prompts, headers, bottom sheets, and search bars to reveal 100% clean map view.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun RealGoogleMapsWebView(
    layer: GoogleMapLayer,
    latitude: Double,
    longitude: Double,
    destination: String,
    zoom: Int,
    onLoadingChanged: (Boolean) -> Unit,
    onWebViewCreated: (WebView) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialUrl = remember(layer, latitude, longitude, destination, zoom) {
        buildGoogleMapsUrl(layer, latitude, longitude, destination, zoom)
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                try {
                    setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                } catch (_: Exception) {}

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    setGeolocationEnabled(true)
                    databaseEnabled = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    builtInZoomControls = false
                    displayZoomControls = false
                    cacheMode = WebSettings.LOAD_DEFAULT
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onGeolocationPermissionsShowPrompt(
                        origin: String?,
                        callback: GeolocationPermissions.Callback?
                    ) {
                        callback?.invoke(origin, true, false)
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        onLoadingChanged(true)
                        injectMapCleanerCss(view, layer == GoogleMapLayer.NIGHT_HUD)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        onLoadingChanged(false)
                        injectMapCleanerCss(view, layer == GoogleMapLayer.NIGHT_HUD)
                    }

                    override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                        return true
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        super.onReceivedError(view, request, error)
                        onLoadingChanged(false)
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val requestUrl = request?.url?.toString() ?: return false
                        if (requestUrl.startsWith("intent://") ||
                            requestUrl.startsWith("android-app://") ||
                            requestUrl.startsWith("market://")
                        ) {
                            return true
                        }
                        return false
                    }
                }

                loadUrl(initialUrl)
                onWebViewCreated(this)
            }
        },
        update = { webView ->
            onWebViewCreated(webView)
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            // Clean up
        }
    }
}

/**
 * Aggressively removes any Google Maps web card, directions inputs, search boxes,
 * app banners, bottom sheets, or watermarks so only the clean map is visible.
 */
private fun injectMapCleanerCss(view: WebView?, isNightMode: Boolean) {
    if (view == null) return

    val nightFilter = if (isNightMode) {
        "html, body, #mapDiv, canvas { filter: invert(90%) hue-rotate(180deg) contrast(125%) brightness(95%) !important; }"
    } else ""

    val cssRules = """
        .place-card, .place-desc-large, .navigate, .review-box,
        .gm-style-cc, .gmnoprint, a[href*='maps.google.com'],
        .bottom-actions, .google-maps-link, .app-banner,
        header, #header, .header-container,
        .ml-mobile-banner, #app-banner, .google-maps-app-banner,
        .header-install-app-banner, div[role='dialog'], [aria-label*='banner' i],
        #searchbox, .searchbox-container, .omnibox-container,
        #pane, .widget-pane, .widget-pane-visible, .widget-pane-content,
        .section-directions-trip, div[data-is-bottom-sheet='true'],
        div[class*='bottom-sheet'], .scene-footer-container, .watermark,
        #assistive-chips, div[jsaction*='appBanner'], div[class*='banner'] {
            display: none !important;
            visibility: hidden !important;
            opacity: 0 !important;
            height: 0 !important;
            width: 0 !important;
            pointer-events: none !important;
            z-index: -9999 !important;
        }

        html, body, #mapDiv {
            height: 100% !important;
            width: 100% !important;
            margin: 0 !important;
            padding: 0 !important;
            overflow: hidden !important;
            background: #000000 !important;
        }

        $nightFilter
    """.trimIndent().replace("\n", " ")

    val js = """
        javascript:(function() {
            var style = document.getElementById('motohud_cleaner_style');
            if (!style) {
                style = document.createElement('style');
                style.id = 'motohud_cleaner_style';
                document.head.appendChild(style);
            }
            style.innerHTML = '$cssRules';

            function purgeClutter() {
                var badSelectors = [
                    '.place-card', '.place-desc-large', '.navigate',
                    '.ml-mobile-banner', '.app-banner', '#app-banner',
                    'header', '#header', '#searchbox', '.searchbox-container',
                    '#pane', '.widget-pane', '.section-directions-trip',
                    'div[data-is-bottom-sheet="true"]', 'div[class*="bottom-sheet"]',
                    'div[role="dialog"]', '.watermark'
                ];
                badSelectors.forEach(function(sel) {
                    var items = document.querySelectorAll(sel);
                    for (var i = 0; i < items.length; i++) {
                        items[i].style.setProperty('display', 'none', 'important');
                    }
                });
            }

            purgeClutter();
            if (!window.__motohud_cleaner_observed) {
                window.__motohud_cleaner_observed = true;
                var obs = new MutationObserver(purgeClutter);
                obs.observe(document.body || document.documentElement, { childList: true, subtree: true });
            }
        })()
    """.trimIndent()

    view.loadUrl(js)
}
