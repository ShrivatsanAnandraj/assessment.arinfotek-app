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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.AppViewModel
import com.example.myapplication.data.Survey
import com.example.myapplication.ui.theme.ArAccent
import com.example.myapplication.ui.theme.ArPrimary
import com.example.myapplication.ui.theme.ArSlate100
import com.example.myapplication.ui.theme.ArSlate200
import com.example.myapplication.ui.theme.ArSlate500
import com.example.myapplication.ui.theme.ArSlate700
import com.example.myapplication.ui.theme.ArSlate800
import com.example.myapplication.ui.theme.ArGreen
import com.example.myapplication.ui.theme.ArGreenLight

@Composable
fun SurveyScreen(
    vm: AppViewModel,
    survey: Survey,
    onComplete: () -> Unit
) {
    val answers = remember { mutableStateMapOf<Int, String>() }
    var currentIndex by remember { mutableStateOf(0) }
    var submitted by remember { mutableStateOf(false) }

    val questions = survey.questions
    val total = questions.size

    if (submitted) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(64.dp).clip(CircleShape).background(ArGreenLight),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", fontSize = 30.sp, color = ArGreen, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(14.dp))
            Text("Thank You!", fontWeight = FontWeight.Black, fontSize = 20.sp, color = ArSlate800)
            Spacer(Modifier.height(6.dp))
            Text("Your feedback has been submitted successfully.", fontSize = 13.sp, color = ArSlate500)
            Spacer(Modifier.height(20.dp))
            GradientButton(text = "Back to Home") {
                vm.finishSession()
                onComplete()
            }
        }
        return
    }

    if (total == 0) {
        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("No survey questions available.", color = ArSlate500)
        }
        return
    }

    val current = questions[currentIndex]
    val questionOptions = current.options ?: listOf("Very Poor", "Poor", "Average", "Good", "Excellent")
    val isNps = current.nps
    val answeredCount = answers.size

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ArPrimary)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(survey.templateName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(survey.course, color = Color(0xFFCBD5E1), fontSize = 11.sp)
            }
            Text("$answeredCount/$total answered", color = Color(0xFFCBD5E1), fontSize = 11.sp)
        }

        Column(modifier = Modifier.padding(16.dp)) {
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
                Text(current.text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = ArSlate800)
            }
            Spacer(Modifier.height(16.dp))

            if (isNps) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    questionOptions.forEach { option ->
                        val selected = answers[currentIndex] == option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) ArPrimary else ArSlate100)
                                .clickableNoRipple {
                                    answers[currentIndex] = option
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                option,
                                color = if (selected) Color.White else ArSlate500,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Not at all likely", fontSize = 10.sp, color = ArSlate500)
                    Text("Extremely likely", fontSize = 10.sp, color = ArSlate500)
                }
            } else {
                questionOptions.forEach { option ->
                    val selected = answers[currentIndex] == option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                if (selected) 2.dp else 1.dp,
                                if (selected) ArPrimary else ArSlate200,
                                RoundedCornerShape(12.dp)
                            )
                            .background(if (selected) ArPrimary.copy(alpha = 0.06f) else Color.White)
                            .clickableNoRipple {
                                answers[currentIndex] = option
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .border(2.dp, if (selected) ArPrimary else ArSlate200, CircleShape)
                                .background(if (selected) ArPrimary else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selected) {
                                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color.White))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(option, fontSize = 14.sp, color = ArSlate700)
                    }
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
                    enabled = currentIndex > 0,
                    onClick = { if (currentIndex > 0) currentIndex -= 1 }
                )
                if (currentIndex == total - 1) {
                    GradientButton(
                        text = "Submit Survey",
                        modifier = Modifier.width(170.dp)
                    ) {
                        vm.submitSurvey(answers.toMap())
                        submitted = true
                    }
                } else {
                    PrimaryButton(text = "Next", onClick = { currentIndex += 1 })
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ArSlate100))
            Spacer(Modifier.height(10.dp))
            Text("Question Palette", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ArSlate500)
            Spacer(Modifier.height(10.dp))
            val rows = questions.indices.toList().chunked(8)
            rows.forEach { row ->
                Row(modifier = Modifier.padding(bottom = 8.dp)) {
                    row.forEach { index ->
                        val isCurrent = index == currentIndex
                        val isAnswered = answers.containsKey(index)
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
                                .size(32.dp)
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

