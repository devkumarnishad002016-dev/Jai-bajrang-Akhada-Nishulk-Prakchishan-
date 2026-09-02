package com.example.data.recruitment

object RecruitmentStandardRepository {

    val allCategoryProfiles: List<RecruitmentCategoryProfile> = listOf(
        RecruitmentCategoryProfile(
            id = "ARMY_AGNIVEER_GD",
            nameHindi = "भारतीय थल सेना - अग्निवीर जनरल ड्यूटी (Army GD)",
            nameEnglish = "Indian Army Agniveer GD",
            organization = "Indian Army (भारतीय थल सेना)",
            badgeLabel = "सर्वोच्च प्राथमिकता",
            iconEmoji = "🪖",
            minAge = 17.5,
            maxAge = 21.0,
            ageRelaxationNotesHindi = "अग्निवीर जीडी हेतु अधिकतम आयु सीमा 21 वर्ष निर्धारित है।",
            requiredEducationHindi = "10वीं कक्षा उत्तीर्ण (न्यूनतम 45% कुल एवं प्रत्येक विषय में कम से कम 33%)",
            minEducationLevelRank = 2,
            minHeightMaleCm = 169.0,
            minHeightFemaleCm = 162.0,
            minChestMaleCm = 77.0,
            minChestExpansionCm = 5.0,
            target1600mSeconds = 330, // 5:30 min = 330s (Group 1: 60 marks)
            targetPullups = 10, // 10 pullups = 40 marks
            targetLongJumpFeet = 9.0, // 9 feet ditch (Qualifying)
            targetHighJumpFeet = 0.0, // Zigzag balance instead
            targetShotPutMeters = 0.0,
            runningDescriptionHindi = "1600 मीटर दौड़ (5:30 मिनट पर 60 अंक / 5:45 मिनट पर 48 अंक)",
            physicalEfficiencyOverviewHindi = "• 1600m रनिंग: ग्रुप 1 (5:30 min - 60 अंक), ग्रुप 2 (5:45 min - 48 अंक)\n• बीम (पुल-अप्स): 10 बीम (40 अंक), 9 बीम (33 अंक), 8 बीम (27 अंक), 7 बीम (21 अंक), 6 बीम (16 अंक)\n• 9 फीट गड्ढा कूद: क्वालीफाइंग\n• जिग-जैग बैलेंस: क्वालीफाइंग",
            writtenExamPatternHindi = "CEE ऑनलाइन परीक्षा - 50 प्रश्न (100 अंक), समय 60 मिनट:\n• सामान्य ज्ञान (GK): 15 प्रश्न (30 अंक)\n• सामान्य विज्ञान (GS): 15 प्रश्न (30 अंक)\n• गणित (Maths): 15 प्रश्न (30 अंक)\n• रीजनिंग (Reasoning): 5 प्रश्न (10 अंक)\n(नेगेटिव मार्किंग: 0.50 अंक प्रति गलत उत्तर)",
            selectionProcessHindi = "1. ऑनलाइन कंप्यूटर आधारित लिखित परीक्षा (CEE)\n2. शारीरिक दक्षता परीक्षा (PET) एवं शारीरिक मापदंड (PST)\n3. दस्तावेज सत्यापन (Document Verification)\n4. विस्तृत मेडिकल परीक्षण (RMB/Medical)\n5. ऑल इंडिया मेरिट लिस्ट",
            requiredDocumentsHindi = listOf(
                "10वीं मूल अंकसूची एवं प्रमाण पत्र (Marksheet & Certificate)",
                "डिजिटल मूल निवास प्रमाण पत्र (Domicile Certificate)",
                "स्थाई जाति प्रमाण पत्र (Caste Certificate)",
                "सरपंच / पार्षद द्वारा जारी चरित्र प्रमाण पत्र (6 माह भीतर)",
                "स्कूल/कॉलेज चरित्र प्रमाण पत्र",
                "अविवाहित प्रमाण पत्र (Unmarried Certificate)",
                "आधार कार्ड एवं पैन कार्ड",
                "20 पासपोर्ट साइज फोटो (सफेद बैकग्राउंड, बिना दाढ़ी)",
                "NCC 'A'/'B'/'C' या स्पोर्ट्स सर्टिफिकेट (यदि लागू हो)"
            ),
            medicalRequirementsHindi = listOf(
                "दृष्टि क्षमता (Vision): 6/6 (बिना चश्मे के)",
                "कलर ब्लाइंडनेस (वर्णांधता) नहीं होनी चाहिए",
                "फ्लैट फुट (Flat Foot) एवं नॉक नी (Knock Knee) रहित",
                "कान साफ एवं दोनों कानों से सामान्य श्रवण क्षमता",
                "दांत स्वस्थ (न्यूनतम 14 डेंटल पॉइंट्स)",
                "शरीर पर कोई आपत्तिजनक टैटू नहीं होना चाहिए"
            ),
            preparationTipsHindi = listOf(
                "प्रतिदिन सुबह 5:00 बजे 1600m पेस रनिंग व स्प्रिंट ड्रिल्स करें।",
                "बीम (पुल-अप्स) 10 तक ले जाने के लिए प्रतिदिन 3 सेट लगाएं।",
                "10वीं स्तर के गणित व सामान्य विज्ञान के फॉर्मूले रोज दोहराएं।",
                "साप्ताहिक 2 फुल लेंथ CEE मॉक टेस्ट हल करें।"
            ),
            officialWebsiteUrl = "https://joinindianarmy.nic.in"
        ),
        RecruitmentCategoryProfile(
            id = "CG_STATE_POLICE",
            nameHindi = "राज्य पुलिस / छत्तीसगढ़ पुलिस आरक्षक (Police Constable)",
            nameEnglish = "State Police / CG Police Constable",
            organization = "Chhattisgarh Police Department / State Police",
            badgeLabel = "राज्य मिशन",
            iconEmoji = "👮",
            minAge = 18.0,
            maxAge = 28.0,
            ageRelaxationNotesHindi = "SC, ST एवं OBC वर्ग को अधिकतम आयु सीमा में 5 वर्ष की छूट (33 वर्ष तक)।",
            requiredEducationHindi = "10वीं/12वीं पास (अनुसूचित जनजाति ST वर्ग हेतु 8वीं पास मान्य)",
            minEducationLevelRank = 2,
            minHeightMaleCm = 168.0,
            minHeightFemaleCm = 158.0,
            minChestMaleCm = 81.0,
            minChestExpansionCm = 5.0,
            target1600mSeconds = 330, // 1500m in CG Police
            targetPullups = 0,
            targetLongJumpFeet = 17.0, // 5.40m = ~17.7 ft for max marks
            targetHighJumpFeet = 4.5, // 1.40m = ~4.6 ft for max marks
            targetShotPutMeters = 9.0, // 9 meters for 20 marks (16 lbs)
            runningDescriptionHindi = "1500m दौड़ (पुरुष) / 800m (महिला) एवं 100m स्प्रिंट",
            physicalEfficiencyOverviewHindi = "शारीरिक दक्षता परीक्षा (PET) - कुल 100 अंक (प्रत्येक इवेंट 20 अंक):\n• 1500m दौड़ (पुरुष) / 800m (महिला) - 20 अंक\n• 100m स्प्रिंट दौड़ - 20 अंक\n• लंबी कूद (Long Jump) - 20 अंक\n• ऊंची कूद (High Jump) - 20 अंक\n• गोला फेंक (Shot Put 16 पौंड) - 20 अंक",
            writtenExamPatternHindi = "लिखित परीक्षा - 100 प्रश्न (100 अंक), समय 120 मिनट:\n• सामान्य ज्ञान एवं छत्तीसगढ़ राज्य सामान्य ज्ञान (GK/GS): 50 अंक\n• गणित एवं तार्किक क्षमता (Reasoning & Maths): 25 अंक\n• हिंदी / छत्तीसगढ़ी भाषा ज्ञान: 25 अंक",
            selectionProcessHindi = "1. दस्तावेज परीक्षण एवं शारीरिक नापजोख (PST)\n2. शारीरिक दक्षता परीक्षा (PET - 100 अंक)\n3. लिखित परीक्षा (Written Exam - 100 अंक)\n4. विशेष योग्यता बोनस अंक (NCC/NSS/Sports - 10 अंक)\n5. मेडिकल जांच व अंतिम चयन सूची",
            requiredDocumentsHindi = listOf(
                "10वीं एवं 12वीं अंकसूची (8वीं ST वर्ग)",
                "छत्तीसगढ़ राज्य मूल निवास प्रमाण पत्र",
                "स्थाई जाति प्रमाण पत्र (OBC/SC/ST)",
                "जिला रोजगार कार्यालय का जीवित पंजीयन प्रमाण पत्र (Employment Reg.)",
                "आधार कार्ड / वोटर आईडी",
                "पासपोर्ट साइज नवीनतम रंगीन फोटो",
                "NCC / NSS / होमगार्ड / राष्ट्रीय खेल प्रमाण पत्र (बोनस अंकों हेतु)"
            ),
            medicalRequirementsHindi = listOf(
                "दृष्टि क्षमता 6/6 एवं 6/9 दोनों आंखों में",
                "कलर विजन सामान्य होना अनिवार्य",
                "नॉक-नी (Knock-knee) व धनुषाकार पैर नहीं होने चाहिए",
                "हियरिंग और रेस्पिरेटरी सिस्टम पूर्णतः स्वस्थ"
            ),
            preparationTipsHindi = listOf(
                "ग्राउंड पर पांचों फिजिकल इवेंट्स (दौड़, 100m, लंबी कूद, ऊंची कूद, गोला फेंक) का संतुलित अभ्यास करें।",
                "छत्तीसगढ़ सामान्य ज्ञान एवं राज्य समसामयिकी (Current Affairs) पर विशेष पकड़ बनाएं।",
                "अंकगणित के बेसिक चैप्टर्स (प्रतिशत, अनुपात, औसत) रोज हल करें।"
            ),
            officialWebsiteUrl = "https://cgpolice.gov.in"
        ),
        RecruitmentCategoryProfile(
            id = "SSC_GD_CAPF",
            nameHindi = "कर्मचारी चयन आयोग - एसएससी जीडी (SSC GD Constable)",
            nameEnglish = "SSC GD Constable (BSF, CISF, CRPF, ITBP, SSB, SSF)",
            organization = "Staff Selection Commission & Ministry of Home Affairs",
            badgeLabel = "अखिल भारतीय",
            iconEmoji = "🇮🇳",
            minAge = 18.0,
            maxAge = 23.0,
            ageRelaxationNotesHindi = "OBC: 3 वर्ष (26 वर्ष), SC/ST: 5 वर्ष (28 वर्ष), एक्स-सर्विसमैन नियमानुसार।",
            requiredEducationHindi = "10वीं कक्षा उत्तीर्ण (मान्यता प्राप्त बोर्ड से)",
            minEducationLevelRank = 2,
            minHeightMaleCm = 170.0,
            minHeightFemaleCm = 157.0,
            minChestMaleCm = 80.0,
            minChestExpansionCm = 5.0,
            target1600mSeconds = 1440, // 5km in 24min (1440s)
            targetPullups = 0,
            targetLongJumpFeet = 0.0,
            targetHighJumpFeet = 0.0,
            targetShotPutMeters = 0.0,
            runningDescriptionHindi = "पुरुष: 24 मिनट में 5 किलोमीटर दौड़ | महिला: 8.5 मिनट में 1.6 किलोमीटर दौड़",
            physicalEfficiencyOverviewHindi = "शारीरिक दक्षता परीक्षा (PET - Qualifying):\n• पुरुष अभ्यर्थी: 24 मिनट में 5 KM दौड़\n• महिला अभ्यर्थी: 8.5 मिनट में 1.6 KM दौड़\n• लद्दाख क्षेत्र हेतु: पुरुष 1.6 KM (7 मिनट), महिला 800m (5 मिनट)\n• शारीरिक मानक (PST): पुरुष हाइट 170cm, चेस्ट 80-85cm | महिला हाइट 157cm",
            writtenExamPatternHindi = "कंप्यूटर आधारित परीक्षा (CBE) - 80 प्रश्न (160 अंक), 60 मिनट:\n• सामान्य बुद्धिमत्ता व रीजनिंग: 20 प्रश्न (40 अंक)\n• सामान्य ज्ञान व सामान्य विज्ञान: 20 प्रश्न (40 अंक)\n• प्रारंभिक गणित: 20 प्रश्न (40 अंक)\n• हिंदी अथवा अंग्रेजी व्याकरण: 20 प्रश्न (40 अंक)\n(नेगेटिव मार्किंग: 0.25 अंक प्रति गलत उत्तर)",
            selectionProcessHindi = "1. कंप्यूटर आधारित लिखित परीक्षा (CBE)\n2. शारीरिक मानक व दक्षता परीक्षा (PST/PET)\n3. विस्तृत चिकित्सा परीक्षण (DME) एवं रिव्यू मेडिकल (RME)\n4. मूल दस्तावेज सत्यापन (DV)\n5. सीएपीएफ आवंटन मेरिट लिस्ट",
            requiredDocumentsHindi = listOf(
                "10वीं हाई स्कूल बोर्ड मार्कशीट एवं सनद",
                "केंद्रीय प्रारूप में जाति प्रमाण पत्र (Central Format OBC/SC/ST)",
                "मूल निवास / Domicile Certificate",
                "वैध फोटो पहचान पत्र (आधार कार्ड/ड्राइविंग लाइसेंस)",
                "पासपोर्ट साइज फोटोग्राफ",
                "NCC 'A', 'B', 'C' सर्टिफिकेट (2% से 5% बोनस अंक)"
            ),
            medicalRequirementsHindi = listOf(
                "दृष्टि 6/6 एवं 6/9 बिना चश्मे के",
                "हथेली का पसीना, वेरीकोज वेन्स और नॉक नी की जांच",
                "मानसिक एवं शारीरिक रूप से पूर्णतः स्वस्थ"
            ),
            preparationTipsHindi = listOf(
                "लगातार 5KM एंड्योरेंस रनिंग का अभ्यास सप्ताह में 3 दिन करें।",
                "60 मिनट में 80 प्रश्न हल करने हेतु स्पीड और एक्यूरेसी पर फोकस करें।",
                "हिंदी व्याकरण एवं रीजनिंग में 100% स्कोर का टारगेट रखें।"
            ),
            officialWebsiteUrl = "https://ssc.gov.in"
        ),
        RecruitmentCategoryProfile(
            id = "CRPF_CONSTABLE",
            nameHindi = "केंद्रीय रिजर्व पुलिस बल (CRPF Constable GD & Tradesman)",
            nameEnglish = "Central Reserve Police Force (CRPF)",
            organization = "Central Reserve Police Force (MHA)",
            badgeLabel = "केंद्रीय बल",
            iconEmoji = "🛡️",
            minAge = 18.0,
            maxAge = 23.0,
            ageRelaxationNotesHindi = "ट्रेड्समैन पदों हेतु 18-26 वर्ष, आरक्षित श्रेणियों को 3 से 5 वर्ष की छूट।",
            requiredEducationHindi = "10वीं कक्षा उत्तीर्ण (ट्रेड्समैन हेतु संबंधित ट्रेड का बेसिक ज्ञान / आईटीआई)",
            minEducationLevelRank = 2,
            minHeightMaleCm = 170.0,
            minHeightFemaleCm = 157.0,
            minChestMaleCm = 80.0,
            minChestExpansionCm = 5.0,
            target1600mSeconds = 600, // 10 min for tradesman or 5km for GD
            targetPullups = 0,
            targetLongJumpFeet = 0.0,
            targetHighJumpFeet = 0.0,
            targetShotPutMeters = 0.0,
            runningDescriptionHindi = "जीडी: 5KM (24 मिनट) | ट्रेड्समैन: 1.6KM (10 मिनट)",
            physicalEfficiencyOverviewHindi = "• पुरुष जीडी: 5 KM रनिंग (24 मिनट में क्वालीफाइंग)\n• महिला जीडी: 1.6 KM रनिंग (8.5 मिनट)\n• ट्रेड्समैन (कुक/सफाई/धोबी): 1.6 KM दौड़ 10 मिनट में\n• ट्रेड टेस्ट (ट्रेड्समैन पदों के लिए क्वालीफाइंग 50 अंक)",
            writtenExamPatternHindi = "लिखित परीक्षा (CBT) - 100 प्रश्न (100 अंक), 120 मिनट:\n• सामान्य बुद्धिमत्ता व रीजनिंग: 25 प्रश्न (25 अंक)\n• सामान्य ज्ञान व जीएस: 25 प्रश्न (25 अंक)\n• प्राथमिक गणित: 25 प्रश्न (25 अंक)\n• सामान्य हिंदी / अंग्रेजी: 25 प्रश्न (25 अंक)",
            selectionProcessHindi = "1. ऑनलाइन सीबीटी परीक्षा (CBT)\n2. शारीरिक दक्षता एवं मानक परीक्षण (PST/PET)\n3. ट्रेड टेस्ट (ट्रेड पदों हेतु)\n4. दस्तावेज सत्यापन (DV)\n5. विस्तृत चिकित्सा परीक्षा (DME)",
            requiredDocumentsHindi = listOf(
                "10वीं मार्कशीट एवं पासिंग सर्टिफिकेट",
                "जाति एवं निवास प्रमाण पत्र",
                "संबंधित ट्रेड अनुभव / आईटीआई (यदि लागू हो)",
                "आधार कार्ड एवं 10 रंगीन पासपोर्ट फोटो"
            ),
            medicalRequirementsHindi = listOf(
                "कलर विजन सीपी-III श्रेणी",
                "रक्तचाप एवं ईसीजी सामान्य",
                "चेस्ट एक्स-रे एवं सामान्य मेडिकल टेस्ट"
            ),
            preparationTipsHindi = listOf(
                "शारीरिक मजबूती व स्टेमिना के लिए रोज 4-5 KM जॉगिंग करें।",
                "जीके एवं करंट अफेयर्स का नियमित अभ्यास करें।"
            ),
            officialWebsiteUrl = "https://rect.crpf.gov.in"
        ),
        RecruitmentCategoryProfile(
            id = "BSF_CONSTABLE",
            nameHindi = "सीमा सुरक्षा बल (BSF Constable GD & Tradesman)",
            nameEnglish = "Border Security Force (BSF)",
            organization = "Border Security Force (MHA)",
            badgeLabel = "सीमा रक्षक",
            iconEmoji = "🦅",
            minAge = 18.0,
            maxAge = 23.0,
            ageRelaxationNotesHindi = "OBC को 3 वर्ष तथा SC/ST को 5 वर्ष की नियमानुसार छूट।",
            requiredEducationHindi = "10वीं कक्षा उत्तीर्ण (ट्रेड्समैन हेतु ट्रेड प्रोफिशिएंसी)",
            minEducationLevelRank = 2,
            minHeightMaleCm = 170.0,
            minHeightFemaleCm = 157.0,
            minChestMaleCm = 80.0,
            minChestExpansionCm = 5.0,
            target1600mSeconds = 1440,
            targetPullups = 0,
            targetLongJumpFeet = 0.0,
            targetHighJumpFeet = 0.0,
            targetShotPutMeters = 0.0,
            runningDescriptionHindi = "5 KM दौड़ 24 मिनट में (पुरुष) / 1.6 KM 8.5 मिनट में (महिला)",
            physicalEfficiencyOverviewHindi = "• 5 KM दौड़ पुरुष 24 मिनट\n• 1.6 KM दौड़ महिला 8 मिनट 30 सेकंड\n• शारीरिक माप: पुरुष ऊंचाई 170 सेमी, सीना 80-85 सेमी",
            writtenExamPatternHindi = "लिखित परीक्षा (OMR/CBT) - 100 प्रश्न (100 अंक), समय 120 मिनट:\n• सामान्य ज्ञान व चेतना: 25 प्रश्न\n• गणित: 25 प्रश्न\n• रीजनिंग: 25 प्रश्न\n• हिंदी / अंग्रेजी: 25 प्रश्न",
            selectionProcessHindi = "1. लिखित परीक्षा (Phase-1)\n2. PST/PET एवं ट्रेड टेस्ट (Phase-2)\n3. दस्तावेज सत्यापन\n4. मेडिकल परीक्षण (DME/RME)",
            requiredDocumentsHindi = listOf(
                "10वीं बोर्ड अंकसूची",
                "केंद्रीय जाति एवं निवास प्रमाण पत्र",
                "पहचान पत्र एवं फोटो"
            ),
            medicalRequirementsHindi = listOf(
                "दूर दृष्टि 6/6 एवं 6/9",
                "फ्लैट फुट और नॉक नी नहीं होना चाहिए"
            ),
            preparationTipsHindi = listOf(
                "लंबी दूरी की दौड़ के साथ स्ट्रेंथ ट्रेनिंग करें।",
                "विगत वर्षों के पेपर्स रोज हल करें।"
            ),
            officialWebsiteUrl = "https://rectt.bsf.gov.in"
        ),
        RecruitmentCategoryProfile(
            id = "CISF_CONSTABLE",
            nameHindi = "केंद्रीय औद्योगिक सुरक्षा बल (CISF Constable Fire & GD)",
            nameEnglish = "Central Industrial Security Force (CISF)",
            organization = "Central Industrial Security Force (MHA)",
            badgeLabel = "सुरक्षा मिशन",
            iconEmoji = "🏢",
            minAge = 18.0,
            maxAge = 23.0,
            ageRelaxationNotesHindi = "कांस्टेबल फायर हेतु 12वीं साइंस अनिवार्य, आयु छूट 18-23 वर्ष + नियमानुसार।",
            requiredEducationHindi = "12वीं पास (साइंस विषय के साथ - Constable Fire) / 10वीं पास (GD)",
            minEducationLevelRank = 3,
            minHeightMaleCm = 170.0,
            minHeightFemaleCm = 157.0,
            minChestMaleCm = 80.0,
            minChestExpansionCm = 5.0,
            target1600mSeconds = 1440,
            targetPullups = 0,
            targetLongJumpFeet = 0.0,
            targetHighJumpFeet = 0.0,
            targetShotPutMeters = 0.0,
            runningDescriptionHindi = "5 KM दौड़ 24 मिनट में पूर्ण करनी होगी (पुरुष)",
            physicalEfficiencyOverviewHindi = "• 5 KM दौड़ 24 मिनट में (क्वालीफाइंग)\n• ऊंचाई: न्यूनतम 170 सेमी\n• सीना: 80 सेमी (न्यूनतम 5 सेमी फुलाव अनिवार्य)",
            writtenExamPatternHindi = "CBT लिखित परीक्षा - 100 प्रश्न (100 अंक), 120 मिनट:\n• रीजनिंग: 25 प्रश्न (25 अंक)\n• सामान्य ज्ञान: 25 प्रश्न (25 अंक)\n• गणित: 25 प्रश्न (25 अंक)\n• हिंदी/अंग्रेजी: 25 प्रश्न (25 अंक)",
            selectionProcessHindi = "1. PST / PET एवं दस्तावेज सत्यापन\n2. लिखित परीक्षा (CBT)\n3. विस्तृत मेडिकल परीक्षण (DME)\n4. अंतिम चयन सूची",
            requiredDocumentsHindi = listOf(
                "10वीं एवं 12वीं (विज्ञान संकाय) अंकसूची",
                "डोमिसाइल एवं जाति प्रमाण पत्र",
                "फोटो पहचान पत्र"
            ),
            medicalRequirementsHindi = listOf(
                "दृष्टि 6/6 एवं पूर्ण शारीरिक फिटनेस"
            ),
            preparationTipsHindi = listOf(
                "12वीं स्तर के बेसिक साइंस व मैथ्स का नियमित रिवीजन करें।"
            ),
            officialWebsiteUrl = "https://cisfrectt.cisf.gov.in"
        ),
        RecruitmentCategoryProfile(
            id = "ITBP_CONSTABLE",
            nameHindi = "भारत-तिब्बत सीमा पुलिस (ITBP Constable GD & Telecom)",
            nameEnglish = "Indo-Tibetan Border Police (ITBP)",
            organization = "Indo-Tibetan Border Police (MHA)",
            badgeLabel = "हिमवीर",
            iconEmoji = "🏔️",
            minAge = 18.0,
            maxAge = 23.0,
            ageRelaxationNotesHindi = "टेलीकॉम व मोटर मैकेनिक हेतु 18-25 वर्ष, आरक्षित श्रेणियों को 3-5 वर्ष छूट।",
            requiredEducationHindi = "10वीं पास (जीडी) / 10वीं + आईटीआई / 12वीं साइंस (टेलीकॉम)",
            minEducationLevelRank = 2,
            minHeightMaleCm = 170.0,
            minHeightFemaleCm = 157.0,
            minChestMaleCm = 80.0,
            minChestExpansionCm = 5.0,
            target1600mSeconds = 450, // 1.6km in 7:30 min for trades/telecom or 5km for GD
            targetPullups = 0,
            targetLongJumpFeet = 11.0, // 11 feet long jump in ITBP
            targetHighJumpFeet = 3.5, // 3.5 feet high jump
            targetShotPutMeters = 0.0,
            runningDescriptionHindi = "1.6 KM दौड़ 7 मिनट 30 सेकंड में, 11 फीट लंबी कूद एवं 3.5 फीट ऊंची कूद",
            physicalEfficiencyOverviewHindi = "• 1.6 KM दौड़: 7 मिनट 30 सेकंड (पुरुष)\n• लंबी कूद (Long Jump): 11 फीट (3 अवसर)\n• ऊंची कूद (High Jump): 3.5 फीट (3 अवसर)\n• महिला: 800m दौड़ (4:45 min), 9 फीट लॉन्ग जंप, 3 फीट हाई जंप",
            writtenExamPatternHindi = "लिखित परीक्षा - 100 प्रश्न (100 अंक), 120 मिनट:\n• सामान्य ज्ञान: 25 प्रश्न\n• गणित: 25 प्रश्न\n• रीजनिंग: 25 प्रश्न\n• हिंदी / अंग्रेजी: 25 प्रश्न",
            selectionProcessHindi = "1. PET / PST चरण\n2. लिखित परीक्षा (OMR/CBT)\n3. दस्तावेज सत्यापन व स्किल टेस्ट\n4. विस्तृत चिकित्सा परीक्षा",
            requiredDocumentsHindi = listOf(
                "10वीं/12वीं अंकसूची",
                "जाति, निवास एवं पहचान प्रमाण पत्र",
                "पासपोर्ट साइज फोटो"
            ),
            medicalRequirementsHindi = listOf(
                "उच्च तुंगता (High Altitude) सहन करने योग्य शारीरिक व फेफड़ों की क्षमता",
                "दृष्टि 6/6 एवं सामान्य स्वास्थ्य"
            ),
            preparationTipsHindi = listOf(
                "हाई जंप व लॉन्ग जंप की तकनीक का नियमित ग्राउंड अभ्यास करें।",
                "कार्डियो स्टेमिना बढ़ाने हेतु हिल स्प्रिंट्स करें।"
            ),
            officialWebsiteUrl = "https://recruitment.itbpolice.nic.in"
        ),
        RecruitmentCategoryProfile(
            id = "SSB_CONSTABLE",
            nameHindi = "सशस्त्र सीमा बल (SSB Constable GD & Tradesman)",
            nameEnglish = "Sashastra Seema Bal (SSB)",
            organization = "Sashastra Seema Bal (MHA)",
            badgeLabel = "सेवा, सुरक्षा, बंधुत्व",
            iconEmoji = "🎯",
            minAge = 18.0,
            maxAge = 23.0,
            ageRelaxationNotesHindi = "ड्राइवर पदों हेतु 21-27 वर्ष, आरक्षित श्रेणियों को 3 से 5 वर्ष की छूट।",
            requiredEducationHindi = "10वीं कक्षा उत्तीर्ण (ड्राइवर हेतु भारी वाहन ड्राइविंग लाइसेंस HMV/LMV)",
            minEducationLevelRank = 2,
            minHeightMaleCm = 170.0,
            minHeightFemaleCm = 157.0,
            minChestMaleCm = 80.0,
            minChestExpansionCm = 5.0,
            target1600mSeconds = 1440,
            targetPullups = 0,
            targetLongJumpFeet = 0.0,
            targetHighJumpFeet = 0.0,
            targetShotPutMeters = 0.0,
            runningDescriptionHindi = "4.8 KM दौड़ 24 मिनट में (पुरुष) / 2.4 KM 18 मिनट में (महिला)",
            physicalEfficiencyOverviewHindi = "• पुरुष अभ्यर्थी: 4.8 KM दौड़ (24 मिनट में)\n• महिला अभ्यर्थी: 2.4 KM दौड़ (18 मिनट में)\n• ऊंचाई: न्यूनतम 170 सेमी (पुरुष), 157 सेमी (महिला)\n• सीना: 80 सेमी (न्यूनतम 5 सेमी फुलाव अनिवार्य)",
            writtenExamPatternHindi = "लिखित परीक्षा (CBT) - 100 प्रश्न (100 अंक), 120 मिनट:\n• सामान्य ज्ञान व जीएस: 25 प्रश्न (25 अंक)\n• गणित: 25 प्रश्न (25 अंक)\n• रीजनिंग: 25 प्रश्न (25 अंक)\n• सामान्य हिंदी / अंग्रेजी: 25 प्रश्न (25 अंक)",
            selectionProcessHindi = "1. शारीरिक दक्षता एवं मानक परीक्षा (PST/PET)\n2. लिखित परीक्षा (CBT)\n3. दस्तावेज सत्यापन एवं स्किल / ट्रेड टेस्ट\n4. विस्तृत चिकित्सा परीक्षा (DME/RME)",
            requiredDocumentsHindi = listOf(
                "10वीं बोर्ड अंकसूची व प्रमाण पत्र",
                "केंद्रीय प्रारूप जाति एवं स्थायी निवास प्रमाण पत्र",
                "वैध ड्राइविंग लाइसेंस (ड्राइवर पदों हेतु)",
                "आधार कार्ड एवं 10 पासपोर्ट फोटो"
            ),
            medicalRequirementsHindi = listOf(
                "दृष्टि 6/6 एवं 6/9 बिना चश्मे के",
                "सामान्य शारीरिक व मानसिक स्वास्थ्य",
                "नॉक-नी, फ्लैट-फुट रहित"
            ),
            preparationTipsHindi = listOf(
                "4.8 KM एंड्योरेंस दौड़ का ग्राउंड पर निरंतर अभ्यास रखें।",
                "जीएस और बेसिक अंकगणित के कॉन्सेप्ट्स मजबूत करें।"
            ),
            officialWebsiteUrl = "https://ssbrectt.gov.in"
        )
    )

    fun findProfileByIdOrGoal(goalOrId: String): RecruitmentCategoryProfile {
        val normalized = goalOrId.trim().lowercase()
        return allCategoryProfiles.find {
            it.id.equals(normalized, ignoreCase = true) ||
            it.nameEnglish.lowercase().contains(normalized) ||
            it.nameHindi.lowercase().contains(normalized) ||
            (normalized.contains("army") && it.id.contains("ARMY")) ||
            (normalized.contains("police") && it.id.contains("POLICE")) ||
            (normalized.contains("ssc") && it.id.contains("SSC")) ||
            (normalized.contains("crpf") && it.id.contains("CRPF")) ||
            (normalized.contains("bsf") && it.id.contains("BSF")) ||
            (normalized.contains("cisf") && it.id.contains("CISF")) ||
            (normalized.contains("itbp") && it.id.contains("ITBP")) ||
            (normalized.contains("ssb") && it.id.contains("SSB"))
        } ?: allCategoryProfiles.first()
    }
}
