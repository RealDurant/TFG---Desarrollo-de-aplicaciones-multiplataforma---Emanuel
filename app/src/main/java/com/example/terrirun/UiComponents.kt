package com.example.terrirun

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Castle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private val TerriPurple = Color(0xFF6C4BC3)
private val TerriPurpleLight = Color(0xFFF0EAFE)
private val TerriCard = Color(0xFFF8F5FF)

@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.95f),
        tonalElevation = 4.dp,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (icon != null) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = TerriPurpleLight
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = TerriPurple,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Column(modifier = Modifier.padding(top = 12.dp)) {
                content()
            }
        }
    }
}

@Composable
fun ActivityStatRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF5C6670),
            modifier = Modifier.size(22.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun PrimaryFloatingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.PlayArrow
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = TerriPurple,
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 10.dp
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(text)
    }
}

@Composable
fun SecondaryFloatingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Castle,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = TerriPurpleLight,
            contentColor = TerriPurple,
            disabledContainerColor = Color(0xFFE5E7EB),
            disabledContentColor = Color(0xFF9CA3AF)
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(text)
    }
}

@Composable
fun TerritoryMiniCard(
    title: String,
    subtitle: String,
    control: Int,
    status: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = TerriCard,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280)
            )
            Text(
                text = "Control: $control%",
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = status,
                color = when (status) {
                    "Estable" -> Color(0xFF1F9D55)
                    "En consolidación" -> Color(0xFFD97706)
                    "Muy débil" -> Color(0xFFDC2626)
                    else -> Color(0xFF6B7280)
                },
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}