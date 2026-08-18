package com.cs22.example.smarthire.ui.seeker

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.cs22.example.smarthire.ui.theme.*
import com.cs22.example.smarthire.viewmodel.SeekerViewModel
import com.cs22.example.smarthire.viewmodel.SeekerUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CvVaultScreen(viewModel: SeekerViewModel, navController: NavHostController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.uploadCv(it, context) }
    }
    
    val cvsState by viewModel.cvsState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.fetchCVs()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CV Vault", color = StOnSurface, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = StOnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StBackground,
                    scrolledContainerColor = StBackground
                )
            )
        },
        containerColor = StBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            // Upload Section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawRoundRect(
                                color = StOutlineVariant,
                                style = Stroke(
                                    width = 2.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                ),
                                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                            )
                        }
                        .clip(RoundedCornerShape(16.dp))
                        .background(StSurface)
                        .clickable { launcher.launch("application/pdf") }
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, tint = StPrimary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Upload New CV", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StOnSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("PDF, DOCX up to 10MB", fontSize = 14.sp, color = StTextSecondary)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { launcher.launch("application/pdf") },
                            border = BorderStroke(1.dp, StPrimary),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StPrimary)
                        ) {
                            Text("Browse Files")
                        }
                    }
                }
            }

            when (val state = cvsState) {
                is SeekerUiState.Loading -> {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = StPrimary)
                            Spacer(Modifier.height(16.dp))
                            Text("Extracting skills...", color = StTextSecondary)
                        }
                    }
                }
                is SeekerUiState.Success -> {
                    val cvs = state.data
                    if (cvs.isNotEmpty()) {
                        item {
                            Text("Active CV", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StOnSurface, modifier = Modifier.padding(bottom = 8.dp))
                            ActiveCvCard(
                                filename = "Resume_${cvs[0].id}.pdf",
                                date = "Parsed",
                                onView = {
                                    cvs[0].cv?.let { url ->
                                        try {
                                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url.toString()))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // Handle missing PDF viewer
                                        }
                                    }
                                },
                                onReplace = { launcher.launch("application/pdf") },
                                onDelete = { viewModel.deleteCv(cvs[0].id) }
                            )
                        }
                        if (cvs.size > 1) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("CV History", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StOnSurface, modifier = Modifier.padding(bottom = 8.dp))
                            }
                            items(cvs.size - 1) { index ->
                                val cv = cvs[index + 1]
                                HistoryCvCard(
                                    filename = "Resume_${cv.id}.pdf",
                                    date = "Parsed",
                                    onDelete = { viewModel.deleteCv(cv.id) }
                                )
                            }
                        }
                    } else {
                        item {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("No CVs uploaded yet.", color = StTextSecondary, modifier = Modifier.padding(32.dp))
                            }
                        }
                    }
                }
                is SeekerUiState.Error -> {
                    item {
                        Text("Error: ${state.message}", color = StError, modifier = Modifier.padding(16.dp))
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun ActiveCvCard(
    filename: String, 
    date: String, 
    onView: () -> Unit = {},
    onReplace: () -> Unit, 
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = StSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, StOutlineVariant)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Accent left border
            Box(modifier = Modifier.fillMaxHeight().width(4.dp).background(StPrimary))
            
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = StMatchBadgeBg,
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.VerifiedUser, null, tint = StSuccess, modifier = Modifier.padding(8.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(filename, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = StOnSurface)
                            Text("Uploaded $date", fontSize = 12.sp, color = StTextSecondary)
                        }
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, null, tint = StError)
                    }
                }
                Spacer(Modifier.height(16.dp))
                // Extracted skills chip cloud placeholder
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkillChip("Kotlin")
                    SkillChip("Android")
                    SkillChip("Jetpack Compose")
                }
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onView,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StPrimary),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text("View")
                    }
                    OutlinedButton(
                        onClick = onReplace,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StPrimary),
                        border = BorderStroke(1.dp, StOutlineVariant),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text("Replace")
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryCvCard(filename: String, date: String, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StSurface),
        border = BorderStroke(1.dp, StOutlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(StSurfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = StTextSecondary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(filename, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = StOnSurface)
                Spacer(Modifier.height(4.dp))
                Text("Uploaded $date", fontSize = 12.sp, color = StTextSecondary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StTextSecondary)
            }
        }
    }
}

@Composable
fun SkillChip(label: String) {
    Surface(
        color = StSurfaceContainerLow,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, StOutlineVariant)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            color = StOnSurface
        )
    }
}
