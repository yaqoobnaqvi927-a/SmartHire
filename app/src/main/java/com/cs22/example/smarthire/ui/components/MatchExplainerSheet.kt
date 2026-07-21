package com.cs22.example.smarthire.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs22.example.smarthire.model.DjangoJob
import kotlinx.coroutines.delay

private val PremiumBg = Color(0xFF0F131D)
private val PremiumSurface = Color(0xFF161B28)
private val PremiumSurfaceContainer = Color(0xFF1D2433)
private val PremiumPrimary = Color(0xFF3B82F6)
private val PremiumSecondary = Color(0xFF8B5CF6)
private val PremiumText = Color(0xFFE1E2E4)
private val PremiumTextMuted = Color(0xFFC2C6D6)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MatchExplainerSheet(
    job: DjangoJob,
    candidateSkills: List<String>,
    onDismiss: () -> Unit
) {
    var animationPlayed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        animationPlayed = true
    }

    val scoreAnim by animateFloatAsState(
        targetValue = if (animationPlayed) (job.match_percentage.toFloat() / 100f) * 360f else 0f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "scoreRing"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 500),
        label = "contentFade"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = PremiumSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = PremiumTextMuted.copy(alpha = 0.5f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Score Ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(160.dp)
            ) {
                Canvas(modifier = Modifier.size(140.dp)) {
                    drawArc(
                        color = PremiumSurfaceContainer,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        brush = Brush.linearGradient(listOf(PremiumPrimary, PremiumSecondary)),
                        startAngle = -90f,
                        sweepAngle = scoreAnim,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${job.match_percentage.toInt()}%",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = PremiumText
                    )
                    Text(text = "Match", fontSize = 14.sp, color = PremiumTextMuted)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Here's why you're a match",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = PremiumText,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(16.dp))

            val candidateSkillsLower = candidateSkills.map { it.lowercase() }.toSet()
            val jobSkillsLowerMap = job.skillsList.associateBy { it.lowercase() }

            val intersection = candidateSkillsLower.intersect(jobSkillsLowerMap.keys)
            val missing = jobSkillsLowerMap.keys.subtract(candidateSkillsLower)

            val skillsYouHave = intersection.mapNotNull { jobSkillsLowerMap[it] }
            val skillsToDevelop = missing.mapNotNull { jobSkillsLowerMap[it] }

            Column(modifier = Modifier.fillMaxWidth().alpha(contentAlpha)) {
                if (skillsYouHave.isNotEmpty()) {
                    Text("Skills You Have", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = PremiumText)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        skillsYouHave.forEach { skill ->
                            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF10B981).copy(alpha = 0.15f)) {
                                Text(
                                    text = skill,
                                    color = Color(0xFF10B981),
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (skillsToDevelop.isNotEmpty()) {
                    Text("Skills to Develop", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = PremiumText)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        skillsToDevelop.forEach { skill ->
                            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFEF4444).copy(alpha = 0.15f)) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = skill, color = Color(0xFFEF4444), fontSize = 14.sp)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(50.dp).alpha(contentAlpha),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        brush = Brush.horizontalGradient(listOf(PremiumPrimary, PremiumSecondary))
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("View Full Skill Gap Report", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
