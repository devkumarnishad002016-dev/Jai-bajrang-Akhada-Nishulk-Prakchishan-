package com.example.data.mission

enum class LibraryCategory(val categoryId: String, val titleHindi: String, val icon: String) {
    ALL("ALL", "सभी सामग्री (All)", "📚"),
    NOTES("NOTES", "शॉर्ट नोट्स व सूत्र (Notes)", "📝"),
    PYQ("PYQ", "विगत वर्ष प्रश्न (PYQ)", "📜"),
    PRACTICE_SET("PRACTICE", "प्रैक्टिस सेट (Practice)", "🎯"),
    GK_GS("GK_GS", "महत्वपूर्ण GK / GS सार", "🇮🇳"),
    RECRUITMENT_GUIDE("GUIDE", "भर्ती गाइड व टिप्स", "⭐")
}

data class VillageLibraryItem(
    val id: String,
    val titleHindi: String,
    val subject: String,
    val category: LibraryCategory,
    val targetExam: String, // "All Exams", "Indian Army", "CG Police", "SSC GD"
    val summaryHindi: String,
    val contentMarkdownHindi: String,
    val keyFormulasOrPoints: List<String>,
    val questionsIncludedCount: Int = 0,
    val estimatedReadMinutes: Int = 5,
    val isOfflineAvailable: Boolean = true
)

object VillageDigitalLibraryData {

    fun getAllLibraryItems(): List<VillageLibraryItem> = listOf(
        // 1. NOTES: Number System & Divisibility
        VillageLibraryItem(
            id = "lib_math_01",
            titleHindi = "संख्या पद्धति एवं विभाज्यता के नियम (Number System Core)",
            subject = "गणित (Mathematics)",
            category = LibraryCategory.NOTES,
            targetExam = "All Exams",
            summaryHindi = "प्राकृत, पूर्ण, अभाज्य संख्याएँ तथा 2 से 11 तक विभाज्यता के अचूक नियम।",
            contentMarkdownHindi = """
                # संख्या पद्धति एवं विभाज्यता नियम
                
                ### 1. संख्याओं के प्रकार:
                • **प्राकृत संख्याएं (Natural Numbers):** 1, 2, 3, 4, 5...
                • **पूर्ण संख्याएं (Whole Numbers):** 0, 1, 2, 3...
                • **अभाज्य संख्याएं (Prime Numbers):** 2, 3, 5, 7, 11, 13, 17, 19, 23, 29... (2 एकमात्र सम अभाज्य संख्या है)।
                
                ### 2. विभाज्यता नियम (Divisibility Rules):
                • **2 से:** अंतिम अंक 0, 2, 4, 6, 8 हो।
                • **3 से:** सभी अंकों का योग 3 से पूर्णतः विभाजित हो।
                • **4 से:** अंतिम दो अंकों से बनी संख्या 4 से विभाजित हो।
                • **5 से:** अंतिम अंक 0 या 5 हो।
                • **9 से:** सभी अंकों का योग 9 से विभाजित हो।
                • **11 से:** (विषम स्थानों के अंकों का योग) - (सम स्थानों के अंकों का योग) = 0 या 11 का गुणज।
            """.trimIndent(),
            keyFormulasOrPoints = listOf(
                "भाज्य = (भाजक × भागफल) + शेषफल",
                "प्रथम n प्राकृत संख्याओं का योग = [n(n+1)] / 2",
                "प्रथम n सम संख्याओं का योग = n(n+1)",
                "प्रथम n विषम संख्याओं का योग = n²"
            ),
            questionsIncludedCount = 15,
            estimatedReadMinutes = 6
        ),

        // 2. NOTES: Percentage & Profit-Loss
        VillageLibraryItem(
            id = "lib_math_02",
            titleHindi = "प्रतिशत एवं लाभ-हानि सूत्र संग्रह (Percentage & Profit/Loss)",
            subject = "गणित (Mathematics)",
            category = LibraryCategory.NOTES,
            targetExam = "All Exams",
            summaryHindi = "भर्ती परीक्षा में सबसे ज्यादा पूछे जाने वाले लाभ, हानि एवं बट्टा के सूत्र।",
            contentMarkdownHindi = """
                # प्रतिशत एवं लाभ-हानि सूत्र
                
                • प्रतिशत वृद्धि = [(वृद्धि / प्रारंभिक मान) × 100]%
                • प्रतिशत कमी = [(कमी / प्रारंभिक मान) × 100]%
                • लाभ = विक्रय मूल्य (SP) - क्रय मूल्य (CP)
                • हानि = क्रय मूल्य (CP) - विक्रय मूल्य (SP)
                • लाभ % = (लाभ / CP) × 100
                • हानि % = (हानि / CP) × 100
                • छूट % = (अंकित मूल्य - विक्रय मूल्य) / अंकित मूल्य × 100
            """.trimIndent(),
            keyFormulasOrPoints = listOf(
                "CP = [SP × 100] / (100 + लाभ%)",
                "SP = [CP × (100 + लाभ%)] / 100",
                "दो क्रमागत छूट (x% और y%) की समतुल्य छूट = [x + y - (xy/100)]%"
            ),
            questionsIncludedCount = 20,
            estimatedReadMinutes = 5
        ),

        // 3. PYQ: CG Police Constable Solved Paper
        VillageLibraryItem(
            id = "lib_pyq_cg_01",
            titleHindi = "छत्तीसगढ़ पुलिस आरक्षक विगत वर्ष प्रश्न (CG Police PYQ)",
            subject = "सामान्य ज्ञान व छत्तीसगढ़",
            category = LibraryCategory.PYQ,
            targetExam = "CG Police",
            summaryHindi = "छत्तीसगढ़ पुलिस भर्ती परीक्षा में पूछे गए पिछले वर्षों के 25 महत्वपूर्ण प्रश्न मय व्याख्या।",
            contentMarkdownHindi = """
                # छत्तीसगढ़ पुलिस आरक्षक - विगत परीक्षा प्रश्न
                
                **प्रश्न 1:** छत्तीसगढ़ राज्य का गठन किस तिथि को हुआ था?
                • उत्तर: 1 नवम्बर 2000 (26वां राज्य)।
                
                **प्रश्न 2:** छत्तीसगढ़ की जीवन रेखा किस नदी को कहा जाता है?
                • उत्तर: महानदी (धमतरी के सिहावा पर्वत से उद्गम)।
                
                **प्रश्न 3:** चित्रकोट जलप्रपात किस नदी पर स्थित है?
                • उत्तर: इंद्रावती नदी (बस्तर जिला, भारत का नियाग्रा)।
                
                **प्रश्न 4:** छत्तीसगढ़ में प्रसिद्ध 'भोरमदेव मंदिर' किस जिले में स्थित है?
                • उत्तर: कबीरधाम (कवर्धा) जिला।
                
                **प्रश्न 5:** तीजन बाई किस लोक गायन शैली के लिए विश्व प्रसिद्ध हैं?
                • उत्तर: पंडवानी (पद्म विभूषण से सम्मानित)।
            """.trimIndent(),
            keyFormulasOrPoints = listOf(
                "छत्तीसगढ़ में कुल 33 जिले हैं।",
                "महानदी का उद्गम सिहावा (धमतरी) से होता है।",
                "हसदेव बांगो बांध कोरबा जिले में स्थित है।",
                "गुरु घासीदास राष्ट्रीय उद्यान कोरिया/मनेंद्रगढ़ में स्थित है।"
            ),
            questionsIncludedCount = 25,
            estimatedReadMinutes = 8
        ),

        // 4. PYQ: Indian Army Agniveer GD Paper
        VillageLibraryItem(
            id = "lib_pyq_army_01",
            titleHindi = "इंडियन आर्मी अग्निवीर GD सोल्व्ड पेपर (Army GD PYQ)",
            subject = "जीके व सामान्य विज्ञान",
            category = LibraryCategory.PYQ,
            targetExam = "Indian Army",
            summaryHindi = "आर्मी जीडी कॉमन एंट्रेंस एग्जाम (CEE) के सामान्य ज्ञान एवं सामान्य विज्ञान प्रश्न।",
            contentMarkdownHindi = """
                # आर्मी अग्निवीर जनरल ड्यूटी (GD) मॉडल प्रश्नोत्तर
                
                **प्रश्न 1:** पानीपत की पहली लड़ाई किस वर्ष लड़ी गई थी?
                • उत्तर: 1526 ई. (बाबर और इब्राहिम लोदी के बीच)।
                
                **प्रश्न 2:** भारतीय थल सेना का मुख्यालय कहाँ स्थित है?
                • उत्तर: नई दिल्ली।
                
                **प्रश्न 3:** प्रकाश वर्ष किसका मात्रक है?
                • उत्तर: खगोलीय दूरी का।
                
                **प्रश्न 4:** मानव शरीर की सबसे बड़ी ग्रंथि कौन सी है?
                • उत्तर: यकृत (Liver)।
                
                **प्रश्न 5:** साधारण नमक का रासायनिक सूत्र क्या है?
                • उत्तर: NaCl (सोडियम क्लोराइड)।
            """.trimIndent(),
            keyFormulasOrPoints = listOf(
                "वायु में ध्वनि की चाल लगभग 332 मी/से होती है।",
                "रक्त का pH मान 7.4 (हल्का क्षारीय) होता है।",
                "विटामिन C की कमी से स्कर्वी रोग होता है।",
                "विटामिन D का मुख्य स्रोत सूर्य का प्रकाश है।"
            ),
            questionsIncludedCount = 30,
            estimatedReadMinutes = 7
        ),

        // 5. PRACTICE SET: 50 GK/GS Speed Booster
        VillageLibraryItem(
            id = "lib_practice_01",
            titleHindi = "डिफेंस स्पीड बूस्टर प्रैक्टिस सेट (50 वस्तुनिष्ठ प्रश्न)",
            subject = "मिश्रित अभ्यास (Speed Test)",
            category = LibraryCategory.PRACTICE_SET,
            targetExam = "All Exams",
            summaryHindi = "परीक्षा पैटर्न पर आधारित 50 त्वरित प्रश्न - इतिहास, संविधान, भूगोल व विज्ञान।",
            contentMarkdownHindi = """
                # डिफेंस स्पीड बूस्टर अभ्यास सेट
                
                • भारत का संविधान 26 नवम्बर 1949 को अंगीकार हुआ और 26 जनवरी 1950 को लागू हुआ।
                • प्रारूप समिति के अध्यक्ष डॉ. भीमराव अम्बेडकर थे।
                • भारतीय सेना दिवस प्रतिवर्ष 15 जनवरी को मनाया जाता है।
                • भारत की सबसे लंबी अंतरराष्ट्रीय सीमा बांग्लादेश के साथ है (4,096.7 किमी)।
                • कार्य और ऊर्जा का SI मात्रक जूल (Joule) है।
                • बल का मात्रक न्यूटन (Newton) है।
            """.trimIndent(),
            keyFormulasOrPoints = listOf(
                "संविधान के अनुच्छेद 32 को डॉ. अम्बेडकर ने 'संविधान की आत्मा' कहा।",
                "राष्ट्रपति बनने की न्यूनतम आयु 35 वर्ष है।",
                "राज्यसभा एक स्थायी सदन है, इसके सदस्यों का कार्यकाल 6 वर्ष होता है।"
            ),
            questionsIncludedCount = 50,
            estimatedReadMinutes = 10
        ),

        // 6. GK_GS: Defence Forces & Commands of India
        VillageLibraryItem(
            id = "lib_gk_defence_01",
            titleHindi = "भारतीय सेना, कमान एवं प्रमुख रक्षा तथ्य (Defence Core Facts)",
            subject = "सामान्य ज्ञान (Defence GK)",
            category = LibraryCategory.GK_GS,
            targetExam = "Indian Army",
            summaryHindi = "थल सेना, नौसेना, वायुसेना के कमान मुख्यालय, रैंक संरचना एवं शौर्य पदक।",
            contentMarkdownHindi = """
                # भारतीय सशस्त्र बल - महत्वपूर्ण सामान्य ज्ञान
                
                ### तीनों सेनाओं के सर्वोच्च सेनापति:
                • भारत के माननीय राष्ट्रपति।
                
                ### भारतीय थल सेना की 7 कमानें:
                1. पश्चिमी कमान - चंडीमंदिर
                2. पूर्वी कमान - कोलकाता
                3. उत्तरी कमान - उधमपुर
                4. दक्षिणी कमान - पुणे
                5. केंद्रीय कमान - लखनऊ
                6. दक्षिण-पश्चिमी कमान - जयपुर
                7. आर्मी ट्रेनिंग कमान (ARTRAC) - शिमला
                
                ### सर्वोच्च वीरता पुरस्कार:
                • युद्धकाल: परमवीर चक्र (PVC)
                • शांतिकाल: अशोक चक्र
            """.trimIndent(),
            keyFormulasOrPoints = listOf(
                "थल सेना दिवस: 15 जनवरी",
                "वायु सेना दिवस: 8 अक्टूबर",
                "नौसेना दिवस: 4 दिसंबर",
                "कारगिल विजय दिवस: 26 जुलाई"
            ),
            questionsIncludedCount = 20,
            estimatedReadMinutes = 6
        ),

        // 7. RECRUITMENT_GUIDE: Medical & Document Verification Guide
        VillageLibraryItem(
            id = "lib_guide_01",
            titleHindi = "भर्ती मेडिकल व दस्तावेज सत्यापन संपूर्ण गाइड (Medical & Docs Guide)",
            subject = "भर्ती मार्गदर्शन",
            category = LibraryCategory.RECRUITMENT_GUIDE,
            targetExam = "All Exams",
            summaryHindi = "भर्ती रैली में ले जाने वाले अनिवार्य 10 दस्तावेज एवं मेडिकल जांच की सावधानियां।",
            contentMarkdownHindi = """
                # भर्ती मेडिकल एवं दस्तावेज सत्यापन चेकलिस्ट
                
                ### अनिवार्य मूल दस्तावेज (Original Documents):
                1. 10वीं एवं 12वीं की मूल अंकसूची एवं 3-3 सत्यापित छायाप्रतियां।
                2. मूल निवास प्रमाण पत्र (Domicile Certificate)।
                3. जाति प्रमाण पत्र (Caste Certificate)।
                4. चरित्र प्रमाण पत्र (स्कूल/कॉलेज व सरपंच/पार्षद द्वारा जारी, 6 माह से पुराना न हो)।
                5. आधार कार्ड व पैन कार्ड।
                6. 20 पासपोर्ट साइज हालिया रंगीन फोटो (सफेद पृष्ठभूमि)।
                7. अविवाहित प्रमाण पत्र (गाँव के सरपंच द्वारा जारी)।
                8. NCC / स्पोर्ट्स / ITI प्रमाण पत्र (यदि लागू हो)।
                
                ### मेडिकल जांच की सावधानियां:
                • कानों की सफाई (वैक्स हटाना) किसी योग्य डॉक्टर से कराएं।
                • आंखों का विजन 6/6 मानक पर होना चाहिए।
                • टैटू केवल कोहनी के नीचे आंतरिक भाग पर सीमित धार्मिक मान्यताओं वाला ही मान्य।
                • फ्लैट फुट (सपाट पैर) एवं नॉक-नी (घुटने टकराना) की जांच पहले से करा लें।
            """.trimIndent(),
            keyFormulasOrPoints = listOf(
                "सभी फोटो बिना दाढ़ी और बिना चश्मे के होने चाहिए (सिख कैडेट्स को छोड़कर)।",
                "दस्तावेजों में नाम व पिता के नाम की स्पेलिंग 10वीं की अंकसूची के अनुरूप होनी चाहिए।"
            ),
            questionsIncludedCount = 0,
            estimatedReadMinutes = 5
        )
    )
}
