package com.cs22.example.smarthire.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
val PrimaryAccent = Color(0xFF3B82F6)
val SurfaceLight = Color(0xFF151A23)
val BackgroundLight = Color(0xFF0B0F19)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdvancedSearchFilterSheet(
    onDismiss: () -> Unit,
    initialSkills: String = "",
    initialExperience: Int = 0,
    initialType: String = "all",
    onApply: (skills: String, experience: Int, type: String) -> Unit
) {
    var skills by remember { mutableStateOf(initialSkills) }
    var experience by remember { mutableFloatStateOf(initialExperience.toFloat()) }
    var selectedType by remember { mutableStateOf(initialType) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceLight,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                "Filter Intelligence",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Skills Input
            Text("AI Skill Alignment", color = Color.Gray, fontSize = 14.sp)
            OutlinedTextField(
                value = skills,
                onValueChange = { skills = it },
                placeholder = { Text("e.g. Kotlin, React, AI", color = Color.DarkGray) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = PrimaryAccent,
                    unfocusedBorderColor = Color.DarkGray
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Experience Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Experience Depth", color = Color.Gray, fontSize = 14.sp)
                Text("${experience.toInt()} Years", color = PrimaryAccent, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = experience,
                onValueChange = { experience = it },
                valueRange = 0f..10f,
                steps = 9,
                colors = SliderDefaults.colors(
                    thumbColor = PrimaryAccent,
                    activeTrackColor = PrimaryAccent,
                    inactiveTrackColor = Color.DarkGray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Job Type Chips
            Text("Work Domain", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("all", "remote", "onsite", "hybrid").forEach { type ->
                    val isSelected = selectedType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedType = type },
                        label = { Text(type.uppercase(), fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryAccent,
                            selectedLabelColor = Color.White,
                            labelColor = Color.Gray,
                            containerColor = Color.Transparent
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) PrimaryAccent else Color.DarkGray,
                            selectedBorderColor = PrimaryAccent,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { 
                    onApply(skills, experience.toInt(), selectedType)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Calibrate Match Engine", fontWeight = FontWeight.Bold)
            }
        }
    }
}
