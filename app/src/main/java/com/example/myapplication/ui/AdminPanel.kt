package com.example.myapplication.ui

import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.AppViewModel
import com.example.myapplication.data.ExcelQuestions
import com.example.myapplication.data.Question
import com.example.myapplication.data.SurveyResponse
import com.example.myapplication.data.TabFlag
import com.example.myapplication.data.Test
import com.example.myapplication.ui.theme.ArAccent
import com.example.myapplication.ui.theme.ArOrangeLight
import com.example.myapplication.ui.theme.ArPrimary
import com.example.myapplication.ui.theme.ArSlate100
import com.example.myapplication.ui.theme.ArSlate200
import com.example.myapplication.ui.theme.ArSlate400
import com.example.myapplication.ui.theme.ArSlate500
import com.example.myapplication.ui.theme.ArSlate600
import com.example.myapplication.ui.theme.ArSlate700
import com.example.myapplication.ui.theme.ArSlate800
import com.example.myapplication.ui.theme.ArGreen
import com.example.myapplication.ui.theme.ArGreenLight
import com.example.myapplication.ui.theme.ArRed
import com.example.myapplication.ui.theme.ArRedLight

private enum class AdminView(val title: String, val icon: String) {
    MENU("Admin Panel", "⚙"),
    CREATE_TEST("Create Test", "➕"),
    IMPORT_EXCEL("Import Questions from Excel", "📥"),
    VIEW_QUESTIONS("View Questions", "❓"),
    SCORES("Scores", "🏆"),
    FLAGS("Flagged", "🚩"),
    CREATE_SURVEY("Create Survey", "🗒"),
    FEEDBACK("Feedback", "💬")
}

@Composable
fun AdminPanel(vm: AppViewModel) {
    var view by remember { mutableStateOf(AdminView.MENU) }

    LaunchedEffect(view) {
        if (view == AdminView.FLAGS) vm.refreshFlags()
    }

    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        if (view != AdminView.MENU) {
            OutlineButton(text = "← Back to Admin", onClick = { view = AdminView.MENU })
            Spacer(Modifier.height(10.dp))
        }
        when (view) {
            AdminView.MENU -> AdminMenu(onSelect = { view = it })
            AdminView.CREATE_TEST -> CreateTestView(vm, onImported = { view = AdminView.CREATE_TEST })
            AdminView.IMPORT_EXCEL -> ImportExcelView(vm, onGoToCreate = { view = AdminView.CREATE_TEST })
            AdminView.VIEW_QUESTIONS -> ViewQuestionsView(vm)
            AdminView.SCORES -> ScoresView(vm)
            AdminView.FLAGS -> FlagsView(vm)
            AdminView.CREATE_SURVEY -> CreateSurveyView(vm)
            AdminView.FEEDBACK -> FeedbackView(vm)
        }
    }
}

@Composable
private fun AdminMenu(onSelect: (AdminView) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text("Admin Panel", fontWeight = FontWeight.Black, fontSize = 20.sp, color = ArSlate800)
        Spacer(Modifier.height(4.dp))
        Text("Manage tests, scores and feedback", fontSize = 13.sp, color = ArSlate500)
        Spacer(Modifier.height(16.dp))
        AdminMenuItems.forEach { menu ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, ArSlate200, RoundedCornerShape(12.dp))
                    .clickableNoRipple { onSelect(menu.view) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ArPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(menu.view.icon, fontSize = 20.sp)
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(menu.view.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ArSlate800)
                    Text(menu.subtitle, fontSize = 12.sp, color = ArSlate500)
                }
            }
        }
    }
}

private data class AdminMenuItem(val view: AdminView, val subtitle: String)

private val AdminMenuItems = listOf(
    AdminMenuItem(AdminView.CREATE_TEST, "Create a new MCQ test with questions"),
    AdminMenuItem(AdminView.IMPORT_EXCEL, "Upload questions from an Excel file"),
    AdminMenuItem(AdminView.VIEW_QUESTIONS, "Browse questions of existing tests"),
    AdminMenuItem(AdminView.SCORES, "View all student attempts and scores"),
    AdminMenuItem(AdminView.FLAGS, "Approve or review tab-switch violations"),
    AdminMenuItem(AdminView.CREATE_SURVEY, "Attach a feedback survey to a test code"),
    AdminMenuItem(AdminView.FEEDBACK, "Review survey responses and analysis")
)

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(title, fontWeight = FontWeight.Black, fontSize = 17.sp, color = ArSlate800)
        Spacer(Modifier.height(14.dp))
        content()
    }
}

@Composable
private fun CreateTestView(vm: AppViewModel, onImported: () -> Unit = {}) {
    var title by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("30") }
    var subject by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("") }
    var topics by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var messageIsError by remember { mutableStateOf(false) }

    val questions = remember { mutableStateListOf(EditableQuestion()) }

    LaunchedEffect(Unit) {
        vm.consumeImportedQuestions()?.let { imported ->
            questions.clear()
            imported.forEach { q -> questions.add(EditableQuestion.from(q)) }
            message = "Imported ${imported.size} questions from Excel."
            messageIsError = false
        }
    }

    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        SectionCard(title = "Create Test") {
            FieldLabel("Test Title")
            AppTextField(title, { title = it }, "e.g. Python Basics Assessment")
            Spacer(Modifier.height(10.dp))
            FieldLabel("Test Code")
            AppTextField(code, { code = it.uppercase() }, "e.g. APT01")
            Spacer(Modifier.height(10.dp))
            FieldLabel("Duration (minutes)")
            AppTextField(duration, { duration = it.filter { c -> c.isDigit() } }, "30")
            Spacer(Modifier.height(10.dp))
            FieldLabel("Subject")
            AppTextField(subject, { subject = it }, "e.g. Python")
            Spacer(Modifier.height(10.dp))
            FieldLabel("Course")
            AppTextField(course, { course = it }, "e.g. Python Programming")
            Spacer(Modifier.height(10.dp))
            FieldLabel("Level")
            AppTextField(level, { level = it }, "basic / intermediate / advanced")
            Spacer(Modifier.height(10.dp))
            FieldLabel("Topics (comma separated)")
            AppTextField(topics, { topics = it }, "variables, loops, functions")
        }

        Spacer(Modifier.height(12.dp))
        SectionCard(title = "Questions (${questions.size})") {
            questions.forEachIndexed { index, q ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ArSlate100)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Question ${index + 1}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ArSlate700)
                        if (questions.size > 1) {
                            Text(
                                "Remove",
                                color = ArRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickableNoRipple { questions.removeAt(index) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    AppTextField(q.text, { q.text = it }, "Question text", singleLine = false)
                    Spacer(Modifier.height(8.dp))
                    listOf("A", "B", "C", "D").forEachIndexed { oi, label ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (q.correct == oi) ArGreen else ArSlate200)
                                    .clickableNoRipple { q.correct = oi },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(Modifier.width(8.dp))
                            AppTextField(
                                value = q.options[oi],
                                onValueChange = { q.options[oi] = it },
                                placeholder = "Option $label"
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                    Text(
                        "Tap a letter to mark it correct (currently: ${listOf("A", "B", "C", "D")[q.correct]})",
                        fontSize = 11.sp,
                        color = ArSlate500
                    )
                }
            }
            OutlineButton(text = "＋ Add Question", modifier = Modifier.fillMaxWidth()) {
                questions.add(EditableQuestion())
            }
        }

        Spacer(Modifier.height(12.dp))
        if (message.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (messageIsError) ArRedLight else ArGreenLight)
                    .padding(12.dp)
            ) {
                Text(message, color = if (messageIsError) ArRed else ArGreen, fontSize = 13.sp)
            }
            Spacer(Modifier.height(12.dp))
        }

        GradientButton(text = "Create Test") {
            val error = validateQuestions(questions.toList())
            if (error != null) {
                message = error
                messageIsError = true
                return@GradientButton
            }
            val result = vm.createTest(
                title = title,
                testCode = code,
                duration = duration.toIntOrNull() ?: 30,
                subject = subject,
                course = course,
                level = level,
                topics = topics,
                questions = questions.mapIndexed { i, q ->
                    Question(i, 0, q.text.trim(), q.options.toList(), q.correct)
                }
            )
            if (result == null) {
                message = "Success! Test \"${code.uppercase()}\" created with ${questions.size} questions."
                messageIsError = false
                title = ""; code = ""; duration = "30"; subject = ""; course = ""; level = ""; topics = ""
                questions.clear()
                questions.add(EditableQuestion())
            } else {
                message = "Error: $result"
                messageIsError = true
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

private fun validateQuestions(questions: List<EditableQuestion>): String? {
    if (questions.isEmpty()) return "Add at least one question."
    questions.forEachIndexed { i, q ->
        if (q.text.isBlank()) return "Error: Question ${i + 1} text is empty."
        q.options.forEachIndexed { oi, opt ->
            if (opt.isBlank()) {
                val letter = listOf("A", "B", "C", "D")[oi]
                return "Error: Question ${i + 1}, Option $letter is empty."
            }
        }
    }
    return null
}

@Composable
private fun ImportExcelView(vm: AppViewModel, onGoToCreate: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var message by remember { mutableStateOf("") }
    var messageIsError by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            val stream = context.contentResolver.openInputStream(uri)
                ?: throw IllegalStateException("Could not open file")
            stream.use { ExcelQuestions.parse(it) }
        }.onSuccess { parsed ->
            vm.storeImportedQuestions(parsed)
            message = "Imported ${parsed.size} questions from Excel."
            messageIsError = false
            onGoToCreate()
        }.onFailure { e ->
            message = e.message ?: "Error: Failed to parse Excel file. Check the format."
            messageIsError = true
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
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
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(ArAccent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text("📄", fontSize = 26.sp)
            }
            Spacer(Modifier.height(12.dp))
            Text("Import Questions", fontWeight = FontWeight.Black, fontSize = 18.sp, color = ArSlate800)
            Spacer(Modifier.height(6.dp))
            Text(
                "Upload an Excel file or download the template first",
                fontSize = 13.sp,
                color = ArSlate500
            )
            Spacer(Modifier.height(18.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlineButton(
                    text = "Cancel",
                    modifier = Modifier.weight(1f),
                    onClick = { message = "" }
                )
                Spacer(Modifier.width(8.dp))
                PrimaryButton(
                    text = "Download Template",
                    modifier = Modifier.weight(1f),
                    onClick = { downloadTemplate(context) { msg -> message = msg; messageIsError = false } }
                )
            }
            Spacer(Modifier.height(8.dp))
            GradientButton(text = "Upload File", modifier = Modifier.fillMaxWidth()) {
                message = ""
                picker.launch(
                    arrayOf(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        "application/vnd.ms-excel"
                    )
                )
            }
        }

        if (message.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (messageIsError) ArRedLight else ArGreenLight)
                    .padding(12.dp)
            ) {
                Text(message, color = if (messageIsError) ArRed else ArGreen, fontSize = 13.sp)
            }
        }
    }
}

private fun downloadTemplate(context: android.content.Context, onResult: (String) -> Unit) {
    val name = "arinfotek-question-template.xlsx"
    runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(
                    MediaStore.MediaColumns.MIME_TYPE,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Download")
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IllegalStateException("Could not create file")
            resolver.openOutputStream(uri)?.use { ExcelQuestions.writeTemplate(it) }
                ?: throw IllegalStateException("Could not write file")
        } else {
            val dir = context.getExternalFilesDir(null) ?: context.filesDir
            val file = java.io.File(dir, name)
            java.io.FileOutputStream(file).use { ExcelQuestions.writeTemplate(it) }
        }
    }.onSuccess {
        onResult("Template saved: $name")
    }.onFailure {
        onResult("Error: " + (it.message ?: "Could not save template"))
    }
}

private class EditableQuestion {
    var text by mutableStateOf("")
    val options = mutableStateListOf("", "", "", "")
    var correct by mutableStateOf(0)

    companion object {
        fun from(q: Question): EditableQuestion = EditableQuestion().apply {
            text = q.questionText
            q.options.take(4).forEachIndexed { i, o -> options[i] = o }
            correct = q.correctAnswer.coerceIn(0, 3)
        }
    }
}

@Composable
private fun ViewQuestionsView(vm: AppViewModel) {
    val tests = vm.allTests()
    if (tests.isEmpty()) {
        SectionCard(title = "View Questions") {
            Text("No tests created yet.", color = ArSlate500, fontSize = 13.sp)
        }
        return
    }
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(tests) { test ->
            TestQuestionCard(
                test = test,
                questions = vm.questionsForTest(test.id),
                onDelete = { vm.deleteTest(test.testCode) }
            )
        }
    }
}

@Composable
private fun TestQuestionCard(test: Test, questions: List<Question>, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(test.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ArSlate800)
                Text(
                    "Code: ${test.testCode} · ${test.durationMinutes} min · ${questions.size} questions",
                    fontSize = 12.sp,
                    color = ArSlate500
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(ArRedLight)
                    .clickableNoRipple { onDelete() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Delete", color = ArRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            if (expanded) "Hide questions" else "Show questions",
            color = ArPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.clickableNoRipple { expanded = !expanded }
        )
        if (expanded) {
            Spacer(Modifier.height(10.dp))
            questions.forEachIndexed { i, q ->
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text("Q${i + 1}. ${q.questionText}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = ArSlate800)
                    q.options.forEachIndexed { oi, opt ->
                        val correct = oi == q.correctAnswer
                        Text(
                            "${listOf("A", "B", "C", "D").getOrElse(oi) { "?" }}. $opt" + if (correct) "  ✓" else "",
                            fontSize = 12.sp,
                            color = if (correct) ArGreen else ArSlate500,
                            fontWeight = if (correct) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoresView(vm: AppViewModel) {
    var refresh by remember { mutableStateOf(0) }
    val attempts = remember(refresh) { vm.allAttempts() }

    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        SectionCard(title = "Scores (${attempts.size})") {
            if (attempts.isEmpty()) {
                Text("No attempts yet.", color = ArSlate500, fontSize = 13.sp)
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ArRedLight)
                            .clickableNoRipple { vm.deleteAllAttempts(); refresh++ }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Clear all", color = ArRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
                Spacer(Modifier.height(10.dp))
                attempts.forEach { attempt ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ArSlate100)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(attempt.studentName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ArSlate800)
                            Text("${attempt.testCode} · Reg: ${attempt.studentRegisterId}", fontSize = 11.sp, color = ArSlate500)
                        }
                        val passed = attempt.total > 0 && attempt.score * 100 / attempt.total >= 40
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (passed) ArGreenLight else ArRedLight)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "${attempt.score}/${attempt.total}",
                                color = if (passed) ArGreen else ArRed,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "✕",
                            color = ArRed,
                            fontSize = 14.sp,
                            modifier = Modifier.clickableNoRipple { vm.deleteAttempt(attempt.id); refresh++ }
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun FlagsView(vm: AppViewModel) {
    val flags = vm.adminFlags
    SectionCard(title = "Flagged Attempts (${flags.size})") {
        if (flags.isEmpty()) {
            Text("No active flags. Refreshes automatically.", color = ArSlate500, fontSize = 13.sp)
        } else {
            flags.forEach { flag: TabFlag ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ArRedLight.copy(alpha = 0.5f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(flag.studentName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ArSlate800)
                        Text(
                            "${flag.testCode} · Reg: ${flag.studentRegisterId} · ${flag.violationCount} violation(s)",
                            fontSize = 11.sp,
                            color = ArSlate500
                        )
                        Text(flag.reason, fontSize = 11.sp, color = ArRed)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ArPrimary)
                            .clickableNoRipple { vm.approveFlag(flag.id) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("Approve", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateSurveyView(vm: AppViewModel) {
    var testCode by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var trainee by remember { mutableStateOf("") }
    var noOfDays by remember { mutableStateOf("") }
    var templateName by remember { mutableStateOf(vm.templates.firstOrNull() ?: "") }
    var message by remember { mutableStateOf("") }
    var messageIsError by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        SectionCard(title = "Create Survey") {
            FieldLabel("Test Code")
            AppTextField(testCode, { testCode = it.uppercase() }, "e.g. APT01")
            Spacer(Modifier.height(10.dp))
            FieldLabel("Course")
            AppTextField(course, { course = it }, "e.g. Python Programming")
            Spacer(Modifier.height(10.dp))
            FieldLabel("Trainee / Batch")
            AppTextField(trainee, { trainee = it }, "e.g. Batch 2025")
            Spacer(Modifier.height(10.dp))
            FieldLabel("No. of Days")
            AppTextField(noOfDays, { noOfDays = it.filter { c -> c.isDigit() } }, "e.g. 5")
            Spacer(Modifier.height(10.dp))
            FieldLabel("Survey Template")
            vm.templates.forEach { template ->
                val selected = template == templateName
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(
                            if (selected) 2.dp else 1.dp,
                            if (selected) ArPrimary else ArSlate200,
                            RoundedCornerShape(10.dp)
                        )
                        .background(if (selected) ArPrimary.copy(alpha = 0.06f) else Color.White)
                        .clickableNoRipple { templateName = template }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .border(2.dp, if (selected) ArPrimary else ArSlate200, CircleShape)
                            .background(if (selected) ArPrimary else Color.Transparent)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(template, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = ArSlate700)
                }
            }
            Text(
                "Template preview (${vm.templateQuestions(templateName).size} questions)",
                fontSize = 11.sp,
                color = ArSlate500
            )
        }
        Spacer(Modifier.height(12.dp))
        SectionCard(title = "Template Questions") {
            vm.templateQuestions(templateName).forEachIndexed { i, q ->
                Text("Q${i + 1}. ${q.text}", fontSize = 12.sp, color = ArSlate600, modifier = Modifier.padding(bottom = 6.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        if (message.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (messageIsError) ArRedLight else ArGreenLight)
                    .padding(12.dp)
            ) {
                Text(message, color = if (messageIsError) ArRed else ArGreen, fontSize = 13.sp)
            }
            Spacer(Modifier.height(12.dp))
        }
        GradientButton(text = "Create Survey") {
            vm.createSurvey(
                testCode = testCode,
                course = course,
                trainee = trainee,
                noOfDays = noOfDays.toIntOrNull() ?: 0,
                templateName = templateName
            ) { error ->
                if (error == null) {
                    message = "Success! Survey created for test code: ${testCode.uppercase()}"
                    messageIsError = false
                    testCode = ""; course = ""; trainee = ""; noOfDays = ""
                } else {
                    message = "Error: $error"
                    messageIsError = true
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun FeedbackView(vm: AppViewModel) {
    var refresh by remember { mutableStateOf(0) }
    var selectedTest by remember { mutableStateOf("all") }
    var analysisCode by remember { mutableStateOf("") }
    var analysis by remember { mutableStateOf<com.example.myapplication.DataAnalysis?>(null) }

    val responses = remember(refresh) { vm.surveyResponses() }
    val filtered = if (selectedTest == "all") responses else responses.filter { it.testCode == selectedTest }
    val testCodes = responses.map { it.testCode }.distinct()

    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        SectionCard(title = "Feedback Responses (${filtered.size})") {
            if (testCodes.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip("All", selected = selectedTest == "all") { selectedTest = "all" }
                    testCodes.forEach { code ->
                        FilterChip(code, selected = selectedTest == code) { selectedTest = code }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
            if (filtered.isEmpty()) {
                Text("No survey responses yet.", color = ArSlate500, fontSize = 13.sp)
            } else {
                filtered.forEach { response ->
                    FeedbackResponseCard(
                        response = response,
                        onDelete = { vm.deleteSurveyResponse(response.id); refresh++ }
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlineButton(text = "Clear all") { vm.deleteAllSurveyResponses(); refresh++ }
                    if (selectedTest != "all") {
                        OutlineButton(text = "Clear $selectedTest") {
                            vm.deleteSurveyResponsesByTest(selectedTest); refresh++
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        SectionCard(title = "Data Analysis") {
            FieldLabel("Test Code")
            AppTextField(analysisCode, { analysisCode = it.uppercase() }, "e.g. APT01")
            Spacer(Modifier.height(10.dp))
            PrimaryButton(text = "Analyze", modifier = Modifier.fillMaxWidth()) {
                analysis = vm.analyze(analysisCode)
            }
            analysis?.let { a ->
                Spacer(Modifier.height(12.dp))
                if (a.error != null) {
                    Text(a.error!!, color = ArRed, fontSize = 13.sp)
                } else {
                    Text("Overall: ${a.overallSentiment}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ArSlate800)
                    Text("${a.totalResponses} responses", fontSize = 12.sp, color = ArSlate500)
                    Spacer(Modifier.height(8.dp))
                    AnalysisGroup("Positives", a.positives, ArGreen)
                    AnalysisGroup("Needs Improvement", a.improvements, ArAccent)
                    AnalysisGroup("Negatives", a.negatives, ArRed)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(end = 8.dp)
            .clip(RoundedCornerShape(50))
            .background(if (selected) ArPrimary else ArSlate100)
            .clickableNoRipple(onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(label, color = if (selected) Color.White else ArSlate500, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun AnalysisGroup(title: String, buckets: List<com.example.myapplication.AnalysisBucket>, color: Color) {
    Spacer(Modifier.height(10.dp))
    Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = color)
    if (buckets.isEmpty()) {
        Text("— none", fontSize = 12.sp, color = ArSlate500)
    } else {
        buckets.forEach {
            Text("• ${it.question}  (${it.positivePct}% positive)", fontSize = 12.sp, color = ArSlate600)
        }
    }
}

@Composable
private fun FeedbackResponseCard(response: SurveyResponse, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(ArSlate100)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(response.studentName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ArSlate800)
                Text(
                    "${response.testCode} · Reg: ${response.studentRegisterId} · ${response.templateName}",
                    fontSize = 11.sp,
                    color = ArSlate500
                )
            }
            Text(
                "✕",
                color = ArRed,
                fontSize = 14.sp,
                modifier = Modifier.clickableNoRipple { onDelete() }
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            if (expanded) "Hide answers" else "Show answers",
            color = ArPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.clickableNoRipple { expanded = !expanded }
        )
        if (expanded) {
            Spacer(Modifier.height(8.dp))
            response.questions.forEachIndexed { i, q ->
                Column(modifier = Modifier.padding(bottom = 6.dp)) {
                    Text("Q${i + 1}: ${q.text}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = ArSlate800)
                    Text(
                        "Answer: ${response.answers[i.toString()] ?: "Not answered"}",
                        fontSize = 12.sp,
                        color = ArSlate600
                    )
                }
            }
        }
    }
}
