package com.cs22.example.smarthire.ui.seeker

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.cs22.example.smarthire.viewmodel.SeekerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CvVaultScreen(viewModel: SeekerViewModel, navController: NavHostController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.uploadCv(it, context) }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF0F131D)).padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF1D2433).copy(alpha=0.8f))) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color(0xFFE1E2E4))
                }
                Spacer(Modifier.width(16.dp))
                Text("CV Vault", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE1E2E4))
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { launcher.launch("application/pdf") },
                shape = CircleShape,
                containerColor = Color.Transparent,
                modifier = Modifier
                    .size(64.dp)
                    .background(Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))), CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Upload CV", tint = Color.White, modifier = Modifier.size(32.dp))
            }
        },
        containerColor = Color(0xFF0F131D)
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Background glows
            Box(modifier = Modifier.offset(x = (-50).dp, y = (-50).dp).size(300.dp).background(Color(0xFF8B5CF6).copy(alpha = 0.1f), CircleShape).blur(120.dp))
            
            LazyColumn(
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Manage your uploaded resumes to tailor your applications.", color = Color(0xFFC2C6D6), fontSize = 16.sp)
                    Spacer(Modifier.height(24.dp))
                }
                
                // Example Active CV Card
                item {
                    CvCard(filename = "Software_Eng_Resume.pdf", date = "Today", isActive = true)
                }
                // Example Inactive CV Card
                item {
                    CvCard(filename = "Old_Resume_2024.pdf", date = "Jan 12, 2026", isActive = false)
                }
            }
        }
    }
}

@Composable
fun CvCard(filename: String, date: String, isActive: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B28)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFF1D2433)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = if (isActive) Color(0xFF3B82F6) else Color(0xFFC2C6D6), modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(filename, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFE1E2E4))
                Spacer(Modifier.height(4.dp))
                Text("Uploaded $date", fontSize = 14.sp, color = Color(0xFFC2C6D6))
            }
            Spacer(Modifier.width(16.dp))
            if (isActive) {
                Surface(color = Color(0xFF10B981).copy(alpha=0.15f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha=0.3f))) {
                    Text("Active", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = {}) {
                Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color(0xFFC2C6D6))
            }
        }
    }
}
