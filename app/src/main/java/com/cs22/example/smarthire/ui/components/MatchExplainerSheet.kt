package com.cs22.example.smarthire.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs22.example.smarthire.model.DjangoJob
import com.cs22.example.smarthire.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchExplainerSheet(
    job: DjangoJob,
    candidateSkills: List<String>,
    onApplyClick: () -> Unit = {},
    onDismiss: () -> Unit
) {
    var animationPlayed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        animationPlayed = true
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = StSurfaceContainerLow,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(32.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(StSurfaceContainer)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Why This Match?", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StOnSurface)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Big Score Circle
            Box(
                modifier = Modifier.size(100.dp).clip(CircleShape).background(StMatchBadgeBg),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { (job.match_percentage.toFloat() / 100f) },
                    modifier = Modifier.size(100.dp),
                    color = StPrimary,
                    trackColor = Color.Transparent,
                    strokeWidth = 8.dp
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${job.match_percentage.toInt()}%", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = StPrimary)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(if (job.match_percentage >= 80) "Strong Match" else "Good Match", color = StOnSurface, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            Text("Based on your profile and skills", color = StTextSecondary, fontSize = 14.sp)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Breakdowns
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                MatchRow("Skills Match", "8/10 required skills", true)
                MatchRow("Experience Level", "Meets 3 years requirement", true)
                MatchRow("Location Type", "Remote", true)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { 
                    onApplyClick()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StPrimary)
            ) {
                Text("Apply Now", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MatchRow(title: String, desc: String, isMatch: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(
            imageVector = if (isMatch) Icons.Default.CheckCircle else Icons.Default.Close,
            contentDescription = null,
            tint = if (isMatch) StSuccess else StError,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, color = StOnSurface, fontSize = 16.sp)
            Text(desc, color = StTextSecondary, fontSize = 14.sp)
        }
    }
}
