package com.cs22.example.smarthire.ui.recruiter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.cs22.example.smarthire.viewmodel.RecruiterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobPostingWizardScreen(viewModel: RecruiterViewModel, navController: NavHostController) {
    var step by remember { mutableIntStateOf(1) }
    var jobTitle by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    var skillsRequired by remember { mutableStateOf("") }
    var yearsOfExperience by remember { mutableStateOf("") }
    var jobType by remember { mutableStateOf("onsite") }
    var jobTypeExpanded by remember { mutableStateOf(false) }
    var generatedDescription by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF0F131D)).padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF1D2433).copy(alpha=0.8f))) {
                    Icon(Icons.Default.Close, null, tint = Color(0xFFE1E2E4))
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Create Job Post", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE1E2E4))
                    Text("Step $step of 3", fontSize = 12.sp, color = Color(0xFFC2C6D6))
                }
            }
        },
        bottomBar = {
            Box(Modifier.fillMaxWidth().background(Color(0xFF0F131D)).padding(24.dp)) {
                if (step < 3) {
                    Button(
                        onClick = { step++ },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                    ) {
                        Text("Next Step", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, null)
                    }
                } else {
                    Button(
                        onClick = { 
                            val skillsList = skillsRequired.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            val minExp = yearsOfExperience.toIntOrNull() ?: 0
                            viewModel.postJob(jobTitle, "My Company", generatedDescription, skillsList, minExp, "Bachelors", jobType, location, salary)
                            navController.popBackStack()
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Publish Job", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = Color(0xFF0F131D)
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Box(modifier = Modifier.offset(x = 100.dp, y = 100.dp).size(400.dp).background(Color(0xFF8B5CF6).copy(alpha = 0.05f), CircleShape).blur(120.dp))

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Progress Bar
                LinearProgressIndicator(
                    progress = { step / 3f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF3B82F6),
                    trackColor = Color(0xFF1D2433)
                )

                if (step == 1) {
                    Text("Basic Details", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE1E2E4))
                    
                    OutlinedTextField(
                        value = jobTitle, onValueChange = { jobTitle = it },
                        label = { Text("Job Title", color = Color(0xFFC2C6D6)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6), unfocusedBorderColor = Color(0xFF1D2433),
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = location, onValueChange = { location = it },
                        label = { Text("Location", color = Color(0xFFC2C6D6)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6), unfocusedBorderColor = Color(0xFF1D2433),
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = salary, onValueChange = { salary = it },
                        label = { Text("Salary Range", color = Color(0xFFC2C6D6)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6), unfocusedBorderColor = Color(0xFF1D2433),
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = skillsRequired, onValueChange = { skillsRequired = it },
                        label = { Text("Skills Required (comma separated)", color = Color(0xFFC2C6D6)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6), unfocusedBorderColor = Color(0xFF1D2433),
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = yearsOfExperience, onValueChange = { yearsOfExperience = it },
                        label = { Text("Years of Experience", color = Color(0xFFC2C6D6)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6), unfocusedBorderColor = Color(0xFF1D2433),
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        )
                    )
                    ExposedDropdownMenuBox(
                        expanded = jobTypeExpanded,
                        onExpandedChange = { jobTypeExpanded = !jobTypeExpanded }
                    ) {
                        OutlinedTextField(
                            value = jobType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Job Type", color = Color(0xFFC2C6D6)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = jobTypeExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF3B82F6), unfocusedBorderColor = Color(0xFF1D2433),
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = jobTypeExpanded,
                            onDismissRequest = { jobTypeExpanded = false }
                        ) {
                            listOf("remote", "hybrid", "onsite").forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        jobType = selectionOption
                                        jobTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (step == 2) {
                    Text("AI Description", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE1E2E4))
                    Text("Let Gemini write the perfect job description for you based on the title.", color = Color(0xFFC2C6D6), fontSize = 14.sp)
                    
                    Button(
                        onClick = { 
                            isGenerating = true
                            // Simulate AI Generation
                            generatedDescription = "We are seeking a highly skilled $jobTitle to join our innovative team in $location. You will be responsible for building scalable architectures and leading technical initiatives. \n\nRequired Skills:\n- 3+ years experience\n- Strong communication skills\n- Problem solving attitude"
                            isGenerating = false
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp).background(Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6))), RoundedCornerShape(28.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Icon(Icons.Default.AutoAwesome, null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Auto-Generate with Gemini", fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedTextField(
                        value = generatedDescription, onValueChange = { generatedDescription = it },
                        label = { Text("Description", color = Color(0xFFC2C6D6)) },
                        modifier = Modifier.fillMaxWidth().height(250.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF8B5CF6), unfocusedBorderColor = Color(0xFF1D2433),
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        )
                    )
                }

                if (step == 3) {
                    Text("Review", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE1E2E4))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B28)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(Modifier.padding(24.dp)) {
                            Text(jobTitle.ifEmpty { "Job Title" }, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(Modifier.height(8.dp))
                            Text("$location • $salary", color = Color(0xFF3B82F6), fontSize = 14.sp)
                            Spacer(Modifier.height(16.dp))
                            Text(generatedDescription.take(150) + "...", color = Color(0xFFC2C6D6), fontSize = 14.sp)
                        }
                    }
                }
                
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}
