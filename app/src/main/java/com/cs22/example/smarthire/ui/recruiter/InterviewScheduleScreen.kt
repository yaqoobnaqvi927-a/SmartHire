package com.cs22.example.smarthire.ui.recruiter

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs22.example.smarthire.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewScheduleScreen() {
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var interviewType by remember { mutableStateOf("Video Call") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schedule Interview", color = StOnSurface, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = StOnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StBackground)
            )
        },
        containerColor = StBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StSurface),
                border = BorderStroke(1.dp, StOutlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Interview Details", color = StOnSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Candidate Name", color = StTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = "John Doe",
                        onValueChange = { },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = StOutlineVariant,
                            focusedBorderColor = StPrimary,
                            unfocusedContainerColor = StSurface,
                            focusedContainerColor = StSurface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Date", color = StTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = date,
                                onValueChange = { date = it },
                                placeholder = { Text("YYYY-MM-DD") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = StOutlineVariant, focusedBorderColor = StPrimary),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Time", color = StTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = time,
                                onValueChange = { time = it },
                                placeholder = { Text("14:00") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = StOutlineVariant, focusedBorderColor = StPrimary),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Interview Type", color = StTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = interviewType,
                        onValueChange = { interviewType = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = StOutlineVariant, focusedBorderColor = StPrimary),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { /* Schedule */ },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StPrimary)
            ) {
                Text("Confirm Schedule", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
