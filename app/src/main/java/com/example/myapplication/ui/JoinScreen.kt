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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.example.myapplication.AppViewModel
import com.example.myapplication.data.Test
import com.example.myapplication.ui.theme.ArAccent
import com.example.myapplication.ui.theme.ArOrangeLight
import com.example.myapplication.ui.theme.ArPrimary
import com.example.myapplication.ui.theme.ArSlate100
import com.example.myapplication.ui.theme.ArSlate200
import com.example.myapplication.ui.theme.ArSlate400
import com.example.myapplication.ui.theme.ArSlate500
import com.example.myapplication.ui.theme.ArSlate600
import com.example.myapplication.ui.theme.ArSlate800
import com.example.myapplication.ui.theme.ArGreen
import com.example.myapplication.ui.theme.ArGreenLight

@Composable
fun JoinScreen(
    vm: AppViewModel,
    onVerified: () -> Unit,
    onStartTest: () -> Unit,
    onStartSurvey: () -> Unit
) {
    var name by remember { mutableStateOf(vm.studentName) }
    var regId by remember { mutableStateOf(vm.studentRegisterId) }
    var code by remember { mutableStateOf("") }

    val verified = vm.verifiedTest

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (verified != null) {
            VerifiedCard(
                test = verified,
                name = if (name.isBlank()) vm.studentName else name,
                questionCount = vm.verifiedQuestionCount,
                onTakeTest = onStartTest,
                onTakeSurvey = onStartSurvey,
                onBack = { vm.resetVerification() }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(20.dp)
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ArOrangeLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📝", fontSize = 24.sp)
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    "Online Assessment",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = ArSlate800,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Enter your details and test code to begin",
                    fontSize = 13.sp,
                    color = ArSlate500,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(20.dp))

                FieldLabel("Full Name")
                AppTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Enter your full name"
                )
                Spacer(Modifier.height(12.dp))

                FieldLabelWithHint("Register ID", "(as per college records)")
                AppTextField(
                    value = regId,
                    onValueChange = { regId = it },
                    placeholder = "Enter your register ID"
                )
                Spacer(Modifier.height(12.dp))

                FieldLabel("Test Code")
                AppTextField(
                    value = code,
                    onValueChange = { code = it.uppercase() },
                    placeholder = "e.g. ARPB248",
                    monoUppercase = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        keyboardType = KeyboardType.Text
                    )
                )

                if (vm.joinError.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFEF2F2))
                            .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(vm.joinError, color = Color(0xFFDC2626), fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(18.dp))
                GradientButton(
                    text = if (vm.verifying) "Checking..." else "Continue",
                    enabled = !vm.verifying
                ) {
                    vm.verifyTestCode(name, regId, code) { ok ->
                        if (ok) onVerified()
                    }
                }

                if (vm.verifying) {
                    Spacer(Modifier.height(10.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp).align(Alignment.CenterHorizontally),
                        color = ArPrimary,
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun VerifiedCard(
    test: Test,
    name: String,
    questionCount: Int,
    onTakeTest: () -> Unit,
    onTakeSurvey: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(20.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(60.dp)
                .clip(CircleShape)
                .background(ArGreenLight),
            contentAlignment = Alignment.Center
        ) {
            Text("✓", fontSize = 28.sp, color = ArGreen, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Welcome, $name",
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            color = ArSlate800,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Choose what you want to do",
            fontSize = 12.sp,
            color = ArSlate500,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(18.dp))

        ChoiceCard(
            icon = "📋",
            title = "Take Test",
            subtitle = "${test.title} — $questionCount questions",
            accent = ArPrimary,
            onClick = onTakeTest
        )
        Spacer(Modifier.height(10.dp))
        ChoiceCard(
            icon = "🗒",
            title = "Take Survey",
            subtitle = "Provide feedback for this training session",
            accent = ArAccent,
            onClick = onTakeSurvey
        )

        Spacer(Modifier.height(14.dp))
        OutlineButton(text = "Go Back", modifier = Modifier.fillMaxWidth(), onClick = onBack)
    }
}

@Composable
private fun ChoiceCard(
    icon: String,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(2.dp, ArSlate200, RoundedCornerShape(14.dp))
            .clickableNoRipple(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 22.sp)
        }
        Spacer(Modifier.size(14.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ArSlate800)
            Text(subtitle, fontSize = 12.sp, color = ArSlate500)
        }
    }
}

@Composable
fun FieldLabel(text: String) {
    Text(
        text,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = ArSlate600,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
fun FieldLabelWithHint(text: String, hint: String) {
    Row(modifier = Modifier.padding(bottom = 6.dp)) {
        Text(text + " ", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ArSlate600)
        Text(hint, fontWeight = FontWeight.Normal, fontSize = 13.sp, color = ArSlate400)
    }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    monoUppercase: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = ArSlate400, fontSize = 14.sp) },
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        textStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = if (monoUppercase) androidx.compose.ui.text.font.FontFamily.Monospace
            else androidx.compose.ui.text.font.FontFamily.Default,
            letterSpacing = if (monoUppercase) 1.sp else androidx.compose.ui.unit.TextUnit.Unspecified
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ArPrimary,
            unfocusedBorderColor = ArSlate200,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}



