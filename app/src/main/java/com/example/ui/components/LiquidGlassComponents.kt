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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSavings
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassSurfaceLight
import com.example.ui.theme.TextPrimary

@Composable
fun LiquidGlassBackground(
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_glow")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val backgroundColors = if (isDarkMode) {
        listOf(
            BackgroundDark,
            Color(0xFF0F1522),
            Color(0xFF161E2E)
        )
    } else {
        listOf(
            Color(0xFFE2E8F0),
            Color(0xFFEDF2F7),
            Color(0xFFF8FAFC)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = backgroundColors))
            .drawBehind {
                // Top-right subtle cyan ambient blur orb
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

                // Bottom-left subtle emerald ambient blur orb
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
}

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = GlassSurface,
    borderColor: Color = GlassBorder,
    glowColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val borderBrush = if (glowColor != null) {
        Brush.linearGradient(
            colors = listOf(glowColor.copy(alpha = 0.6f), glowColor.copy(alpha = 0.15f), Color(0x10FFFFFF))
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0x33FFFFFF), Color(0x10FFFFFF), Color(0x05FFFFFF))
        )
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
            .background(backgroundColor)
            .border(
                border = BorderStroke(1.dp, borderBrush),
                shape = shape
            )
            .then(clickableModifier)
    ) {
        // Specular highlight line along top edge for glossy glass effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x40FFFFFF),
                            Color(0x20FFFFFF),
                            Color.Transparent
                        )
                    )
                )
        )
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
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .background(accentColor.copy(alpha = 0.16f))
            .border(
                BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
                shape = RoundedCornerShape(30.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = text,
            color = accentColor,
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
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = if (enabled) listOf(
                        color.copy(alpha = 0.9f),
                        color.copy(alpha = 0.7f)
                    ) else listOf(Color.Gray.copy(alpha = 0.3f), Color.DarkGray.copy(alpha = 0.3f))
                )
            )
            .border(
                BorderStroke(1.dp, if (enabled) color else Color.Gray),
                RoundedCornerShape(16.dp)
            )
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
                    tint = Color(0xFF06141D),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = Color(0xFF06141D),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
