package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MarketPrice
import com.example.ui.components.GlowingActionButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.NeonBadge
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSavings
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ShoppingViewModel
import com.example.util.LocalizationUtil

@Composable
fun ScannerScreen(
    viewModel: ShoppingViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isArabic by viewModel.isArabic.collectAsStateWithLifecycle()
    val allMarketPrices by viewModel.marketPrices.collectAsStateWithLifecycle()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }

    var scannedBarcode by remember { mutableStateOf("6130001000123") }
    var detectedPrices by remember { mutableStateOf<List<MarketPrice>>(emptyList()) }
    var scannedProductTitle by remember { mutableStateOf("Candia Viva Milk 1L") }
    var scannedProductTitleAr by remember { mutableStateOf("حليب كانديا فيفا 1 لتر") }

    var isResultCardVisible by remember { mutableStateOf(true) }
    var isItemAdded by remember { mutableStateOf(false) }
    var showDemoPicker by remember { mutableStateOf(false) }

    // When barcode changes, fetch matching prices
    LaunchedEffect(scannedBarcode, allMarketPrices) {
        if (scannedBarcode.isNotBlank()) {
            val prices = allMarketPrices.filter { it.barcode == scannedBarcode }
            detectedPrices = prices
            if (prices.isNotEmpty()) {
                scannedProductTitle = prices.first().itemName
                scannedProductTitleAr = prices.first().itemNameAr
                isResultCardVisible = true
                isItemAdded = false
            }
        }
    }

    // Smooth laser sweep animation
    val infiniteTransition = rememberInfiniteTransition(label = "laser_sweep")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_anim"
    )

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF070B12))) {
        // 1. Camera Viewfinder or High-Tech Background
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        try {
                            cameraProvider.unbindAll()
                            val camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview
                            )
                            cameraControl = camera.cameraControl
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Dark futuristic background when camera permission not yet granted
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF070B12))
            )
        }

        // 2. Radial Vignette for immersive contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color(0xCC05080E)),
                        radius = 850f
                    )
                )
        )

        // 3. Top Action Bar & HUD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0x80000000))
                    .border(BorderStroke(1.dp, Color(0x33FFFFFF)), CircleShape)
                    .size(44.dp)
                    .testTag("scanner_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            // Title Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x990A111E))
                    .border(BorderStroke(1.dp, GlassBorder), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isArabic) "قارئ الباركود والأسعار" else "Price Scanner",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Flashlight / Torch Toggle Button
            IconButton(
                onClick = {
                    if (hasCameraPermission && cameraControl != null) {
                        val nextTorch = !isTorchOn
                        cameraControl?.enableTorch(nextTorch)
                        isTorchOn = nextTorch
                    } else if (!hasCameraPermission) {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isTorchOn) ElectricCyan else Color(0x80000000))
                    .border(BorderStroke(1.dp, if (isTorchOn) ElectricCyan else Color(0x33FFFFFF)), CircleShape)
                    .size(44.dp)
                    .testTag("scanner_flashlight_button")
            ) {
                Icon(
                    imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Flashlight",
                    tint = if (isTorchOn) Color(0xFF041420) else TextPrimary
                )
            }
        }

        // 4. Random / Sample Product Scan Feature - Positioned right ABOVE the barcode reticle
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-230).dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xD90A111E))
                    .border(BorderStroke(1.dp, if (showDemoPicker) ElectricCyan else Color(0x4038BDF8)), RoundedCornerShape(20.dp))
                    .clickable { showDemoPicker = !showDemoPicker }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoFixHigh,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isArabic) "تجربة منتج عشوائي" else "Try Sample Product",
                    color = ElectricCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Demo Picker Chips (Appears smoothly above reticle when clicked)
        if (showDemoPicker) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-275).dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xF00A111E))
                    .border(BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f)), RoundedCornerShape(14.dp))
                    .padding(8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isArabic) "اختر منتجاً للمحاكاة الفورية:" else "Select sample to simulate:",
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    val demoSamples = listOf(
                        Triple("6130001000123", "Candia Milk", "حليب كانديا"),
                        Triple("6130002000456", "Elio Oil 5L", "زيت إيليو"),
                        Triple("6130003000789", "Famico Coffee", "قهوة فاميكو"),
                        Triple("6130004000111", "Cab Tomato", "طماطم كاب"),
                        Triple("6130010000777", "Farm Eggs 30", "بيض 30 حبة")
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(demoSamples) { (barcode, enName, arName) ->
                            val isSelected = scannedBarcode == barcode
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) ElectricCyan else Color(0x33FFFFFF))
                                    .clickable {
                                        scannedBarcode = barcode
                                        showDemoPicker = false
                                        isResultCardVisible = true
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isArabic) arName else enName,
                                    color = if (isSelected) Color(0xFF041420) else TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Center Scanning Reticle Box (Lifted Upward for generous bottom space)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-85).dp)
                .size(240.dp)
                .testTag("scanner_reticle_box"),
            contentAlignment = Alignment.Center
        ) {
            // High-tech Corner Brackets
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        BorderStroke(1.5.dp, ElectricCyan.copy(alpha = 0.35f)),
                        RoundedCornerShape(22.dp)
                    )
                    .drawBehind {
                        val bracketLen = 28.dp.toPx()
                        val stroke = 4.dp.toPx()
                        val color = if (isResultCardVisible) EmeraldSavings else ElectricCyan

                        // Top-left
                        drawLine(color, Offset(0f, 0f), Offset(bracketLen, 0f), stroke)
                        drawLine(color, Offset(0f, 0f), Offset(0f, bracketLen), stroke)

                        // Top-right
                        drawLine(color, Offset(size.width, 0f), Offset(size.width - bracketLen, 0f), stroke)
                        drawLine(color, Offset(size.width, 0f), Offset(size.width, bracketLen), stroke)

                        // Bottom-left
                        drawLine(color, Offset(0f, size.height), Offset(bracketLen, size.height), stroke)
                        drawLine(color, Offset(0f, size.height), Offset(0f, size.height - bracketLen), stroke)

                        // Bottom-right
                        drawLine(color, Offset(size.width, size.height), Offset(size.width - bracketLen, size.height), stroke)
                        drawLine(color, Offset(size.width, size.height), Offset(size.width, size.height - bracketLen), stroke)
                    }
            )

            // Animated Laser Sweep Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .offset(y = (-110 + (220 * laserOffset)).dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                (if (isResultCardVisible) EmeraldSavings else ElectricCyan).copy(alpha = 0.8f),
                                Color.White,
                                (if (isResultCardVisible) EmeraldSavings else ElectricCyan).copy(alpha = 0.8f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Center Target Crosshair
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .border(BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f)), CircleShape)
            )
        }

        // 6. Minimalist Scanning Instructions Pill (Positioned neatly under the lifted reticle)
        if (!isResultCardVisible) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 65.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xCC0A111E))
                    .border(BorderStroke(1.dp, Color(0x33475569)), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (isArabic) "ضع الباركود داخل الإطار للمقارنة الفورية" else "Align barcode inside frame to compare",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 7. Camera Permission Warning Card (if not granted)
        if (!hasCameraPermission) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xEE0F172A))
                    .border(BorderStroke(1.dp, ElectricCyan), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isArabic) "إذن الكاميرا مطلوب للمسح" else "Camera Permission Required",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isArabic) "اسمح للتطبيق باستخدام الكاميرا لمسح الباركود في المتاجر." else "Grant camera access to scan barcodes and compare prices.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    GlowingActionButton(
                        text = if (isArabic) "تفعيل الكاميرا" else "Enable Camera",
                        icon = Icons.Default.CameraAlt,
                        color = ElectricCyan,
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                    )
                }
            }
        }

        // 8. Elegant Result Sheet (Slides up cleanly from bottom when product is detected)
        AnimatedVisibility(
            visible = isResultCardVisible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            val minPrice = detectedPrices.minByOrNull { it.priceDzd }?.priceDzd ?: 0.0
            val bestStore = detectedPrices.minByOrNull { it.priceDzd }

            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("scanned_result_card"),
                glowColor = EmeraldSavings
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header: Product Title, Barcode, and Dismiss Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                NeonBadge(
                                    text = scannedBarcode,
                                    accentColor = ElectricCyan,
                                    icon = Icons.Default.QrCodeScanner
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                NeonBadge(
                                    text = if (isArabic) "تم العثور عليه" else "MATCH FOUND",
                                    accentColor = EmeraldSavings
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isArabic) scannedProductTitleAr else scannedProductTitle,
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Close / Dismiss Button to continue scanning
                        IconButton(
                            onClick = { isResultCardVisible = false },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0x22FFFFFF))
                                .size(32.dp)
                                .testTag("close_scanned_card_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Best Price Highlight Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x2200E676))
                            .border(BorderStroke(1.dp, EmeraldSavings.copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isArabic) "أفضل سعر متاح الآن:" else "Lowest price detected:",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = if (isArabic) (bestStore?.storeNameAr ?: "") else (bestStore?.storeName ?: ""),
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = LocalizationUtil.formatDzd(minPrice, isArabic),
                                color = EmeraldSavings,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Store Comparison Horizontal Chips (Clean & compact, non-messy)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(detectedPrices) { price ->
                            val isCheapest = price.priceDzd == minPrice
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isCheapest) EmeraldSavings.copy(alpha = 0.15f) else Color(0x15FFFFFF))
                                    .border(
                                        BorderStroke(1.dp, if (isCheapest) EmeraldSavings else Color(0x22FFFFFF)),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = if (isCheapest) EmeraldSavings else TextSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isArabic) price.storeNameAr else price.storeName,
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isCheapest) FontWeight.Bold else FontWeight.Normal
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = LocalizationUtil.formatDzd(price.priceDzd, isArabic),
                                    color = if (isCheapest) EmeraldSavings else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Button: Add to Cart / Shopping List
                    if (!isItemAdded) {
                        GlowingActionButton(
                            text = if (isArabic) "إضافة إلى سلة التسوق" else "Add to Shopping Basket",
                            icon = Icons.Default.AddShoppingCart,
                            color = EmeraldSavings,
                            onClick = {
                                viewModel.addItem(
                                    name = scannedProductTitle,
                                    nameAr = scannedProductTitleAr,
                                    barcode = scannedBarcode,
                                    estimatedPriceDzd = minPrice
                                )
                                isItemAdded = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0x3300E676))
                                .border(BorderStroke(1.dp, EmeraldSavings), RoundedCornerShape(16.dp))
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = EmeraldSavings,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isArabic) "تمت الإضافة إلى سلتك بنجاح!" else "Added to your basket!",
                                    color = EmeraldSavings,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
