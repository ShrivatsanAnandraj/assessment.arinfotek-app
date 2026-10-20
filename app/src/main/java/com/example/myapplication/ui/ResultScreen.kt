package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.AppViewModel
import com.example.myapplication.data.TestResult
import com.example.myapplication.ui.theme.ArAccent
import com.example.myapplication.ui.theme.ArPrimary
import com.example.myapplication.ui.theme.ArSlate100
import com.example.myapplication.ui.theme.ArSlate200
import com.example.myapplication.ui.theme.ArSlate500
import com.example.myapplication.ui.theme.ArSlate800
import com.example.myapplication.ui.theme.ArGreen
import com.example.myapplication.ui.theme.ArGreenLight
import com.example.myapplication.ui.theme.ArRed
import com.example.myapplication.ui.theme.ArRedLight

@Composable
fun ResultScreen(
    vm: AppViewModel,
    result: TestResult,
    onTakeSurvey: () -> Unit
) {
    val labels = listOf("A", "B", "C", "D")
    val passedColor = if (result.passed) ArGreen else ArRed
    val passedBg = if (result.passed) ArGreenLight else ArRedLight

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        // Score card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(passedBg)
                    .border(4.dp, passedColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("${result.percentage}%", color = passedColor, fontWeight = FontWeight.Black, fontSize = 22.sp)
            }
            Spacer(Modifier.height(14.dp))
            Text(
                if (result.passed) "Congratulations!" else "Better Luck Next Time",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = ArSlate800
            )
            Spacer(Modifier.height(4.dp))
            Text("${vm.studentName} — ${vm.generatedTestTitle}", fontSize = 13.sp, color = ArSlate500)
            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                ScoreStat("${result.score}", "Correct", ArPrimary)
                Box(modifier = Modifier.width(1.dp).height(34.dp).background(ArSlate200))
                ScoreStat("${result.total - result.score}", "Incorrect", Color(0xFFF87171))
                Box(modifier = Modifier.width(1.dp).height(34.dp).background(ArSlate200))
                ScoreStat("${result.total}", "Total", Color(0xFF475569))
            }
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(passedBg)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    if (result.passed) "PASSED" else "NOT PASSED",
                    color = passedColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Review
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text("Answer Review", fontWeight = FontWeight.Black, fontSize = 16.sp, color = ArSlate800)
            Spacer(Modifier.height(12.dp))
            result.review.forEachIndexed { index, item ->
                val isCorrect = item.selectedAnswer == item.correctAnswer
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            2.dp,
                            if (isCorrect) Color(0xFFBBF7D0) else Color(0xFFFECACA),
                            RoundedCornerShape(12.dp)
                        )
                        .background(if (isCorrect) Color(0xFFF0FDF4) else Color(0xFFFEF2F2))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCorrect) ArGreenLight else ArRedLight)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "Q${index + 1}",
                                color = if (isCorrect) ArGreen else ArRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(item.question, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = ArSlate800)
                    }
                    Spacer(Modifier.height(8.dp))
                    item.options.forEachIndexed { j, opt ->
                        val isThisCorrect = j == item.correctAnswer
                        val isThisSelected = j == item.selectedAnswer
                        val bg = when {
                            isThisCorrect -> ArGreenLight
                            isThisSelected -> ArRedLight
                            else -> Color.Transparent
                        }
                        val fg = when {
                            isThisCorrect -> ArGreen
                            isThisSelected -> ArRed
                            else -> ArSlate500
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bg)
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(labels.getOrElse(j) { "?" } + ".", fontWeight = FontWeight.Bold, color = fg, fontSize = 12.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(opt, color = fg, fontSize = 12.sp, fontWeight = if (isThisCorrect) FontWeight.Bold else FontWeight.Normal)
                            Spacer(Modifier.weight(1f))
                            if (isThisCorrect) Text("Correct", color = ArGreen, fontSize = 10.sp)
                            if (isThisSelected && !isThisCorrect) Text("Your answer", color = ArRed, fontSize = 10.sp)
                        }
                    }
                    if (item.selectedAnswer == null) {
                        Spacer(Modifier.height(4.dp))
                        Text("Not answered", color = ArSlate400c, fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GradientButton(
                text = "Take Survey",
                modifier = Modifier.width(200.dp),
                onClick = onTakeSurvey
            )
        }
    }
}

@Composable
private fun ScoreStat(value: String, label: String, color: Color) {
    Column(
        modifier = Modifier.padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontWeight = FontWeight.Black, fontSize = 22.sp, color = color)
        Text(label, fontSize = 11.sp, color = ArSlate500)
    }
}

private val ArSlate400c = Color(0xFF94A3B8)
