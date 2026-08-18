package com.cs22.example.smarthire.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs22.example.smarthire.ui.theme.*

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
                .padding(24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Filters", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = StOnSurface)
                TextButton(onClick = { 
                    skills = ""
                    experience = 0f
                    selectedType = "all"
                }) {
                    Text("Reset All", color = StPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            Text("Skills", color = StOnSurface, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = skills,
                onValueChange = { skills = it },
                placeholder = { Text("e.g. Kotlin, React", color = StTextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StPrimary,
                    unfocusedBorderColor = StOutlineVariant,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = StOnSurface,
                    unfocusedTextColor = StOnSurface
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Experience Level", color = StOnSurface, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text("${experience.toInt()} Years", color = StPrimary, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = experience,
                onValueChange = { experience = it },
                valueRange = 0f..10f,
                steps = 9,
                colors = SliderDefaults.colors(
                    thumbColor = StPrimary,
                    activeTrackColor = StPrimary,
                    inactiveTrackColor = StOutlineVariant
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Job Type", color = StOnSurface, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("all", "remote", "onsite", "hybrid").forEach { type ->
                    val isSelected = selectedType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedType = type },
                        label = { Text(if (type == "all") "Any" else type.replaceFirstChar { it.uppercase() }, fontSize = 14.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StMatchBadgeBg,
                            selectedLabelColor = StPrimary,
                            labelColor = StTextSecondary,
                            containerColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = StOutlineVariant,
                            selectedBorderColor = StPrimary,
                            enabled = true,
                            selected = isSelected
                        ),
                        shape = RoundedCornerShape(16.dp)
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
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StPrimary)
            ) {
                Text("Apply Filters", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
            }
        }
    }
}
