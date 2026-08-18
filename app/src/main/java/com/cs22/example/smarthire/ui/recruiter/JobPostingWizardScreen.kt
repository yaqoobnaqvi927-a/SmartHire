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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.cs22.example.smarthire.ui.theme.*
import com.cs22.example.smarthire.viewmodel.RecruiterViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JobPostingWizardScreen(viewModel: RecruiterViewModel, navController: NavHostController) {
    var step by remember { mutableIntStateOf(1) }
    var jobTitle by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    var skillsRequired by remember { mutableStateOf("") }
    var skillsList by remember { mutableStateOf(listOf<String>()) }
    var yearsOfExperience by remember { mutableStateOf("") }
    var jobType by remember { mutableStateOf("onsite") }
    var jobTypeExpanded by remember { mutableStateOf(false) }
    var generatedDescription by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Smart Hire", fontWeight = FontWeight.Bold, color = StOnSurface) },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Back", color = StOnSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = StSurface)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = StSurface,
                border = BorderStroke(1.dp, StOutlineVariant),
                shadowElevation = 8.dp
            ) {
                Box(Modifier.padding(16.dp)) {
                    if (step < 3) {
                        Button(
                            onClick = { step++ },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StPrimary)
                        ) {
                            Text("Continue to Details", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { 
                                val minExp = yearsOfExperience.toIntOrNull() ?: 0
                                viewModel.postJob(jobTitle, "My Company", generatedDescription, skillsList, minExp, "Bachelors", jobType, location, salary)
                                navController.popBackStack()
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StPrimary)
                        ) {
                            Icon(Icons.Default.CheckCircle, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Publish Job", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        containerColor = StBackground
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Progress Bar
            LinearProgressIndicator(
                progress = { step / 3f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = StPrimary,
                trackColor = StSurfaceContainer
            )

            Text("Create Job Post", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = StOnSurface)
            Text("Step $step: ${if(step == 1) "Basic Information" else if (step == 2) "Job Details" else "Review"}", fontSize = 16.sp, color = StOnSurfaceVariant)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StSurface),
                border = BorderStroke(1.dp, StOutlineVariant),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (step == 1) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = jobTitle, onValueChange = { jobTitle = it },
                                label = { Text("Job Title", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = StSurface)
                            )
                            OutlinedTextField(
                                value = "My Company", onValueChange = { },
                                label = { Text("Company", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                readOnly = true,
                                trailingIcon = { Icon(Icons.Default.Lock, null) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = StSurfaceContainer)
                            )
                        }

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = location, onValueChange = { location = it },
                                label = { Text("Location", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = StSurface)
                            )
                            ExposedDropdownMenuBox(
                                expanded = jobTypeExpanded,
                                onExpandedChange = { jobTypeExpanded = !jobTypeExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = jobType,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Job Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = jobTypeExpanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = StSurface)
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
                    }

                    if (step == 2) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = salary, onValueChange = { salary = it },
                                label = { Text("Salary Range (Min - Max)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = yearsOfExperience, onValueChange = { yearsOfExperience = it },
                                label = { Text("Min Experience (yrs)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Text("Description", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StOnSurfaceVariant)
                        OutlinedTextField(
                            value = generatedDescription, onValueChange = { generatedDescription = it },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            shape = RoundedCornerShape(12.dp),
                            placeholder = { Text("Enter job description") }
                        )

                        Text("Skills", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StOnSurfaceVariant)
                        OutlinedTextField(
                            value = skillsRequired, onValueChange = { skillsRequired = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            placeholder = { Text("Type skill and tap +") },
                            trailingIcon = {
                                IconButton(onClick = {
                                    if(skillsRequired.isNotBlank()) {
                                        skillsList = skillsList + skillsRequired.trim()
                                        skillsRequired = ""
                                    }
                                }) {
                                    Icon(Icons.Default.Add, null)
                                }
                            }
                        )

                        if(skillsList.isNotEmpty()) {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                skillsList.forEach { skill ->
                                    InputChip(
                                        selected = true,
                                        onClick = { skillsList = skillsList - skill },
                                        label = { Text(skill, color = StPrimary) },
                                        trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) },
                                        colors = InputChipDefaults.inputChipColors(selectedContainerColor = StMatchBadgeBg),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (step == 3) {
                        Text("Review Job Posting", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Surface(color = StSurfaceContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text(jobTitle.ifEmpty { "Job Title Not Set" }, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StOnSurface)
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp), tint = StOnSurfaceVariant)
                                        Spacer(Modifier.width(4.dp))
                                        Text(location.ifEmpty { "Location Not Set" }, fontSize = 14.sp, color = StOnSurfaceVariant)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Work, null, modifier = Modifier.size(16.dp), tint = StOnSurfaceVariant)
                                        Spacer(Modifier.width(4.dp))
                                        Text(jobType, fontSize = 14.sp, color = StOnSurfaceVariant)
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                                Text("Description:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(generatedDescription.ifEmpty { "No description provided." }, fontSize = 14.sp, color = StOnSurfaceVariant)
                                Spacer(Modifier.height(16.dp))
                                Text("Required Skills:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    skillsList.forEach { s ->
                                        Text(s, fontSize = 12.sp, color = StPrimary, modifier = Modifier.background(StMatchBadgeBg, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(100.dp))
        }
    }
}
