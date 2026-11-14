package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSavings
import com.example.ui.theme.NeonAmber
import kotlinx.coroutines.launch
import kotlin.math.abs

private data class TutorialStep(
    val badgeAr: String,
    val badgeEn: String,
    val titleAr: String,
    val titleEn: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val tipAr: String,
    val tipEn: String,
    val accentColor: Color
)

private val TutorialSteps = listOf(
    TutorialStep(
        badgeAr = "مرحباً بك",
        badgeEn = "WELCOME",
        titleAr = "تسوق أذكى مع Shopy Pulse",
        titleEn = "Shop Smarter with Shopy Pulse",
        descriptionAr = "تطبيقك الذكي لإدارة قائمة المشتريات، مقارنة أسعار المتاجر بالدينار الجزائري، ومتابعة ميزانيتك بسلاسة تامة.",
        descriptionEn = "Your smart companion to manage shopping lists, compare supermarket baskets in DZD, and track monthly budgets effortlessly.",
        tipAr = "اسحب يميناً أو يساراً لاستكشاف المزايا",
        tipEn = "Swipe left or right to explore features",
        accentColor = ElectricCyan
    ),
    TutorialStep(
        badgeAr = "قائمة التسوق",
        badgeEn = "SMART LIST",
        titleAr = "سحب للأعلى لإضافة المنتجات",
        titleEn = "Pull Up to Add Items",
        descriptionAr = "عندما تكون قائمة التسوق فارغة، اسحب البطاقة للأعلى بإصبع واحد لفتح نافذة الإضافة فوراً، أو أضف من العناصر الشائعة بلمسة واحدة.",
        descriptionEn = "When your list is empty, pull the card up with one finger to add items instantly, or tap quick frequent presets above.",
        tipAr = "اسحب أي منتج جانبياً لحذفه بسرعة",
        tipEn = "Swipe any item horizontally to delete it",
        accentColor = EmeraldSavings
    ),
    TutorialStep(
        badgeAr = "مقارنة الأسعار",
        badgeEn = "PRICE RADAR",
        titleAr = "اكتشف أرخص متجر لسلتك",
        titleEn = "Find the Lowest Basket Price",
        descriptionAr = "يقارن التطبيق إجمالي قائمة مشترياتك بين كبرى المتاجر تلقائياً ويرشح لك أفضل متجر بأكبر توفير ممكن.",
        descriptionEn = "Automatically compares your full basket across top supermarkets and highlights the store that saves you the most.",
        tipAr = "تُحدث الأسعار تلقائياً ويمكنك إضافة منتجات أو مجموعات",
        tipEn = "Prices sync automatically and support custom groups",
        accentColor = TutorialSky
    ),
    TutorialStep(
        badgeAr = "وضع السوبرماركت",
        badgeEn = "CART & BUDGET",
        titleAr = "تسوّق داخل المتجر وتحكم بميزانيتك",
        titleEn = "In-Store Cart & Live Budget",
        descriptionAr = "افتح وضع السلة أثناء التسوق لتحديد المنتجات المشتراة ومراقبة عداد الميزانية المباشر، ثم انقل الفاتورة للإحصائيات بضغطة واحدة.",
        descriptionEn = "Switch to Cart Mode in-store to check off items against your live budget ring, then transfer totals straight to analytics.",
        tipAr = "راقب شريط الميزانية حتى لا تتجاوز الحد الشهري",
        tipEn = "Watch the progress ring to stay within budget",
        accentColor = NeonAmber
    ),
    TutorialStep(
        badgeAr = "الإيماءات الذكية",
        badgeEn = "PRO GESTURES",
        titleAr = "اسحب بإصبعين لوضع ملء الشاشة",
        titleEn = "Two-Finger Full Screen Swipe",
        descriptionAr = "اسحب بإصبعين للأعلى لتفعيل وضع ملء الشاشة وإخفاء الشريط العلوي، واسحب بإصبعين للأسفل للخروج مع ثبات تام للصفحات.",
        descriptionEn = "Swipe up with two fingers to hide the status bar for Full Screen mode, and swipe down with two fingers to restore it seamlessly.",
        tipAr = "يمكنك التحكم في هذه الإيماءة من الإعدادات في أي وقت",
        tipEn = "Toggle this gesture anytime inside Settings",
        accentColor = TutorialViolet
    ),
    TutorialStep(
        badgeAr = "المشاركة والتحديثات",
        badgeEn = "SYNC & UPDATES",
        titleAr = "مشاركة QR دون إنترنت وتحديث مباشر",
        titleEn = "Offline QR Share & Live Updates",
        descriptionAr = "شارك قائمتك عبر رمز QR دون الحاجة للإنترنت، احفظ نسخة احتياطية على Google Drive، واحصل على التحديثات مباشرة داخل التطبيق.",
        descriptionEn = "Share lists offline via QR code, back up to Google Drive, and download official updates directly inside the app.",
        tipAr = "يمكنك إعادة فتح هذا الدليل في أي وقت من الإعدادات",
        tipEn = "You can replay this guide anytime from Settings",
        accentColor = EmeraldSavings
    )
)

@Composable
fun AppTutorialOverlay(
    isArabic: Boolean,
    isDarkMode: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val layoutDir = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { TutorialSteps.size })
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val currentStep = TutorialSteps[pagerState.currentPage]
    val animatedAccent by animateColorAsState(
        targetValue = currentStep.accentColor,
        animationSpec = tween(durationMillis = 420),
        label = "tutorial_accent"
    )

    val infiniteBg = rememberInfiniteTransition(label = "tutorial_aurora")
    val auroraShift by infiniteBg.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aurora_shift"
    )

    val scrimBg = if (isDarkMode) Color(0xEC060B16) else Color(0xEEF8FAFC)
    val cardBg = if (isDarkMode) {
        Brush.verticalGradient(listOf(Color(0xFF131E34), Color(0xFF0A1120)))
    } else {
        Brush.verticalGradient(listOf(Color.White, Color(0xFFF1F5F9)))
    }
    val titleColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val bodyColor = if (isDarkMode) Color(0xFFCBD5E1) else Color(0xFF475569)
    val mutedColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(scrimBg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .testTag("app_tutorial_overlay"),
        contentAlignment = Alignment.Center
    ) {
        // Dynamic Ambient Aurora Radial Glow behind card
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            animatedAccent.copy(alpha = (if (isDarkMode) 0.22f else 0.14f) * auroraShift),
                            Color.Transparent
                        ),
                        radius = 850f
                    )
                )
        )

        CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val adaptiveSceneHeight = (maxHeight * 0.30f).coerceIn(148.dp, 220.dp)
                val adaptivePagerHeight = (maxHeight * 0.56f).coerceIn(310.dp, 420.dp)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 430.dp)
                        .shadow(
                            elevation = if (isDarkMode) 28.dp else 18.dp,
                            shape = RoundedCornerShape(30.dp),
                            ambientColor = animatedAccent.copy(alpha = 0.35f),
                            spotColor = animatedAccent.copy(alpha = 0.45f)
                        )
                        .clip(RoundedCornerShape(30.dp))
                        .background(cardBg)
                        .border(
                            BorderStroke(
                                1.3.dp,
                                Brush.linearGradient(
                                    colors = listOf(
                                        animatedAccent.copy(alpha = 0.72f),
                                        Color.White.copy(alpha = if (isDarkMode) 0.20f else 0.7f),
                                        animatedAccent.copy(alpha = 0.32f)
                                    )
                                )
                            ),
                            RoundedCornerShape(30.dp)
                        )
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                // Top Header Bar: Step Counter + Skip (48dp touch target)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(animatedAccent.copy(alpha = 0.14f))
                            .border(
                                BorderStroke(1.dp, animatedAccent.copy(alpha = 0.45f)),
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = animatedAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${pagerState.currentPage + 1} / ${TutorialSteps.size}",
                            color = animatedAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .defaultMinSize(minWidth = 64.dp, minHeight = 48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, color = animatedAccent),
                                onClick = {
                                    try {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    } catch (_: Exception) {}
                                    onClose()
                                }
                            )
                            .padding(horizontal = 10.dp)
                            .testTag("tutorial_skip_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isArabic) "تخطي" else "Skip",
                                color = mutedColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = if (isArabic) "تخطي الدليل" else "Skip tutorial",
                                tint = mutedColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Interactive Parallax Horizontal Pager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(adaptivePagerHeight)
                        .testTag("tutorial_pager")
                ) { page ->
                    val step = TutorialSteps[page]
                    val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).coerceIn(-1f, 1f)
                    val absOffset = abs(pageOffset)
                    val cardScale = 1f - absOffset * 0.08f
                    val cardAlpha = (1f - absOffset * 0.45f).coerceIn(0.4f, 1f)

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = cardScale
                                scaleY = cardScale
                                alpha = cardAlpha
                            }
                            .padding(horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Crisp 60fps Animated Canvas Scene
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(adaptiveSceneHeight)
                                .clip(RoundedCornerShape(22.dp))
                                .background(
                                    if (isDarkMode) Color(0x66060D1A) else Color(0x88E2E8F0)
                                )
                                .border(
                                    BorderStroke(1.dp, step.accentColor.copy(alpha = 0.25f)),
                                    RoundedCornerShape(22.dp)
                                )
                                .graphicsLayer {
                                    translationX = -pageOffset * 28f
                                }
                        ) {
                            TutorialStepAnimatedScene(
                                stepIndex = page,
                                accentColor = step.accentColor,
                                isDarkMode = isDarkMode,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Step Badge Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(step.accentColor.copy(alpha = 0.14f))
                                .border(BorderStroke(1.dp, step.accentColor.copy(alpha = 0.4f)), RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isArabic) step.badgeAr else step.badgeEn,
                                color = step.accentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // Step Headline
                        Text(
                            text = if (isArabic) step.titleAr else step.titleEn,
                            color = titleColor,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )

                        // Step Description
                        Text(
                            text = if (isArabic) step.descriptionAr else step.descriptionEn,
                            color = bodyColor,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        // Subtle Pro Tip Pill
                        Text(
                            text = if (isArabic) "• ${step.tipAr}" else "• ${step.tipEn}",
                            color = step.accentColor.copy(alpha = 0.92f),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Morphing Liquid Capsule Progress Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(TutorialSteps.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        val pillWidth by animateDpAsState(
                            targetValue = if (isSelected) 28.dp else 8.dp,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "indicator_width_$index"
                        )
                        val dotColor = if (isSelected) animatedAccent else mutedColor.copy(alpha = 0.35f)
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(pillWidth)
                                .clip(CircleShape)
                                .background(dotColor)
                                .clickable {
                                    scope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom Action Controls: Back + Next / Finish
                val isFirst = pagerState.currentPage == 0
                val isLast = pagerState.currentPage == TutorialSteps.lastIndex

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isFirst) {
                        Box(
                            modifier = Modifier
                                .weight(0.38f)
                                .height(50.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isDarkMode) Color(0x18FFFFFF) else Color(0xFFE2E8F0))
                                .border(
                                    BorderStroke(1.dp, if (isDarkMode) Color(0x30FFFFFF) else Color(0xFFCBD5E1)),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true, color = animatedAccent),
                                    onClick = {
                                        try {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        } catch (_: Exception) {}
                                        scope.launch {
                                            pagerState.animateScrollToPage(
                                                (pagerState.currentPage - 1).coerceAtLeast(0)
                                            )
                                        }
                                    }
                                )
                                .testTag("tutorial_back_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                    tint = titleColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isArabic) "السابق" else "Back",
                                    color = titleColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Primary Glowing Gradient Next / Start Shopping Button
                    Box(
                        modifier = Modifier
                            .weight(if (isFirst) 1f else 0.62f)
                            .height(50.dp)
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(16.dp),
                                ambientColor = animatedAccent.copy(alpha = 0.45f),
                                spotColor = animatedAccent.copy(alpha = 0.55f)
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(animatedAccent, EmeraldSavings)
                                )
                            )
                            .border(
                                BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, color = Color.White),
                                onClick = {
                                    if (isLast) {
                                        try {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        } catch (_: Exception) {}
                                        onClose()
                                    } else {
                                        try {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        } catch (_: Exception) {}
                                        scope.launch {
                                            pagerState.animateScrollToPage(
                                                (pagerState.currentPage + 1).coerceAtMost(TutorialSteps.lastIndex)
                                            )
                                        }
                                    }
                                }
                            )
                            .testTag(if (isLast) "tutorial_finish_button" else "tutorial_next_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when {
                                    isLast && isArabic -> "ابدأ التسوق الآن"
                                    isLast -> "Start Shopping"
                                    isArabic -> "التالي"
                                    else -> "Next"
                                },
                                color = Color(0xFF04121D),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = if (isLast) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFF04121D),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
            }
        }
    }
}
