package com.cs22.example.smarthire.ui.seeker

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.cs22.example.smarthire.ui.theme.*
import com.cs22.example.smarthire.viewmodel.SeekerUiState
import com.cs22.example.smarthire.viewmodel.SeekerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CvUploadScreen(navController: NavHostController, viewModel: SeekerViewModel) {
    val ctx = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }
    val cvSyncState by viewModel.cvSyncState.collectAsState()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { 
            selectedUri = it
            fileName = "Selected_Resume.pdf"
        }
    }

    LaunchedEffect(cvSyncState) {
        if (cvSyncState is SeekerUiState.Success) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload CV", color = StOnSurface, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = StOnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StBackground)
            )
        },
        containerColor = StBackground
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                // Hero Area
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(StMatchBadgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = StPrimary, modifier = Modifier.size(40.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Step Indicators
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StepCircle(1, true)
                    Box(modifier = Modifier.weight(1f).height(2.dp).background(StPrimary))
                    StepCircle(2, selectedUri != null)
                    Box(modifier = Modifier.weight(1f).height(2.dp).background(if (selectedUri != null) StPrimary else StOutlineVariant))
                    StepCircle(3, false)
                }
                
                Spacer(modifier = Modifier.height(32.dp))

                // Upload Zone (Dashed Border Card)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(StSurface)
                        .drawBehind {
                            val stroke = Stroke(
                                width = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                            )
                            drawRoundRect(
                                color = StOutlineVariant,
                                style = stroke,
                                cornerRadius = CornerRadius(16.dp.toPx())
                            )
                        }
                        .clickable { launcher.launch("application/pdf") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedUri == null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = StPrimary, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Tap to select your CV", fontWeight = FontWeight.Bold, color = StOnSurface, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("PDF or DOCX (Max 10MB)", color = StTextSecondary, fontSize = 12.sp)
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = StPrimary, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(fileName, fontWeight = FontWeight.Bold, color = StOnSurface)
                                Text("Ready to upload", color = StSuccess, fontSize = 12.sp)
                            }
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StSuccess)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))

                // Info Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InfoChip(Icons.Default.AutoAwesome, "AI Extracts Skills")
                    InfoChip(Icons.Default.FlashOn, "Instant Matching")
                    InfoChip(Icons.Default.Lock, "Secure Upload")
                }
                
                if (cvSyncState is SeekerUiState.Error) {
                    Text(
                        text = (cvSyncState as SeekerUiState.Error).message,
                        color = StError,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                val isUploading = cvSyncState is SeekerUiState.Loading

                Button(
                    onClick = {
                        selectedUri?.let { uri ->
                            viewModel.uploadCv(uri, ctx)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = selectedUri != null && !isUploading,
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StPrimary)
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Upload & Analyze", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            if (cvSyncState is SeekerUiState.Loading) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = StSurface),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = StPrimary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Analyzing with AI...", color = StOnSurface, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepCircle(step: Int, isActive: Boolean) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (isActive) StPrimary else StOutlineVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(step.toString(), color = if (isActive) StSurface else StTextSecondary, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = StPrimary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(8.dp))
        Text(label, fontSize = 12.sp, color = StTextSecondary, textAlign = TextAlign.Center)
    }
}
