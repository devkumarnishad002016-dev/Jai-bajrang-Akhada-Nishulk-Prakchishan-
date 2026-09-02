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
import androidx.compose.ui.text.font.FontWeight
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
                                            }
                                            IconButton(onClick = { onDeleteTrainer(trainer) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
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

        AlertDialog(
            onDismissRequest = { showTrainerDialog = false },
            title = { Text("नया प्रशिक्षक जोड़ें") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("प्रशिक्षक का नाम *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = bg, onValueChange = { bg = it }, label = { Text("पृष्ठभूमि (उदा. पूर्व सेना / खेल प्रशिक्षक)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = exp, onValueChange = { exp = it }, label = { Text("अनुभव (उदा. 6+ वर्ष)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = spec, onValueChange = { spec = it }, label = { Text("विशेषज्ञता (उदा. 1600m रनिंग, बीम)") }, modifier = Modifier.fillMaxWidth())
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
                                    introduction = intro.trim()
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
