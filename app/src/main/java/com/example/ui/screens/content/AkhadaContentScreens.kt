package com.example.ui.screens.content

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.theme.*

// ==========================================
// 1. ABOUT AKHADA (हमारे बारे में)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAkhadaScreen(
    onNavigateBack: () -> Unit,
    onJoinCampaign: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("हमारे बारे में (About Akhada)", fontWeight = FontWeight.Bold) },
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
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SaffronContainer.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, SaffronPrimary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🚩 जय बजरंग अखाड़ा", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = SaffronDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("मौरीकला गुफा • गांव से सेना/पुलिस भर्ती अभियान", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = SaffronPrimary,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                "100% निःशुल्क शारीरिक एवं लिखित परीक्षा प्रशिक्षण",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Overview
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("अभियान का परिचय", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SaffronDark)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "जय बजरंग अखाड़ा (मौरिकला गुफा) ग्रामीण क्षेत्र के युवाओं को भारतीय सेना (Indian Army - Agniveer), राज्य पुलिस (Police Constable), केंद्रीय सशस्त्र पुलिस बलों (CAPF - SSC GD, CRPF, BSF, CISF, ITBP, SSB) तथा अन्य सरकारी सुरक्षा बलों में चयन हेतु समर्पित एक निःशुल्क सेवा अभियान है।",
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // Core Pillars
            item {
                Text("प्रशिक्षण के चार मूल स्तंभ", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PillarCard("शारीरिक दक्षता", "1600m दौड़, पुश-अप्स, चिन-अप्स, एंड्योरेंस व ग्राउंड ड्रिल", Icons.Default.DirectionsRun, SaffronPrimary, Modifier.weight(1f))
                    PillarCard("लिखित तैयारी", "गणित, रीजनिंग, GK, GS एवं नियमित ओएमआर/CBT टेस्ट", Icons.Default.MenuBook, NavySecondary, Modifier.weight(1f))
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PillarCard("सख्त अनुशासन", "प्रातः 5:00 बजे ग्राउंड, समयबद्धता और चरित्र निर्माण", Icons.Default.Shield, OliveTertiary, Modifier.weight(1f))
                    PillarCard("राष्ट्रसेवा भावना", "मातृभूमि की रक्षा, त्याग और देश के प्रति निष्ठा", Icons.Default.MilitaryTech, Color(0xFFC2410C), Modifier.weight(1f))
                }
            }

            // CTA Button
            item {
                Button(
                    onClick = onJoinCampaign,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Icon(Icons.Default.HowToReg, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("निःशुल्क अभियान से जुड़ें (Join Campaign)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun PillarCard(title: String, desc: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
        }
    }
}

// ==========================================
// 2. MISSION & VISION (हमारा मिशन)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("हमारा मिशन एवं विज़न", fontWeight = FontWeight.Bold) },
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
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Vision Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = NavyContainer.copy(alpha = 0.8f)
                    ),
                    border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(SaffronPrimary, NavySecondary)))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Visibility, contentDescription = null, tint = SaffronPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("हमारा विज़न (Our Vision)", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = OnNavyContainer)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "“गांव का युवा — फिट, अनुशासित, शिक्षित और राष्ट्रसेवा के लिए तैयार।”",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaffronDark,
                            lineHeight = 24.sp
                        )
                    }
                }
            }

            item {
                Text("मिशन के प्रमुख संकल्प (Core Objectives)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            val missionPoints = listOf(
                Pair("गांव के युवाओं को सेना व पुलिस भर्ती के लिए तैयार करना", Icons.Default.MilitaryTech),
                Pair("आर्थिक रूप से कमजोर युवाओं को निःशुल्क मार्गदर्शन व साधन उपलब्ध कराना", Icons.Default.VolunteerActivism),
                Pair("युवाओं को नशे, आलस्य और गलत आदतों से दूर रखना", Icons.Default.HealthAndSafety),
                Pair("मोबाइल और सोशल मीडिया की अनावश्यक लत को कम कर ग्राउंड से जोड़ना", Icons.Default.PhonelinkOff),
                Pair("शारीरिक फिटनेस (Physical Fitness) और सख्त अनुशासन का विकास", Icons.Default.FitnessCenter),
                Pair("लिखित परीक्षा (CBT/Written Exam) की उत्कृष्ट एवं व्यवस्थित तैयारी", Icons.Default.MenuBook),
                Pair("सरकारी भर्ती व रिक्तियों की सही, सटीक एवं समयबद्ध जानकारी पहुंचाना", Icons.Default.Campaign)
            )

            items(missionPoints) { (point, icon) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SaffronPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(point, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. TRAINING CENTRE (मौरीकला गुफा प्रशिक्षण केंद्र)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingCentreScreen(
    onNavigateBack: () -> Unit,
    onJoin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("प्रशिक्षण केंद्र (Training Centre)", fontWeight = FontWeight.Bold) },
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
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, SaffronPrimary.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("मुख्य प्रशिक्षण केंद्र", fontSize = 12.sp, color = SaffronPrimary, fontWeight = FontWeight.Bold)
                                Text("जय बजरंग अखाड़ा – मौरीकला गुफा", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            color = SaffronContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "“मौरीकला एवं आसपास के सभी गांवों के युवा इस केंद्र से जुड़ सकते हैं।”",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaffronDark,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "मौरिकला गुफा ग्राउंड में नियमित रूप से प्रातःकालीन दौड़, बीम (पुल-अप्स), लॉन्ग जंप, शारीरिक एंड्योरेंस और लिखित परीक्षा की कक्षाएं आयोजित की जाती हैं। सभी ग्रामीण युवाओं के लिए प्रवेश और मार्गदर्शन पूर्णतः निःशुल्क है।",
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("ग्राउंड समय सारणी (Centre Timings)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("प्रातःकालीन सत्र (दौड़ व फिजिकल):", style = MaterialTheme.typography.bodySmall)
                            Text("05:00 AM - 08:30 AM", fontWeight = FontWeight.Bold, color = SaffronPrimary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("सायंकालीन सत्र (स्ट्रेंथ व लिखित क्लास):", style = MaterialTheme.typography.bodySmall)
                            Text("04:30 PM - 07:30 PM", fontWeight = FontWeight.Bold, color = NavySecondary)
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = onJoin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("इस केंद्र से जुड़ें (निःशुल्क प्रवेश)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 4. TRAINERS (हमारे प्रशिक्षक)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainersScreen(
    trainers: List<Trainer>,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("हमारे प्रशिक्षक (Trainers)", fontWeight = FontWeight.Bold) },
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
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (trainers.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.SportsScore, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("प्रशिक्षकों की जानकारी", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "मौरिकला गुफा अखाड़े के मुख्य प्रशिक्षक व उस्ताद द्वारा युवाओं को ग्राउंड पर प्रत्यक्ष मार्गदर्शन दिया जाता है।",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(trainers) { trainer ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                            if (trainer.photoUri.isNotBlank()) {
                                AsyncImage(
                                    model = trainer.photoUri,
                                    contentDescription = trainer.name,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, SaffronPrimary, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(SaffronPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(36.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(trainer.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                if (trainer.serviceBackground.isNotBlank()) {
                                    Text(trainer.serviceBackground, fontSize = 12.sp, color = SaffronDark, fontWeight = FontWeight.SemiBold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                if (trainer.experience.isNotBlank()) {
                                    Text("अनुभव: ${trainer.experience}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (trainer.specialization.isNotBlank()) {
                                    Text("विशेषज्ञता: ${trainer.specialization}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                                if (trainer.introduction.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(trainer.introduction, fontSize = 12.sp, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. TRAINING PROGRAM (प्रशिक्षण कार्यक्रम)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingProgramScreen(
    onNavigateBack: () -> Unit,
    onGoToTraining: () -> Unit,
    onGoToStudy: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Physical, 1: Written, 2: Routine

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("प्रशिक्षण कार्यक्रम (Program)", fontWeight = FontWeight.Bold) },
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
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SaffronPrimary
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("शारीरिक (Physical)") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("लिखित (Written)") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("दिनचर्या (Routine)") })
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        item {
                            Text("शारीरिक दक्षता पाठ्यक्रम (Physical Training)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SaffronDark)
                        }
                        val physicalItems = listOf(
                            Pair("1600m / 5 KM दौड़ (Running)", "आर्मी 5:30 मिनट टाइमिंग व पुलिस एंड्योरेंस हेतु स्टेमिना व स्पीड वर्क"),
                            Pair("पुश-अप्स व सिट-अप्स (Push-ups & Sit-ups)", "चेस्ट और कोर एब्स स्ट्रेंथ मजबूत करने हेतु 40-50 रेप्स के 3 सेट"),
                            Pair("बीम / पुल-अप्स (Pull-ups / Chin-ups)", "आर्मी 10 बीम (40 अंक) हेतु लैटिसिमस डोरसी व ग्रिप ट्रेनिंग"),
                            Pair("स्क्वाट्स व लेग्स वर्कआउट (Squats)", "रनिंग में पैरों की ताकत व स्प्रिंट के लिए नियमित स्क्वाट्स"),
                            Pair("लंबी कूद व ऊंची कूद (Long & High Jump)", "पुलिस व पैरामिलिट्री भर्ती मानकों के अनुसार जंपिंग पिट अभ्यास"),
                            Pair("दैनिक ग्राउंड अनुशासन", "समयबद्धता, वार्म-अप, कूल-डाउन स्ट्रेचिंग व चोट से बचाव")
                        )
                        items(physicalItems) { (title, desc) ->
                            ProgramCard(title, desc, Icons.Default.DirectionsRun, SaffronPrimary)
                        }
                        item {
                            Button(
                                onClick = onGoToTraining,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("दैनिक ट्रेनिंग ट्रैकर खोलें", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    1 -> {
                        item {
                            Text("लिखित परीक्षा पाठ्यक्रम (Written Examination)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavySecondary)
                        }
                        val writtenItems = listOf(
                            Pair("सामान्य ज्ञान (General Knowledge)", "भारतीय इतिहास, भूगोल, संविधान, खेल, पुरस्कार व महत्वपूर्ण दिवस"),
                            Pair("सामान्य विज्ञान (General Science)", "दैनिक जीवन का भौतिक विज्ञान, रसायन विज्ञान व जीव विज्ञान"),
                            Pair("गणित (Mathematics)", "प्रतिशत, अनुपात, औसत, लाभ-हानि, साधारण ब्याज, समय व कार्य, क्षेत्रमिति"),
                            Pair("तर्कशक्ति (Reasoning)", "कोडिंग-डिकोडिंग, दिशा परीक्षण, रक्त संबंध, श्रृंखला व सादृश्यता"),
                            Pair("सामान्य हिंदी (General Hindi)", "संधि, समास, विलोम शब्द, पर्यायवाची, मुहावरे व वाक्य शुद्धि"),
                            Pair("मॉक टेस्ट व अभ्यास", "नियमित 100 प्रश्नों का समयबद्ध ओएमआर / ऑनलाइन टेस्ट")
                        )
                        items(writtenItems) { (title, desc) ->
                            ProgramCard(title, desc, Icons.Default.MenuBook, NavySecondary)
                        }
                        item {
                            Button(
                                onClick = onGoToStudy,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NavySecondary)
                            ) {
                                Icon(Icons.Default.School, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("अध्ययन व टेस्ट सेक्शन खोलें", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    2 -> {
                        item {
                            Text("दैनिक व साप्ताहिक दिनचर्या (Daily & Weekly Routine)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("दैनिक समय सारणी (Daily Schedule)", fontWeight = FontWeight.Bold, color = SaffronDark)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("• 05:00 AM - 05:30 AM : वार्म-अप व ग्राउंड स्ट्रेचिंग", style = MaterialTheme.typography.bodySmall)
                                    Text("• 05:30 AM - 06:45 AM : 1600m / 5KM रनिंग व स्प्रिंट ड्रिल्स", style = MaterialTheme.typography.bodySmall)
                                    Text("• 06:45 AM - 07:45 AM : पुश-अप्स, बीम, सिट-अप्स व जंप्स", style = MaterialTheme.typography.bodySmall)
                                    Text("• 04:30 PM - 06:00 PM : लिखित परीक्षा क्लास (Maths/GK/GS)", style = MaterialTheme.typography.bodySmall)
                                    Text("• 06:00 PM - 07:00 PM : मॉक टेस्ट व रिव्यु", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("साप्ताहिक योजना (Weekly Plan)", fontWeight = FontWeight.Bold, color = NavySecondary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("• सोम, बुध, शुक्र : एंड्योरेंस रनिंग + बीम + मैथ्स/रीजनिंग", style = MaterialTheme.typography.bodySmall)
                                    Text("• मंगल, गुरु : स्प्रिंट ड्रिल्स + कोर वर्कआउट + GK/GS", style = MaterialTheme.typography.bodySmall)
                                    Text("• शनिवार : फुल 1600m टाइम ट्रायल टेस्ट + फुल मॉक टेस्ट", style = MaterialTheme.typography.bodySmall)
                                    Text("• रविवार : एक्टिव रिकवरी व मेंटरशिप सत्र", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgramCard(title: String, desc: String, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
            }
        }
    }
}

// ==========================================
// 6. GALLERY (गैलरी)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    galleryItems: List<GalleryItem>,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("All", "Physical Training", "Running", "Ground Training", "Written Classes", "Events", "Awareness Campaign", "Other")
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredItems = remember(galleryItems, selectedCategory) {
        if (selectedCategory == "All") galleryItems else galleryItems.filter { it.category == selectedCategory }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("अखाड़ा गैलरी (Gallery)", fontWeight = FontWeight.Bold) },
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
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Category Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(if (cat == "All") "सभी (All)" else cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SaffronPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("गैलरी में जल्द ही फोटो जोड़ी जाएंगी।", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredItems) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column {
                                if (item.photoUri.isNotBlank()) {
                                    AsyncImage(
                                        model = item.photoUri,
                                        contentDescription = item.title,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                            .background(SaffronPrimary.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Image, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(48.dp))
                                    }
                                }
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Surface(color = SaffronContainer, shape = RoundedCornerShape(4.dp)) {
                                            Text(item.category, fontSize = 10.sp, color = OnSaffronContainer, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                    if (item.caption.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(item.caption, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

// ==========================================
// 7. SUCCESS STORIES (हमारी उपलब्धियां)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuccessStoriesScreen(
    stories: List<SuccessStory>,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("हमारी उपलब्धियां (Success Stories)", fontWeight = FontWeight.Bold) },
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
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (stories.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                "जल्द ही हमारे चयनित अभ्यर्थियों की सफलता की कहानियां यहां दिखाई जाएंगी।",
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "अखाड़े के सभी छात्र पूरी लगन व मेहनत से ग्राउंड और लिखित परीक्षा की तैयारी कर रहे हैं।",
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(stories) { story ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                            if (story.photoUri.isNotBlank()) {
                                AsyncImage(
                                    model = story.photoUri,
                                    contentDescription = story.studentName,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, GoldAccent, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(GoldAccent.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(36.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(story.studentName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("गाँव: ${story.village} • वर्ष: ${story.year}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(color = OliveContainer, shape = RoundedCornerShape(4.dp)) {
                                    Text(
                                        "चयन: ${story.recruitmentExam}",
                                        fontSize = 11.sp,
                                        color = OnOliveContainer,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                if (story.story.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(story.story, fontSize = 12.sp, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 8. CONTACT & HELP (संपर्क एवं सहायता)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(
    contactInfo: ContactInfo?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val info = contactInfo ?: ContactInfo()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("संपर्क एवं सहायता (Contact)", fontWeight = FontWeight.Bold) },
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
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, SaffronPrimary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(info.organisation, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = SaffronDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("प्रशिक्षण केंद्र: ${info.trainingCentre}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(14.dp))

                        ContactRow(Icons.Default.Person, "संपर्क व्यक्ति (Contact Person)", info.contactPerson)
                        ContactRow(Icons.Default.Phone, "मोबाइल नंबर (Mobile)", info.mobile)
                        ContactRow(Icons.Default.Chat, "व्हाट्सएप (WhatsApp)", info.whatsapp)
                        ContactRow(Icons.Default.LocationOn, "पता (Address)", info.address)
                        ContactRow(Icons.Default.Schedule, "समय (Timings)", info.workingHours)
                    }
                }
            }

            item {
                Surface(
                    color = SaffronContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = SaffronDark)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "यह अभियान ग्रामीण युवाओं के उज्ज्वल भविष्य और राष्ट्रसेवा हेतु पूर्णतः निःशुल्क संचालित है।",
                            style = MaterialTheme.typography.bodySmall,
                            color = SaffronDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactRow(icon: ImageVector, label: String, value: String) {
    if (value.isNotBlank()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(icon, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
