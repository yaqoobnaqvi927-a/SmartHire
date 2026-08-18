package com.cs22.example.smarthire.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs22.example.smarthire.ui.theme.*

@Composable
fun StitchJobCard(
    jobTitle: String,
    companyName: String,
    location: String,
    jobType: String,
    matchPercentage: Int,
    skills: List<String>,
    salary: String = "",
    isQuickApply: Boolean = false,
    onCardClick: () -> Unit,
    onApplyClick: () -> Unit
) {
    Surface(
        onClick = onCardClick,
        shape = RoundedCornerShape(18.dp),
        color = StSurface,
        border = BorderStroke(1.dp, StOutlineVariant),
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(StSurfaceContainer, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Business, contentDescription = null, tint = StPrimary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(jobTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StOnSurface)
                        Text(companyName, fontSize = 14.sp, color = StTextSecondary)
                    }
                }
                StitchMatchBadge(percentage = matchPercentage)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                JobMetaChip(icon = Icons.Default.LocationOn, label = location)
                JobMetaChip(icon = Icons.Default.Work, label = jobType)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(skills) { skill ->
                    Box(
                        modifier = Modifier
                            .background(StSurfaceContainer, CircleShape)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(skill, fontSize = 12.sp, color = StOnSurfaceVariant)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (salary.isNotEmpty()) {
                    Text(salary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = StOnSurface)
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                
                if (isQuickApply) {
                    StitchPrimaryButton(
                        text = "Quick Apply",
                        onClick = onApplyClick,
                        modifier = Modifier.height(40.dp)
                    )
                } else {
                    StitchOutlinedButton(
                        text = "View Details",
                        onClick = onApplyClick,
                        modifier = Modifier.height(40.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun JobMetaChip(icon: ImageVector, label: String) {
    Row(
        modifier = Modifier
            .background(StSurfaceContainer, CircleShape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = StTextSecondary)
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 12.sp, color = StTextSecondary)
    }
}

@Composable
fun StitchMatchBadge(percentage: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(StMatchBadgeBg, CircleShape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(14.dp), tint = StPrimary)
        Spacer(modifier = Modifier.width(2.dp))
        Text("$percentage% Match", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StPrimary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StitchSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Search jobs, companies, or skills...",
    onFilterClick: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = StTextSecondary) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = StTextSecondary) },
        trailingIcon = onFilterClick?.let {
            {
                IconButton(onClick = it) {
                    Icon(Icons.Default.Tune, contentDescription = "Filter", tint = StPrimary)
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = StSurface,
            unfocusedContainerColor = StSurface,
            focusedBorderColor = StPrimary,
            unfocusedBorderColor = StOutlineVariant,
            focusedTextColor = StOnSurface,
            unfocusedTextColor = StOnSurface
        ),
        singleLine = true
    )
}

@Composable
fun StitchPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(25.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = StPrimary,
            contentColor = Color.White,
            disabledContainerColor = StSurfaceContainer,
            disabledContentColor = StTextSecondary
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StitchOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(25.dp),
        border = BorderStroke(1.dp, StPrimary),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = StSurface,
            contentColor = StPrimary
        )
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StitchStatCard(icon: ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = StSurface,
        border = BorderStroke(1.dp, StOutlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(StMatchBadgeBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = StPrimary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = StOnSurface)
            Text(label, fontSize = 12.sp, color = StTextSecondary)
        }
    }
}

@Composable
fun StitchChip(label: String, isActive: Boolean = false, onClick: (() -> Unit)? = null) {
    val bg = if (isActive) StMatchBadgeBg else StSurface
    val border = if (isActive) StPrimary else StOutlineVariant
    val textCol = if (isActive) StPrimary else StTextSecondary

    Surface(
        shape = CircleShape,
        color = bg,
        border = BorderStroke(1.dp, border),
        modifier = Modifier.clickable(enabled = onClick != null) { onClick?.invoke() }
    ) {
        Text(
            text = label,
            color = textCol,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun StitchFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StOnSurface)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = StTextSecondary) },
            readOnly = readOnly,
            trailingIcon = trailingIcon,
            leadingIcon = leadingIcon,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = StSurface,
                unfocusedContainerColor = StSurface,
                focusedBorderColor = StPrimary,
                unfocusedBorderColor = StOutlineVariant,
                focusedTextColor = StOnSurface,
                unfocusedTextColor = StOnSurface
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StitchTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column {
        TopAppBar(
            title = { Text(title, color = StPrimary, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = StOnSurface)
                    }
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(containerColor = StSurface)
        )
        HorizontalDivider(color = StOutlineVariant, thickness = 1.dp)
    }
}

@Composable
fun StitchEmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(StMatchBadgeBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = StPrimary, modifier = Modifier.size(40.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StOnSurface)
        Spacer(modifier = Modifier.height(8.dp))
        Text(message, fontSize = 14.sp, color = StTextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        
        if (action != null) {
            Spacer(modifier = Modifier.height(24.dp))
            action()
        }
    }
}

@Composable
fun StitchSectionHeader(title: String, action: String = "", onAction: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StOnSurface)
        if (action.isNotEmpty()) {
            Text(
                text = action,
                fontSize = 14.sp,
                color = StPrimary,
                modifier = Modifier.clickable { onAction() }
            )
        }
    }
}

@Composable
fun ActivityFeedItem(
    icon: ImageVector,
    iconBg: Color = StMatchBadgeBg,
    iconTint: Color = StPrimary,
    title: String,
    timestamp: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(iconBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, color = StOnSurface)
            Text(timestamp, fontSize = 12.sp, color = StTextSecondary)
        }
    }
}

@Composable
fun InterviewTimeChip(time: String) {
    Row(
        modifier = Modifier
            .background(StMatchBadgeBg, CircleShape)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = StPrimary)
        Spacer(modifier = Modifier.width(6.dp))
        Text(time, fontSize = 14.sp, color = StPrimary, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bg, textCol) = when (status.lowercase()) {
        "under review" -> Color(0xFFFFF3E0) to StWarning
        "interview" -> StMatchBadgeBg to StPrimary
        "offer", "accepted" -> Color(0xFFE8F5E9) to StSuccess
        "rejected" -> Color(0xFFFFEBEE) to StError
        else -> StSurfaceContainerLow to StTextSecondary
    }
    
    Box(
        modifier = Modifier
            .background(bg, CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(status, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textCol)
    }
}
