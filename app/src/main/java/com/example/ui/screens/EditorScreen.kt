package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DEFAULT_HTML_TEMPLATE
import com.example.ui.theme.CodeAreaBackground
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DeepNavyBackground
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun EditorScreen(
    codeState: String,
    onCodeChange: (String) -> Unit,
    onRunCode: () -> Unit
) {
    val context = LocalContext.current
    var isTemplateMenuExpanded by remember { mutableStateOf(false) }

    val lineCount = remember(codeState) {
        if (codeState.isEmpty()) 1 else codeState.count { it == '\n' } + 1
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavyBackground)
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Editor Toolbar Header
            Surface(
                color = DarkSurfaceCard,
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "index.html",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$lineCount lines",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Row {
                        // Templates Menu
                        Box {
                            IconButton(onClick = { isTemplateMenuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.PostAdd,
                                    contentDescription = "Templates",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = isTemplateMenuExpanded,
                                onDismissRequest = { isTemplateMenuExpanded = false },
                                modifier = Modifier.background(DarkSurfaceCard)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Default AI Template", color = TextPrimary) },
                                    onClick = {
                                        onCodeChange(DEFAULT_HTML_TEMPLATE)
                                        isTemplateMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("3D Canvas Animation", color = TextPrimary) },
                                    onClick = {
                                        onCodeChange(CANVAS_3D_TEMPLATE)
                                        isTemplateMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Modern Glassmorphism Card", color = TextPrimary) },
                                    onClick = {
                                        onCodeChange(GLASS_CARD_TEMPLATE)
                                        isTemplateMenuExpanded = false
                                    }
                                )
                            }
                        }

                        // Copy Code
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Code Editor Content", codeState)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy All",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Clear Code
                        IconButton(onClick = { onCodeChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear Code",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Monospaced Code Text Editor Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(CodeAreaBackground, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                    .border(
                        1.dp,
                        DarkSurfaceBorder,
                        RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                    )
                    .padding(12.dp)
            ) {
                if (codeState.isEmpty()) {
                    Text(
                        text = "<!-- Type or paste your HTML/CSS/JS web code here, or generate code in Chat -->",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = TextSecondary.copy(alpha = 0.5f)
                        )
                    )
                }

                BasicTextField(
                    value = codeState,
                    onValueChange = onCodeChange,
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 18.sp
                    ),
                    cursorBrush = SolidColor(CyanPrimary),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Fast Floating Action Button (FAB) to "Run Code"
        ExtendedFloatingActionButton(
            onClick = onRunCode,
            icon = {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Run Code"
                )
            },
            text = {
                Text(
                    text = "Run Code",
                    fontWeight = FontWeight.Bold
                )
            },
            containerColor = CyanPrimary,
            contentColor = DeepNavyBackground,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}

private val CANVAS_3D_TEMPLATE = """<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Interactive Neon Particles</title>
    <style>
        body { margin: 0; overflow: hidden; background: #080c14; }
        canvas { display: block; }
    </style>
</head>
<body>
    <canvas id="canvas"></canvas>
    <script>
        const canvas = document.getElementById('canvas');
        const ctx = canvas.getContext('2d');
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;

        const particles = [];
        for (let i = 0; i < 60; i++) {
            particles.push({
                x: Math.random() * canvas.width,
                y: Math.random() * canvas.height,
                radius: Math.random() * 4 + 2,
                color: `hsl(${Math.random() * 360}, 100%, 50%)`,
                vx: (Math.random() - 0.5) * 3,
                vy: (Math.random() - 0.5) * 3
            });
        }

        function animate() {
            ctx.fillStyle = 'rgba(8, 12, 20, 0.2)';
            ctx.fillRect(0, 0, canvas.width, canvas.height);

            particles.forEach(p => {
                p.x += p.vx;
                p.y += p.vy;
                if (p.x < 0 || p.x > canvas.width) p.vx *= -1;
                if (p.y < 0 || p.y > canvas.height) p.vy *= -1;

                ctx.beginPath();
                ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
                ctx.fillStyle = p.color;
                ctx.shadowBlur = 15;
                ctx.shadowColor = p.color;
                ctx.fill();
            });

            requestAnimationFrame(animate);
        }
        animate();
    </script>
</body>
</html>"""

private val GLASS_CARD_TEMPLATE = """<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Glassmorphism Profile Card</title>
    <style>
        body {
            margin: 0;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            background: linear-gradient(45deg, #120e2e, #0d324d);
            font-family: system-ui, sans-serif;
            color: white;
        }
        .card {
            background: rgba(255, 255, 255, 0.08);
            backdrop-filter: blur(16px);
            border: 1px solid rgba(255, 255, 255, 0.15);
            border-radius: 24px;
            padding: 32px;
            width: 320px;
            text-align: center;
            box-shadow: 0 16px 32px rgba(0,0,0,0.4);
        }
        .avatar {
            width: 80px;
            height: 80px;
            border-radius: 50%;
            background: linear-gradient(135deg, #00f2fe, #4facfe);
            margin: 0 auto 16px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 36px;
        }
        h2 { margin: 0 0 8px; font-size: 22px; }
        p { color: #a0aec0; margin: 0 0 20px; font-size: 14px; }
        .btn {
            background: #00f2fe;
            color: #0b101d;
            border: none;
            padding: 12px 24px;
            border-radius: 30px;
            font-weight: bold;
            cursor: pointer;
            width: 100%;
        }
    </style>
</head>
<body>
    <div class="card">
        <div class="avatar">⚡</div>
        <h2>DeepSeek Developer</h2>
        <p>AI Native Mobile Engineer & Web Creator</p>
        <button class="btn" onclick="alert('Connected with DeepSeek Studio!')">Connect Profile</button>
    </div>
</body>
</html>"""
