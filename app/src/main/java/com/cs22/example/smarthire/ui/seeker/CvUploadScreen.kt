package com.cs22.example.smarthire.ui.seeker

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.cs22.example.smarthire.viewmodel.SeekerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CvUploadScreen(navController: NavHostController, viewModel: SeekerViewModel) {
    val ctx = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.uploadCv(it, ctx) }
    }
    
    var bio by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var education by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("CV Upload & Profile Builder") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Button(
                onClick = { launcher.launch("application/pdf") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Quick Upload PDF")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Manual Profile Builder", style = MaterialTheme.typography.titleLarge)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = skills,
                onValueChange = { skills = it },
                label = { Text("Skills (comma separated)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = experience,
                onValueChange = { experience = it },
                label = { Text("Experience") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = education,
                onValueChange = { education = it },
                label = { Text("Education") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { /* Handle manual profile save */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Profile")
            }
        }
    }
}
