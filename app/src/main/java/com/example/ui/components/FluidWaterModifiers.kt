package com.example.ui.components

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSavings
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.min

private const val MAX_FLUID_RIPPLES = 6

/**
 * AGSL fragment shader performing physically-inspired 2D fluid wave height-field evaluation,
 * surface normal derivation, refractive pixel displacement with subtle chromatic dispersion,
 * and overhead directional Fresnel specular highlights.
 */
private const val FLUID_WATER_AGSL_SHADER = """
    uniform shader content;
    uniform float2 uResolution;
    uniform float uTime;
    uniform float2 uTouchPos;
    uniform float2 uTouchVel;
    uniform float uWakeEnergy;
    uniform float uPageOffset;
    uniform float uIsDark;
    uniform float4 uRipple0;
    uniform float4 uRipple1;
    uniform float4 uRipple2;
    uniform float4 uRipple3;
    uniform float4 uRipple4;
    uniform float4 uRipple5;

    float2 evalWaveSlope(float2 fragCoord, float4 r, float waveSpeed) {
        if (r.w <= 0.002) {
            return float2(0.0);
        }
        float2 delta = fragCoord - r.xy;
        float dist = length(delta) + 0.001;
        float frontRadius = r.z * waveSpeed;
        float bandDist = dist - frontRadius;

        // Expanding Gaussian packet + viscous exponential attenuation
        float ringWidth = 48.0 + r.z * 34.0;
        float envelope = exp(-(bandDist * bandDist) / (ringWidth * ringWidth));
        float attenuation = exp(-r.z * 2.55) * r.w;

        // Multi-harmonic water crest slope
        float phase = bandDist * 0.145;
        float slope = (-sin(phase) - 0.35 * sin(phase * 1.9)) * envelope * attenuation;
        return (delta / dist) * slope;
    }

    half4 main(float2 fragCoord) {
        float waveSpeed = 880.0;
        float2 grad = float2(0.0);
        grad += evalWaveSlope(fragCoord, uRipple0, waveSpeed);
        grad += evalWaveSlope(fragCoord, uRipple1, waveSpeed);
        grad += evalWaveSlope(fragCoord, uRipple2, waveSpeed);
        grad += evalWaveSlope(fragCoord, uRipple3, waveSpeed);
        grad += evalWaveSlope(fragCoord, uRipple4, waveSpeed);
        grad += evalWaveSlope(fragCoord, uRipple5, waveSpeed);

        // Hydrodynamic bow-wave wake trailing active finger motion
        if (uWakeEnergy > 0.004) {
            float2 dTouch = fragCoord - uTouchPos;
            float touchDist = length(dTouch) + 0.001;
            float wakeRadius = 112.0;
            float wakeEnv = exp(-(touchDist * touchDist) / (wakeRadius * wakeRadius)) * uWakeEnergy;
            float wakePhase = touchDist * 0.115 - uTime * 9.5;
            grad += (dTouch / touchDist) * cos(wakePhase) * wakeEnv * 0.68;
        }

        // Page swipe horizontal water current undulation
        float currentStrength = clamp(abs(uPageOffset), 0.0, 1.0);
        if (currentStrength > 0.002) {
            float shearWave = sin(fragCoord.y * 0.011 + uTime * 4.2) * currentStrength * 0.16;
            grad.x += shearWave;
        }

        float2 displacement = grad * 19.0;
        float2 maxCoord = max(uResolution - float2(1.0), float2(1.0));
        float2 sampleCoord = clamp(fragCoord + displacement, float2(0.0), maxCoord);

        // Subtle chromatic dispersion across refracted wave crests
        float2 chromaShift = displacement * 0.14;
        half4 baseSample = content.eval(sampleCoord);
        half rChannel = content.eval(clamp(sampleCoord + chromaShift, float2(0.0), maxCoord)).r;
        half bChannel = content.eval(clamp(sampleCoord - chromaShift, float2(0.0), maxCoord)).b;
        half3 refracted = half3(rChannel, baseSample.g, bChannel);

        // Fresnel specular reflection & water caustic shimmer
        float gradMag = length(grad);
        float3 normalVec = normalize(float3(-grad.x * 1.4, -grad.y * 1.4, 1.0));
        float3 lightDir = normalize(float3(-0.32, -0.56, 0.76));
        float specular = pow(max(dot(normalVec, lightDir), 0.0), 22.0) * clamp(gradMag * 1.85, 0.0, 1.0);
        float caustic = clamp(gradMag * 0.42, 0.0, 0.34);

        half3 waterTint = uIsDark > 0.5 ? half3(0.18, 0.88, 1.0) : half3(0.05, 0.62, 0.90);
        half3 litRgb = refracted + waterTint * half(caustic * 0.28) + half3(1.0) * half(specular * 0.46);
        return half4(litRgb, baseSample.a);
    }
"""

/**
 * Pre-allocated mutable ripple structure to eliminate GC allocations on the 60-90 FPS render path.
 */
@Stable
class WaterRippleNode {
    var originX: Float = 0f
    var originY: Float = 0f
    var ageSeconds: Float = 0f
    var strength: Float = 0f
    var dirX: Float = 0f
    var dirY: Float = 0f
    var directionalBias: Float = 0f
    var active: Boolean = false
}

/**
 * Physics simulation engine for real-time 2D fluid water ripples, viscous drag damping,
 * hydrodynamic bow-wake momentum, and page-swipe current propagation.
 */
@Stable
class FluidWaterState(
    densityPx: Float
) {
    private val densityScale = densityPx.coerceAtLeast(1f)
    internal val waveSpeedPx = 310f * densityScale
    private val minSpawnDistancePx = 22f * densityScale
    private val viscousFrictionPerSec = 2.65f
    private val wakeDecayPerSec = 4.2f
    private val maxRippleAgeSec = 1.45f

    internal val ripples = Array(MAX_FLUID_RIPPLES) { WaterRippleNode() }
    private var nextRippleSlot = 0

    internal var viewportSize by mutableStateOf(IntSize.Zero)
    internal var touchPosition by mutableStateOf(Offset.Zero)
    internal var wakeVelocity by mutableStateOf(Offset.Zero)
    internal var wakeEnergy by mutableFloatStateOf(0f)
    internal var pageScrollDelta by mutableFloatStateOf(0f)
    internal var simulationTimeSec by mutableFloatStateOf(0f)
    internal var frameTick by mutableLongStateOf(0L)
    internal var isPointerDown by mutableStateOf(false)
    internal var isSimulating by mutableStateOf(false)

    private var activePointerId: PointerId? = null
    private var lastSpawnPos: Offset = Offset.Zero
    private var lastSpawnNanos: Long = 0L
    private var lastMoveNanos: Long = 0L

    val hasActiveDisturbance: Boolean
        get() = wakeEnergy > 0.004f ||
            abs(pageScrollDelta) > 0.004f ||
            ripples.any { it.active && it.strength > 0.003f }

    fun updatePageScrollVelocity(offsetFraction: Float) {
        pageScrollDelta = offsetFraction.coerceIn(-1f, 1f)
        if (abs(offsetFraction) > 0.008f) {
            wakeEnergy = maxOf(wakeEnergy, abs(offsetFraction) * 0.42f)
            isSimulating = true
            frameTick++
        }
    }

    fun onPointerDown(pointerId: PointerId, position: Offset, eventTimeNanos: Long) {
        activePointerId = pointerId
        isPointerDown = true
        touchPosition = position
        wakeVelocity = Offset.Zero
        wakeEnergy = maxOf(wakeEnergy, 0.55f)
        lastSpawnPos = position
        lastSpawnNanos = eventTimeNanos
        lastMoveNanos = eventTimeNanos
        isSimulating = true
        spawnRipple(
            origin = position,
            strength = 0.80f,
            direction = Offset.Zero,
            directionalBias = 0f
        )
    }

    fun onPointerMove(pointerId: PointerId, position: Offset, eventTimeNanos: Long) {
        if (activePointerId != null && activePointerId != pointerId) return

        val dtSec = ((eventTimeNanos - lastMoveNanos) / 1_000_000_000f).coerceIn(0.004f, 0.05f)
        val delta = position - touchPosition
        val dist = hypot(delta.x, delta.y)

        if (dist > 0.5f) {
            val instVel = Offset(delta.x / dtSec, delta.y / dtSec)
            // Fluid inertia + viscous smoothing
            wakeVelocity = Offset(
                x = wakeVelocity.x * 0.55f + instVel.x * 0.45f,
                y = wakeVelocity.y * 0.55f + instVel.y * 0.45f
            )
            val speedNorm = (hypot(wakeVelocity.x, wakeVelocity.y) / (1400f * densityScale)).coerceIn(0f, 1.3f)
            wakeEnergy = (wakeEnergy * 0.65f + (0.35f + speedNorm * 0.65f)).coerceIn(0f, 1.15f)
        }

        touchPosition = position
        lastMoveNanos = eventTimeNanos

        val travelFromSpawn = hypot(position.x - lastSpawnPos.x, position.y - lastSpawnPos.y)
        val elapsedSinceSpawnMs = (eventTimeNanos - lastSpawnNanos) / 1_000_000L
        if (travelFromSpawn >= minSpawnDistancePx && elapsedSinceSpawnMs >= 34L) {
            val dirMag = hypot(delta.x, delta.y).coerceAtLeast(1f)
            val dir = Offset(delta.x / dirMag, delta.y / dirMag)
            val impulse = (0.48f + (hypot(wakeVelocity.x, wakeVelocity.y) / (1800f * densityScale)) * 0.62f)
                .coerceIn(0.45f, 1.15f)

            spawnRipple(
                origin = position,
                strength = impulse,
                direction = dir,
                directionalBias = 0.72f
            )
            lastSpawnPos = position
            lastSpawnNanos = eventTimeNanos
        }
    }

    fun onPointerUp(pointerId: PointerId, position: Offset) {
        if (activePointerId != null && activePointerId != pointerId) return
        isPointerDown = false
        activePointerId = null

        val speed = hypot(wakeVelocity.x, wakeVelocity.y)
        if (speed > 260f * densityScale) {
            val dir = Offset(wakeVelocity.x / speed, wakeVelocity.y / speed)
            val releaseStrength = (0.55f + (speed / (2200f * densityScale)) * 0.55f).coerceIn(0.55f, 1.1f)
            spawnRipple(
                origin = position,
                strength = releaseStrength,
                direction = dir,
                directionalBias = 0.5f
            )
        }
    }

    fun clear() {
        for (i in 0 until MAX_FLUID_RIPPLES) {
            ripples[i].active = false
            ripples[i].strength = 0f
            ripples[i].ageSeconds = 0f
        }
        wakeEnergy = 0f
        wakeVelocity = Offset.Zero
        isPointerDown = false
        activePointerId = null
        pageScrollDelta = 0f
        isSimulating = false
        frameTick++
    }

    internal fun stepPhysics(deltaSeconds: Float) {
        val dt = deltaSeconds.coerceIn(0.001f, 0.035f)
        simulationTimeSec += dt

        // Viscous fluid drag decays wake velocity & energy smoothly
        val dragFactor = exp(-wakeDecayPerSec * dt)
        wakeVelocity = Offset(wakeVelocity.x * dragFactor, wakeVelocity.y * dragFactor)
        if (!isPointerDown) {
            wakeEnergy *= exp(-3.4f * dt)
            if (wakeEnergy < 0.003f) wakeEnergy = 0f
            pageScrollDelta *= exp(-4.8f * dt)
            if (abs(pageScrollDelta) < 0.002f) pageScrollDelta = 0f
        } else {
            wakeEnergy = (wakeEnergy * exp(-1.6f * dt)).coerceAtLeast(0.28f)
        }

        // Propagate and attenuate every active water ripple
        val rippleDecay = exp(-viscousFrictionPerSec * dt)
        var anyActive = false
        for (i in 0 until MAX_FLUID_RIPPLES) {
            val node = ripples[i]
            if (!node.active) continue
            node.ageSeconds += dt
            node.strength *= rippleDecay
            if (node.ageSeconds >= maxRippleAgeSec || node.strength < 0.003f) {
                node.active = false
                node.strength = 0f
            } else {
                anyActive = true
            }
        }
        isSimulating = anyActive || isPointerDown || wakeEnergy > 0.004f || abs(pageScrollDelta) > 0.004f
        frameTick++
    }

    private fun spawnRipple(
        origin: Offset,
        strength: Float,
        direction: Offset,
        directionalBias: Float
    ) {
        val node = ripples[nextRippleSlot]
        nextRippleSlot = (nextRippleSlot + 1) % MAX_FLUID_RIPPLES
        node.originX = origin.x
        node.originY = origin.y
        node.ageSeconds = 0f
        node.strength = strength
        node.dirX = direction.x
        node.dirY = direction.y
        node.directionalBias = directionalBias
        node.active = true
        isSimulating = true
        frameTick++
    }
}

/**
 * Remembers and drives a [FluidWaterState] simulation locked to display refresh rate (60-90+ FPS)
 * via [withFrameNanos], automatically sleeping with 0% overhead when the water settles.
 */
@Composable
fun rememberFluidWaterState(enabled: Boolean = true): FluidWaterState {
    val density = LocalDensity.current.density
    val state = remember(density) { FluidWaterState(densityPx = density) }

    LaunchedEffect(state, enabled) {
        if (!enabled) {
            state.clear()
            return@LaunchedEffect
        }
        snapshotFlow { state.isSimulating }
            .collect { running ->
                if (!running) return@collect
                var lastNanos = 0L
                while (enabled && state.isSimulating) {
                    withFrameNanos { frameNanos ->
                        if (lastNanos != 0L) {
                            val dt = (frameNanos - lastNanos) / 1_000_000_000f
                            state.stepPhysics(dt)
                        }
                        lastNanos = frameNanos
                    }
                }
            }
    }

    return state
}

/**
 * Non-consuming pointer-physics observer modifier.
 * Tracks touches and horizontal/vertical finger drags across the screen on [PointerEventPass.Initial]
 * without blocking buttons, lazy lists, or HorizontalPager swiping.
 */
fun Modifier.fluidWaterTouchPhysics(
    state: FluidWaterState,
    enabled: Boolean = true
): Modifier {
    if (!enabled) return this
    return this
        .onSizeChanged { size ->
            state.viewportSize = size
        }
        .pointerInput(state, enabled) {
            awaitPointerEventScope {
                var wasPressed = false
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    val primary = event.changes.firstOrNull() ?: continue
                    val nowNanos = System.nanoTime()

                    if (primary.pressed && !wasPressed) {
                        wasPressed = true
                        state.onPointerDown(primary.id, primary.position, nowNanos)
                    } else if (primary.pressed && wasPressed) {
                        state.onPointerMove(primary.id, primary.position, nowNanos)
                    } else if (!primary.pressed && wasPressed) {
                        wasPressed = false
                        state.onPointerUp(primary.id, primary.position)
                    }
                }
            }
        }
}

/**
 * Applies hardware-accelerated AGSL [RuntimeShader] fluid water refraction & Fresnel specular
 * distortion on Android 13+ (API 33+) via [Modifier.graphicsLayer], plus an optimized Canvas
 * hydrodynamic caustic & capillary ripple highlight pass across all Android versions (API 24+).
 */
fun Modifier.fluidWaterDistortion(
    state: FluidWaterState,
    enabled: Boolean = true,
    isDarkMode: Boolean = true
): Modifier = composed {
    if (!enabled) return@composed this

    val runtimeShader = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                RuntimeShader(FLUID_WATER_AGSL_SHADER)
            } catch (_: Throwable) {
                null
            }
        } else {
            null
        }
    }

    val shaderModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && runtimeShader != null) {
        Modifier.graphicsLayer {
            // Read frameTick so RenderEffect updates cleanly per vsync only while disturbed
            @Suppress("UNUSED_VARIABLE")
            val tick = state.frameTick
            if (state.hasActiveDisturbance && size.width > 0f && size.height > 0f) {
                runtimeShader.setFloatUniform("uResolution", size.width, size.height)
                runtimeShader.setFloatUniform("uTime", state.simulationTimeSec)
                runtimeShader.setFloatUniform("uTouchPos", state.touchPosition.x, state.touchPosition.y)
                runtimeShader.setFloatUniform("uTouchVel", state.wakeVelocity.x, state.wakeVelocity.y)
                runtimeShader.setFloatUniform("uWakeEnergy", state.wakeEnergy)
                runtimeShader.setFloatUniform("uPageOffset", state.pageScrollDelta)
                runtimeShader.setFloatUniform("uIsDark", if (isDarkMode) 1f else 0f)

                setRippleUniform(runtimeShader, "uRipple0", state.ripples[0])
                setRippleUniform(runtimeShader, "uRipple1", state.ripples[1])
                setRippleUniform(runtimeShader, "uRipple2", state.ripples[2])
                setRippleUniform(runtimeShader, "uRipple3", state.ripples[3])
                setRippleUniform(runtimeShader, "uRipple4", state.ripples[4])
                setRippleUniform(runtimeShader, "uRipple5", state.ripples[5])

                renderEffect = RenderEffect
                    .createRuntimeShaderEffect(runtimeShader, "content")
                    .asComposeRenderEffect()
            } else {
                renderEffect = null
            }
        }
    } else {
        Modifier
    }

    val accentCyan = ElectricCyan
    val accentEmerald = EmeraldSavings
    val isApi33Plus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    this
        .then(shaderModifier)
        .drawWithContent {
            drawContent()

            // Read frameTick for crisp synchronized caustic shimmer & pre-API-33 wave rings
            @Suppress("UNUSED_VARIABLE")
            val tick = state.frameTick
            if (!state.hasActiveDisturbance) return@drawWithContent

            val alphaScale = if (isApi33Plus) {
                if (isDarkMode) 0.30f else 0.24f
            } else {
                if (isDarkMode) 0.65f else 0.52f
            }

            // 1. Expanding Capillary & Primary Wave Rings
            for (i in 0 until MAX_FLUID_RIPPLES) {
                val node = state.ripples[i]
                if (!node.active || node.strength <= 0.01f) continue

                val radius = node.ageSeconds * state.waveSpeedPx
                if (radius <= 2f) continue

                val alpha = (node.strength * exp(-node.ageSeconds * 1.4f) * alphaScale).coerceIn(0f, 0.75f)
                if (alpha <= 0.008f) continue

                val center = Offset(node.originX, node.originY)
                val ringStrokeWidth = (3.4.dp.toPx() * (0.4f + node.strength * 0.6f)).coerceAtLeast(1.2.dp.toPx())

                // Outer refractive glow halo
                drawCircle(
                    brush = Brush.radialGradient(
                        0.72f to Color.Transparent,
                        0.88f to accentCyan.copy(alpha = alpha * 0.34f),
                        0.96f to Color.White.copy(alpha = alpha * 0.24f),
                        1.00f to Color.Transparent,
                        center = center,
                        radius = radius + 16.dp.toPx()
                    ),
                    radius = radius + 16.dp.toPx(),
                    center = center
                )

                // Primary fluid wave crest
                drawCircle(
                    color = accentCyan.copy(alpha = alpha * 0.75f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = ringStrokeWidth)
                )

                // Secondary trailing capillary ring
                val innerRadius = radius - 22.dp.toPx()
                if (innerRadius > 4f) {
                    drawCircle(
                        color = accentEmerald.copy(alpha = alpha * 0.45f),
                        radius = innerRadius,
                        center = center,
                        style = Stroke(width = (ringStrokeWidth * 0.55f).coerceAtLeast(1f))
                    )
                }

                // Specular highlight arc oriented against drag direction
                if (node.directionalBias > 0.2f) {
                    val angleDeg = Math.toDegrees(atan2(node.dirY.toDouble(), node.dirX.toDouble())).toFloat()
                    drawArc(
                        color = Color.White.copy(alpha = alpha * 0.65f),
                        startAngle = angleDeg - 42f,
                        sweepAngle = 84f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f),
                        style = Stroke(width = (ringStrokeWidth * 0.7f).coerceAtLeast(1f))
                    )
                }
            }

            // 2. Touch Wake Caustic Meniscus under active finger
            if (state.wakeEnergy > 0.04f && (state.isPointerDown || hypot(state.wakeVelocity.x, state.wakeVelocity.y) > 40f)) {
                val touchAlpha = (state.wakeEnergy * 0.32f * (if (isApi33Plus) 0.75f else 1.0f)).coerceIn(0f, 0.45f)
                val wakeR = (38.dp.toPx() + min(hypot(state.wakeVelocity.x, state.wakeVelocity.y) * 0.014f, 26.dp.toPx()))
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = touchAlpha * 0.45f),
                            accentCyan.copy(alpha = touchAlpha * 0.65f),
                            accentEmerald.copy(alpha = touchAlpha * 0.25f),
                            Color.Transparent
                        ),
                        center = state.touchPosition,
                        radius = wakeR
                    ),
                    radius = wakeR,
                    center = state.touchPosition,
                    blendMode = if (isDarkMode) BlendMode.Screen else BlendMode.SrcOver
                )
            }
        }
}

private fun setRippleUniform(
    shader: RuntimeShader,
    uniformName: String,
    node: WaterRippleNode
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (node.active && node.strength > 0.002f) {
            shader.setFloatUniform(
                uniformName,
                node.originX,
                node.originY,
                node.ageSeconds,
                node.strength
            )
        } else {
            shader.setFloatUniform(uniformName, 0f, 0f, 0f, 0f)
        }
    }
}

/**
 * Convenience container wrapping content with synchronized [fluidWaterTouchPhysics]
 * and hardware-accelerated [fluidWaterDistortion] at 60-90 FPS.
 */
@Composable
fun InteractiveFluidWaterSurface(
    waterState: FluidWaterState,
    enabled: Boolean = true,
    isDarkMode: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .fluidWaterTouchPhysics(state = waterState, enabled = enabled)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .fluidWaterDistortion(
                    state = waterState,
                    enabled = enabled,
                    isDarkMode = isDarkMode
                )
        ) {
            content()
        }
    }
}
