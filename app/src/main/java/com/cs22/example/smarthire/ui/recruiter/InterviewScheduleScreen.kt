package com.cs22.example.smarthire.ui.recruiter

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewScheduleScreen() {
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var interviewType by remember { mutableStateOf("Technical") }
    
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Schedule Interview") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Pick Date/Time and Type", style = MaterialTheme.typography.titleLarge)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date (e.g. YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("Time (e.g. 14:00)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = interviewType,
                onValueChange = { interviewType = it },
                label = { Text("Interview Type") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { /* Schedule */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Schedule Interview")
            }
        }
    }
}
