package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.Screen
import com.example.myapplication.ui.theme.ArAccent
import com.example.myapplication.ui.theme.ArPrimary
import com.example.myapplication.ui.theme.ArSlate100
import com.example.myapplication.ui.theme.ArSlate200
import com.example.myapplication.ui.theme.ArSlate300
import com.example.myapplication.ui.theme.ArSlate400
import com.example.myapplication.ui.theme.ArSlate500
import com.example.myapplication.ui.theme.ArSlate800

@Composable
fun Modifier.clickableNoRipple(enabled: Boolean = true, onClick: () -> Unit): Modifier {
    val interaction = remember { MutableInteractionSource() }
    return this.clickable(
        enabled = enabled,
        interactionSource = interaction,
        indication = null,
        onClick = onClick
    )
}

@Composable
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier {
    val interaction = remember { MutableInteractionSource() }
    return this.clickable(
        enabled = true,
        interactionSource = interaction,
        indication = null,
        onClick = onClick
    )
}

@Composable
fun AppHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ArPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text("AR", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
            Spacer(Modifier.size(8.dp))
            Text("AR INFOTEK", fontWeight = FontWeight.Black, fontSize = 18.sp, color = ArSlate800)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(ArSlate100)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text("Online Assessment", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ArPrimary)
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(ArSlate200)
    )
}

@Composable
fun AppFooter(screen: Screen, onAdminClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ArPrimary)
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        // Brand column
        Column {
            Text("AR INFOTEK", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "Practical, mentor-led online IT training to accelerate your career.",
                color = ArSlate300,
                fontSize = 13.sp
            )
        }

        Spacer(Modifier.height(20.dp))

        // Programs + Company side by side
        Row(modifier = Modifier.fillMaxWidth()) {
            FooterSection(
                title = "Programs",
                items = listOf("AWS Architect", "Data Science", "DevOps"),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.size(16.dp))
            FooterSection(
                title = "Company",
                items = listOf("Why Us", "Internship", "Testimonials"),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(20.dp))

        // Legal
        FooterSection(
            title = "Legal",
            items = listOf("Terms", "Privacy"),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        // Bottom bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.1f))
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("© 2025 AR INFOTEK - All rights reserved.", color = ArSlate400, fontSize = 11.sp)
            Spacer(Modifier.size(16.dp))
            Text(
                text = if (screen == Screen.ADMIN) "Back to Test" else "Admin",
                color = Color(0xFF7DD3FC),
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickableNoRipple(onClick = onAdminClick)
                    .padding(2.dp)
            )
        }
    }
}

@Composable
private fun FooterSection(title: String, items: List<String>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(title, color = ArAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(Modifier.height(10.dp))
        items.forEach { item ->
            Text(item, color = ArSlate200, fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
        }
    }
}

@Composable
fun GradientButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val brush = Brush.horizontalGradient(listOf(ArAccent, Color(0xFFEA580C)))
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) brush else Brush.horizontalGradient(listOf(ArSlate200, ArSlate200)))
            .clickableNoRipple(enabled, onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (enabled) Color.White else ArSlate500,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (enabled) ArPrimary else ArSlate200)
            .clickableNoRipple(enabled, onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (enabled) Color.White else ArSlate500,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
fun OutlineButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .clickableNoRipple(enabled, onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (enabled) ArSlate500 else ArSlate400,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}
