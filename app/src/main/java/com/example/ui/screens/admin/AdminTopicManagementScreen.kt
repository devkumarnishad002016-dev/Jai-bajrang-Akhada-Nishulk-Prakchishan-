package com.example.ui.screens.admin

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Question
import com.example.data.model.StudySubject
import com.example.data.model.StudyTopic
import com.example.data.model.TopicDocument
import com.example.ui.theme.OliveTertiary
import com.example.ui.theme.SaffronPrimary
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusPresent
import com.example.util.TopicFileUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Full Admin Topic Management & Topic-wise PDF / Excel File Upload CMS.
 *
 * Capabilities for Admin:
 * 1. Add new Topic (Subject, Name, Code, Description, Order, Active switch)
 * 2. Edit existing Topic (Name, Subject, Description, Order, Status)
 * 3. Remove / Delete Topic (with confirmation dialog)
 * 4. Topic-wise File Upload (PDF notes & Excel/CSV practice sheets)
 * 5. View, Open (external viewer), Share, Preview in-app table, and Delete uploaded files.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTopicManagementScreen(
    allSubjects: List<StudySubject>,
    allTopics: List<StudyTopic>,
    allQuestions: List<Question> = emptyList(),
    allTopicDocuments: List<TopicDocument> = emptyList(),
    onAddTopic: (StudyTopic) -> Unit,
    onUpdateTopic: (StudyTopic) -> Unit,
    onDeleteTopic: (StudyTopic) -> Unit,
    onAddTopicDocument: (TopicDocument) -> Unit,
    onDeleteTopicDocument: (TopicDocument, Context) -> Unit,
    onUploadTopicFile: (context: Context, uri: Uri, topicId: String, subjectId: String, title: String, description: String, fileType: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableStateOf<String?>(null) }
    var filterType by remember { mutableStateOf<String?>("ALL") } // ALL, WITH_FILES, PDF_ONLY, EXCEL_ONLY

    // Dialog States
    var showAddTopicDialog by remember { mutableStateOf(false) }
    var topicToEdit by remember { mutableStateOf<StudyTopic?>(null) }
    var topicToDelete by remember { mutableStateOf<StudyTopic?>(null) }
    var topicForFiles by remember { mutableStateOf<StudyTopic?>(null) }
    var excelDocToPreview by remember { mutableStateOf<TopicDocument?>(null) }

    // Map questions and documents counts
    val questionCountByTopic = remember(allQuestions) {
        allQuestions.groupBy { it.topicId }.mapValues { it.value.size }
    }
    val docsByTopic = remember(allTopicDocuments) {
        allTopicDocuments.groupBy { it.topicId }
    }

    // Filtered topics
    val filteredTopics = remember(allTopics, searchQuery, selectedSubjectId, filterType, docsByTopic) {
        allTopics.filter { topic ->
            val matchesSearch = searchQuery.isBlank() ||
                topic.topicName.contains(searchQuery, ignoreCase = true) ||
                topic.topicId.contains(searchQuery, ignoreCase = true) ||
                topic.description.contains(searchQuery, ignoreCase = true)

            val matchesSubject = selectedSubjectId == null || topic.subjectId == selectedSubjectId

            val topicDocs = docsByTopic[topic.topicId] ?: emptyList()
            val matchesFilter = when (filterType) {
                "WITH_FILES" -> topicDocs.isNotEmpty()
                "PDF_ONLY" -> topicDocs.any { it.isPdf }
                "EXCEL_ONLY" -> topicDocs.any { it.isExcel }
                else -> true
            }

            matchesSearch && matchesSubject && matchesFilter
        }.sortedBy { it.displayOrder }
    }

    val totalPdfs = remember(allTopicDocuments) { allTopicDocuments.count { it.isPdf } }
    val totalExcels = remember(allTopicDocuments) { allTopicDocuments.count { it.isExcel } }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("admin_topic_management_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("टॉपिक एवं फ़ाइल प्रबंधन (CMS)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            "विषय-वार टॉपिक संपादन एवं PDF/Excel अपलोड",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_topic_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "पीछे जाएं")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddTopicDialog = true }, modifier = Modifier.testTag("admin_add_topic_action")) {
                        Icon(Icons.Default.AddCircle, contentDescription = "नया टॉपिक जोड़ें", tint = SaffronPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddTopicDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("नया टॉपिक जोड़ें", fontWeight = FontWeight.Bold) },
                containerColor = SaffronPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("admin_fab_add_topic")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Summary Metrics Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MetricItem(title = "कुल टॉपिक", value = "${allTopics.size}", icon = Icons.Default.Category, color = SaffronPrimary)
                        MetricItem(title = "PDF नोट्स", value = "$totalPdfs", icon = Icons.Default.PictureAsPdf, color = StatusAbsent)
                        MetricItem(title = "Excel शीट्स", value = "$totalExcels", icon = Icons.Default.TableChart, color = Color(0xFF059669))
                        MetricItem(title = "कुल प्रश्न", value = "${allQuestions.size}", icon = Icons.Default.Quiz, color = OliveTertiary)
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().testTag("admin_topic_search_field"),
                    placeholder = { Text("टॉपिक का नाम या कोड खोजें...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "साफ़ करें")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Subject Filter Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "विषय अनुसार फ़िल्टर:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedSubjectId == null,
                                onClick = { selectedSubjectId = null },
                                label = { Text("सभी विषय (${allSubjects.size})") }
                            )
                        }
                        items(allSubjects) { subject ->
                            FilterChip(
                                selected = selectedSubjectId == subject.subjectId,
                                onClick = {
                                    selectedSubjectId = if (selectedSubjectId == subject.subjectId) null else subject.subjectId
                                },
                                label = { Text("${subject.icon} ${subject.name}") }
                            )
                        }
                    }
                }
            }

            // File Type Filters
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    FilterChip(
                        selected = filterType == "ALL",
                        onClick = { filterType = "ALL" },
                        label = { Text("सभी (${allTopics.size})") }
                    )
                    FilterChip(
                        selected = filterType == "WITH_FILES",
                        onClick = { filterType = "WITH_FILES" },
                        label = { Text("📎 फाइलों वाले टॉपिक") }
                    )
                    FilterChip(
                        selected = filterType == "PDF_ONLY",
                        onClick = { filterType = "PDF_ONLY" },
                        label = { Text("📕 केवल PDF वाले") }
                    )
                    FilterChip(
                        selected = filterType == "EXCEL_ONLY",
                        onClick = { filterType = "EXCEL_ONLY" },
                        label = { Text("📗 केवल Excel वाले") }
                    )
                }
            }

            // Topics Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "टॉपिक सूची (${filteredTopics.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "एडिट, डिलीट एवं फ़ाइल अपलोड उपलब्ध",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Empty State
            if (filteredTopics.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                            Text("कोई टॉपिक नहीं मिला", fontWeight = FontWeight.Bold)
                            Text(
                                "खोज शब्द बदलें या 'नया टॉपिक जोड़ें' पर क्लिक करें।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = { showAddTopicDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                            ) {
                                Text("नया टॉपिक जोड़ें")
                            }
                        }
                    }
                }
            } else {
                // Topic Cards
                items(filteredTopics, key = { it.id }) { topic ->
                    val subject = allSubjects.firstOrNull { it.subjectId == topic.subjectId }
                    val qCount = questionCountByTopic[topic.topicId] ?: 0
                    val topicDocs = docsByTopic[topic.topicId] ?: emptyList()
                    val pdfCount = topicDocs.count { it.isPdf }
                    val excelCount = topicDocs.count { it.isExcel }

                    TopicAdminCard(
                        topic = topic,
                        subject = subject,
                        questionCount = qCount,
                        pdfCount = pdfCount,
                        excelCount = excelCount,
                        onEdit = { topicToEdit = topic },
                        onDelete = { topicToDelete = topic },
                        onManageFiles = { topicForFiles = topic }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    // ==========================================
    // 1. ADD TOPIC DIALOG
    // ==========================================
    if (showAddTopicDialog) {
        TopicEditDialog(
            title = "नया टॉपिक जोड़ें (Add Topic)",
            allSubjects = allSubjects,
            initialTopic = null,
            defaultSubjectId = selectedSubjectId ?: allSubjects.firstOrNull()?.subjectId ?: "SUB_MATH",
            onDismiss = { showAddTopicDialog = false },
            onSave = { newTopic ->
                onAddTopic(newTopic)
                showAddTopicDialog = false
                Toast.makeText(context, "टॉपिक '${newTopic.topicName}' सफलतापूर्वक जोड़ा गया!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // ==========================================
    // 2. EDIT TOPIC DIALOG
    // ==========================================
    topicToEdit?.let { topic ->
        TopicEditDialog(
            title = "टॉपिक संपादित करें (Edit Topic)",
            allSubjects = allSubjects,
            initialTopic = topic,
            defaultSubjectId = topic.subjectId,
            onDismiss = { topicToEdit = null },
            onSave = { updatedTopic ->
                onUpdateTopic(updatedTopic)
                topicToEdit = null
                Toast.makeText(context, "टॉपिक '${updatedTopic.topicName}' सफलतापूर्वक अपडेट हुआ!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // ==========================================
    // 3. DELETE TOPIC CONFIRMATION DIALOG
    // ==========================================
    topicToDelete?.let { topic ->
        val attachedDocs = docsByTopic[topic.topicId]?.size ?: 0
        val attachedQuestions = questionCountByTopic[topic.topicId] ?: 0

        AlertDialog(
            onDismissRequest = { topicToDelete = null },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = StatusAbsent, modifier = Modifier.size(36.dp)) },
            title = { Text("टॉपिक हटाएं (Delete Topic)?", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "क्या आप निश्चित रूप से '${topic.topicName}' टॉपिक को हटाना चाहते हैं?",
                        fontWeight = FontWeight.Medium
                    )
                    Text("टॉपिक कोड: ${topic.topicId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    if (attachedDocs > 0 || attachedQuestions > 0) {
                        Surface(
                            color = StatusAbsent.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("⚠️ ध्यान दें:", fontWeight = FontWeight.Bold, color = StatusAbsent, style = MaterialTheme.typography.labelMedium)
                                if (attachedQuestions > 0) {
                                    Text("• इस टॉपिक से जुड़े $attachedQuestions प्रश्न हैं।", style = MaterialTheme.typography.bodySmall)
                                }
                                if (attachedDocs > 0) {
                                    Text("• इस टॉपिक में $attachedDocs PDF/Excel फाइल्स अपलोड हैं।", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTopic(topic)
                        topicToDelete = null
                        Toast.makeText(context, "टॉपिक '${topic.topicName}' हटा दिया गया!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusAbsent),
                    modifier = Modifier.testTag("admin_confirm_delete_topic_button")
                ) {
                    Text("हां, हटाएं (Delete)")
                }
            },
            dismissButton = {
                TextButton(onClick = { topicToDelete = null }) {
                    Text("रद्द करें")
                }
            }
        )
    }

    // ==========================================
    // 4. TOPIC-WISE PDF / EXCEL FILE MANAGER DIALOG
    // ==========================================
    topicForFiles?.let { topic ->
        val subject = allSubjects.firstOrNull { it.subjectId == topic.subjectId }
        val topicDocs = docsByTopic[topic.topicId] ?: emptyList()

        TopicFileManagerDialog(
            topic = topic,
            subject = subject,
            documents = topicDocs,
            onDismiss = { topicForFiles = null },
            onUploadFromStorage = { uri, title, desc, fileType ->
                onUploadTopicFile(
                    context,
                    uri,
                    topic.topicId,
                    topic.subjectId,
                    title,
                    desc,
                    fileType,
                    {
                        Toast.makeText(context, "फ़ाइल सफलतापूर्वक अपलोड हुई!", Toast.LENGTH_SHORT).show()
                    },
                    { errorMsg ->
                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                    }
                )
            },
            onAddSampleDoc = { newDoc ->
                onAddTopicDocument(newDoc)
                Toast.makeText(context, "${newDoc.title} जोड़ा गया!", Toast.LENGTH_SHORT).show()
            },
            onDeleteDocument = { doc ->
                onDeleteTopicDocument(doc, context)
                Toast.makeText(context, "फ़ाइल हटाई गई!", Toast.LENGTH_SHORT).show()
            },
            onOpenDocument = { doc ->
                TopicFileUtils.openDocument(context, doc)
            },
            onShareDocument = { doc ->
                TopicFileUtils.shareDocument(context, doc)
            },
            onPreviewSpreadsheet = { doc ->
                excelDocToPreview = doc
            }
        )
    }

    // ==========================================
    // 5. EXCEL / CSV IN-APP SPREADSHEET VIEWER DIALOG
    // ==========================================
    excelDocToPreview?.let { doc ->
        ExcelSpreadsheetPreviewDialog(
            doc = doc,
            onDismiss = { excelDocToPreview = null },
            onShare = {
                TopicFileUtils.shareDocument(context, doc)
            },
            onOpenExternal = {
                TopicFileUtils.openDocument(context, doc)
            }
        )
    }
}

// -----------------------------------------------------------------------------
// METRIC CARD ITEM
// -----------------------------------------------------------------------------
@Composable
private fun MetricItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// -----------------------------------------------------------------------------
// TOPIC ADMIN CARD
// -----------------------------------------------------------------------------
@Composable
fun TopicAdminCard(
    topic: StudyTopic,
    subject: StudySubject?,
    questionCount: Int,
    pdfCount: Int,
    excelCount: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onManageFiles: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_topic_card_${topic.topicId}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header Row: Subject, Topic Name & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(OliveTertiary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(subject?.icon ?: "📖", fontSize = 16.sp)
                    }
                    Column {
                        Text(
                            text = topic.topicName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${subject?.name ?: topic.subjectId} • ID: ${topic.topicId}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Active / Inactive Badge
                Surface(
                    color = if (topic.isActive) StatusPresent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (topic.isActive) "सक्रिय" else "निष्क्रिय",
                        color = if (topic.isActive) StatusPresent else MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // Description if present
            if (topic.description.isNotBlank()) {
                Text(
                    text = topic.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Badges: Questions & Uploaded Files
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Questions count badge
                Surface(
                    color = OliveTertiary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(12.dp), tint = OliveTertiary)
                        Text("$questionCount प्रश्न", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = OliveTertiary)
                    }
                }

                // PDF count badge
                Surface(
                    color = if (pdfCount > 0) StatusAbsent.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("📕", fontSize = 10.sp)
                        Text("$pdfCount PDF", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = if (pdfCount > 0) StatusAbsent else MaterialTheme.colorScheme.outline)
                    }
                }

                // Excel count badge
                Surface(
                    color = if (excelCount > 0) Color(0xFF059669).copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("📗", fontSize = 10.sp)
                        Text("$excelCount Excel", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = if (excelCount > 0) Color(0xFF059669) else MaterialTheme.colorScheme.outline)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), thickness = 0.8.dp)

            // Action Buttons: Manage Files, Edit Topic, Delete Topic
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Button to Upload / Manage PDF and Excel
                OutlinedButton(
                    onClick = onManageFiles,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SaffronPrimary),
                    modifier = Modifier.testTag("admin_manage_topic_files_${topic.topicId}")
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF / Excel फ़ाइलें (${pdfCount + excelCount})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Edit Topic Button
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp).testTag("admin_edit_topic_${topic.topicId}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "संपादित करें", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }

                    // Delete Topic Button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp).testTag("admin_delete_topic_${topic.topicId}")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "हटाएं", tint = StatusAbsent, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// TOPIC ADD / EDIT DIALOG
// -----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicEditDialog(
    title: String,
    allSubjects: List<StudySubject>,
    initialTopic: StudyTopic?,
    defaultSubjectId: String,
    onDismiss: () -> Unit,
    onSave: (StudyTopic) -> Unit
) {
    var subjectId by remember { mutableStateOf(initialTopic?.subjectId ?: defaultSubjectId) }
    var topicName by remember { mutableStateOf(initialTopic?.topicName ?: "") }
    var topicId by remember { mutableStateOf(initialTopic?.topicId ?: "") }
    var description by remember { mutableStateOf(initialTopic?.description ?: "") }
    var displayOrder by remember { mutableStateOf((initialTopic?.displayOrder ?: 1).toString()) }
    var isActive by remember { mutableStateOf(initialTopic?.isActive ?: true) }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Subject Dropdown / Selection
                Text("विषय चुनें (Select Subject):", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(allSubjects) { sub ->
                        FilterChip(
                            selected = subjectId == sub.subjectId,
                            onClick = { subjectId = sub.subjectId },
                            label = { Text("${sub.icon} ${sub.name}") }
                        )
                    }
                }

                // Topic Name Field
                OutlinedTextField(
                    value = topicName,
                    onValueChange = {
                        topicName = it
                        nameError = it.isBlank()
                        if (initialTopic == null && topicId.isBlank()) {
                            // Suggest auto ID
                            val subPrefix = subjectId.replace("SUB_", "")
                            val cleanName = it.trim().uppercase().replace(" ", "_").filter { ch -> ch.isLetterOrDigit() || ch == '_' }.take(10)
                            if (cleanName.isNotBlank()) {
                                topicId = "TOPIC_${subPrefix}_$cleanName"
                            }
                        }
                    },
                    label = { Text("टॉपिक का नाम (Topic Name)*") },
                    isError = nameError,
                    supportingText = if (nameError) { { Text("टॉपिक का नाम अनिवार्य है") } } else null,
                    modifier = Modifier.fillMaxWidth().testTag("admin_input_topic_name"),
                    singleLine = true
                )

                // Topic ID / Code Field
                OutlinedTextField(
                    value = topicId,
                    onValueChange = { topicId = it.uppercase() },
                    label = { Text("टॉपिक कोड / ID (e.g. TOPIC_MATH_NUMSYS)") },
                    modifier = Modifier.fillMaxWidth().testTag("admin_input_topic_id"),
                    singleLine = true
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("टॉपिक विवरण व मुख्य बिंदु (Description)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                // Display Order & Active Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = displayOrder,
                        onValueChange = { displayOrder = it.filter { ch -> ch.isDigit() } },
                        label = { Text("प्रदर्शन क्रम") },
                        modifier = Modifier.width(100.dp),
                        singleLine = true
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (isActive) "सक्रिय" else "निष्क्रिय", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it },
                            modifier = Modifier.testTag("admin_switch_topic_active")
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (topicName.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    val finalTopicId = if (topicId.isNotBlank()) {
                        topicId.trim()
                    } else {
                        "TOPIC_${subjectId.replace("SUB_", "")}_${System.currentTimeMillis() % 10000}"
                    }
                    val topic = StudyTopic(
                        id = initialTopic?.id ?: 0L,
                        topicId = finalTopicId,
                        subjectId = subjectId,
                        topicName = topicName.trim(),
                        displayOrder = displayOrder.toIntOrNull() ?: 1,
                        isActive = isActive,
                        description = description.trim()
                    )
                    onSave(topic)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                modifier = Modifier.testTag("admin_save_topic_button")
            ) {
                Text("सहेजें (Save)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("रद्द करें")
            }
        }
    )
}

// -----------------------------------------------------------------------------
// TOPIC-WISE FILE MANAGER DIALOG (PDF & EXCEL UPLOAD)
// -----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicFileManagerDialog(
    topic: StudyTopic,
    subject: StudySubject?,
    documents: List<TopicDocument>,
    onDismiss: () -> Unit,
    onUploadFromStorage: (uri: Uri, title: String, description: String, fileType: String) -> Unit,
    onAddSampleDoc: (TopicDocument) -> Unit,
    onDeleteDocument: (TopicDocument) -> Unit,
    onOpenDocument: (TopicDocument) -> Unit,
    onShareDocument: (TopicDocument) -> Unit,
    onPreviewSpreadsheet: (TopicDocument) -> Unit
) {
    var showUploadModal by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: All, 1: PDF, 2: Excel

    val filteredDocs = remember(documents, selectedTab) {
        when (selectedTab) {
            1 -> documents.filter { it.isPdf }
            2 -> documents.filter { it.isExcel }
            else -> documents
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${subject?.icon ?: "📂"} ${topic.topicName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "टॉपिक PDF नोट्स एवं Excel स्प्रेडशीट CMS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "बंद करें")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Document Filter Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = SaffronPrimary,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("सभी (${documents.size})", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("📕 PDF (${documents.count { it.isPdf }})", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("📗 Excel (${documents.count { it.isExcel }})", fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Documents List
                if (filteredDocs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.outline)
                            Text("इस टॉपिक हेतु कोई फ़ाइल उपलब्ध नहीं है", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                "आप डिवाइस से PDF या Excel/CSV फ़ाइल अपलोड कर सकते हैं।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredDocs, key = { it.id }) { doc ->
                            DocumentItemRow(
                                doc = doc,
                                onOpen = { onOpenDocument(doc) },
                                onShare = { onShareDocument(doc) },
                                onPreviewSpreadsheet = { onPreviewSpreadsheet(doc) },
                                onDelete = { onDeleteDocument(doc) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons at Bottom: Upload File & Quick Template
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showUploadModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("admin_upload_file_btn")
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("फ़ाइल अपलोड करें", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            // Create academy quick template document
                            val isEx = selectedTab == 2
                            val fileType = if (isEx) "EXCEL" else "PDF"
                            val ext = if (isEx) ".xlsx" else ".pdf"
                            val newDoc = TopicDocument(
                                topicId = topic.topicId,
                                subjectId = topic.subjectId,
                                title = "${topic.topicName} " + if (isEx) "अभ्यास प्रश्न शीट" else "विस्तृत नोट्स",
                                fileName = "${topic.topicId.lowercase()}_notes$ext",
                                fileType = fileType,
                                fileSize = if (isEx) "190 KB" else "480 KB",
                                uploadDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
                                description = "${topic.topicName} हेतु जय बजरंग अखाड़ा प्रमाणित अध्ययन सामग्री व प्रश्न हल।",
                                isPublished = true
                            )
                            onAddSampleDoc(newDoc)
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ टेम्पलेट", fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Modal to pick file and enter Title/Description
    if (showUploadModal) {
        UploadDocumentModal(
            topic = topic,
            onDismiss = { showUploadModal = false },
            onUpload = { uri, title, desc, fileType ->
                onUploadFromStorage(uri, title, desc, fileType)
                showUploadModal = false
            }
        )
    }
}

// -----------------------------------------------------------------------------
// DOCUMENT ITEM ROW (Inside Topic File Dialog)
// -----------------------------------------------------------------------------
@Composable
fun DocumentItemRow(
    doc: TopicDocument,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onPreviewSpreadsheet: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(doc.fileIcon, fontSize = 22.sp)
                    Column {
                        Text(
                            text = doc.title,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${doc.fileName} • ${doc.fileSize} • ${doc.uploadDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // File Type Badge
                Surface(
                    color = if (doc.isPdf) StatusAbsent.copy(alpha = 0.15f) else Color(0xFF059669).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = doc.fileType,
                        color = if (doc.isPdf) StatusAbsent else Color(0xFF059669),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (doc.description.isNotBlank()) {
                Text(
                    text = doc.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Document Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // If Excel/CSV: Show In-App Data Preview Button!
                if (doc.isExcel) {
                    TextButton(
                        onClick = onPreviewSpreadsheet,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF059669))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("डेटा देखें", color = Color(0xFF059669), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Open in External App (PDF/Excel)
                TextButton(
                    onClick = onOpen,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("खोलें", fontSize = 12.sp)
                }

                // Share
                IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Share, contentDescription = "शेयर करें", modifier = Modifier.size(16.dp))
                }

                // Delete
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "हटाएं", tint = StatusAbsent, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// UPLOAD DOCUMENT MODAL
// -----------------------------------------------------------------------------
@Composable
fun UploadDocumentModal(
    topic: StudyTopic,
    onDismiss: () -> Unit,
    onUpload: (uri: Uri, title: String, description: String, fileType: String) -> Unit
) {
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var fileType by remember { mutableStateOf("PDF") } // PDF or EXCEL

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            val path = uri.path ?: ""
            val ext = path.substringAfterLast('.', "").lowercase()
            selectedFileName = uri.lastPathSegment?.substringAfterLast('/') ?: "uploaded_file"

            // Auto-detect type
            if (ext.contains("xls") || ext.contains("csv") || selectedFileName.endsWith(".xlsx") || selectedFileName.endsWith(".csv")) {
                fileType = "EXCEL"
            } else {
                fileType = "PDF"
            }

            if (title.isBlank()) {
                title = selectedFileName.substringBeforeLast('.').replace("_", " ")
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("PDF / Excel फ़ाइल अपलोड करें", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "टॉपिक: ${topic.topicName}",
                    fontWeight = FontWeight.SemiBold,
                    color = SaffronPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )

                // Pick File Button / Info
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (selectedUri == null) {
                            Button(
                                onClick = {
                                    filePickerLauncher.launch("*/*")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                                modifier = Modifier.testTag("admin_choose_file_from_device")
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("डिवाइस से फ़ाइल चुनें (PDF/Excel)")
                            }
                            Text(
                                "समर्थित प्रारूप: .pdf, .xlsx, .xls, .csv",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("चयनित फ़ाइल:", style = MaterialTheme.typography.labelSmall)
                                    Text(selectedFileName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                }
                                TextButton(onClick = { filePickerLauncher.launch("*/*") }) {
                                    Text("बदलें")
                                }
                            }
                        }
                    }
                }

                // File Type Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = fileType == "PDF",
                        onClick = { fileType = "PDF" },
                        label = { Text("📕 PDF दस्तावेज़") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = fileType == "EXCEL",
                        onClick = { fileType = "EXCEL" },
                        label = { Text("📗 Excel स्प्रेडशीट") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("फ़ाइल का शीर्षक (Title)*") },
                    placeholder = { Text("जैसे: संख्या पद्धति 100 प्रश्न हल सहित") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("विवरण / नोट्स (Description)") },
                    placeholder = { Text("कैडेट्स हेतु विशेष निर्देश व अध्याय सारांश...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val uri = selectedUri
                    if (uri != null && title.isNotBlank()) {
                        onUpload(uri, title.trim(), description.trim(), fileType)
                    }
                },
                enabled = selectedUri != null && title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                modifier = Modifier.testTag("admin_confirm_upload_button")
            ) {
                Text("अपलोड करें")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("रद्द करें") }
        }
    )
}

// -----------------------------------------------------------------------------
// EXCEL / CSV IN-APP SPREADSHEET VIEWER DIALOG
// -----------------------------------------------------------------------------
@Composable
fun ExcelSpreadsheetPreviewDialog(
    doc: TopicDocument,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onOpenExternal: () -> Unit
) {
    val context = LocalContext.current
    val tableRows = remember(doc) {
        TopicFileUtils.parseSpreadsheetRows(context, doc)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("📗", fontSize = 22.sp)
                        Column {
                            Text(
                                text = doc.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "स्प्रेडशीट डेटा प्रीव्यू • ${tableRows.size} पंक्तियाँ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "बंद करें")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Spreadsheet Table Box with both Horizontal & Vertical Scroll
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    val horizontalScrollState = rememberScrollState()

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(tableRows.size) { rowIndex ->
                            val row = tableRows[rowIndex]
                            val isHeader = rowIndex == 0

                            Row(
                                modifier = Modifier
                                    .horizontalScroll(horizontalScrollState)
                                    .background(
                                        if (isHeader) Color(0xFF059669).copy(alpha = 0.15f)
                                        else if (rowIndex % 2 == 0) MaterialTheme.colorScheme.surface
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                    )
                                    .padding(vertical = 6.dp, horizontal = 4.dp)
                            ) {
                                row.forEachIndexed { colIndex, cellText ->
                                    Box(
                                        modifier = Modifier
                                            .widthIn(min = 100.dp, max = 220.dp)
                                            .padding(horizontal = 6.dp)
                                    ) {
                                        Text(
                                            text = cellText,
                                            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = if (isHeader) 12.sp else 11.sp,
                                            color = if (isHeader) Color(0xFF065F46) else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), thickness = 0.5.dp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onOpenExternal) {
                        Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Excel ऐप में खोलें", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onShare,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("शेयर करें", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
