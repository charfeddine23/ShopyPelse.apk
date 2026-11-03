package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCode
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

    var scannedBarcode by remember { mutableStateOf("6130001000123") }
    var detectedPrices by remember { mutableStateOf<List<MarketPrice>>(emptyList()) }
    var scannedProductTitle by remember { mutableStateOf("Candia Viva Milk 1L") }
    var scannedProductTitleAr by remember { mutableStateOf("حليب كانديا فيفا 1 لتر") }
    var isQrMode by remember { mutableStateOf(false) }

    LaunchedEffect(scannedBarcode, allMarketPrices) {
        val prices = allMarketPrices.filter { it.barcode == scannedBarcode }
        detectedPrices = prices
        if (prices.isNotEmpty()) {
            scannedProductTitle = prices.first().itemName
            scannedProductTitleAr = prices.first().itemNameAr
        }
    }

    // Laser scan animation
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

    Box(modifier = modifier.fillMaxSize()) {
        // Camera View or High-tech Simulation Background
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
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Futuristic Dark Camera Viewfinder with ambient cyber lines
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF070B12))
            )
        }

        // Dark Vignette & Sci-Fi Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color(0xB3000000)),
                        radius = 900f
                    )
                )
        )

        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0x66000000))
                    .border(BorderStroke(1.dp, GlassBorder), CircleShape)
                    .size(44.dp)
                    .testTag("scanner_back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            // Mode Selector Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x80000000))
                    .border(BorderStroke(1.dp, GlassBorder), RoundedCornerShape(20.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (!isQrMode) ElectricCyan else Color.Transparent)
                        .clickable { isQrMode = false }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isArabic) "باركود المنتجات" else "Barcode",
                        color = if (!isQrMode) Color(0xFF031422) else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isQrMode) EmeraldSavings else Color.Transparent)
                        .clickable { isQrMode = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isArabic) "مسح قائمة QR" else "QR List",
                        color = if (isQrMode) Color(0xFF021B10) else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (!hasCameraPermission) {
                IconButton(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(ElectricCyan.copy(alpha = 0.2f))
                        .border(BorderStroke(1.dp, ElectricCyan), CircleShape)
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera Permission",
                        tint = ElectricCyan
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(44.dp))
            }
        }

        // Center Scanning Reticle Box
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(280.dp)
                .testTag("scanner_reticle_box"),
            contentAlignment = Alignment.Center
        ) {
            // Reticle Border & Corner brackets
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        BorderStroke(1.5.dp, ElectricCyan.copy(alpha = 0.4f)),
                        RoundedCornerShape(24.dp)
                    )
                    .drawBehind {
                        val bracketLen = 32.dp.toPx()
                        val stroke = 4.dp.toPx()
                        val color = ElectricCyan

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
                    .height(2.dp)
                    .offset(y = (-130 + (260 * laserOffset)).dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                ElectricCyan.copy(alpha = 0.8f),
                                Color.White,
                                ElectricCyan.copy(alpha = 0.8f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Target Crosshair Center
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .border(BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.6f)), CircleShape)
            )
        }

        // Fast Test Barcode Carousel (For instant barcode verification & testing in any environment)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isArabic) "اختر نموذج باركود للاختبار السريع:" else "Tap sample barcode to simulate live scan:",
                color = TextSecondary,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val samples = listOf(
                    Triple("6130001000123", "Candia Viva", "حليب كانديا"),
                    Triple("6130002000456", "Elio Oil 5L", "زيت إيليو"),
                    Triple("6130003000789", "Famico Coffee", "قهوة فاميكو"),
                    Triple("6130004000111", "Cab Tomato", "طماطم كاب"),
                    Triple("6130010000777", "Farm Eggs 30", "بيض 30 حبة"),
                    Triple("6130007000444", "Isis 3kg", "إيزيس 3 كغ")
                )
                items(samples) { (barcode, enName, arName) ->
                    val isSelected = scannedBarcode == barcode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) ElectricCyan else Color(0x66000000))
                            .border(BorderStroke(1.dp, if (isSelected) ElectricCyan else GlassBorder), RoundedCornerShape(12.dp))
                            .clickable { scannedBarcode = barcode }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isArabic) arName else enName,
                            color = if (isSelected) Color(0xFF041420) else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scanned Product Details Liquid Glass Card
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("scanned_result_card"),
                glowColor = EmeraldSavings
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
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
                                    text = if (isArabic) "متحقق منه" else "VERIFIED",
                                    accentColor = EmeraldSavings
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isArabic) scannedProductTitleAr else scannedProductTitle,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Add to list button
                        GlowingActionButton(
                            text = if (isArabic) "إضافة" else "Add",
                            icon = Icons.Default.Add,
                            color = EmeraldSavings,
                            onClick = {
                                val minPrice = detectedPrices.minByOrNull { it.priceDzd }?.priceDzd ?: 0.0
                                viewModel.addItem(
                                    name = scannedProductTitle,
                                    nameAr = scannedProductTitleAr,
                                    barcode = scannedBarcode,
                                    estimatedPriceDzd = minPrice
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Store Price Comparison Row
                    Text(
                        text = if (isArabic) "الأسعار في المتاجر المجاورة:" else "Prices in Nearby Stores:",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val minPrice = detectedPrices.minByOrNull { it.priceDzd }?.priceDzd ?: 0.0
                        detectedPrices.forEach { price ->
                            val isCheapest = price.priceDzd == minPrice
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isCheapest) Color(0x2200E676) else Color(0x15FFFFFF))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = if (isCheapest) EmeraldSavings else TextSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isArabic) price.storeNameAr else price.storeName,
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isCheapest) FontWeight.Bold else FontWeight.Normal
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isCheapest) {
                                        Text(
                                            text = if (isArabic) "الأرخص" else "Best",
                                            color = EmeraldSavings,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(end = 6.dp)
                                        )
                                    }
                                    Text(
                                        text = LocalizationUtil.formatDzd(price.priceDzd, isArabic),
                                        color = if (isCheapest) EmeraldSavings else TextPrimary,
                                        fontSize = 13.sp,
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
}
