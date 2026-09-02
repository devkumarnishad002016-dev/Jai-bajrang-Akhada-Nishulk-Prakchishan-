package com.example.ui.screens.notices

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.data.model.Notice
import com.example.data.model.RecruitmentInfo
import com.example.data.model.StudentProfile
import com.example.data.recruitment.*
import com.example.ui.theme.*

@Composable
fun NoticeBoardScreen(
    notices: List<Notice>,
    modifier: Modifier = Modifier,
    onTogglePin: ((Notice) -> Unit)? = null,
    onMarkRead: ((Notice) -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var selectedNoticeForDetail by remember { mutableStateOf<Notice?>(null) }

    val categories = listOf(
        "ALL" to "सभी सूचनाएं",
        "प्रशिक्षण" to "प्रशिक्षण (Training)",
        "परीक्षा" to "परीक्षा (Exam)",
        "भर्ती अपडेट" to "भर्ती (Recruitment)",
        "जरूरी" to "अति महत्वपूर्ण (Urgent)",
        "उपलब्धि" to "अखाड़ा गौरव"
    )

    val filteredNotices = remember(notices, searchQuery, selectedCategory) {
        notices.filter { notice ->
            val matchesCategory = when (selectedCategory) {
                "ALL" -> true
                "जरूरी" -> notice.isUrgent || notice.priority.equals("URGENT", true)
                else -> notice.category.contains(selectedCategory, ignoreCase = true)
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                notice.title.lowercase().contains(q) ||
                notice.content.lowercase().contains(q) ||
                notice.author.lowercase().contains(q) ||
                notice.category.lowercase().contains(q)
            }
            matchesCategory && matchesSearch
        }.sortedWith(
            compareByDescending<Notice> { it.isPinned }
                .thenByDescending { it.isUrgent || it.priority.equals("URGENT", true) }
                .thenByDescending { it.id }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("notice_board_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(SaffronPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Campaign,
                                    contentDescription = "Notices",
                                    tint = SaffronPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "अखाड़ा डिजिटल नोटिस बोर्ड",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "जय बजरंग अखाड़ा, मौरिकला गुफा • आधिकारिक सूचनाएं",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            color = SaffronContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${filteredNotices.size} सूचनाएं",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = OnSaffronContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("सूचना, शीर्षक या प्रशिक्षक खोजें...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = SaffronPrimary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("notice_search_field")
                    )
                }
            }
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { (key, label) ->
                    val isSelected = selectedCategory == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = key },
                        label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SaffronPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Notice Cards
        if (filteredNotices.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "इस श्रेणी में कोई सूचना उपलब्ध नहीं है।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredNotices) { notice ->
                NoticeItemCard(
                    notice = notice,
                    onClick = {
                        selectedNoticeForDetail = notice
                        onMarkRead?.invoke(notice)
                    },
                    onTogglePin = onTogglePin
                )
            }
        }
    }

    // Detail Dialog
    selectedNoticeForDetail?.let { notice ->
        NoticeDetailDialog(
            notice = notice,
            onDismiss = { selectedNoticeForDetail = null }
        )
    }
}

@Composable
fun NoticeItemCard(
    notice: Notice,
    onClick: () -> Unit,
    onTogglePin: ((Notice) -> Unit)? = null
) {
    val isUrgent = notice.isUrgent || notice.priority.equals("URGENT", true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("notice_card_${notice.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                notice.isPinned -> SaffronContainer.copy(alpha = 0.35f)
                isUrgent -> StatusAbsent.copy(alpha = 0.08f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = CardDefaults.outlinedCardBorder().let {
            when {
                notice.isPinned -> androidx.compose.foundation.BorderStroke(1.5.dp, SaffronPrimary)
                isUrgent -> androidx.compose.foundation.BorderStroke(1.5.dp, StatusAbsent)
                else -> androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            }
        },
        elevation = CardDefaults.cardElevation(defaultElevation = if (notice.isPinned) 3.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (notice.isPinned) {
                        Surface(
                            color = SaffronDark,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PushPin,
                                    contentDescription = "Pinned",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "पिन सूचना",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Surface(
                        color = when {
                            isUrgent -> StatusAbsent
                            notice.category.contains("प्रशिक्षण") -> OliveTertiary
                            notice.category.contains("परीक्षा") -> SaffronDark
                            notice.category.contains("भर्ती") -> NavySecondary
                            else -> MaterialTheme.colorScheme.outline
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isUrgent) "अति आवश्यक (Urgent)" else notice.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = notice.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = notice.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = notice.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "जारीकर्ता: ${notice.author}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "पूरा पढ़ें →",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = SaffronDark
                )
            }
        }
    }
}

@Composable
fun NoticeDetailDialog(
    notice: Notice,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (notice.isUrgent) StatusAbsent else SaffronPrimary,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = notice.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = notice.date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = notice.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text(
                        text = notice.content,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }

                item {
                    HorizontalDivider()
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "सूचना प्रेषक:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = notice.author,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (notice.expiryDate.isNotBlank()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "वैधता / अंतिम तिथि:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = notice.expiryDate,
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusAbsent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "जय बजरंग अखाड़ा सूचना:\n\n${notice.title}\n\n${notice.content}\n\nजारीकर्ता: ${notice.author} (${notice.date})")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "सूचना शेयर करें"))
                    } catch (e: Exception) {}
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("शेयर करें")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("बंद करें")
            }
        }
    )
}

// -------------------------------------------------------------
// RECRUITMENT & ELIGIBILITY INTELLIGENCE SCREENS
// -------------------------------------------------------------

@Composable
fun RecruitmentInfoScreen(
    recruitmentList: List<RecruitmentInfo>,
    student: StudentProfile? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var activeEligibilityDialogProfile by remember { mutableStateOf<RecruitmentCategoryProfile?>(null) }
    var selectedRecruitmentDetail by remember { mutableStateOf<RecruitmentInfo?>(null) }

    val categoryFilters = listOf(
        "ALL" to "सभी भर्तियां",
        "ARMY" to "Indian Army (थल सेना)",
        "POLICE" to "CG Police (राज्य पुलिस)",
        "SSC" to "SSC GD",
        "PARAMILITARY" to "CRPF / BSF / CISF / ITBP"
    )

    val filteredList = remember(recruitmentList, selectedCategoryFilter, searchQuery) {
        recruitmentList.filter { rec ->
            val matchesFilter = when (selectedCategoryFilter) {
                "ALL" -> true
                "ARMY" -> rec.recruitmentName.contains("Army", ignoreCase = true) || rec.category.contains("Army", ignoreCase = true)
                "POLICE" -> rec.recruitmentName.contains("Police", ignoreCase = true) || rec.category.contains("Police", ignoreCase = true)
                "SSC" -> rec.recruitmentName.contains("SSC", ignoreCase = true) || rec.category.contains("SSC", ignoreCase = true)
                "PARAMILITARY" -> rec.recruitmentName.contains("CRPF", true) || rec.recruitmentName.contains("BSF", true) ||
                                  rec.recruitmentName.contains("CISF", true) || rec.recruitmentName.contains("ITBP", true)
                else -> true
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                rec.recruitmentName.lowercase().contains(q) ||
                rec.organization.lowercase().contains(q) ||
                rec.eligibility.lowercase().contains(q) ||
                rec.physicalTest.lowercase().contains(q)
            }
            matchesFilter && matchesSearch
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("recruitment_info_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(OliveTertiary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MilitaryTech,
                                    contentDescription = "Recruitment",
                                    tint = OliveTertiary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "सेना एवं पुलिस भर्ती मार्गदर्शन",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "पात्रता विश्लेषण, शारीरिक मापदंड व आधिकारिक नोटिफिकेशन",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mandatory Static Disclaimer
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Shield,
                                contentDescription = null,
                                tint = OliveTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "महत्वपूर्ण सूचना: अंतिम पात्रता एवं भर्ती तिथियां संबंधित भर्ती बोर्ड (Army, CG Police, SSC) की नवीनतम आधिकारिक विज्ञप्ति पर निर्भर करती हैं।",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("भर्ती का नाम, बल या योग्यता खोजें...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = OliveTertiary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Student Quick Eligibility Bar (if active student present)
        student?.let { activeStd ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SaffronContainer.copy(alpha = 0.45f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SaffronPrimary.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "👤 एक्टिव कैडेट: ${activeStd.fullName} (आयु: ${activeStd.age} वर्ष)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = SaffronDark
                            )
                            Text(
                                text = "ऊंचाई: ${activeStd.heightCm} cm • वजन: ${activeStd.weightKg} kg • लक्ष्य: ${activeStd.recruitmentGoal}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Button(
                            onClick = {
                                val profile = RecruitmentStandardRepository.findProfileByIdOrGoal(activeStd.recruitmentGoal)
                                activeEligibilityDialogProfile = profile
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("मेरी पात्रता", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categoryFilters) { (key, label) ->
                    val isSelected = selectedCategoryFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryFilter = key },
                        label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OliveTertiary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Recruitment Items List
        items(filteredList) { rec ->
            val standardProfile = remember(rec) {
                RecruitmentStandardRepository.findProfileByIdOrGoal(rec.recruitmentName)
            }

            RecruitmentCard(
                rec = rec,
                student = student,
                onViewDetails = { selectedRecruitmentDetail = rec },
                onCheckEligibility = { activeEligibilityDialogProfile = standardProfile },
                onOpenPortal = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(rec.officialUrl))
                        context.startActivity(intent)
                    } catch (e: Exception) {}
                }
            )
        }
    }

    // Eligibility Evaluation Modal / Dialog
    activeEligibilityDialogProfile?.let { profile ->
        EligibilityIntelligenceModal(
            student = student,
            profile = profile,
            onDismiss = { activeEligibilityDialogProfile = null }
        )
    }

    // Full Recruitment Info Detail Dialog
    selectedRecruitmentDetail?.let { rec ->
        val standardProfile = remember(rec) {
            RecruitmentStandardRepository.findProfileByIdOrGoal(rec.recruitmentName)
        }

        RecruitmentDetailedDialog(
            rec = rec,
            student = student,
            profile = standardProfile,
            onCheckEligibility = {
                selectedRecruitmentDetail = null
                activeEligibilityDialogProfile = standardProfile
            },
            onDismiss = { selectedRecruitmentDetail = null }
        )
    }
}

@Composable
fun RecruitmentCard(
    rec: RecruitmentInfo,
    student: StudentProfile?,
    onViewDetails: () -> Unit,
    onCheckEligibility: () -> Unit,
    onOpenPortal: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() }
            .testTag("recruitment_card_${rec.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = SaffronContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = rec.postName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSaffronContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    color = OliveContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = rec.totalPosts,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OnOliveContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = rec.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (rec.shortDescription.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = rec.shortDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = OliveTertiary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "योग्यता: ${rec.eligibility}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = SaffronDark,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "आयु: ${rec.ageLimit} • ${rec.lastDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onCheckEligibility,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SaffronDark),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("पात्रता जांचें", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onOpenPortal,
                    colors = ButtonDefaults.buttonColors(containerColor = OliveTertiary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ऑफिशियल पोर्टल", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EligibilityIntelligenceModal(
    student: StudentProfile?,
    profile: RecruitmentCategoryProfile,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("पात्रता रिपोर्ट", "शारीरिक मानक", "चयन प्रक्रिया", "दस्तावेज व टिप्स")

    val report = remember(student, profile) {
        EligibilityIntelligenceEngine.evaluateEligibility(student, profile)
    }

    val physicalComparisons = remember(student, profile) {
        EligibilityIntelligenceEngine.comparePhysicalStandards(student, profile)
    }

    val recommendations = remember(student, profile) {
        EligibilityIntelligenceEngine.generatePersonalizedRecruitmentRecommendations(
            student = student,
            targetProfile = profile
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = profile.iconEmoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = profile.nameEnglish,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = profile.organization,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontSize = 12.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth().heightIn(min = 280.dp, max = 460.dp)) {
                when (selectedTab) {
                    0 -> EligibilityReportTab(report = report, student = student, profile = profile)
                    1 -> PhysicalStandardsTab(items = physicalComparisons)
                    2 -> SelectionProcessTab(profile = profile)
                    3 -> DocumentsAndTipsTab(profile = profile, recommendations = recommendations)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(profile.officialWebsiteUrl))
                        context.startActivity(intent)
                    } catch (e: Exception) {}
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
            ) {
                Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("आधिकारिक विज्ञप्ति पोर्टल")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("बंद करें")
            }
        }
    )
}

@Composable
fun EligibilityReportTab(
    report: StudentEligibilityReport,
    student: StudentProfile?,
    profile: RecruitmentCategoryProfile
) {
    val statusColor = when (report.overallStatus) {
        EligibilityStatus.LIKELY_ELIGIBLE -> StatusPresent
        EligibilityStatus.MAY_BE_ELIGIBLE -> SaffronDark
        EligibilityStatus.NEEDS_IMPROVEMENT -> StatusAbsent
        EligibilityStatus.INSUFFICIENT_DATA -> MaterialTheme.colorScheme.outline
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Overall Verdict Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.12f)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, statusColor)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (report.overallStatus) {
                                    EligibilityStatus.LIKELY_ELIGIBLE -> Icons.Default.CheckCircle
                                    EligibilityStatus.MAY_BE_ELIGIBLE -> Icons.Default.HelpOutline
                                    EligibilityStatus.NEEDS_IMPROVEMENT -> Icons.Default.WarningAmber
                                    EligibilityStatus.INSUFFICIENT_DATA -> Icons.Default.Info
                                },
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = report.overallStatus.labelHindi,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }

                        Surface(
                            color = statusColor,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${report.overallScorePercentage}% मैच",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = report.actionAdviceHindi,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Criteria Breakdown
        items(report.criteria) { criterion ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = criterion.criterionName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )

                        val itemColor = when (criterion.isMet) {
                            true -> StatusPresent
                            false -> StatusAbsent
                            null -> SaffronDark
                        }

                        Surface(
                            color = itemColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = criterion.statusTextHindi,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = itemColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "वर्तमान मान: ${criterion.studentValue}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "आवश्यक: ${criterion.requiredValue}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = criterion.guidanceHindi,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Mandatory Disclaimer Notice
        item {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = report.disclaimerText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PhysicalStandardsTab(
    items: List<PhysicalStandardComparisonItem>
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(items) { item ->
            val statusColor = when (item.status) {
                StandardStatus.ACHIEVED -> StatusPresent
                StandardStatus.NEEDS_IMPROVEMENT -> SaffronDark
                StandardStatus.NO_DATA -> MaterialTheme.colorScheme.outline
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = item.iconEmoji, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.metricName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            color = statusColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = item.status.labelHindi,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "कैडेट वर्तमान: ${item.studentCurrentValue}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "भर्ती मानक: ${item.targetRequirement}",
                            style = MaterialTheme.typography.bodySmall,
                            color = OliveTertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { item.progressRatio.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = statusColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "🎯 प्रशिक्षक सलाह: ${item.coachingAdviceHindi}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SelectionProcessTab(
    profile: RecruitmentCategoryProfile
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "📋 चरणबद्ध चयन प्रक्रिया (Selection Process)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = OliveTertiary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = profile.selectionProcessHindi,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "📝 लिखित परीक्षा प्रारूप (Exam Pattern)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SaffronDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = profile.writtenExamPatternHindi,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🏃‍♂️ शारीरिक दक्षता विवरण (PET Standards)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = OliveTertiary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = profile.physicalEfficiencyOverviewHindi,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DocumentsAndTipsTab(
    profile: RecruitmentCategoryProfile,
    recommendations: List<String>
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Personalized Recommendations
        if (recommendations.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SaffronContainer.copy(alpha = 0.4f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SaffronPrimary)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "💡 आपके लिए व्यक्तिगत तैयारी कार्ययोजना",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SaffronDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        recommendations.forEach { rec ->
                            Row(modifier = Modifier.padding(vertical = 3.dp)) {
                                Text(text = "• ", fontWeight = FontWeight.Bold, color = SaffronDark)
                                Text(text = rec, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        // Required Documents
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "📂 आवश्यक मूल दस्तावेज सूची (Document Checklist)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = NavySecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    profile.requiredDocumentsHindi.forEachIndexed { idx, doc ->
                        Row(modifier = Modifier.padding(vertical = 3.dp)) {
                            Text(text = "${idx + 1}. ", fontWeight = FontWeight.Bold, color = OliveTertiary)
                            Text(text = doc, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        // Medical Requirements
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🩺 मेडिकल मापदंड (Medical Standards)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = StatusAbsent
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    profile.medicalRequirementsHindi.forEach { med ->
                        Row(modifier = Modifier.padding(vertical = 3.dp)) {
                            Text(text = "✓ ", fontWeight = FontWeight.Bold, color = StatusAbsent)
                            Text(text = med, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecruitmentDetailedDialog(
    rec: RecruitmentInfo,
    student: StudentProfile?,
    profile: RecruitmentCategoryProfile,
    onCheckEligibility: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Surface(
                    color = SaffronContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = rec.badgeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OnSaffronContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(rec.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(rec.organization, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { Text("📌 पद का नाम: ${rec.postName}", fontWeight = FontWeight.Bold) }
                item { Text("👥 कुल पद / संस्था: ${rec.totalPosts}", color = OliveTertiary, fontWeight = FontWeight.Bold) }
                item { Text("🎓 शैक्षणिक योग्यता: ${rec.eligibility}") }
                item { Text("⏳ आयु सीमा: ${rec.ageLimit}") }
                item { Text("📅 महत्वपूर्ण तिथियां: ${rec.lastDate}", color = StatusAbsent, fontWeight = FontWeight.Bold) }
                item { Text("📐 शारीरिक मापदंड (PST): ${rec.physicalStandards}") }
                item { Text("🏃‍♂️ शारीरिक दक्षता (PET): ${rec.physicalTest}") }
                item { Text("📖 लिखित परीक्षा एवं सिलेबस: ${rec.syllabus}") }
                item { Text("💰 वेतनमान: ${rec.salary}") }
                if (rec.importantDocuments.isNotBlank()) {
                    item { Text("📂 जरूरी दस्तावेज:\n${rec.importantDocuments}", style = MaterialTheme.typography.bodySmall) }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onCheckEligibility,
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
            ) {
                Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("पात्रता चेक करें")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("बंद करें")
            }
        }
    )
}
