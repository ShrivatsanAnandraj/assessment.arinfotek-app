package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.AdminPanel
import com.example.myapplication.ui.AppFooter
import com.example.myapplication.ui.AppHeader
import com.example.myapplication.ui.GradientButton
import com.example.myapplication.ui.JoinScreen
import com.example.myapplication.ui.ResultScreen
import com.example.myapplication.ui.SurveyScreen
import com.example.myapplication.ui.TestScreen
import com.example.myapplication.ui.theme.ArPrimary
import com.example.myapplication.ui.theme.ArSlate50
import com.example.myapplication.ui.theme.ArSlate200
import com.example.myapplication.ui.theme.ArSlate500
import com.example.myapplication.ui.theme.ArSlate800
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AssessmentApp()
            }
        }
    }
}

@Composable
fun AssessmentApp(vm: AppViewModel = viewModel()) {
    var showAdminAuth by remember { mutableStateOf(false) }
    var adminPassword by remember { mutableStateOf("") }
    var adminError by remember { mutableStateOf("") }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && vm.screen == Screen.TEST) {
                vm.flagTabViolation()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        containerColor = ArSlate50,
        topBar = { AppHeader() }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            if (vm.loading) {
                androidx.compose.material3.LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = ArPrimary
                )
            }
            when (vm.screen) {
                Screen.JOIN -> JoinScreen(
                    vm = vm,
                    onVerified = {},
                    onStartTest = { vm.startTest() },
                    onStartSurvey = {
                        vm.openSurveyForCurrentTest { ok ->
                            if (!ok) vm.joinError = "No survey available for this test yet."
                        }
                    }
                )

                Screen.TEST -> TestScreen(vm = vm, onSubmit = { })

                Screen.RESULT -> {
                    val result = vm.result
                    if (result != null) {
                        ResultScreen(
                            vm = vm,
                            result = result,
                            onTakeSurvey = {
                                vm.openSurveyForCurrentTest { ok ->
                                    if (!ok) vm.joinError = "No survey available for this test yet."
                                }
                            }
                        )
                    }
                }

                Screen.SURVEY -> {
                    val survey = vm.activeSurvey
                    if (survey != null) {
                        SurveyScreen(vm = vm, survey = survey, onComplete = { })
                    } else {
                        vm.finishSession()
                    }
                }

                Screen.ADMIN -> AdminPanel(vm = vm)
            }
            Spacer(Modifier.size(16.dp))

            AppFooter(screen = vm.screen) {
                if (vm.screen == Screen.ADMIN) {
                    vm.finishSession()
                } else {
                    showAdminAuth = true
                    adminPassword = ""
                    adminError = ""
                }
            }
        }
    }

    if (showAdminAuth) {
        AdminAuthDialog(
            password = adminPassword,
            error = adminError,
            onPasswordChange = { adminPassword = it },
            onCancel = { showAdminAuth = false },
            onSubmit = {
                if (adminPassword == "demo") {
                    showAdminAuth = false
                    vm.finishSession()
                    vm.openAdmin()
                } else {
                    adminError = "Incorrect password"
                }
            }
        )
    }
}

@Composable
private fun AdminAuthDialog(
    password: String,
    error: String,
    onPasswordChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSubmit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = null,
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(ArPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔒", fontSize = 24.sp)
                }
                Spacer(Modifier.size(12.dp))
                Text("Admin Access", fontWeight = FontWeight.Black, fontSize = 18.sp, color = ArSlate800)
                Spacer(Modifier.size(4.dp))
                Text("Enter the admin password to continue", fontSize = 13.sp, color = ArSlate500)
                Spacer(Modifier.size(14.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    placeholder = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArPrimary,
                        unfocusedBorderColor = ArSlate200
                    )
                )
                if (error.isNotBlank()) {
                    Spacer(Modifier.size(8.dp))
                    Text(error, color = Color(0xFFDC2626), fontSize = 13.sp)
                }
                Spacer(Modifier.size(14.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                        Text("Cancel", color = ArSlate500, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        GradientButton(text = "Login", onClick = onSubmit)
                    }
                }
            }
        },
        confirmButton = {}
    )
}
