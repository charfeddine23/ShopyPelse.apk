package com.example.ui.components

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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.CyanTintBg
import com.example.ui.theme.CyanTintBorder
import com.example.ui.theme.CyanTintText
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSavings
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.LightCardBorder
import com.example.ui.theme.LightCardSurface
import com.example.ui.theme.MintTintBg
import com.example.ui.theme.MintTintBorder
import com.example.ui.theme.MintTintText
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.PeachTintBg
import com.example.ui.theme.PeachTintBorder
import com.example.ui.theme.PeachTintText
import com.example.ui.theme.StrikePulseLavender
import com.example.ui.theme.StrikePulseTopCyan
import com.example.ui.theme.StrikePulseWarmPeach

@Composable
fun LiquidGlassBackground(
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_glow")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    if (isDarkMode) {
        // Deep Dark Liquid Glass with Neon Glow Orbs
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            BackgroundDark,
                            Color(0xFF0F1522),
                            Color(0xFF161E2E)
                        )
                    )
                )
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x2200E5FF),
                                Color(0x0A00E5FF),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.85f, size.height * 0.12f),
                            radius = size.width * 0.65f * pulse
                        ),
                        radius = size.width * 0.65f * pulse,
                        center = Offset(size.width * 0.85f, size.height * 0.12f)
                    )

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x2200E676),
                                Color(0x0800E676),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.15f, size.height * 0.8f),
                            radius = size.width * 0.7f * pulse
                        ),
                        radius = size.width * 0.7f * pulse,
                        center = Offset(size.width * 0.15f, size.height * 0.8f)
                    )
                }
        ) {
            content()
        }
    } else {
        // StrikePulse Aurora Daylight Canvas (Soft Cyan, Warm Apricot, Pale Lavender)
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .drawBehind {
                    // Top Cyan Sky Orb
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                StrikePulseTopCyan.copy(alpha = 0.95f),
                                StrikePulseTopCyan.copy(alpha = 0.45f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.2f, size.height * 0.08f),
                            radius = size.width * 0.85f
                        ),
                        radius = size.width * 0.85f,
                        center = Offset(size.width * 0.2f, size.height * 0.08f)
                    )

                    // Right Center Warm Peach/Apricot Orb
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                StrikePulseWarmPeach.copy(alpha = 0.95f),
                                StrikePulseWarmPeach.copy(alpha = 0.4f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.95f, size.height * 0.42f),
                            radius = size.width * 0.8f * pulse
                        ),
                        radius = size.width * 0.8f * pulse,
                        center = Offset(size.width * 0.95f, size.height * 0.42f)
                    )

                    // Lower Left Soft Lavender Orb
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                StrikePulseLavender.copy(alpha = 0.9f),
                                StrikePulseLavender.copy(alpha = 0.35f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.08f, size.height * 0.78f),
                            radius = size.width * 0.75f * pulse
                        ),
                        radius = size.width * 0.75f * pulse,
                        center = Offset(size.width * 0.08f, size.height * 0.78f)
                    )
                }
        ) {
            content()
        }
    }
}

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    glowColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val defaultBg = if (isLight) LightCardSurface else GlassSurface
    val resolvedBg = backgroundColor ?: defaultBg

    val borderStroke = if (isLight) {
        if (glowColor != null) {
            BorderStroke(1.5.dp, glowColor.copy(alpha = 0.7f))
        } else {
            BorderStroke(1.dp, borderColor ?: LightCardBorder)
        }
    } else {
        if (glowColor != null) {
            BorderStroke(
                1.dp,
                Brush.linearGradient(
                    listOf(glowColor.copy(alpha = 0.6f), glowColor.copy(alpha = 0.15f), Color(0x10FFFFFF))
                )
            )
        } else {
            BorderStroke(
                1.dp,
                Brush.linearGradient(
                    listOf(Color(0x33FFFFFF), Color(0x10FFFFFF), Color(0x05FFFFFF))
                )
            )
        }
    }

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(bounded = true, color = ElectricCyan),
            onClick = onClick
        )
    } else Modifier

    Box(
        modifier = modifier
            .clip(shape)
            .background(resolvedBg)
            .border(borderStroke, shape)
            .then(clickableModifier)
    ) {
        if (!isLight) {
            // Specular highlight line along top edge for glossy glass effect in dark mode
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, Color(0x40FFFFFF), Color(0x20FFFFFF), Color.Transparent)
                        )
                    )
            )
        }
        content()
    }
}

@Composable
fun NeonBadge(
    text: String,
    accentColor: Color = EmeraldSavings,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight

    val (bg, border, contentColor) = if (isLight) {
        when (accentColor) {
            EmeraldSavings, Color(0xFF10B981) -> Triple(MintTintBg, MintTintBorder, MintTintText)
            NeonAmber, Color(0xFFFFB300) -> Triple(PeachTintBg, PeachTintBorder, PeachTintText)
            else -> Triple(CyanTintBg, CyanTintBorder, CyanTintText)
        }
    } else {
        Triple(accentColor.copy(alpha = 0.16f), accentColor.copy(alpha = 0.45f), accentColor)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .background(bg)
            .border(BorderStroke(1.dp, border), RoundedCornerShape(30.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = text,
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun GlowingActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    color: Color = EmeraldSavings,
    enabled: Boolean = true
) {
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight

    val (btnBrush, btnBorder, contentColor) = if (isLight) {
        if (enabled) {
            Triple(
                Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.85f))),
                BorderStroke(1.dp, color),
                Color.White
            )
        } else {
            Triple(
                Brush.horizontalGradient(listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))),
                BorderStroke(1.dp, Color(0xFFCBD5E1)),
                Color(0xFF94A3B8)
            )
        }
    } else {
        if (enabled) {
            Triple(
                Brush.horizontalGradient(listOf(color.copy(alpha = 0.9f), color.copy(alpha = 0.7f))),
                BorderStroke(1.dp, color),
                Color(0xFF06141D)
            )
        } else {
            Triple(
                Brush.horizontalGradient(listOf(Color.Gray.copy(alpha = 0.3f), Color.DarkGray.copy(alpha = 0.3f))),
                BorderStroke(1.dp, Color.Gray),
                Color.Gray
            )
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(btnBrush)
            .border(btnBorder, RoundedCornerShape(16.dp))
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = Color.White),
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = contentColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
