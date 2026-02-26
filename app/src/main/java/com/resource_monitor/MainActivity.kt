package com.resource_monitor

import android.os.Bundle
import android.app.ActivityManager
import android.content.Context
import android.opengl.GLSurfaceView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.ui.draw.scale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.resource_monitor.ui.theme.MainTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SampleScreen(this)
                }
            }
        }
    }
}

@Composable
fun SampleScreen(context: Context) {
    var ramUsedGb by remember { mutableStateOf(0.0) }
    var totalRamGb by remember { mutableStateOf(0.0) }
    var cpuUsage by remember { mutableStateOf(0.0) }
    var gpuFps by remember { mutableStateOf(0) }
    var glRenderer by remember { mutableStateOf<GLRenderer?>(null) }
    
    var ramPressed by remember { mutableStateOf(false) }
    var cpuPressed by remember { mutableStateOf(false) }
    var gpuPressed by remember { mutableStateOf(false) }
    var npuPressed by remember { mutableStateOf(false) }
    
    val ramScale by animateFloatAsState(
        targetValue = if (ramPressed) 0.995f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 300f),
        label = "ram_scale"
    )
    val cpuScale by animateFloatAsState(
        targetValue = if (cpuPressed) 0.995f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 300f),
        label = "cpu_scale"
    )
    val gpuScale by animateFloatAsState(
        targetValue = if (gpuPressed) 0.995f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 300f),
        label = "gpu_scale"
    )
    val npuScale by animateFloatAsState(
        targetValue = if (npuPressed) 0.995f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 300f),
        label = "npu_scale"
    )
    
    LaunchedEffect(ramPressed) {
        if (ramPressed) {
            delay(100)
            ramPressed = false
        }
    }
    
    LaunchedEffect(cpuPressed) {
        if (cpuPressed) {
            delay(100)
            cpuPressed = false
        }
    }
    
    LaunchedEffect(gpuPressed) {
        if (gpuPressed) {
            delay(100)
            gpuPressed = false
        }
    }
    
    LaunchedEffect(npuPressed) {
        if (npuPressed) {
            delay(100)
            npuPressed = false
        }
    }
    
    LaunchedEffect(Unit) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        totalRamGb = memoryInfo.totalMem / (1024.0 * 1024 * 1024)
        
        while (true) {
            // RAM
            activityManager.getMemoryInfo(memoryInfo)
            val usedMemory = memoryInfo.totalMem - memoryInfo.availMem
            ramUsedGb = usedMemory / (1024.0 * 1024 * 1024)
            
            // CPU(proc/selfの推移を観察)
            try {
                val startTime = System.currentTimeMillis()
                val startStat = java.io.File("/proc/self/stat").readText()
                val startParts = startStat.split(Regex("\\s+"))
                val startCpuTime = (startParts[13].toLong() + startParts[14].toLong())
                
                val loopCount = 10000000
                var dummy = 0L
                for (i in 0 until loopCount) {
                    dummy += (i * i)
                }
                
                val endTime = System.currentTimeMillis()
                val endStat = java.io.File("/proc/self/stat").readText()
                val endParts = endStat.split(Regex("\\s+"))
                val endCpuTime = (endParts[13].toLong() + endParts[14].toLong())
                
                val elapsedRealTime = endTime - startTime
                val elapsedCpuTime = endCpuTime - startCpuTime
                
                if (elapsedRealTime > 0) {
                    cpuUsage = (elapsedCpuTime.toDouble() / elapsedRealTime.toDouble() * 100).coerceIn(0.0, 100.0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            delay(500)
        }
    }
    
    LaunchedEffect(glRenderer) {
        while (glRenderer != null) {
            gpuFps = glRenderer!!.fps
            delay(500)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.25f)
                .scale(ramScale)
                .clip(RoundedCornerShape(16.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = true, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    onClick = { ramPressed = !ramPressed }
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE0F7FA).copy(alpha = 0.8f),
                            Color(0xFF80DEEA).copy(alpha = 0.5f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .background(Color.Transparent),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Text("RAM", fontSize = 20.sp, color = Color(0xFF191C1B))
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text("%.1f/%.1fGB".format(ramUsedGb, totalRamGb), fontSize = 24.sp, color = Color(0xFF191C1B))
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.33f)
                .scale(cpuScale)
                .clip(RoundedCornerShape(16.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = true, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    onClick = { cpuPressed = !cpuPressed }
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE0F7FA).copy(alpha = 0.8f),
                            Color(0xFF80DEEA).copy(alpha = 0.5f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .background(Color.Transparent),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Text("CPU", fontSize = 20.sp, color = Color(0xFF191C1B))
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text("%.1f%%".format(cpuUsage), fontSize = 24.sp, color = Color(0xFF191C1B))
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .scale(gpuScale)
                .clip(RoundedCornerShape(16.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = true, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    onClick = { gpuPressed = !gpuPressed }
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE0F7FA).copy(alpha = 0.8f),
                            Color(0xFF80DEEA).copy(alpha = 0.5f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .background(Color.Transparent),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Text("GPU", fontSize = 20.sp, color = Color(0xFF191C1B))
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    // GLSurfaceView（立方体）を透明にして非表示
                    AndroidView(
                        factory = { ctx ->
                            GLSurfaceView(ctx).apply {
                                setEGLContextClientVersion(2)
                                val renderer = GLRenderer()
                                glRenderer = renderer
                                setRenderer(renderer)
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0f)
                    )
                    
                    Text("$gpuFps FPS", fontSize = 24.sp, color = Color(0xFF191C1B))
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(1.0f)
                .scale(npuScale)
                .clip(RoundedCornerShape(16.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = true, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    onClick = { npuPressed = !npuPressed }
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE0F7FA).copy(alpha = 0.8f),
                            Color(0xFF80DEEA).copy(alpha = 0.5f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .background(Color.Transparent),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Text("NPU(TPU)", fontSize = 20.sp, color = Color(0xFF191C1B))
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    // TODO: NPUの使用率を取得して表示
                    Text("0%", fontSize = 24.sp, color = Color(0xFF191C1B))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SampleScreenPreview() {
    MainTheme {
        SampleScreen(context = androidx.compose.ui.platform.LocalContext.current)
    }
}
