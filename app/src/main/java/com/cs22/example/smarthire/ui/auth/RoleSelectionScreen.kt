package com.cs22.example.smarthire.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs22.example.smarthire.ui.components.RoleOptionCard
import com.cs22.example.smarthire.ui.theme.*

@Composable
fun RoleSelectionScreen(
    onJobSeekerSelected: () -> Unit,
    onRecruiterSelected: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SmartHireBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to SmartHire",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = SmartHirePrimary
            )
            Text(
                text = "Choose how you'd like to proceed",
                fontSize = 16.sp,
                color = SmartHireOnSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RoleOptionCard(
                    modifier = Modifier.weight(1f),
                    label = "Job Seeker",
                    icon = Icons.Default.Work,
                    isSelected = false,
                    onClick = onJobSeekerSelected
                )
                RoleOptionCard(
                    modifier = Modifier.weight(1f),
                    label = "Recruiter",
                    icon = Icons.Default.Groups,
                    isSelected = false,
                    onClick = onRecruiterSelected
                )
            }
        }
    }
}
