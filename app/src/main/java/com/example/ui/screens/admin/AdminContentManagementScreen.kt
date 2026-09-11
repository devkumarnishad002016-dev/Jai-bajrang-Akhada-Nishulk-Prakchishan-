package com.example.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminContentManagementScreen(
    trainers: List<Trainer>,
    galleryItems: List<GalleryItem>,
    successStories: List<SuccessStory>,
    contactInfo: ContactInfo?,
    onAddTrainer: (Trainer) -> Unit,
    onDeleteTrainer: (Trainer) -> Unit,
    onUpdateTrainer: (Trainer) -> Unit = {},
    onResetTrainerPassword: (coachId: String, newPassword: String) -> Result<Unit> = { _, _ -> Result.success(Unit) },
    onAddGalleryItem: (GalleryItem) -> Unit,
    onDeleteGalleryItem: (GalleryItem) -> Unit,
    onAddSuccessStory: (SuccessStory) -> Unit,
    onDeleteSuccessStory: (SuccessStory) -> Unit,
    onUpdateContactInfo: (ContactInfo) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Trainers, 1: Gallery, 2: Success Stories, 3: Contact Info

    var showTrainerDialog by remember { mutableStateOf(false) }
    var trainerToEdit by remember { mutableStateOf<Trainer?>(null) }
    var trainerToDelete by remember { mutableStateOf<Trainer?>(null) }
    var trainerForPasswordReset by remember { mutableStateOf<Trainer?>(null) }
    var showGalleryDialog by remember { mutableStateOf(false) }
    var showStoryDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("अखाड़ा कंटेंट प्रबंधन (Content CMS)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SaffronPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (selectedTab < 3) {
                FloatingActionButton(
                    onClick = {
                        when (selectedTab) {
                            0 -> showTrainerDialog = true
                            1 -> showGalleryDialog = true
                            2 -> showStoryDialog = true
                        }
                    },
                    containerColor = SaffronPrimary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Item")
                }
            }
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SaffronPrimary,
                edgePadding = 16.dp
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("प्रशिक्षक (${trainers.size})") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("गैलरी (${galleryItems.size})") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("सफलताएं (${successStories.size})") })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("संपर्क विवरण") })
            }

            if (selectedTab == 3) {
                ContactEditForm(
                    contactInfo = contactInfo,
                    onUpdateContactInfo = onUpdateContactInfo
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (selectedTab) {
                        0 -> {
                            if (trainers.isEmpty()) {
                                item {
                                    EmptyStateCard("कोई प्रशिक्षक दर्ज नहीं है। नीचे + बटन दबाकर नया प्रशिक्षक जोड़ें।")
                                }
                            } else {
                                items(trainers) { trainer ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(trainer.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                if (trainer.serviceBackground.isNotBlank()) {
                                                    Text(trainer.serviceBackground, fontSize = 12.sp, color = SaffronDark)
                                                }
                                                if (trainer.specialization.isNotBlank()) {
                                                    Text(trainer.specialization, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                if (trainer.experience.isNotBlank() || trainer.contactNumber.isNotBlank()) {
                                                    Text(
                                                        listOfNotNull(
                                                            trainer.experience.takeIf { it.isNotBlank() },
                                                            trainer.contactNumber.takeIf { it.isNotBlank() }?.let { "📞 $it" }
                                                        ).joinToString(" • "),
                                                        fontSize = 11.sp,
                                                        color = OliveTertiary
                                                    )
                                                }
                                            }
                                            IconButton(
                                                onClick = { trainerForPasswordReset = trainer },
                                                modifier = Modifier.testTag("cms_reset_coach_pass_${trainer.coachId}")
                                            ) {
                                                Icon(Icons.Default.LockReset, contentDescription = "Reset Coach Password", tint = Color(0xFFDC2626))
                                            }
                                            IconButton(onClick = { trainerToEdit = trainer }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit Coach", tint = SaffronPrimary)
                                            }
                                            IconButton(onClick = { trainerToDelete = trainer }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete Coach", tint = Color.Red.copy(alpha = 0.7f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            if (galleryItems.isEmpty()) {
                                item {
                                    EmptyStateCard("गैलरी में कोई आइटम नहीं है। नीचे + बटन दबाकर फोटो व कैप्शन जोड़ें।")
                                }
                            } else {
                                items(galleryItems) { item ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                Text("श्रेणी: ${item.category}", fontSize = 12.sp, color = SaffronPrimary)
                                                if (item.caption.isNotBlank()) {
                                                    Text(item.caption, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                            IconButton(onClick = { onDeleteGalleryItem(item) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            if (successStories.isEmpty()) {
                                item {
                                    EmptyStateCard("कोई सफलता की कहानी दर्ज नहीं है। नीचे + बटन दबाकर चयनित छात्रों का विवरण जोड़ें।")
                                }
                            } else {
                                items(successStories) { story ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(story.studentName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                Text("गाँव: ${story.village} • चयन: ${story.recruitmentExam} (${story.year})", fontSize = 12.sp, color = OliveTertiary)
                                                if (story.story.isNotBlank()) {
                                                    Text(story.story, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                            IconButton(onClick = { onDeleteSuccessStory(story) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Trainer Dialog
    if (showTrainerDialog) {
        var name by remember { mutableStateOf("") }
        var bg by remember { mutableStateOf("") }
        var exp by remember { mutableStateOf("") }
        var spec by remember { mutableStateOf("") }
        var intro by remember { mutableStateOf("") }
        var contact by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showTrainerDialog = false },
            title = { Text("नया प्रशिक्षक / कोच जोड़ें") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("प्रशिक्षक का नाम *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("मोबाइल / संपर्क नंबर") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = bg, onValueChange = { bg = it }, label = { Text("पृष्ठभूमि (उदा. पूर्व सेना / खेल प्रशिक्षक)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = exp, onValueChange = { exp = it }, label = { Text("अनुभव (उदा. 6+ वर्ष)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = spec, onValueChange = { spec = it }, label = { Text("विशेषज्ञता (उदा. 1600m रनिंग, बीम, फिजिकल)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = intro, onValueChange = { intro = it }, label = { Text("संक्षिप्त परिचय") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onAddTrainer(
                                Trainer(
                                    name = name.trim(),
                                    serviceBackground = bg.trim(),
                                    experience = exp.trim(),
                                    specialization = spec.trim(),
                                    introduction = intro.trim(),
                                    contactNumber = contact.trim()
                                )
                            )
                            showTrainerDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) { Text("जोड़ें") }
            },
            dismissButton = {
                TextButton(onClick = { showTrainerDialog = false }) { Text("रद्द करें") }
            }
        )
    }

    // Edit Trainer Dialog (Full Admin Edit Power)
    trainerToEdit?.let { currentTrainer ->
        var editName by remember(currentTrainer) { mutableStateOf(currentTrainer.name) }
        var editBg by remember(currentTrainer) { mutableStateOf(currentTrainer.serviceBackground) }
        var editExp by remember(currentTrainer) { mutableStateOf(currentTrainer.experience) }
        var editSpec by remember(currentTrainer) { mutableStateOf(currentTrainer.specialization) }
        var editIntro by remember(currentTrainer) { mutableStateOf(currentTrainer.introduction) }
        var editContact by remember(currentTrainer) { mutableStateOf(currentTrainer.contactNumber) }

        AlertDialog(
            onDismissRequest = { trainerToEdit = null },
            title = { Text("प्रशिक्षक / कोच विवरण संपादित करें (Edit Coach)") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("प्रशिक्षक का नाम *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editContact, onValueChange = { editContact = it }, label = { Text("मोबाइल / संपर्क नंबर") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editBg, onValueChange = { editBg = it }, label = { Text("पृष्ठभूमि (उदा. पूर्व सेना / फिटनेस कोच)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editExp, onValueChange = { editExp = it }, label = { Text("अनुभव (उदा. 8+ वर्ष)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editSpec, onValueChange = { editSpec = it }, label = { Text("विशेषज्ञता (उदा. 1600m रनिंग, पुश-अप्स)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editIntro, onValueChange = { editIntro = it }, label = { Text("संक्षिप्त परिचय") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isNotBlank()) {
                            onUpdateTrainer(
                                currentTrainer.copy(
                                    name = editName.trim(),
                                    serviceBackground = editBg.trim(),
                                    experience = editExp.trim(),
                                    specialization = editSpec.trim(),
                                    introduction = editIntro.trim(),
                                    contactNumber = editContact.trim()
                                )
                            )
                            trainerToEdit = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) { Text("सहेजें (Save Changes)") }
            },
            dismissButton = {
                TextButton(onClick = { trainerToEdit = null }) { Text("रद्द करें") }
            }
        )
    }

    // Delete Trainer Confirmation Dialog
    trainerToDelete?.let { targetTrainer ->
        AlertDialog(
            onDismissRequest = { trainerToDelete = null },
            title = { Text("कोच हटाएं (Remove Coach)") },
            text = {
                Text("क्या आप निश्चित रूप से कोच '${targetTrainer.name}' को हटाना चाहते हैं? यह कार्यवाही वापस नहीं होगी।")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTrainer(targetTrainer)
                        trainerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("हटाएं (Remove)") }
            },
            dismissButton = {
                TextButton(onClick = { trainerToDelete = null }) { Text("रद्द करें") }
            }
        )
    }

    // Add Gallery Dialog
    if (showGalleryDialog) {
        var title by remember { mutableStateOf("") }
        var cat by remember { mutableStateOf("Physical Training") }
        var caption by remember { mutableStateOf("") }
        val cats = listOf("Physical Training", "Running", "Ground Training", "Written Classes", "Events", "Awareness Campaign", "Other")

        AlertDialog(
            onDismissRequest = { showGalleryDialog = false },
            title = { Text("गैलरी फोटो/इवेंट जोड़ें") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("शीर्षक (Title) *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = caption, onValueChange = { caption = it }, label = { Text("कैप्शन / विवरण") }, modifier = Modifier.fillMaxWidth())
                    Text("श्रेणी चुनें:", style = MaterialTheme.typography.labelSmall)
                    ScrollableTabRow(selectedTabIndex = cats.indexOf(cat).coerceAtLeast(0), edgePadding = 0.dp) {
                        cats.forEach { c ->
                            Tab(selected = cat == c, onClick = { cat = c }, text = { Text(c, fontSize = 11.sp) })
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onAddGalleryItem(
                                GalleryItem(
                                    title = title.trim(),
                                    category = cat,
                                    caption = caption.trim()
                                )
                            )
                            showGalleryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) { Text("जोड़ें") }
            },
            dismissButton = {
                TextButton(onClick = { showGalleryDialog = false }) { Text("रद्द करें") }
            }
        )
    }

    // Add Success Story Dialog
    if (showStoryDialog) {
        var studentName by remember { mutableStateOf("") }
        var village by remember { mutableStateOf("") }
        var exam by remember { mutableStateOf("") }
        var year by remember { mutableStateOf("2025") }
        var story by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showStoryDialog = false },
            title = { Text("सफलता की कहानी जोड़ें") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = studentName, onValueChange = { studentName = it }, label = { Text("छात्र का नाम *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = village, onValueChange = { village = it }, label = { Text("गाँव का नाम *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = exam, onValueChange = { exam = it }, label = { Text("चयनित पद/भर्ती (उदा. Indian Army GD) *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("चयन वर्ष (उदा. 2025)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = story, onValueChange = { story = it }, label = { Text("सफलता का संदेश / अनुभव") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (studentName.isNotBlank() && exam.isNotBlank()) {
                            onAddSuccessStory(
                                SuccessStory(
                                    studentName = studentName.trim(),
                                    village = village.trim(),
                                    recruitmentExam = exam.trim(),
                                    year = year.trim(),
                                    story = story.trim()
                                )
                            )
                            showStoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) { Text("जोड़ें") }
            },
            dismissButton = {
                TextButton(onClick = { showStoryDialog = false }) { Text("रद्द करें") }
            }
        )
    }

    // Reset Coach Password Dialog
    trainerForPasswordReset?.let { coach ->
        val context = LocalContext.current
        var newPass by remember { mutableStateOf("") }
        var confirmPass by remember { mutableStateOf("") }
        var isPasswordVisible by remember { mutableStateOf(false) }
        var resetError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { trainerForPasswordReset = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.LockReset,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "कोच पासवर्ड रीसेट व संपादन",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "एडमिन नियंत्रण (Admin Authority)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = SaffronContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = coach.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Surface(
                                    color = SaffronPrimary,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = coach.coachId.ifEmpty { "COACH" },
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "अनुभव / विशेषज्ञता: ${coach.experience.ifEmpty { coach.specialization.ifEmpty { "प्रशिक्षक" } }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (resetError != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = resetError ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Text(
                        text = "⚡ त्वरित डिफ़ॉल्ट चुनें (Quick Presets):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SuggestionChip(
                            onClick = {
                                newPass = "Coach@123456"
                                confirmPass = "Coach@123456"
                                resetError = null
                            },
                            label = { Text("Coach@123456", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                        SuggestionChip(
                            onClick = {
                                newPass = "JBA@Coach2026"
                                confirmPass = "JBA@Coach2026"
                                resetError = null
                            },
                            label = { Text("JBA@Coach2026", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = newPass,
                        onValueChange = {
                            newPass = it
                            resetError = null
                        },
                        label = { Text("नया पासवर्ड (New Password)") },
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Visibility"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_cms_new_coach_pass")
                    )

                    OutlinedTextField(
                        value = confirmPass,
                        onValueChange = {
                            confirmPass = it
                            resetError = null
                        },
                        label = { Text("पासवर्ड पुष्टि करें (Confirm Password)") },
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_cms_confirm_coach_pass")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newPass.trim()
                        if (trimmed.isEmpty()) {
                            resetError = "कृपया नया पासवर्ड दर्ज करें या त्वरित बटन चुनें।"
                        } else if (trimmed.length < 6) {
                            resetError = "पासवर्ड कम से कम 6 अक्षरों का होना चाहिए।"
                        } else if (trimmed != confirmPass.trim()) {
                            resetError = "पासवर्ड और पुष्टि पासवर्ड मेल नहीं खाते।"
                        } else {
                            val coachIdToUpdate = coach.coachId.ifEmpty { coach.id.toString() }
                            val result = onResetTrainerPassword(coachIdToUpdate, trimmed)
                            if (result.isSuccess) {
                                val successMsg = "कोच ${coach.name} का नया पासवर्ड सफलतापूर्वक सेट हो गया: $trimmed"
                                android.widget.Toast.makeText(context, successMsg, android.widget.Toast.LENGTH_LONG).show()
                                trainerForPasswordReset = null
                            } else {
                                resetError = result.exceptionOrNull()?.message ?: "पासवर्ड अपडेट करने में त्रुटि हुई।"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    modifier = Modifier.testTag("btn_confirm_cms_coach_pass")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("पासवर्ड सहेजें (Save)")
                }
            },
            dismissButton = {
                TextButton(onClick = { trainerForPasswordReset = null }) {
                    Text("रद्द करें")
                }
            }
        )
    }
}

@Composable
fun ContactEditForm(
    contactInfo: ContactInfo?,
    onUpdateContactInfo: (ContactInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentContact = contactInfo ?: ContactInfo()
    var contactPerson by remember(currentContact) { mutableStateOf(currentContact.contactPerson) }
    var mobile by remember(currentContact) { mutableStateOf(currentContact.mobile) }
    var whatsapp by remember(currentContact) { mutableStateOf(currentContact.whatsapp) }
    var address by remember(currentContact) { mutableStateOf(currentContact.address) }
    var timings by remember(currentContact) { mutableStateOf(currentContact.workingHours) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("अखाड़ा संपर्क जानकारी संपादित करें", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                OutlinedTextField(
                    value = contactPerson,
                    onValueChange = { contactPerson = it },
                    label = { Text("संपर्क व्यक्ति (Contact Person)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("मोबाइल नंबर (Mobile)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { whatsapp = it },
                    label = { Text("व्हाट्सएप नंबर (WhatsApp)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("पता / केंद्र स्थल (Address)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = timings,
                    onValueChange = { timings = it },
                    label = { Text("समय सारणी (Timings)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        onUpdateContactInfo(
                            currentContact.copy(
                                contactPerson = contactPerson,
                                mobile = mobile,
                                whatsapp = whatsapp,
                                address = address,
                                workingHours = timings
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text("संपर्क जानकारी सुरक्षित करें (Save Contact)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
