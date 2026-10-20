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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.AppViewModel
import com.example.myapplication.data.GeneratedPaper
import com.example.myapplication.ui.theme.ArAccent
import com.example.myapplication.ui.theme.ArPrimary
import com.example.myapplication.ui.theme.ArSlate100
import com.example.myapplication.ui.theme.ArSlate200
import com.example.myapplication.ui.theme.ArSlate400
import com.example.myapplication.ui.theme.ArSlate500
import com.example.myapplication.ui.theme.ArSlate600
import com.example.myapplication.ui.theme.ArSlate800
import com.example.myapplication.ui.theme.ArGreen
import com.example.myapplication.ui.theme.ArGreenLight
import com.example.myapplication.ui.theme.ArRed
import kotlinx.coroutines.delay

@Composable
fun TestScreen(
    vm: AppViewModel,
    onSubmit: () -> Unit
) {
    val paper: GeneratedPaper = vm.activePaper ?: return
    val questions = paper.questions
    val total = questions.size
    val durationSeconds = (vm.verifiedTest?.durationMinutes ?: 30) * 60

    var currentIndex by remember { mutableIntStateOf(0) }
    val answers = remember { mutableStateMapOf<Int, Int>() }
    var secondsLeft by remember { mutableIntStateOf(durationSeconds) }
    var showConfirm by remember { mutableStateOf(false) }
    val frozen = vm.tabFlagged

    // Countdown timer
    LaunchedEffect(frozen) {
        while (secondsLeft > 0 && !frozen) {
            delay(1000)
            secondsLeft -= 1
        }
        if (secondsLeft <= 0 && !frozen) {
            vm.submitTest(answers.toMap())
            onSubmit()
        }
    }

    // Poll for admin approval while frozen
    LaunchedEffect(frozen) {
        while (frozen) {
            delay(2000)
            vm.refreshCurrentFlagStatus { status ->
                if (status == "approved") vm.clearTabFlag()
            }
        }
    }

    if (total == 0) {
        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("No questions available for this test.", color = ArSlate500)
        }
        return
    }

    val current = questions[currentIndex]
    val answeredCount = answers.size
    val isFirst = currentIndex == 0
    val isLast = currentIndex == total - 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ArPrimary)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    vm.generatedTestTitle,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(Modifier.width(8.dp))
                Text("$answeredCount/$total answered", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                Spacer(Modifier.width(8.dp))
                TimerBadge(secondsLeft)
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(ArSlate100)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((currentIndex + 1).toFloat() / total)
                            .height(6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Brush.horizontalGradient(listOf(ArPrimary, ArAccent)))
                    )
                }
                Spacer(Modifier.height(18.dp))

                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ArPrimary)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Q${currentIndex + 1}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        current.questionText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = ArSlate800
                    )
                }
                Spacer(Modifier.height(16.dp))

                val labels = listOf("A", "B", "C", "D")
                current.options.forEachIndexed { i, opt ->
                    val selected = answers[current.id] == i
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) ArPrimary else ArSlate200,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .background(if (selected) ArPrimary.copy(alpha = 0.06f) else Color.White)
                            .clickableNoRipple {
                                answers[current.id] = i
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(if (selected) ArPrimary else ArSlate100),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                labels.getOrElse(i) { "?" },
                                color = if (selected) Color.White else ArSlate500,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(opt, fontSize = 14.sp, color = ArSlate700)
                    }
                }

                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlineButton(
                        text = "Previous",
                        enabled = !isFirst,
                        onClick = { if (!isFirst) currentIndex -= 1 }
                    )
                    if (isLast) {
                        GradientButton(
                            text = "Submit Test",
                            modifier = Modifier.width(150.dp),
                            onClick = { showConfirm = true }
                        )
                    } else {
                        PrimaryButton(
                            text = "Next",
                            onClick = { currentIndex += 1 }
                        )
                    }
                }
            }

            // Question palette
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(ArSlate100)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("Question Palette", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ArSlate500)
                    Spacer(Modifier.height(10.dp))
                    val rows = questions.indices.chunked(8)
                    rows.forEach { row ->
                        Row(modifier = Modifier.padding(bottom = 8.dp)) {
                            row.forEach { index ->
                                val q = questions[index]
                                val isCurrent = index == currentIndex
                                val isAnswered = answers.containsKey(q.id)
                                val bg = when {
                                    isCurrent -> ArPrimary
                                    isAnswered -> ArGreenLight
                                    else -> ArSlate100
                                }
                                val fg = when {
                                    isCurrent -> Color.White
                                    isAnswered -> ArGreen
                                    else -> ArSlate500
                                }
                                Box(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(bg)
                                        .clickableNoRipple { currentIndex = index },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${index + 1}", color = fg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (frozen) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEE2E2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("!", color = ArRed, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text("Tabs changing found", fontWeight = FontWeight.Black, color = ArSlate800)
                }
            },
            text = {
                Text(
                    "Your test has been paused because a tab or window change was detected. " +
                        "Your answers are safe and your timer is frozen.\n\n" +
                        "Waiting for admin approval to continue...",
                    fontSize = 14.sp,
                    color = ArSlate500
                )
            }
        )
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Are you sure you want to submit?", fontWeight = FontWeight.Black) },
            text = {
                Column {
                    Text(
                        "You have answered ${answers.size} of $total questions.",
                        fontSize = 14.sp,
                        color = ArSlate500
                    )
                    if (answers.size < total) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "${total - answers.size} question(s) unanswered!",
                            fontSize = 14.sp,
                            color = ArRed
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirm = false
                    vm.submitTest(answers.toMap())
                    onSubmit()
                }) { Text("Yes, Submit", color = ArAccent, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("No", color = ArSlate500) }
            }
        )
    }
}

@Composable
private fun TimerBadge(secondsLeft: Int) {
    val bg = when {
        secondsLeft <= 60 -> ArRed
        secondsLeft <= 300 -> Color(0xFFEAB308)
        else -> Color.White.copy(alpha = 0.2f)
    }
    val mins = secondsLeft / 60
    val secs = secondsLeft % 60
    val label = "${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
    }
}

private val ArSlate700 = Color(0xFF334155)


