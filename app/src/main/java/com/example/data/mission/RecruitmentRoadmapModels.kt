package com.example.data.mission

data class RoadmapStage(
    val stepNumber: Int,
    val titleHindi: String,
    val phaseName: String,
    val descriptionHindi: String,
    val keyChecklistHindi: List<String>,
    val tipsHindi: String,
    val iconEmoji: String
)

data class ExamRoadmap(
    val examId: String,
    val examNameHindi: String,
    val organization: String,
    val eligibilityBrief: String,
    val stages: List<RoadmapStage>
)

object RecruitmentRoadmapData {

    fun getAllRoadmaps(): List<ExamRoadmap> = listOf(
        // 1. Indian Army Agniveer GD
        ExamRoadmap(
            examId = "army_gd",
            examNameHindi = "इंडियन आर्मी अग्निवीर जनरल ड्यूटी (Army GD)",
            organization = "भारतीय थल सेना (Indian Army)",
            eligibilityBrief = "10वीं कक्षा 45% अंकों के साथ (प्रत्येक विषय में कम से कम 33%) | आयु: 17.5 से 21 वर्ष | लम्बाई: 168+ सेमी",
            stages = listOf(
                RoadmapStage(
                    stepNumber = 1,
                    titleHindi = "पात्रता एवं दस्तावेज तैयारी (Eligibility & Documents)",
                    phaseName = "दस्तावेज व मानक",
                    descriptionHindi = "10वीं की मूल अंकसूची, निवास, जाति, चरित्र व आधार कार्ड तैयार रखें।",
                    keyChecklistHindi = listOf(
                        "10वीं में कुल 45% व प्रत्येक विषय में न्यूनतम 33% अंक",
                        "आयु 17.5 से 21 वर्ष के बीच हो",
                        "ऊंचाई (Height) 168 सेमी और सीना 77 सेमी (+5 सेमी फुलाव)"
                    ),
                    tipsHindi = "सभी दस्तावेजों में नाम, जन्मतिथि व पिता का नाम अक्षरशः समान होना अनिवार्य है।",
                    iconEmoji = "📋"
                ),
                RoadmapStage(
                    stepNumber = 2,
                    titleHindi = "ग्राउंड फिजिकल ट्रेनिंग (Ground Physical Regimen)",
                    phaseName = "फिजिकल तैयारी",
                    descriptionHindi = "1600 मीटर रनिंग, 10 बीम (पुल-अप्स), 9 फीट गड्ढा और ज़िग-ज़ैग बैलेंस।",
                    keyChecklistHindi = listOf(
                        "1600m रनिंग: 5:30 मिनट के भीतर (ग्रुप-1 के 60 अंक)",
                        "10 बीम (पुल-अप्स): 40 अंक",
                        "9-फीट गड्ढा कूद और संतुलन बीम पास करना"
                    ),
                    tipsHindi = "जय बजरंग अखाड़े में प्रातः 5 बजे नियमित 1600m ट्रायल एवं बीम का अभ्यास करें।",
                    iconEmoji = "🏃"
                ),
                RoadmapStage(
                    stepNumber = 3,
                    titleHindi = "लिखित परीक्षा अध्ययन व नोट्स (CEE Written Preparation)",
                    phaseName = "लिखित परीक्षा",
                    descriptionHindi = "सामान्य ज्ञान (15 प्रश्न), सामान्य विज्ञान (15 प्रश्न), गणित (15 प्रश्न), रीजनिंग (5 प्रश्न)।",
                    keyChecklistHindi = listOf(
                        "कुल 50 प्रश्न, 100 अंक, समय 60 मिनट",
                        "प्रत्येक सही उत्तर के 2 अंक, गलत उत्तर पर 0.50 नेगेटिव मार्किंग",
                        "पासिंग मार्क्स: 35 अंक"
                    ),
                    tipsHindi = "अखाड़ा ऐप में उपलब्ध 63 अध्यायों के नोट्स पढ़ें और रोज 20 प्रश्नों का क्विज लगाएं।",
                    iconEmoji = "📖"
                ),
                RoadmapStage(
                    stepNumber = 4,
                    titleHindi = "ऑनलाइन आवेदन व एडमिट कार्ड (Application Form)",
                    phaseName = "फॉर्म प्रक्रिया",
                    descriptionHindi = "joinindianarmy.nic.in पोर्टल पर समय रहते ऑनलाइन आवेदन करें।",
                    keyChecklistHindi = listOf(
                        "आधिकारिक अधिसूचना जारी होने पर प्रथम सप्ताह में आवेदन भरें",
                        "सफेद बैकग्राउंड वाली पासपोर्ट फोटो और हस्ताक्षर अपलोड करें",
                        "आवेदन शुल्क का भुगतान कर रसीद सुरक्षित रखें"
                    ),
                    tipsHindi = "फॉर्म भरते समय आधार लिंक मोबाइल नंबर ही दर्ज करें ताकि OTP में कोई रुकावट न हो।",
                    iconEmoji = "💻"
                ),
                RoadmapStage(
                    stepNumber = 5,
                    titleHindi = "कंप्यूटर आधारित परीक्षा (Common Entrance Exam - CEE)",
                    phaseName = "ऑनलाइन सीबीटी",
                    descriptionHindi = "प्रथम चरण में ऑनलाइन सीबीटी परीक्षा आयोजित की जाती है।",
                    keyChecklistHindi = listOf(
                        "एडमिट कार्ड, आधार कार्ड व 2 पासपोर्ट फोटो परीक्षा केंद्र ले जाएं",
                        "नेगेटिव मार्किंग से बचने के लिए केवल निश्चित प्रश्नों को ही हल करें",
                        "समय का सही विभाजन: 20 मिनट गणित, 15 मिनट विज्ञान, 15 मिनट जीके"
                    ),
                    tipsHindi = "परीक्षा से पूर्व कम से कम 10 ऑनलाइन फुल मॉक टेस्ट अवश्य पूरे करें।",
                    iconEmoji = "🖥️"
                ),
                RoadmapStage(
                    stepNumber = 6,
                    titleHindi = "भर्ती रैली फिजिकल टेस्ट (PET & PST Rally)",
                    phaseName = "ग्राउंड रैली",
                    descriptionHindi = "लिखित परीक्षा में चयनित अभ्यर्थियों को ग्राउंड फिजिकल रैली में बुलाया जाता है।",
                    keyChecklistHindi = listOf(
                        "रैली मैदान में 1600 मीटर दौड़ एवं बीम टेस्ट",
                        "शारीरिक माप (PST): ऊंचाई, सीना व वजन की जांच",
                        "बायोमेट्रिक सत्यापन एवं मूल दस्तावेज जांच"
                    ),
                    tipsHindi = "रैली के दिन पर्याप्त पानी पिएं और ऊर्जा हेतु केला/ग्लूकोज साथ रखें।",
                    iconEmoji = "🏅"
                ),
                RoadmapStage(
                    stepNumber = 7,
                    titleHindi = "विस्तृत मेडिकल जांच (Medical Examination)",
                    phaseName = "मेडिकल जांच",
                    descriptionHindi = "सैन्य डॉक्टरों द्वारा संपूर्ण शारीरिक स्वास्थ्य व अंगों की जांच की जाती है।",
                    keyChecklistHindi = listOf(
                        "आंखों का विजन 6/6 एवं कलर ब्लाइंडनेस जांच",
                        "कानों की सफाई (वैक्स शून्य होना चाहिए)",
                        "फ्लैट फुट, नॉक-नी, हड्डियों में कोई पुराना फ्रैक्चर न हो"
                    ),
                    tipsHindi = "मेडिकल से 2 सप्ताह पूर्व डॉक्टर से कानों की जांच कराएं और पर्याप्त नींद लें।",
                    iconEmoji = "🩺"
                ),
                RoadmapStage(
                    stepNumber = 8,
                    titleHindi = "अंतिम चयन सूची एवं ट्रैनिंग सेंटर ज्वाइनिंग (Final Merit & Joining)",
                    phaseName = "अंतिम चयन",
                    descriptionHindi = "CEE लिखित परीक्षा + फिजिकल टेस्ट के संयुक्त अंकों के आधार पर अंतिम मेरिट सूची।",
                    keyChecklistHindi = listOf(
                        "आधिकारिक वेबसाइट पर फाइनल मेरिट में नाम देखें",
                        "ज्वाइनिंग लेटर व डिस्पैच निर्देश प्राप्त करें",
                        "रेजिमेंटल सेंटर में 6 माह की मूलभूत सैन्य ट्रेनिंग हेतु प्रस्थान"
                    ),
                    tipsHindi = "वर्दी का गौरव आपका इंतजार कर रहा है! अखाड़े और परिवार का नाम रोशन करें।",
                    iconEmoji = "🇮🇳"
                )
            )
        ),

        // 2. CG Police Constable (छत्तीसगढ़ पुलिस आरक्षक)
        ExamRoadmap(
            examId = "cg_police",
            examNameHindi = "छत्तीसगढ़ पुलिस आरक्षक (CG Police Constable)",
            organization = "छत्तीसगढ़ पुलिस विभाग (CG Police)",
            eligibilityBrief = "10वीं/12वीं पास (एसटी वर्ग हेतु 8वीं पास) | आयु: 18 से 28 वर्ष (छूट नियमानुसार) | ऊंचाई: पुरुष 168 सेमी, महिला 158 सेमी",
            stages = listOf(
                RoadmapStage(
                    stepNumber = 1,
                    titleHindi = "पात्रता एवं मानक सत्यापन (Eligibility Verification)",
                    phaseName = "प्रारंभिक चरण",
                    descriptionHindi = "छत्तीसगढ़ का मूल निवास प्रमाण पत्र और न्यूनतम शैक्षणिक योग्यता।",
                    keyChecklistHindi = listOf(
                        "छत्तीसगढ़ का स्थायी निवासी प्रमाण पत्र",
                        "सामान्य/ओबीसी 10वीं/12वीं पास, एसटी वर्ग हेतु 8वीं पास मान्य",
                        "पुरुष: ऊंचाई 168 सेमी, सीना 81-86 सेमी | महिला: ऊंचाई 158 सेमी"
                    ),
                    tipsHindi = "जाति प्रमाण पत्र एवं निवास प्रमाण पत्र डिजिटल रूप से सत्यापित रखें।",
                    iconEmoji = "📋"
                ),
                RoadmapStage(
                    stepNumber = 2,
                    titleHindi = "फिजिकल टेस्ट 5 स्पर्धाओं की तैयारी (100 Marks Physical)",
                    phaseName = "फिजिकल तैयारी",
                    descriptionHindi = "100m, 800m, लंबी कूद, ऊंची कूद और गोला फेंक (प्रत्येक 20 अंक)।",
                    keyChecklistHindi = listOf(
                        "100m दौड़: <= 12s (20 अंक) | 800m दौड़: <= 2:00 (20 अंक)",
                        "लंबी कूद: >= 5.50m (20 अंक) | ऊंची कूद: >= 1.50m (20 अंक)",
                        "गोला फेंक: >= 9.00m (20 अंक)"
                    ),
                    tipsHindi = "छत्तीसगढ़ पुलिस में फिजिकल के 100 अंक मेरिट में सीधे जुड़ते हैं, अतः 80+ अंक का लक्ष्य रखें।",
                    iconEmoji = "🏃"
                ),
                RoadmapStage(
                    stepNumber = 3,
                    titleHindi = "लिखित परीक्षा पाठ्यक्रम तैयारी (Written Exam Prep)",
                    phaseName = "लिखित परीक्षा",
                    descriptionHindi = "100 अंकों की लिखित परीक्षा: सामान्य ज्ञान, छत्तीसगढ़ विशेष, बुद्धि क्षमता व अंकगणित।",
                    keyChecklistHindi = listOf(
                        "छत्तीसगढ़ का इतिहास, भूगोल, जनजाति व संस्कृति (विशेष भार)",
                        "सामान्य विज्ञान, भारतीय संविधान एवं समसामयिकी",
                        "प्रारम्भिक गणित एवं सामान्य मानसिक योग्यता"
                    ),
                    tipsHindi = "अखाड़ा लाइब्रेरी से छत्तीसगढ़ जीके एवं विगत वर्षों के प्रश्न हल करें।",
                    iconEmoji = "📖"
                ),
                RoadmapStage(
                    stepNumber = 4,
                    titleHindi = "दस्तावेज जांच एवं शारीरिक नापजोख (DV & PST)",
                    phaseName = "दस्तावेज व नाप",
                    descriptionHindi = "आरक्षक भर्ती केंद्रों पर दस्तावेजों का परीक्षण एवं बायोमेट्रिक प्रमाणीकरण।",
                    keyChecklistHindi = listOf(
                        "मूल दस्तावेजों का सत्यापन",
                        "ऊंचाई, सीना (फुलाव सहित) और वजन की आधिकारिक माप",
                        "फिजिकल दक्षता परीक्षा (PET) हेतु चेस्ट नंबर का आवंटन"
                    ),
                    tipsHindi = "सीना फुलाने का नियमित योगाभ्यास करें ताकि 5 सेमी फुलाव सरलता से आ सके।",
                    iconEmoji = "📏"
                ),
                RoadmapStage(
                    stepNumber = 5,
                    titleHindi = "शारीरिक दक्षता परीक्षा (Physical Efficiency Test - 100 Marks)",
                    phaseName = "फिजिकल ग्राउंड",
                    descriptionHindi = "5 स्पर्धाओं में सेंसर और डिजिटल टाइमिंग द्वारा पारदर्शी परीक्षा।",
                    keyChecklistHindi = listOf(
                        "सेंसर आधारित 100 मीटर एवं 800 मीटर रनिंग",
                        "लंबी कूद एवं ऊंची कूद के 3-3 अवसर",
                        "7.26 किग्रा गोला फेंक के 3 अवसर"
                    ),
                    tipsHindi = "फाउल से बचें! पहले प्रयास में सुरक्षित दूरी कूदकर अंक पक्का करें।",
                    iconEmoji = "🎯"
                ),
                RoadmapStage(
                    stepNumber = 6,
                    titleHindi = "लिखित परीक्षा (Written Examination)",
                    phaseName = "लिखित परीक्षा",
                    descriptionHindi = "फिजिकल में सफल उम्मीदवारों की 100 अंकों की 2 घंटे की वस्तुनिष्ठ परीक्षा।",
                    keyChecklistHindi = listOf(
                        "OMR शीट आधारित 100 बहुविकल्पीय प्रश्न",
                        "छत्तीसगढ़ सामान्य ज्ञान पर विशेष फोकस",
                        "समय का प्रभावी प्रबंधन"
                    ),
                    tipsHindi = "प्रत्येक प्रश्न को ध्यान से पढ़ें और OMR शीट भरने में सावधानी बरतें।",
                    iconEmoji = "📝"
                ),
                RoadmapStage(
                    stepNumber = 7,
                    titleHindi = "चिकित्सीय परीक्षण व चरित्र सत्यापन (Medical & Police Verification)",
                    phaseName = "मेडिकल व चरित्र",
                    descriptionHindi = "जिला चिकित्सालय में मेडिकल बोर्ड द्वारा स्वास्थ्य परीक्षण।",
                    keyChecklistHindi = listOf(
                        "दृष्टि परीक्षण (6/6), कलर विजन व श्रवण क्षमता",
                        "स्थानीय थाने द्वारा चरित्र सत्यापन रिपोर्ट",
                        "अंतिम चयन सूची का प्रकाशन"
                    ),
                    tipsHindi = "किसी भी कानूनी विवाद से दूर रहें और अपने आचरण को अनुशासित रखें।",
                    iconEmoji = "🩺"
                ),
                RoadmapStage(
                    stepNumber = 8,
                    titleHindi = "पुलिस ट्रेनिंग स्कूल ज्वाइनिंग (PTS Training & Oath)",
                    phaseName = "प्रशिक्षण व सेवा",
                    descriptionHindi = "माना/राजनांदगांव/मेनपाट पुलिस प्रशिक्षण शाला में 9 माह का आधारभूत प्रशिक्षण।",
                    keyChecklistHindi = listOf(
                        "नियुक्ति पत्र एवं आदेश प्राप्त करना",
                        "शारीरिक, कानूनी व शस्त्र प्रशिक्षण की शुरुआत",
                        "छत्तीसगढ़ शासन की सेवा में शपथ ग्रहण"
                    ),
                    tipsHindi = "अनुशासन और सेवा भावना के साथ अपने क्षेत्र और अखाड़े का मान बढ़ाएं।",
                    iconEmoji = "👮"
                )
            )
        ),

        // 3. SSC GD Constable (CRPF, BSF, CISF, ITBP, SSB)
        ExamRoadmap(
            examId = "ssc_gd",
            examNameHindi = "एसएससी जीडी कांस्टेबल (SSC GD - BSF, CRPF, CISF)",
            organization = "स्टाफ सिलेक्शन कमीशन व गृह मंत्रालय (MHA / CAPFs)",
            eligibilityBrief = "10वीं कक्षा उत्तीर्ण | आयु: 18 से 23 वर्ष | पुरुष: 170 सेमी, महिला: 157 सेमी | 5 KM रनिंग 24 मिनट में",
            stages = listOf(
                RoadmapStage(
                    stepNumber = 1,
                    titleHindi = "पात्रता एवं पद वरीयता चयन (Eligibility & Force Preference)",
                    phaseName = "प्रारंभिक तैयारी",
                    descriptionHindi = "10वीं पास, 18-23 वर्ष आयु तथा CISF, BSF, CRPF, ITBP आदि की वरीयता तय करना।",
                    keyChecklistHindi = listOf(
                        "10वीं बोर्ड परीक्षा किसी भी मान्यता प्राप्त बोर्ड से उत्तीर्ण",
                        "पुरुष ऊंचाई 170 सेमी, महिला 157 सेमी (आरक्षित वर्गों को नियमानुसार छूट)",
                        "पोस्ट वरीयता का सावधानीपूर्वक चयन (CISF > SSB > ITBP > BSF > CRPF)"
                    ),
                    tipsHindi = "अपनी रुचि व कट-ऑफ के आधार पर बलों की सही वरीयता क्रम चुनें।",
                    iconEmoji = "📋"
                ),
                RoadmapStage(
                    stepNumber = 2,
                    titleHindi = "सीबीटी ऑनलाइन परीक्षा तैयारी (CBT Examination Prep)",
                    phaseName = "लिखित तैयारी",
                    descriptionHindi = "80 प्रश्न, 160 अंक, 60 मिनट: रीजनिंग (20), जीके (20), गणित (20), हिंदी/इंग्लिश (20)।",
                    keyChecklistHindi = listOf(
                        "प्रत्येक सही उत्तर के 2 अंक, 0.25 या 0.50 नेगेटिव मार्किंग",
                        "गणित व रीजनिंग में 100% स्कोरिंग की रणनीति",
                        "हिंदी व्याकरण (मुहावरे, पर्यायवाची, विलोम, वर्तनी) में पूरे अंक"
                    ),
                    tipsHindi = "स्पीड और एक्यूरेसी ही मेरिट दिलाती है। प्रतिदिन 1 मॉक टेस्ट अनिवार्य है।",
                    iconEmoji = "📖"
                ),
                RoadmapStage(
                    stepNumber = 3,
                    titleHindi = "कंप्यूटर आधारित परीक्षा (CBT Online Exam)",
                    phaseName = "ऑनलाइन सीबीटी",
                    descriptionHindi = "TCS द्वारा आयोजित राष्ट्रव्यापी ऑनलाइन बहुविकल्पीय परीक्षा।",
                    keyChecklistHindi = listOf(
                        "समय का प्रबंधन: रीजनिंग (15 मि), हिंदी (10 मि), जीके (10 मि), गणित (25 मि)",
                        "कठिन प्रश्नों पर समय न गंवाएं, पहले सरल प्रश्न हल करें",
                        "70+ प्रश्नों का सही प्रयास सुरक्षित स्कोर माना जाता है"
                    ),
                    tipsHindi = "कंप्यूटर स्क्रीन पर रफ शीट का सुव्यवस्थित उपयोग करें।",
                    iconEmoji = "💻"
                ),
                RoadmapStage(
                    stepNumber = 4,
                    titleHindi = "शारीरिक मानक एवं दक्षता परीक्षा (PST & PET)",
                    phaseName = "फिजिकल टेस्ट",
                    descriptionHindi = "पुरुष: 5 किमी दौड़ 24 मिनट में | महिला: 1.6 किमी दौड़ 8:30 मिनट में।",
                    keyChecklistHindi = listOf(
                        "5 किलोमीटर रोड रनिंग (केवल क्वालिफाइंग, कोई अंक नहीं)",
                        "हाइट, चेस्ट और वजन का मानक परीक्षण",
                        "बायोमेट्रिक मिलान"
                    ),
                    tipsHindi = "लंबे कदमों से दौड़ें और पहले 2 किमी में बहुत तेज न भागें, स्थिर पेस रखें।",
                    iconEmoji = "🏃"
                ),
                RoadmapStage(
                    stepNumber = 5,
                    titleHindi = "विस्तृत मेडिकल व दस्तावेज सत्यापन (DME & DV)",
                    phaseName = "मेडिकल चरण",
                    descriptionHindi = "CAPF के संयुक्त चिकित्सा बोर्ड द्वारा विस्तृत चिकित्सीय परीक्षण।",
                    keyChecklistHindi = listOf(
                        "विजन टेस्ट 6/6 बिना चश्मे के",
                        "रक्त व यूरिन जांच, एक्स-रे व ईसीजी",
                        "सभी मूल प्रमाण पत्रों की सूक्ष्म जांच"
                    ),
                    tipsHindi = "मेडिकल से पहले वजन को बीएमआई (BMI) चार्ट के अनुकूल रखें।",
                    iconEmoji = "🩺"
                ),
                RoadmapStage(
                    stepNumber = 6,
                    titleHindi = "ऑल इंडिया मेरिट एवं ट्रेनिंग (All India Merit & RTC Joining)",
                    phaseName = "अंतिम चयन",
                    descriptionHindi = "सीबीटी लिखित परीक्षा के अंकों पर आधारित राज्यवार एवं बलवार मेरिट सूची।",
                    keyChecklistHindi = listOf(
                        "SSC द्वारा अंतिम परिणाम की घोषणा",
                        "आवंटित सुरक्षा बल (BSF/CRPF/CISF) द्वारा ऑफर ऑफ अपॉइंटमेंट",
                        "रिक्रूट ट्रेनिंग सेंटर (RTC) में 44 सप्ताह का कड़ा सैन्य प्रशिक्षण"
                    ),
                    tipsHindi = "देश की सीमाओं और आंतरिक सुरक्षा में सेवा का संकल्प पूर्ण करें। जय हिंद!",
                    iconEmoji = "🇮🇳"
                )
            )
        )
    )
}
