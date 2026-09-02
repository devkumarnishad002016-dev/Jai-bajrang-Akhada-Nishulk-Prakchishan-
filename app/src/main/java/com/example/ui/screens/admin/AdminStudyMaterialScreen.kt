package com.example.ui.screens.admin

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Chapter
import com.example.data.model.ChapterValidator
import com.example.ui.components.MetricStatCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStudyMaterialScreen(
    allChapters: List<Chapter>,
    onAddChapter: (Chapter) -> Unit,
    onUpdateChapter: (Chapter) -> Unit,
    onDeleteChapter: (Chapter) -> Unit,
    onTogglePublish: (Chapter) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubjectFilter by remember { mutableStateOf("ALL") }
    var publishStatusFilter by remember { mutableStateOf("ALL") } // ALL, PUBLISHED, UNPUBLISHED

    var showAddDialog by remember { mutableStateOf(false) }
    var chapterToEdit by remember { mutableStateOf<Chapter?>(null) }
    var chapterToDelete by remember { mutableStateOf<Chapter?>(null) }
    var chapterDetailView by remember { mutableStateOf<Chapter?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val subjectsList = remember {
        listOf("ALL", "Mathematics", "Reasoning", "Hindi", "English", "GK / GS")
    }

    // Filtered chapters
    val filteredChapters = remember(allChapters, searchQuery, selectedSubjectFilter, publishStatusFilter) {
        allChapters.filter { chapter ->
            val matchSubject = selectedSubjectFilter == "ALL" || chapter.subjectName.equals(selectedSubjectFilter, ignoreCase = true)
            val matchSearch = searchQuery.isBlank() ||
                    chapter.chapterName.contains(searchQuery, ignoreCase = true) ||
                    chapter.description.contains(searchQuery, ignoreCase = true) ||
                    chapter.chapterNumber.toString() == searchQuery.trim() ||
                    chapter.subjectName.contains(searchQuery, ignoreCase = true)
            val matchPublish = when (publishStatusFilter) {
                "PUBLISHED" -> chapter.isPublished
                "UNPUBLISHED" -> !chapter.isPublished
                else -> true
            }
            matchSubject && matchSearch && matchPublish
        }.sortedWith(compareBy({ it.displayOrder }, { it.chapterNumber }, { it.id }))
    }

    val totalCount = allChapters.size
    val publishedCount = allChapters.count { it.isPublished }
    val draftCount = totalCount - publishedCount

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("admin_study_material_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "अध्याय-वार स्टडी मटेरियल प्रबंधन",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "63 सिलेबस चैप्टर्स • PDF नोट्स व वीडियो प्रबंधन",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("admin_study_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "वापस जाएं"
                        )
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = SaffronPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 8.dp).testTag("add_new_chapter_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("नया चैप्टर", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        snackbarHost = {
            snackbarMessage?.let { msg ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { snackbarMessage = null }) {
                            Text("OK", color = Color.White)
                        }
                    },
                    containerColor = Color(0xFF1E293B)
                ) {
                    Text(msg, color = Color.White)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Stats Overview
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        title = "कुल चैप्टर्स",
                        value = "$totalCount",
                        subtitle = "63 सिलेबस चैप्टर्स",
                        icon = Icons.Default.MenuBook,
                        iconColor = SaffronPrimary,
                        modifier = Modifier.weight(1f).testTag("stat_total_chapters")
                    )
                    MetricStatCard(
                        title = "प्रकाशित (Live)",
                        value = "$publishedCount",
                        subtitle = "छात्रों को दृश्यमान",
                        icon = Icons.Default.CheckCircle,
                        iconColor = OliveTertiary,
                        modifier = Modifier.weight(1f).testTag("stat_published_chapters")
                    )
                    MetricStatCard(
                        title = "ड्राफ्ट / अप्रकाशित",
                        value = "$draftCount",
                        subtitle = "संशोधनाधीन",
                        icon = Icons.Default.Drafts,
                        iconColor = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f).testTag("stat_draft_chapters")
                    )
                }
            }

            // Search and Publish Filter
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("चैप्टर का नाम, नंबर या विवरण खोजें...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_chapter_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Publish Filter Radios / Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "स्थिति:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            listOf("ALL" to "सभी", "PUBLISHED" to "प्रकाशित", "UNPUBLISHED" to "ड्राफ्ट").forEach { (key, label) ->
                                FilterChip(
                                    selected = publishStatusFilter == key,
                                    onClick = { publishStatusFilter = key },
                                    label = { Text(label, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SaffronContainer,
                                        selectedLabelColor = OnSaffronContainer
                                    ),
                                    modifier = Modifier.testTag("filter_publish_$key")
                                )
                            }
                        }
                    }
                }
            }

            // Subject Filter Tabs
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(subjectsList) { subject ->
                        val isSelected = selectedSubjectFilter == subject
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSubjectFilter = subject },
                            label = {
                                val labelText = when (subject) {
                                    "ALL" -> "सभी विषय (${allChapters.size})"
                                    "Mathematics" -> "गणित (${allChapters.count { it.subjectName.equals("Mathematics", ignoreCase = true) }})"
                                    "Reasoning" -> "रीजनिंग (${allChapters.count { it.subjectName.equals("Reasoning", ignoreCase = true) }})"
                                    "Hindi" -> "हिंदी (${allChapters.count { it.subjectName.equals("Hindi", ignoreCase = true) }})"
                                    "English" -> "English (${allChapters.count { it.subjectName.equals("English", ignoreCase = true) }})"
                                    "GK / GS" -> "GK/GS (${allChapters.count { it.subjectName.equals("GK / GS", ignoreCase = true) }})"
                                    else -> subject
                                }
                                Text(
                                    text = labelText,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SaffronContainer,
                                selectedLabelColor = OnSaffronContainer
                            ),
                            modifier = Modifier.testTag("filter_subject_$subject")
                        )
                    }
                }
            }

            // Chapter Count Summary
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "सूचीबद्ध अध्याय (${filteredChapters.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "अध्याय संख्या व क्रम अनुसार व्यवस्थित",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Chapter List Items
            if (filteredChapters.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "कोई अध्याय नहीं मिला",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "कृपया सर्च क्वेरी या फ़िल्टर बदलें",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredChapters, key = { it.id }) { chapter ->
                    AdminChapterCard(
                        chapter = chapter,
                        onEdit = { chapterToEdit = chapter },
                        onDelete = { chapterToDelete = chapter },
                        onTogglePublish = { onTogglePublish(chapter) },
                        onViewDetail = { chapterDetailView = chapter },
                        onMoveUp = {
                            val newOrder = (chapter.displayOrder - 1).coerceAtLeast(1)
                            onUpdateChapter(chapter.copy(displayOrder = newOrder, updatedAt = "2026-08-30"))
                        },
                        onMoveDown = {
                            val newOrder = chapter.displayOrder + 1
                            onUpdateChapter(chapter.copy(displayOrder = newOrder, updatedAt = "2026-08-30"))
                        },
                        onOpenPdf = {
                            if (chapter.fileUrl.isNotBlank()) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(chapter.fileUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    snackbarMessage = "PDF लिंक खोलने में त्रुटि: ${e.message}"
                                }
                            } else {
                                snackbarMessage = "इस अध्याय के लिए अभी PDF लिंक अपलोड नहीं है।"
                            }
                        }
                    )
                }
            }
        }
    }

    // Add Chapter Dialog
    if (showAddDialog) {
        ChapterFormDialog(
            title = "नया सिलेबस अध्याय जोड़ें",
            chapter = null,
            allChapters = allChapters,
            onDismiss = { showAddDialog = false },
            onSave = { newChapter ->
                onAddChapter(newChapter)
                showAddDialog = false
                snackbarMessage = "अध्याय '${newChapter.chapterName}' सफलतापूर्वक जोड़ा गया!"
            }
        )
    }

    // Edit Chapter Dialog
    chapterToEdit?.let { editTarget ->
        ChapterFormDialog(
            title = "अध्याय संपादित करें (Edit Chapter)",
            chapter = editTarget,
            allChapters = allChapters,
            onDismiss = { chapterToEdit = null },
            onSave = { updatedChapter ->
                onUpdateChapter(updatedChapter)
                chapterToEdit = null
                snackbarMessage = "अध्याय '${updatedChapter.chapterName}' अद्यतन (Update) कर दिया गया!"
            }
        )
    }

    // Delete Confirmation Dialog
    chapterToDelete?.let { deleteTarget ->
        AlertDialog(
            onDismissRequest = { chapterToDelete = null },
            title = { Text("अध्याय हटाएं (Delete Chapter)", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("क्या आप वाकई इस अध्याय को हटाना चाहते हैं?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• विषय: ${deleteTarget.subjectName}\n• अध्याय ${deleteTarget.chapterNumber}: ${deleteTarget.chapterName}",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteChapter(deleteTarget)
                        chapterToDelete = null
                        snackbarMessage = "अध्याय '${deleteTarget.chapterName}' हटा दिया गया!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    modifier = Modifier.testTag("confirm_delete_chapter_button")
                ) {
                    Text("हटाएं (Delete)", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { chapterToDelete = null }) {
                    Text("रद्द करें")
                }
            }
        )
    }

    // Chapter Detail View Sheet / Dialog
    chapterDetailView?.let { detailChapter ->
        AlertDialog(
            onDismissRequest = { chapterDetailView = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = SaffronPrimary.copy(alpha = 0.15f),
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${detailChapter.chapterNumber}",
                                fontWeight = FontWeight.Bold,
                                color = SaffronPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = detailChapter.chapterName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${detailChapter.subjectName} • ${if (detailChapter.isPublished) "प्रकाशित (Live)" else "ड्राफ्ट"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (detailChapter.isPublished) OliveTertiary else Color(0xFFF59E0B)
                        )
                    }
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "विवरण / मुख्य विषय सूची:",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = detailChapter.description.ifBlank { "कोई विवरण नहीं" },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (detailChapter.notesContent.isNotBlank()) {
                        item {
                            Text(
                                text = "विस्तृत अध्ययन नोट्स:",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = detailChapter.notesContent,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }

                    item {
                        Divider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("PDF स्टडी नोट्स:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(
                                text = if (detailChapter.fileUrl.isNotBlank()) detailChapter.fileName.ifBlank { "PDF संलग्न है" } else "कोई PDF नहीं",
                                fontSize = 12.sp,
                                color = if (detailChapter.fileUrl.isNotBlank()) OliveTertiary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (detailChapter.videoTitle.isNotBlank() || detailChapter.videoUrl.isNotBlank()) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("वीडियो क्लास:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(
                                    text = detailChapter.videoTitle.ifBlank { "वीडियो लिंक उपलब्ध" },
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("अभ्यास प्रश्न:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("${detailChapter.practiceQuestionsCount} प्रश्न", fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { chapterDetailView = null },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text("बंद करें")
                }
            }
        )
    }
}

@Composable
fun AdminChapterCard(
    chapter: Chapter,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTogglePublish: () -> Unit,
    onViewDetail: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onOpenPdf: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onViewDetail() }
            .testTag("admin_chapter_card_${chapter.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Subject, Chapter No, Publish Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = when (chapter.subjectName) {
                            "Mathematics" -> Color(0xFF3B82F6).copy(alpha = 0.15f)
                            "Reasoning" -> Color(0xFF8B5CF6).copy(alpha = 0.15f)
                            "Hindi" -> Color(0xFFEF4444).copy(alpha = 0.15f)
                            "English" -> Color(0xFF10B981).copy(alpha = 0.15f)
                            else -> SaffronPrimary.copy(alpha = 0.15f)
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${chapter.subjectName} • Ch ${chapter.chapterNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (chapter.subjectName) {
                                "Mathematics" -> Color(0xFF1D4ED8)
                                "Reasoning" -> Color(0xFF6D28D9)
                                "Hindi" -> Color(0xFFB91C1C)
                                "English" -> Color(0xFF047857)
                                else -> SaffronDark
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        color = if (chapter.isPublished) OliveTertiary.copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (chapter.isPublished) "✓ प्रकाशित" else "ड्राफ्ट (Draft)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (chapter.isPublished) OliveTertiary else Color(0xFFD97706),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Quick Switch for publish
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = chapter.isPublished,
                        onCheckedChange = { onTogglePublish() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = OliveTertiary
                        ),
                        modifier = Modifier.height(28.dp).testTag("toggle_publish_${chapter.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chapter Title
            Text(
                text = "${chapter.chapterNumber}. ${chapter.chapterName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (chapter.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = chapter.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Feature Pills: PDF, Video, Questions, Order
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (chapter.fileUrl.isNotBlank()) {
                    AssistChip(
                        onClick = onOpenPdf,
                        label = { Text("PDF नोट्स", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.PictureAsPdf,
                                contentDescription = "PDF",
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFFDC2626)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFFFEE2E2),
                            labelColor = Color(0xFF991B1B)
                        ),
                        modifier = Modifier.height(28.dp)
                    )
                }

                if (chapter.videoUrl.isNotBlank()) {
                    AssistChip(
                        onClick = onViewDetail,
                        label = { Text("वीडियो", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.PlayCircle,
                                contentDescription = "Video",
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFF2563EB)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFFDBEAFE),
                            labelColor = Color(0xFF1E40AF)
                        ),
                        modifier = Modifier.height(28.dp)
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${chapter.practiceQuestionsCount} प्रश्न",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Reorder buttons (Move up / down)
                IconButton(
                    onClick = onMoveUp,
                    modifier = Modifier.size(28.dp).testTag("move_up_${chapter.id}")
                ) {
                    Icon(
                        Icons.Default.ArrowDropUp,
                        contentDescription = "ऊपर ले जाएं",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onMoveDown,
                    modifier = Modifier.size(28.dp).testTag("move_down_${chapter.id}")
                ) {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "नीचे ले जाएं",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Action Buttons (Edit, Delete, Details)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onViewDetail,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("नोट्स देखें", fontSize = 12.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = onEdit,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("edit_chapter_${chapter.id}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("संपादित करें", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("delete_chapter_${chapter.id}")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("हटाएं", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterFormDialog(
    title: String,
    chapter: Chapter?,
    allChapters: List<Chapter>,
    onDismiss: () -> Unit,
    onSave: (Chapter) -> Unit
) {
    val subjects = remember { listOf("Mathematics", "Reasoning", "Hindi", "English", "GK / GS") }

    var subjectName by remember { mutableStateOf(chapter?.subjectName ?: "Mathematics") }
    var chapterNumberText by remember { mutableStateOf((chapter?.chapterNumber ?: 1).toString()) }
    var chapterName by remember { mutableStateOf(chapter?.chapterName ?: "") }
    var description by remember { mutableStateOf(chapter?.description ?: "") }
    var notesContent by remember { mutableStateOf(chapter?.notesContent ?: "") }
    var fileUrl by remember { mutableStateOf(chapter?.fileUrl ?: "") }
    var fileName by remember { mutableStateOf(chapter?.fileName ?: "") }
    var videoTitle by remember { mutableStateOf(chapter?.videoTitle ?: "") }
    var videoUrl by remember { mutableStateOf(chapter?.videoUrl ?: "") }
    var practiceCountText by remember { mutableStateOf((chapter?.practiceQuestionsCount ?: 20).toString()) }
    var isPublished by remember { mutableStateOf(chapter?.isPublished ?: true) }
    var examType by remember { mutableStateOf(chapter?.examType ?: "ALL") }
    var language by remember { mutableStateOf(chapter?.language ?: "Hindi") }

    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Validation Error Banner
                validationError?.let { err ->
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(err, color = Color(0xFF991B1B), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // 1. Subject Selector Chips
                item {
                    Text("विषय चुनें (Subject)*", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(subjects) { sub ->
                            FilterChip(
                                selected = subjectName == sub,
                                onClick = { subjectName = sub },
                                label = { Text(sub, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SaffronContainer,
                                    selectedLabelColor = OnSaffronContainer
                                )
                            )
                        }
                    }
                }

                // 2. Chapter Number & Chapter Name
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = chapterNumberText,
                            onValueChange = { chapterNumberText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("अध्याय सं.*") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.width(100.dp).testTag("input_chapter_number")
                        )

                        OutlinedTextField(
                            value = chapterName,
                            onValueChange = { chapterName = it },
                            label = { Text("अध्याय का नाम (Chapter Name)*") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("input_chapter_name")
                        )
                    }
                }

                // 3. Description
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("संक्षिप्त विवरण / विषय सूची") },
                        maxLines = 2,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_chapter_description")
                    )
                }

                // 4. Notes Content (Key concepts / tricks)
                item {
                    OutlinedTextField(
                        value = notesContent,
                        onValueChange = { notesContent = it },
                        label = { Text("मुख्य नोट्स व सूत्र (Key Formulae / Concepts)") },
                        maxLines = 4,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_chapter_notes")
                    )
                }

                // 5. PDF Study Notes URL & PDF File Name
                item {
                    Text("PDF स्टडी नोट्स संलग्नक (Study Material PDF)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    OutlinedTextField(
                        value = fileUrl,
                        onValueChange = {
                            fileUrl = it
                            if (fileName.isBlank() && it.isNotBlank()) {
                                fileName = it.substringAfterLast("/").substringBefore("?")
                            }
                        },
                        label = { Text("PDF URL / स्टोरेज लिंक") },
                        placeholder = { Text("https://example.com/notes/math_ch1.pdf") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFDC2626)) },
                        modifier = Modifier.fillMaxWidth().testTag("input_chapter_pdf_url")
                    )
                }

                item {
                    OutlinedTextField(
                        value = fileName,
                        onValueChange = { fileName = it },
                        label = { Text("PDF फ़ाइल का नाम (उदा. Maths_Ch1.pdf)") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_chapter_pdf_name")
                    )
                }

                // 6. Video Lecture Title & URL
                item {
                    Text("वीडियो लेक्चर लिंक (Video Class)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    OutlinedTextField(
                        value = videoTitle,
                        onValueChange = { videoTitle = it },
                        label = { Text("वीडियो का शीर्षक (Video Title)") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_chapter_video_title")
                    )
                }

                item {
                    OutlinedTextField(
                        value = videoUrl,
                        onValueChange = { videoUrl = it },
                        label = { Text("YouTube / वीडियो URL") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        leadingIcon = { Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color(0xFF2563EB)) },
                        modifier = Modifier.fillMaxWidth().testTag("input_chapter_video_url")
                    )
                }

                // 7. Practice Questions Count
                item {
                    OutlinedTextField(
                        value = practiceCountText,
                        onValueChange = { practiceCountText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("अभ्यास प्रश्नों की संख्या") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_practice_count")
                    )
                }

                // 8. Exam Target & Language
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = examType,
                            onValueChange = { examType = it },
                            label = { Text("लक्षित परीक्षा") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = language,
                            onValueChange = { language = it },
                            label = { Text("भाषा (Language)") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 9. Publish Status Switch
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isPublished) "तुरंत प्रकाशित करें (Publish Now)" else "ड्राफ्ट में रखें (Save as Draft)",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = if (isPublished) "छात्रों को तुरंत दिखेगा" else "केवल एडमिन को दिखेगा",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isPublished,
                                onCheckedChange = { isPublished = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = OliveTertiary
                                ),
                                modifier = Modifier.testTag("dialog_publish_switch")
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val chNum = chapterNumberText.toIntOrNull() ?: 1
                    val practiceCount = practiceCountText.toIntOrNull() ?: 20

                    val targetChapter = (chapter ?: Chapter(
                        subjectName = subjectName,
                        chapterName = chapterName.trim(),
                        chapterNumber = chNum,
                        description = description.trim(),
                        displayOrder = chNum
                    )).copy(
                        subjectName = subjectName,
                        chapterNumber = chNum,
                        chapterName = chapterName.trim(),
                        description = description.trim(),
                        notesContent = notesContent.trim(),
                        fileUrl = fileUrl.trim(),
                        fileName = fileName.trim(),
                        videoTitle = videoTitle.trim(),
                        videoUrl = videoUrl.trim(),
                        practiceQuestionsCount = practiceCount,
                        examType = examType.trim(),
                        language = language.trim(),
                        isPublished = isPublished,
                        updatedAt = "2026-08-30"
                    )

                    // Validate
                    val validation = ChapterValidator.validate(targetChapter)
                    if (!validation.isValid) {
                        validationError = validation.errorMessage
                        return@Button
                    }

                    // Duplicate check (ignore self when editing)
                    val isDuplicate = allChapters.any { existing ->
                        existing.id != targetChapter.id &&
                                existing.subjectName.equals(targetChapter.subjectName, ignoreCase = true) &&
                                (existing.chapterNumber == targetChapter.chapterNumber ||
                                        existing.chapterName.equals(targetChapter.chapterName, ignoreCase = true))
                    }

                    if (isDuplicate) {
                        validationError = "त्रुटि: ${targetChapter.subjectName} में अध्याय ${targetChapter.chapterNumber} या नाम '${targetChapter.chapterName}' पहले से मौजूद है!"
                        return@Button
                    }

                    onSave(targetChapter)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                modifier = Modifier.testTag("save_chapter_form_button")
            ) {
                Text("सुरक्षित करें (Save)")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("रद्द करें")
            }
        }
    )
}
