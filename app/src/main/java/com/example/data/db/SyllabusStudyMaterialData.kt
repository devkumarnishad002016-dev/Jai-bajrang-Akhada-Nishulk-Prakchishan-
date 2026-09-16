package com.example.data.db

import com.example.data.model.Chapter
import com.example.data.model.StudySubject

/**
 * Official Complete Syllabus Data Provider for Jai Bajrang Akhada.
 * Strictly adheres to the exam syllabus topics (CG Police, Army GD, SSC GD, Defence).
 * Contains exactly the 63 chapters requested across 5 subjects with comprehensive study notes,
 * formulas, practice questions count, and cloud PDF download links.
 */
object SyllabusStudyMaterialData {

    fun getSyllabusSubjects(): List<StudySubject> = listOf(
        StudySubject(id = 1, subjectId = "SUB_MATH", name = "गणित (Mathematics)", icon = "📐", displayOrder = 1, isActive = true),
        StudySubject(id = 2, subjectId = "SUB_REASONING", name = "रीजनिंग (Reasoning)", icon = "🧠", displayOrder = 2, isActive = true),
        StudySubject(id = 3, subjectId = "SUB_HINDI", name = "सामान्य हिंदी (Hindi)", icon = "📖", displayOrder = 3, isActive = true),
        StudySubject(id = 4, subjectId = "SUB_GK", name = "सामान्य ज्ञान व विज्ञान (GK / GS)", icon = "🇮🇳", displayOrder = 4, isActive = true),
        StudySubject(id = 5, subjectId = "SUB_ENGLISH", name = "अंग्रेजी (English Language)", icon = "🔤", displayOrder = 5, isActive = true),
        StudySubject(id = 6, subjectId = "SUB_COMPUTER", name = "कंप्यूटर ज्ञान (Computer)", icon = "💻", displayOrder = 6, isActive = true)
    )

    fun getAllSyllabusChapters(): List<Chapter> {
        val list = mutableListOf<Chapter>()
        list.addAll(getMathChapters())
        list.addAll(getReasoningChapters())
        list.addAll(getHindiChapters())
        list.addAll(getGkChapters())
        list.addAll(getEnglishChapters())
        list.addAll(getComputerChapters())
        return list
    }

    private fun getMathChapters(): List<Chapter> = listOf(
        Chapter(
            id = 1,
            subjectName = "Mathematics",
            chapterNumber = 1,
            chapterName = "संख्या पद्धति (Number System)",
            chapterOrder = 1,
            description = "संख्याओं का वर्गीकरण, स्थानीय/जातीय मान, योग सूत्र, अभाज्य/भाज्य, विभाज्यता (2-99), इकाई अंक, शेषफल प्रमेय, शून्य की संख्या, गुणनखंड व आवर्त दशमलव",
            notesContent = """
                📘 संपूर्ण संख्या पद्धति सार-संग्रह (Complete Number System Handbook)
                ========================================================================

                1. संख्याओं का वर्गीकरण (Classification of Numbers):
                • प्राकृत संख्याएँ (Natural Numbers - N): गिनती की संख्याएँ N = {1, 2, 3, 4, 5, ...}। सबसे छोटी प्राकृत संख्या = 1।
                • पूर्ण संख्याएँ (Whole Numbers - W): शून्य सहित प्राकृत संख्याएँ W = {0, 1, 2, 3, ...}। सबसे छोटी पूर्ण संख्या = 0। (सभी प्राकृत संख्याएँ पूर्ण संख्याएँ होती हैं, किन्तु सभी पूर्ण संख्याएँ प्राकृत नहीं होतीं क्योंकि 0 प्राकृत नहीं है)।
                • पूर्णांक संख्याएँ (Integers - Z): Z = {..., -3, -2, -1, 0, 1, 2, 3, ...}। 
                  - धनात्मक पूर्णांक: {1, 2, 3...}, ऋणात्मक पूर्णांक: {-1, -2, -3...}
                  - 0 (शून्य): न तो धनात्मक है और न ही ऋणात्मक, यह उदासीन (Neutral) पूर्णांक है तथा सम संख्या है।
                • सम संख्याएँ (Even Numbers): 2 से पूर्णतः विभाज्य संख्याएँ (..., -4, -2, 0, 2, 4, 6, 8, ...)। व्यापक रूप = 2n।
                • विषम संख्याएँ (Odd Numbers): 2 से पूर्णतः विभाजित न होने वाली संख्याएँ (..., -3, -1, 1, 3, 5, 7, ...)। व्यापक रूप = 2n ± 1।
                • भाज्य संख्याएँ (Composite Numbers): वे प्राकृत संख्याएँ जिनके 2 से अधिक गुणनखंड (Factors) हों। (उदा: 4, 6, 8, 9, 10, 12, ...)।
                  - सबसे छोटी भाज्य संख्या = 4
                  - सबसे छोटी विषम भाज्य संख्या = 9
                  - 1 न तो भाज्य है और न ही अभाज्य संख्या है।
                  - 1 से 100 तक कुल 74 भाज्य संख्याएँ होती हैं (100 - 25 अभाज्य - 1 = 74)।
                • अभाज्य संख्याएँ (Prime Numbers): 1 से बड़ी वे संख्याएँ जिनके केवल 2 गुणनखंड (1 और स्वयं) होते हैं।
                  - 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97
                  - 2 एकमात्र सम अभाज्य संख्या (Even Prime) है और सबसे छोटी अभाज्य संख्या भी है।
                  - 1 से 25 तक: 9 अभाज्य संख्याएँ
                  - 1 से 50 तक: 15 अभाज्य संख्याएँ
                  - 50 से 100 तक: 10 अभाज्य संख्याएँ
                  - 1 से 100 तक: कुल 25 अभाज्य संख्याएँ (याद रखने की ट्रिक: 44 22 32 23 21)
                  - 3 अंकों की सबसे छोटी अभाज्य संख्या = 101, 2 अंकों की सबसे बड़ी अभाज्य = 97।
                • सह-अभाज्य संख्याएँ (Co-Prime Numbers): दो संख्याएँ जिनका महत्तम समापवर्तक (HCF) 1 हो (उदा: 8 और 15, 9 और 16)। आवश्यक नहीं कि वे स्वयं अभाज्य हों।
                • जुड़वां अभाज्य (Twin Primes): ऐसी दो अभाज्य संख्याएँ जिनका अंतर 2 हो (उदा: 3-5, 5-7, 11-13, 17-19, 29-31, 41-43, 59-61, 71-73)।
                • परिमेय संख्याएँ (Rational Numbers - Q): जिन्हें p/q के रूप में लिखा जा सके (q ≠ 0 तथा p, q पूर्णांक हैं)। उदा: 3/5, -7/2, 0 (0/1), 0.7̅, 4 आदि।
                • अपरिमेय संख्याएँ (Irrational Numbers): जिन्हें p/q रूप में न लिखा जा सके (अशांत व अनावर्ती दशमलव प्रसार)। उदा: √2, √3, √5, π।
                  - ध्यान दें: π अपरिमेय है, किन्तु 22/7 परिमेय संख्या है।
                  - परिमेय + अपरिमेय = सदैव अपरिमेय।
                • वास्तविक संख्याएँ (Real Numbers - R): परिमेय तथा अपरिमेय संख्याओं का संयुक्त समूह।

                2. स्थानीय मान (Place Value) एवं जातीय मान (Face Value):
                • जातीय मान (Face/Absolute Value): अंक का वास्तविक मान जो कभी नहीं बदलता (उदा: 5472 में 7 का जातीय मान = 7)।
                • स्थानीय मान (Place Value): संख्या में अंक की स्थिति के अनुसार मान।
                  - इकाई = अंक × 1, दहाई = अंक × 10, सैकड़ा = अंक × 100, हजार = अंक × 1000...
                  - उदा: 5472 में 4 का स्थानीय मान = 4 × 100 = 400।

                3. महत्वपूर्ण योग सूत्र (Important Summation Formulas):
                • प्रथम n प्राकृत संख्याओं का योग = [n(n + 1)] / 2
                • प्रथम n सम संख्याओं का योग = n(n + 1)
                • प्रथम n विषम संख्याओं का योग = n²
                • प्रथम n प्राकृत संख्याओं के वर्गों का योग = [n(n + 1)(2n + 1)] / 6
                • प्रथम n प्राकृत संख्याओं के घनों का योग = [[n(n + 1)] / 2]²
                • समांतर श्रेणी (AP) का nवाँ पद Tn = a + (n - 1)d
                • समांतर श्रेणी का योग Sn = (n / 2)[2a + (n - 1)d] = (n / 2)[पहला पद + अंतिम पद]

                4. विभाज्यता के अचूक नियम (Rules of Divisibility):
                • 2 से विभाज्यता: इकाई का अंक 0, 2, 4, 6, 8 हो।
                • 3 से विभाज्यता: सभी अंकों का योग 3 से विभाज्य हो।
                • 4 से विभाज्यता: संख्या के अंतिम 2 अंकों से बनी संख्या 4 से विभाज्य हो या अंतिम दोनों 00 हों।
                • 5 से विभाज्यता: इकाई का अंक 0 या 5 हो।
                • 6 से विभाज्यता: संख्या 2 और 3 दोनों से एक साथ विभाज्य हो (सम संख्या जिसका योग 3 से कटे)।
                • 7 से विभाज्यता: इकाई अंक का दोगुना शेष बची संख्या में से घटाने पर प्राप्त परिणाम 0 या 7 का गुणज हो।
                • 8 से विभाज्यता: अंतिम 3 अंकों से बनी संख्या 8 से विभाज्य हो या अंतिम तीन 000 हों।
                • 9 से विभाज्यता: सभी अंकों का योग 9 से विभाज्य हो।
                • 10 से विभाज्यता: इकाई का अंक 0 हो।
                • 11 से विभाज्यता: (विषम स्थानों के अंकों का योग) - (सम स्थानों के अंकों का योग) = 0 या 11 का गुणज हो।
                • संयुक्त संख्याओं के नियम: 
                  - 12 से विभाज्यता: 3 और 4 दोनों से विभाज्य हो।
                  - 72 से विभाज्यता: 8 और 9 दोनों से विभाज्य हो (8×9=72, HCF=1)।
                  - 88 से विभाज्यता: 8 और 11 दोनों से विभाज्य हो।
                  - 99 से विभाज्यता: 9 और 11 दोनों से विभाज्य हो।
                • विशेष नियम: किसी भी एक अंक की 6 बार पुनरावृत्ति से बनी संख्या (उदा: 111111, 777777) सदैव 3, 7, 11, 13 और 37 से पूर्णतः विभाजित होती है।

                5. इकाई का अंक (Unit Digit) ज्ञात करने के सार्वभौमिक नियम:
                • नियम 1: 0, 1, 5, 6 की घात चाहे कुछ भी हो, इकाई अंक क्रमशः 0, 1, 5, 6 ही रहता है।
                • नियम 2: अंक 4 और 9 के लिए:
                  - 4 की विषम घात = 4, सम घात = 6
                  - 9 की विषम घात = 9, सम घात = 1
                • नियम 3: अंक 2, 3, 7, 8 के लिए (चक्रीयता = 4):
                  - घात को 4 से भाग दें और शेषफल को नई घात बना लें।
                  - यदि शेषफल = 0 आए, तो घात 4 ली जाती है।
                  - (2⁴ का इकाई अंक 6, 3⁴ का 1, 7⁴ का 1, 8⁴ का 6)।
                • फैक्टोरियल में इकाई अंक: 5! (120) और उसके आगे के सभी फैक्टोरियल का इकाई अंक सदैव 0 होता है।

                6. शेषफल प्रमेय (Remainder Theorem) एवं विभाजन कलन:
                • भाज्य = (भाजक × भागफल) + शेषफल (Dividend = Divisor × Quotient + Remainder)
                • जब किसी संख्या को लगातार d₁ और d₂ से भाग देने पर शेषफल r₁ और r₂ बचें, तो:
                  संख्या का रूप = d₁d₂k + d₁r₂ + r₁।
                • बीजगणितीय शेषफल नियम:
                  - (xⁿ - aⁿ) सदैव (x - a) से पूर्णतः विभाज्य होता है (n के सभी मानों के लिए)।
                  - (xⁿ - aⁿ) सदैव (x + a) से विभाज्य होता है जब n एक सम संख्या हो।
                  - (xⁿ + aⁿ) सदैव (x + a) से विभाज्य होता है जब n एक विषम संख्या हो।
                • (a - 1)ⁿ को a से भाग देने पर शेषफल: n सम होने पर +1 तथा n विषम होने पर (a - 1) बचता है।

                7. अनुगामी शून्य (Trailing Zeroes) ज्ञात करना:
                • शून्य सदैव 2 और 5 के जोड़े (2 × 5 = 10) से बनते हैं।
                • फैक्टोरियल n! में शून्यों की संख्या = ⌊n/5⌋ + ⌊n/25⌋ + ⌊n/125⌋ + ...
                • उदा: 100! में शून्य = 100/5 + 100/25 = 20 + 4 = 24 शून्य।
                • केवल विषम संख्याओं के गुणनफल (1 × 3 × 5 × ... × 99) में कोई 2 न होने के कारण शून्यों की संख्या 0 होती है।

                8. गुणनखंडों की संख्या एवं उनका योग (Factors / Divisors):
                • माना किसी संख्या N का अभाज्य गुणनखंड रूप N = pᵃ × qᵇ × rᶜ है:
                  - कुल गुणनखंडों की संख्या = (a + 1)(b + 1)(c + 1)
                  - विषम गुणनखंडों की संख्या = 2 की घात को छोड़कर बाकी घातों में 1 जोड़कर गुणा करें।
                  - सम गुणनखंडों की संख्या = कुल गुणनखंड - विषम गुणनखंड
                  - सभी गुणनखंडों का योग = (p⁰ + p¹ + ... + pᵃ)(q⁰ + q¹ + ... + qᵇ)...
                  - पूर्ण वर्ग संख्याओं के कुल गुणनखंडों की संख्या सदैव विषम (Odd) होती है।

                9. आवर्त दशमलव (Recurring Decimals) को भिन्न में बदलना:
                • शुद्ध आवर्त: 0.x̅ = x/9, 0.xy̅ = xy/99, 0.xyz̅ = xyz/999
                • मिश्रित आवर्त: 0.ab̅ = (ab - a) / 90
                • 0.abc̅ = (abc - ab) / 900
                • 0.ab(cd)̅ = (abcd - ab) / 9900

                10. परीक्षा में बार-बार पूछे जाने वाले विशेष सूत्र:
                • सिर और पैर वाले प्रश्न:
                  - 4 पैर वाले जानवर = (कुल पैर / 2) - कुल सिर
                  - 2 पैर वाले पक्षी = कुल सिर - 4 पैर वाले जानवर
                • हाथ मिलाने वाले प्रश्न: n व्यक्तियों द्वारा हाथ मिलाने की संख्या = [n(n - 1)] / 2
                • उपहार (Gift) देने वाले प्रश्न: n व्यक्तियों द्वारा उपहार बांटने की कुल संख्या = n(n - 1)
                • दो संख्याओं का योग S और अंतर D हो तो:
                  - बड़ी संख्या = (S + D) / 2
                  - छोटी संख्या = (S - D) / 2
            """.trimIndent(),
            fileUrl = "https://jaybajrangakhada.org/materials/math_ch01_number_system.pdf",
            fileName = "01_Number_System_Math_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 1,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Number System Complete Concept & Tricks",
            videoUrl = "https://youtube.com/results?search_query=number+system+defence+maths",
            practiceQuestionsCount = 35,
            isCompleted = true,
            progressPercentage = 100,
            isTodayTarget = true
        ),
        Chapter(
            id = 2,
            subjectName = "Mathematics",
            chapterNumber = 2,
            chapterName = "सरलीकरण (Simplification & BODMAS)",
            chapterOrder = 2,
            description = "VBODMAS नियम, कोष्ठक हल करना, बीजगणितीय सूत्र, वर्ग, वर्गमूल, घन, घनमूल, घातांक व करणी के संपूर्ण नियम",
            notesContent = """
                📘 संपूर्ण सरलीकरण सार-संग्रह (Simplification & Algebraic Identities)
                ========================================================================

                1. VBODMAS नियम का सही क्रम (Order of Operations):
                • V (Vinculum / Bar): रेखा कोष्ठक (उदा: 8 - 5̅ - 2̅ = 8 - 3 = 5)। सबसे पहले बार हल करें।
                • B (Brackets): कोष्ठक हल करने का क्रम:
                  - 1. रेखा कोष्ठक: (—)
                  - 2. छोटा कोष्ठक: ( ) Circular Bracket
                  - 3. मंझला कोष्ठक: { } Curly Bracket
                  - 4. बड़ा कोष्ठक: [ ] Square Bracket
                • O (Of / Orders): 'का' का अर्थ गुणा होता है, जिसे भाग और गुणा से पहले हल किया जाता है (उदा: 20 ÷ 5 का 2 = 20 ÷ 10 = 2)।
                • D (Division): भाग की संक्रिया (÷)
                • M (Multiplication): गुणा की संक्रिया (×)
                • A (Addition): योग की संक्रिया (+)
                • S (Subtraction): घटाव की संक्रिया (-)
                (याद रखें: एक साथ कई भाग आने पर बाईं ओर से दाईं ओर हल करें: a ÷ b ÷ c = (a / b) / c = a / (b × c))

                2. महत्वपूर्ण बीजगणितीय सर्वसमिकाएं (Algebraic Identities):
                • (a + b)² = a² + 2ab + b²
                • (a - b)² = a² - 2ab + b²
                • a² - b² = (a - b)(a + b)
                • (a + b)² + (a - b)² = 2(a² + b²)
                • (a + b)² - (a - b)² = 4ab  =>  [(a + b)² - (a - b)²] / (ab) = 4
                • (a + b)³ = a³ + b³ + 3ab(a + b)
                • (a - b)³ = a³ - b³ - 3ab(a - b)
                • a³ + b³ = (a + b)(a² - ab + b²)  =>  (a³ + b³) / (a² - ab + b²) = a + b
                • a³ - b³ = (a - b)(a² + ab + b²)  =>  (a³ - b³) / (a² + ab + b²) = a - b
                • a³ + b³ + c³ - 3abc = (a + b + c)(a² + b² + c² - ab - bc - ca)
                • यदि a + b + c = 0 हो, तो a³ + b³ + c³ = 3abc (परीक्षा में सर्वाधिक पूछा जाने वाला नियम)।

                3. वर्ग एवं वर्गमूल निकालने की त्वरित ट्रिक्स (Square & Roots):
                • 1 से 30 तक वर्ग: 1²=1, 2²=4, 3²=9, 4²=16, 5²=25, 6²=36, 7²=49, 8²=64, 9²=81, 10²=100, 11²=121, 12²=144, 13²=169, 14²=196, 15²=225, 16²=256, 17²=289, 18²=324, 19²=361, 20²=400, 21²=441, 22²=484, 23²=529, 24²=576, 25²=625.
                • इकाई अंक 5 वाली संख्या का वर्ग: (N5)² = [N × (N + 1)] | 25
                  - उदा: 75² = 7 × 8 | 25 = 5625; 95² = 9 × 10 | 25 = 9025.
                • किसी पूर्ण वर्ग संख्या के इकाई अंक पर कभी भी 2, 3, 7 या 8 नहीं हो सकता।
                • अंकों की संख्या: यदि संख्या में n अंक हैं, तो वर्गमूल में अंकों की संख्या n सम होने पर n/2 तथा n विषम होने पर (n + 1)/2 होगी।

                4. घन एवं घनमूल (Cubes & Cube Roots):
                • 1³=1, 2³=8, 3³=27, 4³=64, 5³=125, 6³=216, 7³=343, 8³=512, 9³=729, 10³=1000, 11³=1331, 12³=1728, 15³=3375.
                • इकाई अंक नियम: 1->1, 4->4, 5->5, 6->6, 9->9, 0->0; केवल 2<->8 और 3<->7 आपस में बदलते हैं।

                5. घातांक एवं करणी (Indices & Surds):
                • aᵐ × aⁿ = aᵐ⁺ⁿ, aᵐ ÷ aⁿ = aᵐ⁻ⁿ, (aᵐ)ⁿ = aᵐⁿ, a⁰ = 1, a⁻ⁿ = 1/aⁿ.
                • √(x√(x√(x...))) अनन्त तक = x
                • √(x + √(x + √(x...))) अनन्त तक: यदि x के क्रमागत गुणनखंड n(n + 1) हों, तो उत्तर बड़ा मान (n + 1) होगा (उदा: √(20 + √(20...)) = 5)।
                • √(x - √(x - √(x...))) अनन्त तक: उत्तर छोटा मान n होगा (उदा: √(20 - √(20...)) = 4)।
                • √(x√(x√(x...))) n बार = x^[(2ⁿ - 1) / 2ⁿ]।
            """.trimIndent(),
            fileUrl = "https://jaybajrangakhada.org/materials/math_ch02_simplification.pdf",
            fileName = "02_Simplification_Math_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 2,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "BODMAS & Simplification Crash Course",
            videoUrl = "https://youtube.com/results?search_query=simplification+maths+tricks",
            practiceQuestionsCount = 30,
            isCompleted = true,
            progressPercentage = 90,
            isTodayTarget = false
        ),
        Chapter(
            id = 3,
            subjectName = "Mathematics",
            chapterNumber = 3,
            chapterName = "भिन्न एवं दशमलव (Fractions & Decimals)",
            chapterOrder = 3,
            description = "उचित, अनुचित, मिश्रित व लंगड़ी भिन्न, आवर्त दशमलव, भिन्नों की तुलना व आरोही/अवरोही क्रम",
            notesContent = """
                📘 संपूर्ण भिन्न एवं दशमलव सार-संग्रह (Fractions & Decimals Handbook)
                ========================================================================

                1. भिन्नों का वर्गीकरण (Classification of Fractions):
                • उचित भिन्न (Proper Fraction): अंश < हर (उदा: 2/3, 5/7, 11/15)। इनका मान सदैव 1 से कम होता है।
                • अनुचित भिन्न (Improper Fraction): अंश > हर (उदा: 7/4, 9/5, 13/8)। इनका मान सदैव 1 से अधिक होता है।
                • मिश्रित भिन्न (Mixed Fraction): पूर्णांक एवं उचित भिन्न का योग (उदा: 2¾ = 2 + 3/4 = 11/4)।
                • व्युत्क्रम भिन्न (Reciprocal Fraction): a/b का व्युत्क्रम b/a होता है (गुणनफल = 1)।
                • वितत भिन्न / लंगड़ी भिन्न (Continued Fraction): 1 + 1/(1 + 1/(1 + ...))। इन्हें सदैव नीचे से ऊपर की ओर हल किया जाता है।

                2. भिन्नों की तुलना (Comparison of Fractions - बड़ी/छोटी भिन्न पहचानना):
                • विधि 1: तिर्यक गुणा विधि (Cross Multiplication):
                  - 3/7 और 4/9 की तुलना: 3 × 9 = 27, 7 × 4 = 28। चूँकि 28 > 27, अतः 4/9 > 3/7।
                • विधि 2: अंतर समान होने का नियम:
                  - यदि उचित भिन्नों के अंश और हर का अंतर समान हो, तो सबसे बड़े अंश वाली भिन्न सबसे बड़ी होगी (उदा: 15/17, 19/21, 23/25 में 23/25 सबसे बड़ी है)।
                • विधि 3: शून्य लगाकर भाग विधि:
                  - सभी अंशों में 0 लगाकर हर से भाग देकर दशमलव मान से तुलना करें।

                3. आवर्त दशमलव भिन्न (Recurring / Repeating Decimals):
                • शुद्ध आवर्त दशमलव (Pure Periodic):
                  - 0.x̅ = x / 9 (उदा: 0.7̅ = 7/9)
                  - 0.xy̅ = xy / 99 (उदा: 0.35̅ = 35/99)
                  - 0.xyz̅ = xyz / 999 (उदा: 0.123̅ = 123/999 = 41/333)
                • मिश्रित आवर्त दशमलव (Mixed Periodic):
                  - 0.ab̅ = (ab - a) / 90 (उदा: 0.47̅ = (47 - 4) / 90 = 43/90)
                  - 0.abc̅ = (abc - ab) / 900 (उदा: 0.125̅ = (125 - 12) / 900 = 113/900)
                  - 0.ab(cd)̅ = (abcd - ab) / 9900 (उदा: 0.3572̅ = (3572 - 35) / 9900 = 3537/9900)

                4. शांत एवं अशांत दशमलव (Terminating vs Non-Terminating):
                • किसी भिन्न का दशमलव प्रसार तभी शांत होगा जब उसके सरलतम रूप में हर के अभाज्य गुणनखंड केवल 2ᵐ × 5ⁿ के रूप में हों।
                • यदि हर में 2 और 5 के अतिरिक्त कोई अन्य अभाज्य गुणनखंड (3, 7, 11 आदि) उपस्थित हो, तो वह अशांत-आवर्ती होगा।
            """.trimIndent(),
            fileUrl = "https://jaybajrangakhada.org/materials/math_ch03_fractions.pdf",
            fileName = "03_Fractions_Decimals_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 3,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Fractions and Decimals Easy Methods",
            videoUrl = "https://youtube.com/results?search_query=fractions+decimals+maths",
            practiceQuestionsCount = 25,
            isCompleted = false,
            progressPercentage = 60,
            isTodayTarget = false
        ),
        Chapter(
            id = 4,
            subjectName = "Mathematics",
            chapterNumber = 4,
            chapterName = "ल.स.प. एवं म.स.प. (LCM & HCF)",
            chapterOrder = 4,
            description = "गुणनखंड व भाग विधि, भिन्नों व दशमलव का LCM/HCF, शेषफल आधारित नियम, घंटियां व वृत्ताकार धावक",
            notesContent = """
                📘 संपूर्ण ल.स.प. एवं म.स.प. सार-संग्रह (Complete LCM & HCF Handbook)
                ========================================================================

                1. परिभाषा एवं संकल्पना:
                • महत्तम समापवर्तक (HCF - Highest Common Factor): वह बड़ी से बड़ी संख्या जो दी गई सभी संख्याओं को पूर्णतः विभाजित कर दे।
                • लघुत्तम समापवर्त्य (LCM - Lowest Common Multiple): वह छोटी से छोटी संख्या जो दी गई सभी संख्याओं से पूर्णतः विभाजित हो जाए।

                2. मूलभूत सूत्र एवं सम्बन्ध:
                • पहली संख्या × दूसरी संख्या = LCM × HCF
                • दो संख्याओं का गुणनफल सदैव उनके LCM और HCF के गुणनफल के बराबर होता है।
                • यदि दो संख्याओं का अनुपात a : b हो और उनका HCF = H हो:
                  - संख्याएँ = Ha और Hb
                  - उनका LCM = H × a × b
                  - सह-अभाज्य संख्याओं का HCF = 1 तथा LCM = a × b।
                • HCF सदैव LCM का एक पूर्ण गुणनखंड (Factor) होता है।

                3. भिन्नों एवं दशमलव का LCM व HCF:
                • भिन्नों का LCM = (अंशों का LCM) / (हरों का HCF)
                • भिन्नों का HCF = (अंशों का HCF) / (हरों का LCM)
                • दशमलव संख्याओं का: दशमलव स्थान समान कर साधारण संख्याओं की भांति निकालें और अंत में दशमलव लगाएँ।
                • घातांकों का:
                  - समान आधार होने पर LCM = सबसे बड़ी घात (उदा: 2³, 2⁵, 2⁸ का LCM = 2⁸)
                  - समान आधार होने पर HCF = सबसे छोटी घात (उदा: 2³, 2⁵, 2⁸ का HCF = 2³)

                4. परीक्षा में पूछे जाने वाले 4 प्रमुख शेषफल प्रारूप (Golden Rules):
                • प्रारूप 1 (बड़ी से बड़ी संख्या - समान शेषफल r):
                  - वह बड़ी से बड़ी संख्या जिससे x, y, z को भाग देने पर प्रत्येक दशा में r शेष बचे = HCF(x - r, y - r, z - r)
                • प्रारूप 2 (बड़ी से बड़ी संख्या - शेषफल अज्ञात पर समान):
                  - = HCF(|x - y|, |y - z|, |z - x|)
                • प्रारूप 3 (छोटी से छोटी संख्या - समान शेषफल r):
                  - वह छोटी से छोटी संख्या जिसे x, y, z से भाग देने पर r शेष बचे = [LCM(x, y, z) × k] + r
                • प्रारूप 4 (छोटी से छोटी संख्या - अलग-अलग शेषफल):
                  - यदि x, y, z से भाग देने पर क्रमशः a, b, c शेष बचे और (x - a = y - b = z - c = d समान हो):
                  - अभीष्ट संख्या = [LCM(x, y, z)] - d

                5. ट्रैफिक बत्ती, घंटी एवं वृत्ताकार पथ प्रश्न:
                • अलग-अलग समय अंतरालों (t₁, t₂, t₃...) पर बजने वाली घंटियों के एक साथ बजने का समय = LCM(t₁, t₂, t₃)।
                • किसी निश्चित समय T में बजने की कुल संख्या = [T / LCM] + 1 (प्रारंभिक बार जोड़कर)।
            """.trimIndent(),
            fileUrl = "https://jaybajrangakhada.org/materials/math_ch04_lcm_hcf.pdf",
            fileName = "04_LCM_and_HCF_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 4,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "LCM and HCF Complete Tricks",
            videoUrl = "https://youtube.com/results?search_query=lcm+hcf+shortcuts",
            practiceQuestionsCount = 30,
            isCompleted = false,
            progressPercentage = 50,
            isTodayTarget = true
        ),
        Chapter(
            id = 5,
            subjectName = "Mathematics",
            chapterNumber = 5,
            chapterName = "अनुपात, समानुपात एवं साझेदारी (Ratio, Proportion & Partnership)",
            chapterOrder = 5,
            description = "मध्यानुपाती, तृतीयानुपाती, चतुर्थानुपाती, सिक्कों व थैली वाले प्रश्न, आयु सम्बन्धी अनुपात व साझेदारी",
            notesContent = """
                📘 संपूर्ण अनुपात, समानुपात एवं साझेदारी सार-संग्रह (Ratio & Proportion)
                ========================================================================

                1. अनुपात के मूल नियम (Rules of Ratio):
                • यदि A : B = a : b तथा B : C = c : d हो, तो A : B : C = (a × c) : (b × c) : (b × d)
                  (शॉर्टकट: खाली जगह को उसके ठीक बगल वाली संख्या से भरें)।
                • यदि 2A = 3B = 4C हो, तो A : B : C = 1/2 : 1/3 : 1/4 = 6 : 4 : 3 (LCM 12 से गुणा करने पर)।
                • विलोमानुपात (Inverse Ratio): a : b का विलोमानुपात b : a; तथा a : b : c का 1/a : 1/b : 1/c होता है।

                2. समानुपाती पद (Proportionality Formulas):
                • समानुपात नियम: a : b :: c : d  =>  a × d = b × c (बाह्य पदों का गुणनफल = मध्य पदों का गुणनफल)।
                • मध्यानुपाती (Mean Proportional of a and b) = √(a × b)
                • तृतीयानुपाती (Third Proportional of a and b) = b² / a
                • चतुर्थानुपाती (Fourth Proportional of a, b, c) = (b × c) / a

                3. सिक्कों एवं रुपयों वाले प्रश्न (Coins in a Bag):
                • कुल मान = (सिक्कों की संख्या) × (प्रत्येक सिक्के का मूल्य)
                • ₹1 = 1 रुपया, 50 पैसे = ₹1/2, 25 पैसे = ₹1/4, 20 पैसे = ₹1/5, 10 पैसे = ₹1/10।
                • यदि सिक्कों का अनुपात दिया हो, तो उन्हें रुपयों में बदलकर कुल रुपयों के बराबर रखें।
                • यदि कुल रुपयों का अनुपात दिया हो, तो सिक्कों की संख्या में बदलकर कुल सिक्कों के बराबर रखें।

                4. आयु सम्बन्धी प्रश्न (Problems on Ages):
                • दो व्यक्तियों की आयु का अंतर सदैव समान रहता है (समय बीतने पर अंतर नहीं बदलता)।
                • अनुपातों के अंतर को समान करें (तिर्यक अंतर से गुणा करके), फिर 1 इकाई का मान ज्ञात करें।

                5. साझेदारी (Partnership):
                • लाभ का अनुपात = (पूँजी₁ × समय₁) : (पूँजी₂ × समय₂) : (पूँजी₃ × समय₃)
                • यदि पूँजी समान हो, तो लाभ समय के अनुपात में बँटता है।
                • यदि समय समान हो, तो लाभ पूँजी के अनुपात में बँटता है।
                • सक्रिय साझेदार (Working Partner) को प्रबंधन हेतु दिया जाने वाला वेतन कुल लाभ में से पहले घटाया जाता है।
            """.trimIndent(),
            fileUrl = "https://jaybajrangakhada.org/materials/math_ch05_ratio_proportion.pdf",
            fileName = "05_Ratio_and_Proportion_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 5,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Ratio & Proportion Masterclass",
            videoUrl = "https://youtube.com/results?search_query=ratio+and+proportion+police+maths",
            practiceQuestionsCount = 35,
            isCompleted = false,
            progressPercentage = 40,
            isTodayTarget = false
        ),
        Chapter(
            id = 6,
            subjectName = "Mathematics",
            chapterNumber = 6,
            chapterName = "प्रतिशतता (Percentage)",
            chapterOrder = 6,
            description = "प्रतिशत-भिन्न तुल्यता, क्रमिक प्रतिशत, मूल्य-खपत नियम, जनसंख्या, परीक्षा एवं चुनाव आधारित सभी ट्रिक्स",
            notesContent = """
                📘 संपूर्ण प्रतिशतता सार-संग्रह (Complete Percentage Handbook)
                ========================================================================

                1. प्रतिशत-भिन्न तुल्यता तालिका (Fraction to Percentage Table - कंठस्थ रखें):
                • 1 = 100%               • 1/2 = 50%              • 1/3 = 33⅓% (33.33%)
                • 1/4 = 25%              • 1/5 = 20%              • 1/6 = 16⅔% (16.66%)
                • 1/7 = 14²⁄₇% (14.28%)   • 1/8 = 12½% (12.5%)     • 1/9 = 11¹⁄₉% (11.11%)
                • 1/10 = 10%             • 1/11 = 9¹⁄₁₁% (9.09%)   • 1/12 = 8⅓% (8.33%)
                • 1/15 = 6⅔% (6.66%)     • 1/16 = 6¼% (6.25%)     • 1/20 = 5%
                • 1/25 = 4%              • 3/8 = 37.5%            • 5/8 = 62.5%

                2. क्रमिक प्रतिशत परिवर्तन (Successive Percentage Formula):
                • कुल प्रतिशत परिवर्तन = [A + B + (A × B) / 100]%
                  - वृद्धि होने पर धनात्मक (+) चिह्न लें, कमी होने पर ऋणात्मक (-) चिह्न लें।
                • यदि किसी वस्तु का मान पहले x% बढ़ाया जाए और फिर x% घटाया जाए (या पहले घटाया फिर बढ़ाया):
                  - परिणाम में सदैव कमी होती है: कमी % = x² / 100 % (उदा: 20% वृद्धि व 20% कमी => 20²/100 = 4% कमी)।

                3. मूल्य, खपत एवं व्यय नियम (Price, Consumption & Expenditure):
                • व्यय = मूल्य × खपत
                • यदि किसी वस्तु (चीनी, पेट्रोल आदि) के मूल्य में r% वृद्धि हो जाए, तो व्यय अपरिवर्तित रखने हेतु:
                  - खपत में कमी % = [r / (100 + r)] × 100%
                • यदि मूल्य में r% की कमी हो जाए, तो व्यय अपरिवर्तित रखने हेतु:
                  - खपत में वृद्धि % = [r / (100 - r)] × 100%

                4. जनसंख्या एवं मूल्यह्रास (Population & Depreciation):
                • यदि वर्तमान मान P हो और वार्षिक वृद्धि दर r% हो:
                  - n वर्ष बाद मान = P × (1 + r/100)ⁿ
                  - n वर्ष पूर्व मान = P / (1 + r/100)ⁿ
                • यदि मशीन का अवमूल्यन (Depreciation) हो रहा हो, तो चिन्ह (-) आएगा: P × (1 - r/100)ⁿ।

                5. परीक्षा एवं चुनाव ट्रिक्स (Exam & Election Shortcuts):
                • परीक्षा में उत्तीर्ण न्यूनतम अंक = (पूर्णांक × उत्तीर्ण%) / 100
                • दो विषयों में उत्तीर्ण/अनुत्तीर्ण (वेन आरेख):
                  - कुल उत्तीर्ण % = उत्तीर्ण(A) + उत्तीर्ण(B) - दोनों में उत्तीर्ण
                • चुनाव सम्बन्धी:
                  - हार-जीत का अंतर = (विजेता के मत% - पराजित के मत%) × कुल वैध मत।
            """.trimIndent(),
            fileUrl = "https://jaybajrangakhada.org/materials/math_ch06_percentage.pdf",
            fileName = "06_Percentage_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 6,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Percentage Zero to Hero Class",
            videoUrl = "https://youtube.com/results?search_query=percentage+shortcuts+maths",
            practiceQuestionsCount = 40,
            isCompleted = false,
            progressPercentage = 60,
            isTodayTarget = true
        ),
        Chapter(
            id = 7,
            subjectName = "Mathematics",
            chapterNumber = 7,
            chapterName = "लाभ, हानि एवं बट्टा (Profit, Loss & Discount)",
            chapterOrder = 7,
            description = "क्रय-विक्रय मूल्य, समतुल्य बट्टा, बेईमान दुकानदार ट्रिक्स, क्रय मूल्य व अंकित मूल्य का स्वर्णिम अनुपात",
            notesContent = """
                📘 संपूर्ण लाभ, हानि एवं बट्टा सार-संग्रह (Profit, Loss & Discount)
                ========================================================================

                1. मूलभूत शब्दावली एवं सूत्र:
                • क्रय मूल्य (Cost Price - CP): वस्तु को खरीदने में लगा कुल धन।
                • विक्रय मूल्य (Selling Price - SP): वस्तु को बेचने पर प्राप्त धन।
                • अंकित मूल्य (Marked Price - MP): वस्तु पर छपा हुआ मूल्य।
                • लाभ = SP - CP (जब SP > CP)
                • हानि = CP - SP (जब CP > SP)
                • लाभ % = (लाभ / CP) × 100
                • हानि % = (हानि / CP) × 100 (नोट: लाभ व हानि की गणना सदैव CP पर होती है!)
                • SP = CP × [(100 + लाभ%) / 100]  अथवा  CP × [(100 - हानि%) / 100]
                • CP = SP × [100 / (100 + लाभ%)]  अथवा  SP × [100 / (100 - हानि%)]

                2. बट्टा / छूट (Discount) एवं समतुल्य बट्टा:
                • छूट सदैव अंकित मूल्य (MP) पर दी जाती है: छूट = MP - SP; छूट % = (छूट / MP) × 100।
                • SP = MP × [(100 - छूट%) / 100]।
                • दो क्रमिक छूटों (d₁% और d₂%) की एकल समतुल्य छूट:
                  - समतुल्य बट्टा = [d₁ + d₂ - (d₁ × d₂) / 100]% (उदा: 20% व 10% => 20 + 10 - 2 = 28%)।
                • तीन क्रमिक छूटों (d₁, d₂, d₃) का मान = 100 - 100 × (1 - d₁/100) × (1 - d₂/100) × (1 - d₃/100)।

                3. CP और MP का स्वर्णिम सम्बन्ध (Golden Ratio CP : MP):
                • CP : MP = (100 - छूट%) : (100 + लाभ%)
                  - यदि 10% छूट देकर 20% लाभ कमाना हो, तो CP : MP = (100 - 10) : (100 + 20) = 90 : 120 = 3 : 4।

                4. परीक्षा के महत्वपूर्ण विशेष नियम (Special Exam Cases):
                • समान विक्रय मूल्य पर समान लाभ व हानि:
                  - जब दो वस्तुओं में प्रत्येक को समान SP पर बेचा जाए, एक पर x% लाभ तथा दूसरी पर x% हानि हो:
                  - पूरे व्यापार में सदैव हानि होती है = x² / 100 % हानि।
                • x वस्तुओं का क्रय मूल्य = y वस्तुओं का विक्रय मूल्य:
                  - लाभ/हानि % = [(x - y) / y] × 100% (+ मान आए तो लाभ, - आए तो हानि)।
                • बेईमान दुकानदार (Dishonest Shopkeeper):
                  - यदि 1 किग्रा के स्थान पर g ग्राम का बाँट प्रयुक्त करे:
                  - लाभ % = [(सत्य मान - गलत मान) / गलत मान] × 100 = [(1000 - g) / g] × 100।
            """.trimIndent(),
            fileUrl = "https://jaybajrangakhada.org/materials/math_ch07_profit_loss.pdf",
            fileName = "07_Profit_Loss_Discount_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 7,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Profit Loss and Discount Complete Tricks",
            videoUrl = "https://youtube.com/results?search_query=profit+loss+discount+tricks",
            practiceQuestionsCount = 35,
            isCompleted = false,
            progressPercentage = 30,
            isTodayTarget = false
        ),
        Chapter(
            id = 8,
            subjectName = "Mathematics",
            chapterNumber = 8,
            chapterName = "साधारण ब्याज (Simple Interest)",
            chapterOrder = 8,
            description = "मूलधन, ब्याज दर, समय, n-गुनी राशि के नियम, दर व समय समान होने पर ट्रिक, किश्त (Installment)",
            notesContent = """
                📘 संपूर्ण साधारण ब्याज सार-संग्रह (Simple Interest Handbook)
                ========================================================================

                1. आधारभूत सूत्र:
                • साधारण ब्याज (SI) = (P × R × T) / 100
                  (P = मूलधन / Principal, R = वार्षिक दर / Rate %, T = समय वर्ष में / Time)
                • मिश्रधन (A - Amount) = P + SI = P × [1 + (R × T) / 100]
                • P = (SI × 100) / (R × T)
                • R = (SI × 100) / (P × T)
                • T = (SI × 100) / (P × R)
                (विशेष: साधारण ब्याज प्रत्येक वर्ष का समान रहता है, क्योंकि यह केवल मूलधन पर लगता है।)

                2. गुनी होने वाली राशि की त्वरित ट्रिक्स (Times / Multiplier Trick):
                • यदि कोई धन साधारण ब्याज की दर से T वर्षों में n गुना हो जाता है:
                  - दर R = [(n - 1) × 100] / T %
                • यदि कोई धन R% वार्षिक दर से कितने समय में n गुना हो जाएगा:
                  - समय T = [(n - 1) × 100] / R वर्ष
                  - (उदा: कोई धन 8 वर्ष में 3 गुना हो, तो दर = (3 - 1) × 100 / 8 = 200/8 = 25% वार्षिक)।
                • दो अलग-अलग समय एवं गुने का सम्बन्ध:
                  - T₁ / T₂ = (n₁ - 1) / (n₂ - 1)
                  - (उदा: 5 वर्ष में 2 गुना, तो कितने वर्ष में 4 गुना होगा? => 5 / T₂ = (2-1)/(4-1) = 1/3 => T₂ = 15 वर्ष)।

                3. जब दर और समय संख्यात्मक रूप से समान हों (R = T):
                • यदि किसी धन पर साधारण ब्याज, मूलधन का a/b भाग हो तथा R = T हो:
                  - दर R = समय T = √[(a/b) × 100]
                  - (उदा: SI = 9/16 मूलधन, तो R = √(900/16) = 30/4 = 7.5% या 7 वर्ष 6 माह)।

                4. साधारण ब्याज की वार्षिक किश्त (Installment in SI):
                • देय ऋण A को T वर्षों में r% वार्षिक दर से चुकता करने हेतु प्रत्येक वार्षिक किश्त:
                  - वार्षिक किश्त = (100 × A) / [100T + {r × T × (T - 1) / 2}]
            """.trimIndent(),
            fileUrl = "https://jaybajrangakhada.org/materials/math_ch08_simple_interest.pdf",
            fileName = "08_Simple_Interest_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 8,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Simple Interest Short Tricks",
            videoUrl = "https://youtube.com/results?search_query=simple+interest+maths",
            practiceQuestionsCount = 25,
            isCompleted = false,
            progressPercentage = 20,
            isTodayTarget = false
        ),
        Chapter(
            id = 9,
            subjectName = "Mathematics",
            chapterNumber = 9,
            chapterName = "चक्रवृद्धि ब्याज (Compound Interest)",
            chapterOrder = 9,
            description = "अर्धवार्षिक व त्रैमासिक संयोजन, 2 व 3 वर्ष का CI-SI अंतर, ट्री विधि, अनुपात विधि, n-गुनी राशि",
            notesContent = """
                📘 संपूर्ण चक्रवृद्धि ब्याज सार-संग्रह (Compound Interest Handbook)
                ========================================================================

                1. आधारभूत सूत्र:
                • मिश्रधन (A) = P × (1 + R / 100)ᵀ
                • चक्रवृद्धि ब्याज (CI) = A - P = P × [(1 + R / 100)ᵀ - 1]

                2. संयोजन की शर्तें (Compounding Frequencies):
                • वार्षिक (Annually): दर = R%, समय = T वर्ष।
                • अर्धवार्षिक / छमाही (Half-Yearly): दर आधी हो जाती है (R / 2) तथा समय दोगुना हो जाता है (2T)।
                • त्रैमासिक / तिमाही (Quarterly): दर चौथाई हो जाती है (R / 4) तथा समय चार गुना हो जाता है (4T)।

                3. CI और SI का अंतर (Difference between CI and SI - अति महत्वपूर्ण):
                • 2 वर्षों के लिए अंतर:
                  - अंतर (D₂) = P × (R / 100)²
                • 3 वर्षों के लिए अंतर:
                  - अंतर (D₃) = P × (R / 100)² × [3 + R / 100] = [P × R² × (300 + R)] / 100³
                  - शॉर्टकट: D₃ / D₂ = 3 + R/100 (सीधे दर निकालने हेतु)।

                4. ट्री मेथड एवं स्वर्णिम अनुपात (Golden Ratios):
                • 2 वर्ष के लिए अनुपात = 2 : 1
                  - CI = 2 × (P का R%) + 1 × (ब्याज का R%)
                • 3 वर्ष के लिए अनुपात = 3 : 3 : 1
                  - CI = 3 × A + 3 × B + 1 × C (जहाँ A = P का R%, B = A का R%, C = B का R%)

                5. गुने वाले प्रश्न (Multiplication Rule in CI):
                • चक्रवृद्धि ब्याज में धन गुणोत्तर श्रेणी (G.P.) में बढ़ता है:
                  - यदि कोई धन T वर्षों में n गुना हो जाता है, तो nᵏ गुना होने में (k × T) वर्ष लगेंगे!
                  - (उदा: कोई धन 3 वर्ष में 2 गुना होता है, तो 8 गुना (= 2³) होने में समय = 3 × 3 = 9 वर्ष लगेंगे)।
            """.trimIndent(),
            fileUrl = "https://jaybajrangakhada.org/materials/math_ch09_compound_interest.pdf",
            fileName = "09_Compound_Interest_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 9,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Compound Interest Tree Method",
            videoUrl = "https://youtube.com/results?search_query=compound+interest+tricks",
            practiceQuestionsCount = 30,
            isCompleted = false,
            progressPercentage = 10,
            isTodayTarget = false
        ),
        Chapter(
            id = 10,
            subjectName = "Mathematics",
            chapterNumber = 10,
            chapterName = "औसत (Average)",
            chapterOrder = 10,
            description = "प्राकृत, सम व विषम संख्याओं का औसत, समूह में नए सदस्य का आना/जाना, क्रिकेट बल्लेबाजी व गेंदबाजी औसत",
            notesContent = """
                📘 संपूर्ण औसत सार-संग्रह (Complete Average Handbook)
                ========================================================================

                1. आधारभूत नियम:
                • औसत (Average) = सभी प्रेक्षणों का कुल योग / प्रेक्षणों की कुल संख्या
                • कुल योग = औसत × प्रेक्षणों की संख्या

                2. विशेष संख्याओं के औसत सूत्र:
                • प्रथम n प्राकृत संख्याओं का औसत = (n + 1) / 2
                • प्रथम n पूर्ण संख्याओं का औसत = (n - 1) / 2
                • प्रथम n सम संख्याओं का औसत = n + 1 (उदा: प्रथम 50 सम संख्याओं का औसत = 51)
                • प्रथम n विषम संख्याओं का औसत = n (उदा: प्रथम 25 विषम संख्याओं का औसत = 25)
                • लगातार समांतर श्रेणी (A.P.) या समान अंतर वाली संख्याओं का औसत:
                  - औसत = (पहला पद + अंतिम पद) / 2
                • प्रथम n प्राकृत संख्याओं के वर्गों का औसत = [(n + 1)(2n + 1)] / 6
                • प्रथम n प्राकृत संख्याओं के घनों का औसत = [n(n + 1)²] / 4

                3. समूह में किसी के आने या जाने पर (Inclusion / Exclusion Trick):
                • नया सदस्य आने पर:
                  - नए सदस्य का मान = नया औसत ± (कुल संख्या × औसत में वृद्धि/कमी)
                • सदस्य जाने पर:
                  - जाने वाले का मान = पुराना औसत ∓ (शेष संख्या × औसत में वृद्धि/कमी)
                • किसी के स्थान पर नया आने पर (प्रतिस्थापन / Replacement):
                  - नए का मान = जाने वाले का मान ± (कुल संख्या × औसत में परिवर्तन)

                4. क्रिकेट आधारित औसत (Cricket Batting & Bowling Average):
                • बल्लेबाजी औसत = कुल बनाए गए रन / कुल आउट पारियाँ (नाबाद पारियों को नहीं गिना जाता)।
                  - नए मैच के बाद नया औसत: नई पारी के रन = नया औसत + (पुरानी पारियाँ × औसत वृद्धि)।
                • गेंदबाजी औसत = दिए गए कुल रन / लिए गए कुल विकेट।
                  - गेंदबाजी औसत कम होना अच्छा प्रदर्शन माना जाता है।
            """.trimIndent(),
            fileUrl = "https://jaybajrangakhada.org/materials/math_ch10_average.pdf",
            fileName = "10_Average_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 10,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Average Best Shortcuts",
            videoUrl = "https://youtube.com/results?search_query=average+maths+shortcuts",
            practiceQuestionsCount = 25,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        ),
        Chapter(
            id = 11,
            subjectName = "Mathematics",
            chapterNumber = 11,
            chapterName = "समय, कार्य एवं नल-टंकी (Time, Work & Pipes)",
            chapterOrder = 11,
            description = "कार्य क्षमता (Efficiency), MDH मसाला सूत्र, एकांतर दिन (Alternate Days), नल एवं टंकी रिसाव",
            notesContent = """
                📘 संपूर्ण समय एवं कार्य तथा नल-टंकी सार-संग्रह (Time, Work & Pipes)
                ========================================================================

                1. ल.स.प. विधि एवं कार्यक्षमता (LCM Method & Efficiency):
                • कुल कार्य = दिए गए दिनों/घंटों का LCM
                • 1 दिन का कार्य (कार्यक्षमता / Efficiency) = कुल कार्य / कुल दिन
                • समय और कार्यक्षमता एक-दूसरे के व्युत्क्रमानुपाती होते हैं (Time ∝ 1 / Efficiency)।
                • यदि A किसी कार्य को x दिन में तथा B उसे y दिन में करे:
                  - दोनों मिलकर करेंगे = (x × y) / (x + y) दिन
                • यदि A, B, C क्रमशः x, y, z दिन में करें:
                  - तीनों मिलकर करेंगे = (x × y × z) / (xy + yz + zx) दिन

                2. MDH मसाला सूत्र (Man-Days-Hours Formula - अति महत्वपूर्ण):
                • (M₁ × D₁ × H₁ × E₁) / W₁ = (M₂ × D₂ × H₂ × E₂) / W₂
                  - M = व्यक्तियों की संख्या (Men/Women)
                  - D = दिनों की संख्या (Days)
                  - H = प्रतिदिन कार्य के घंटे (Hours per day)
                  - E = कार्यक्षमता (Efficiency)
                  - W = किया गया कुल कार्य, बनाई गई वस्तुएं, खाई गई सामग्री अथवा अर्जित मजदूरी (Work / Wages)

                3. एकांतर दिन कार्य (Alternate Days Work):
                • जब A और B बारी-बारी से कार्य करें:
                  - 2 दिन के एक पूरे चक्र (Cycle) का कार्य निकालें = (Efficiency_A + Efficiency_B)
                  - चक्र को कुल कार्य के निकटतम गुणज तक ले जाएँ, शेष कार्य को अगले की बारी पर करवाएं।

                4. नल एवं टंकी (Pipes and Cisterns):
                • भरने वाला पाइप (Inlet): धनात्मक कार्य (+ve Efficiency)
                • खाली करने वाला पाइप / रिसाव (Outlet / Leakage): ऋणात्मक कार्य (-ve Efficiency)
                • यदि नल A, x घंटे में भरे और नल B, y घंटे में खाली करे (x < y):
                  - टंकी भरने में लगा समय = (x × y) / (y - x) घंटे।
            """.trimIndent(),
            fileUrl = "https://jaybajrangakhada.org/materials/math_ch11_time_work.pdf",
            fileName = "11_Time_and_Work_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 11,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Time & Work / Pipes & Cistern Masterclass",
            videoUrl = "https://youtube.com/results?search_query=time+and+work+defence+maths",
            practiceQuestionsCount = 35,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        ),
        Chapter(
            id = 12,
            subjectName = "Mathematics",
            chapterNumber = 12,
            chapterName = "समय, चाल, दूरी, रेलगाड़ी व नाव (Speed, Distance, Trains & Boats)",
            chapterOrder = 12,
            description = "चाल रूपान्तरण, औसत चाल, सापेक्ष चाल, रेलगाड़ी (खंभा/प्लेटफॉर्म), नाव एवं धारा के संपूर्ण नियम",
            notesContent = """
                📘 संपूर्ण समय, चाल, दूरी, रेलगाड़ी व नाव-धारा सार-संग्रह (TSD & Trains)
                ========================================================================

                1. आधारभूत सूत्र एवं इकाई रूपान्तरण:
                • चाल (Speed) = दूरी / समय; दूरी = चाल × समय; समय = दूरी / चाल
                • km/h से m/s में बदलना: चाल × (5 / 18)
                • m/s से km/h में बदलना: चाल × (18 / 5)

                2. औसत चाल (Average Speed):
                • औसत चाल = तय की गई कुल दूरी / लगा कुल समय
                • जब दो समान दूरियाँ क्रमशः x km/h और y km/h की चाल से तय की जाएँ:
                  - औसत चाल = (2xy) / (x + y) km/h
                • जब तीन समान दूरियाँ x, y, z की चाल से तय की जाएँ:
                  - औसत चाल = (3xyz) / (xy + yz + zx) km/h

                3. सापेक्ष चाल (Relative Speed):
                • विपरीत दिशा में (Opposite Directions - आमने-सामने): सापेक्ष चाल = u + v
                • समान दिशा में (Same Direction - एक ही दिशा): सापेक्ष चाल = |u - v|

                4. रेलगाड़ी सम्बन्धी नियम (Problems on Trains):
                • स्थिति 1 (नगण्य लंबाई वाली वस्तु पार करना - खंभा, पेड़, खड़ा व्यक्ति):
                  - कुल दूरी = केवल ट्रेन की लंबाई (L)
                  - समय = L / चाल
                • स्थिति 2 (लंबाई वाली वस्तु पार करना - प्लेटफॉर्म, पुल, सुरंग, खड़ी ट्रेन):
                  - कुल दूरी = ट्रेन की लंबाई + प्लेटफॉर्म की लंबाई (L₁ + L₂)
                  - समय = (L₁ + L₂) / चाल
                • स्थिति 3 (दो गतिशील ट्रेनों का एक-दूसरे को पार करना):
                  - कुल दूरी = L₁ + L₂
                  - समय = (L₁ + L₂) / (सापेक्ष चाल)

                5. नाव एवं धारा (Boats & Streams):
                • शांत जल में नाव की चाल = u km/h, धारा / नदी की चाल = v km/h
                • धारा की दिशा में चाल (Downstream - अनुप्रवाह): D = u + v
                • धारा की विपरीत चाल (Upstream - ऊर्ध्वप्रवाह): U = u - v
                • शांत जल में नाव की चाल u = (D + U) / 2
                • धारा की चाल v = (D - U) / 2
            """.trimIndent(),
            fileUrl = "https://jaybajrangakhada.org/materials/math_ch12_speed_distance.pdf",
            fileName = "12_Time_Speed_Distance_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 12,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Speed Time Distance Train and Boat Tricks",
            videoUrl = "https://youtube.com/results?search_query=speed+time+distance+trains",
            practiceQuestionsCount = 35,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        ),
        Chapter(
            id = 13,
            subjectName = "Mathematics",
            chapterNumber = 13,
            chapterName = "क्षेत्रमिति 2D (Mensuration 2D - Area & Perimeter)",
            chapterOrder = 13,
            description = "त्रिभुज, आयत, वर्ग, समचतुर्भुज, समलम्ब, वृत्त, त्रिज्यखंड व रास्ते के क्षेत्रफल व परिमाप सूत्र",
            notesContent = """
                📘 संपूर्ण क्षेत्रमिति 2D सार-संग्रह (Mensuration 2D Handbook)
                ========================================================================

                1. त्रिभुज (Triangles):
                • साधारण त्रिभुज: क्षेत्रफल = 1/2 × आधार × ऊँचाई
                • समबाहु त्रिभुज (Equilateral): क्षेत्रफल = (√3 / 4) × a²; ऊँचाई = (√3 / 2) × a; परिमाप = 3a
                • विषमबाहु त्रिभुज (हीरो का सूत्र): s = (a + b + c) / 2; क्षेत्रफल = √[s(s - a)(s - b)(s - c)]
                • समकोण त्रिभुज (Right angled): क्षेत्रफल = 1/2 × आधार × लम्ब; कर्ण² = लम्ब² + आधार² (पाइथागोरस ट्रिपलेट्स: 3-4-5, 5-12-13, 7-24-25, 8-15-17, 9-40-41, 20-21-29)।
                • समद्विबाहु त्रिभुज (Isosceles): क्षेत्रफल = (b / 4) × √(4a² - b²) (जहाँ a = समान भुजा, b = आधार)

                2. चतुर्भुज (Quadrilaterals):
                • आयत (Rectangle):
                  - क्षेत्रफल = l × b; परिमाप = 2(l + b); विकर्ण = √(l² + b²)
                  - आयत के बाहर x चौड़ा रास्ता: रास्ते का क्षेत्रफल = 2x(l + b + 2x)
                  - आयत के अंदर x चौड़ा रास्ता: रास्ते का क्षेत्रफल = 2x(l + b - 2x)
                • वर्ग (Square):
                  - क्षेत्रफल = a² = d² / 2 (d = विकर्ण); परिमाप = 4a; विकर्ण = a√2
                • समचतुर्भुज (Rhombus):
                  - क्षेत्रफल = 1/2 × d₁ × d₂; परिमाप = 4a; सम्बन्ध: 4a² = d₁² + d₂²
                • समांतर चतुर्भुज (Parallelogram): क्षेत्रफल = आधार × ऊँचाई
                • समलम्ब चतुर्भुज (Trapezium): क्षेत्रफल = 1/2 × (समांतर भुजाओं का योग) × (बीच की लम्बवत् दूरी)

                3. वृत्त एवं त्रिज्यखंड (Circle & Sector):
                • पूर्ण वृत्त: क्षेत्रफल = πr² (π ≈ 22/7); परिधि = 2πr (r = 7 होने पर परिधि = 44, क्षेत्रफल = 154)
                • अर्धवृत्त (Semi-Circle): क्षेत्रफल = (πr²) / 2; परिमाप = πr + 2r = (36 / 7) × r
                • त्रिज्यखंड (Sector of Angle θ):
                  - चाप की लंबाई (l) = (θ / 360) × 2πr
                  - क्षेत्रफल = (θ / 360) × πr² = 1/2 × l × r
                • पहिए द्वारा n चक्करों में तय दूरी = n × 2πr
            """.trimIndent(),
            fileUrl = "https://jaybajrangakhada.org/materials/math_ch13_mensuration_2d.pdf",
            fileName = "13_Mensuration_2D_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 13,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "2D Mensuration Complete Formulas & Tricks",
            videoUrl = "https://youtube.com/results?search_query=mensuration+2d+formulas",
            practiceQuestionsCount = 30,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        ),
        Chapter(
            id = 14,
            subjectName = "Mathematics",
            chapterNumber = 14,
            chapterName = "क्षेत्रमिति 3D (Mensuration 3D - Volume & Surface)",
            chapterOrder = 14,
            description = "घन, घनाभ, बेलन, शंकु, गोला, अर्धगोला के आयतन, वक्रपृष्ठ, सम्पूर्ण पृष्ठ व पिघलाने के नियम",
            notesContent = """
                📘 संपूर्ण क्षेत्रमिति 3D सार-संग्रह (Mensuration 3D Handbook)
                ========================================================================

                1. घन (Cube):
                • आयतन = a³
                • वक्र / पार्श्व पृष्ठीय क्षेत्रफल (4 दीवारें) = 4a²
                • सम्पूर्ण पृष्ठीय क्षेत्रफल = 6a²
                • सबसे लंबा विकर्ण = a√3

                2. घनाभ (Cuboid - माचिस, कमरा):
                • आयतन = l × b × h
                • कमरे की चारों दीवारों का क्षेत्रफल = 2h(l + b)
                • सम्पूर्ण पृष्ठीय क्षेत्रफल = 2(lb + bh + hl)
                • कमरे में रखी जा सकने वाली सबसे लंबी छड़ (विकर्ण) = √(l² + b² + h²)

                3. लम्बवृत्तीय बेलन (Right Circular Cylinder):
                • आयतन = πr²h
                • वक्रपृष्ठीय क्षेत्रफल (Curved Surface Area - CSA) = 2πrh
                • सम्पूर्ण पृष्ठीय क्षेत्रफल (Total Surface Area - TSA) = 2πr(h + r)
                • खोखला बेलन आयतन = π(R² - r²)h

                4. लम्बवृत्तीय शंकु (Right Circular Cone):
                • तिर्यक ऊँचाई (Slant Height) l = √(r² + h²)
                • आयतन = 1/3 πr²h (बेलन का एक-तिहाई)
                • वक्रपृष्ठीय क्षेत्रफल (CSA) = πrl
                • सम्पूर्ण पृष्ठीय क्षेत्रफल (TSA) = πr(l + r)

                5. गोला एवं अर्धगोला (Sphere & Hemisphere):
                • ठोस गोला:
                  - आयतन = 4/3 πr³
                  - पृष्ठीय क्षेत्रफल = 4πr²
                • अर्धगोला:
                  - आयतन = 2/3 πr³
                  - वक्रपृष्ठीय क्षेत्रफल (CSA) = 2πr²
                  - सम्पूर्ण पृष्ठीय क्षेत्रफल (TSA) = 3πr² (ऊपरी वृत्ताकार ढक्कन πr² सहित)

                6. पिघलाने एवं पुनर्गठन के नियम (Melting & Recasting):
                • जब किसी ठोस को पिघलाकर दूसरी आकृतियाँ बनाई जाती हैं, तो कुल आयतन अपरिवर्तित रहता है।
                • बनने वाली छोटी गोलियों/वस्तुओं की संख्या = (बड़ी मूल वस्तु का आयतन) / (एक छोटी वस्तु का आयतन)।
            """.trimIndent(),
            fileUrl = "https://jaybajrangakhada.org/materials/math_ch14_mensuration_3d.pdf",
            fileName = "14_Mensuration_3D_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 14,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "3D Mensuration All Formulas",
            videoUrl = "https://youtube.com/results?search_query=mensuration+3d+formulas",
            practiceQuestionsCount = 25,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        ),
        Chapter(
            id = 15,
            subjectName = "Mathematics",
            chapterNumber = 15,
            chapterName = "आंकड़ों का विश्लेषण (Data Interpretation - DI)",
            chapterOrder = 15,
            description = "वृत्त आरेख (Pie Chart), दंड आरेख (Bar Graph), रेखा आरेख (Line Graph) एवं तालिका (Tabular)",
            notesContent = """
                📘 संपूर्ण आँकड़ों का विश्लेषण सार-संग्रह (Data Interpretation Handbook)
                ========================================================================

                1. वृत्त आरेख (Pie Chart) के महत्वपूर्ण सूत्र:
                • पूर्ण वृत्त का कोण = 360°
                • पूर्ण प्रतिशत = 100%
                • अतः 100% = 360°  =>  1% = 3.6°
                • डिग्री को प्रतिशत में बदलना: प्रतिशत % = (डिग्री मान / 360) × 100%
                • प्रतिशत को डिग्री में बदलना: केंद्रीय कोण = (प्रतिशत मान / 100) × 360°
                  - (उदा: 25% = 90°; 20% = 72°; 10% = 36°; 5% = 18°)।

                2. बार ग्राफ एवं रेखा आरेख (Bar Graph & Line Graph):
                • प्रतिशत वृद्धि / कमी:
                  - % वृद्धि = [(नया मान - पुराना मान) / पुराना मान] × 100%
                  - % कमी = [(पुराना मान - नया मान) / पुराना मान] × 100%
                • किसी वर्ष के संदर्भ में तुलना:
                  - A, B का कितना प्रतिशत है = (A / B) × 100%
                  - A, B से कितना % अधिक है = [(A - B) / B] × 100%
                  - A, B से कितना % कम है = [(B - A) / B] × 100%

                3. तालिका (Tabulation Analysis):
                • पंक्ति (Row) एवं स्तंभ (Column) के योग की त्वरित विधियाँ
                • औसत मान = सभी वर्षों या मदों का कुल योग / मदों की संख्या
                • अनुपात विधि द्वारा बड़े आँकड़ों का त्वरित सरलीकरण।
            """.trimIndent(),
            fileUrl = "https://jaybajrangakhada.org/materials/math_ch15_data_interpretation.pdf",
            fileName = "15_Data_Interpretation_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 15,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Data Interpretation Masterclass",
            videoUrl = "https://youtube.com/results?search_query=data+interpretation+pie+chart",
            practiceQuestionsCount = 20,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        )
    )

    private fun getReasoningChapters(): List<Chapter> = listOf(
        Chapter(
            id = 16,
            subjectName = "Reasoning",
            chapterNumber = 1,
            chapterName = "सादृश्यता परीक्षण (Analogy)",
            chapterOrder = 1,
            description = "शब्द सादृश्यता, संख्या सादृश्यता, अक्षर सादृश्यता, राज्य-राजधानी, मात्रक-इकाई",
            notesContent = "• देश : राजधानी (भारत : नई दिल्ली :: ऑस्ट्रेलिया : कैनबरा)\n• उपकरण : मापन (थर्मामीटर : तापमान :: बैरोमीटर : वायुदाब)\n• संख्या सम्बन्ध: वर्ग, घन, गुणा, भाज्य-अभाज्य सम्बन्धी तर्क।",
            fileUrl = "https://jaybajrangakhada.org/materials/reasoning_ch01_analogy.pdf",
            fileName = "01_Analogy_Reasoning_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 16,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Analogy Complete Concept",
            videoUrl = "https://youtube.com/results?search_query=analogy+reasoning+tricks",
            practiceQuestionsCount = 35,
            isCompleted = true,
            progressPercentage = 100,
            isTodayTarget = true
        ),
        Chapter(
            id = 17,
            subjectName = "Reasoning",
            chapterNumber = 2,
            chapterName = "वर्गीकरण (Classification)",
            chapterOrder = 2,
            description = "विषम शब्द, विषम संख्या, विषम अक्षर समूह पहचानना (Odd One Out)",
            notesContent = "• 4 विकल्पों में से 3 एक समान गुण दर्शाते हैं, 1 भिन्न होता है।\n• अभाज्य संख्याएँ, सम-विषम, स्वर (Vowels - A E I O U), सार्थक-निरर्थक शब्द।",
            fileUrl = "https://jaybajrangakhada.org/materials/reasoning_ch02_classification.pdf",
            fileName = "02_Classification_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 17,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Odd One Out Classification Tricks",
            videoUrl = "https://youtube.com/results?search_query=classification+reasoning",
            practiceQuestionsCount = 30,
            isCompleted = false,
            progressPercentage = 50,
            isTodayTarget = false
        ),
        Chapter(
            id = 18,
            subjectName = "Reasoning",
            chapterNumber = 3,
            chapterName = "कोडिंग-डिकोडिंग (Coding-Decoding)",
            chapterOrder = 3,
            description = "लेटर कोडिंग, नंबर कोडिंग, विपरीत अक्षर (Opposite Letters), चाइनीज कोडिंग",
            notesContent = "• EJOTY: E=5, J=10, O=15, T=20, Y=25.\n• विपरीत अक्षर ट्रिक: A-Z (AZAD), B-Y (BOY), C-X (CRUX), D-W (DEW), E-V (EVENING), F-U (FULL), G-T (GT ROAD), H-S (HIGH SCHOOL), I-R (INDIAN RAILWAY), J-Q (JACK QUEEN), K-P (KURTA PAIJAMA), L-O (LOVE), M-N (MAN).\n• दो विपरीत अक्षरों के स्थान मान का योग सदैव 27 होता है।",
            fileUrl = "https://jaybajrangakhada.org/materials/reasoning_ch03_coding_decoding.pdf",
            fileName = "03_Coding_Decoding_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 18,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Coding Decoding Full Tricks",
            videoUrl = "https://youtube.com/results?search_query=coding+decoding+tricks",
            practiceQuestionsCount = 40,
            isCompleted = true,
            progressPercentage = 90,
            isTodayTarget = true
        ),
        Chapter(
            id = 19,
            subjectName = "Reasoning",
            chapterNumber = 4,
            chapterName = "श्रृंखला परीक्षण (Series)",
            chapterOrder = 4,
            description = "संख्या श्रृंखला (Number Series), अक्षर श्रृंखला (Alphabet Series), पुनरावृत्त अक्षर श्रृंखला (Repeating Series)",
            notesContent = "• अंतर (Step Difference), वर्ग (Square) व घन (Cube) श्रृंखला।\n• अल्टरनेटिव (एकान्तर) श्रृंखला: एक पद छोड़कर सम्बन्ध देखना।\n• रिपीटेड पैटर्न: अक्षरों को 3-3 या 4-4 के समूहों में बांटकर पैटर्न पकड़ना।",
            fileUrl = "https://jaybajrangakhada.org/materials/reasoning_ch04_series.pdf",
            fileName = "04_Series_Test_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 19,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Number and Alphabet Series Secrets",
            videoUrl = "https://youtube.com/results?search_query=number+series+reasoning",
            practiceQuestionsCount = 35,
            isCompleted = false,
            progressPercentage = 40,
            isTodayTarget = false
        ),
        Chapter(
            id = 20,
            subjectName = "Reasoning",
            chapterNumber = 5,
            chapterName = "रक्त संबंध (Blood Relation)",
            chapterOrder = 5,
            description = "पारिवारिक आरेख (Family Tree), तस्वीर/इशारा आधारित प्रश्न, कोडेड रक्त संबंध (A + B का अर्थ)",
            notesContent = "• चिन्ह: पुरुष (+), महिला (-), वैवाहिक जोड़ा (= या ↔), भाई-बहन (--).\n• पीढ़ी अंतर: माता-पिता (ऊपर की पंक्ति), बच्चे (नीचे की पंक्ति)।\n• 'वह मेरे पिता के एकमात्र पुत्र का पुत्र है' = स्वयं का पुत्र।",
            fileUrl = "https://jaybajrangakhada.org/materials/reasoning_ch05_blood_relation.pdf",
            fileName = "05_Blood_Relation_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 20,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Blood Relation Family Tree Method",
            videoUrl = "https://youtube.com/results?search_query=blood+relation+reasoning",
            practiceQuestionsCount = 30,
            isCompleted = false,
            progressPercentage = 30,
            isTodayTarget = false
        ),
        Chapter(
            id = 21,
            subjectName = "Reasoning",
            chapterNumber = 6,
            chapterName = "दिशा एवं दूरी (Direction & Distance)",
            chapterOrder = 6,
            description = "4 मुख्य व 4 उप-दिशाएं, दायां-बायां मोड़ (90° Clockwise/Anticlockwise), पाइथागोरस दूरी, सूर्योदय-सूर्यास्त परछाई",
            notesContent = "• 8 दिशाएं: उत्तर, पूर्व, दक्षिण, पश्चिम, उत्तर-पूर्व, दक्षिण-पूर्व, दक्षिण-पश्चिम, उत्तर-पश्चिम।\n• दायाँ मुड़ना = 90° Clockwise; बायाँ मुड़ना = 90° Anti-clockwise.\n• न्यूनतम दूरी निकालने हेतु पाइथागोरस: कर्ण = √(लम्ब² + आधार²).\n• सूर्योदय के समय परछाई पश्चिम में; सूर्यास्त के समय परछाई पूर्व में बनती है।",
            fileUrl = "https://jaybajrangakhada.org/materials/reasoning_ch06_direction_distance.pdf",
            fileName = "06_Direction_and_Distance_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 21,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Direction Sense Test Easy Concept",
            videoUrl = "https://youtube.com/results?search_query=direction+and+distance+tricks",
            practiceQuestionsCount = 30,
            isCompleted = false,
            progressPercentage = 20,
            isTodayTarget = false
        ),
        Chapter(
            id = 22,
            subjectName = "Reasoning",
            chapterNumber = 7,
            chapterName = "बैठने की व्यवस्था (Seating Arrangement)",
            chapterOrder = 7,
            description = "वृत्ताकार मेज (केंद्र की ओर / केंद्र से बाहर मुख), रेखीय व्यवस्था (उत्तर/दक्षिण मुख), वर्गाकार मेज",
            notesContent = "• वृत्ताकार केंद्रोन्मुख (Facing Center): बायाँ = घड़ी की दिशा (Clockwise), दायाँ = घड़ी की विपरीत दिशा (Anti-clockwise).\n• निश्चित जानकारी (Definite clue) से आरेख बनाना शुरू करें।",
            fileUrl = "https://jaybajrangakhada.org/materials/reasoning_ch07_seating_arrangement.pdf",
            fileName = "07_Seating_Arrangement_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 22,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Seating Arrangement Circular and Linear",
            videoUrl = "https://youtube.com/results?search_query=seating+arrangement+tricks",
            practiceQuestionsCount = 25,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        ),
        Chapter(
            id = 23,
            subjectName = "Reasoning",
            chapterNumber = 8,
            chapterName = "क्रम व्यवस्था परीक्षण (Order & Ranking)",
            chapterOrder = 8,
            description = "पंक्ति में कुल व्यक्ति, स्थान परिवर्तन (Interchange of positions), मध्य के व्यक्ति ज्ञात करना",
            notesContent = "• कुल व्यक्ति = (बाएं से स्थान + दाएं से स्थान) - 1.\n• बाएं से स्थान = (कुल व्यक्ति + 1) - दाएं से स्थान.\n• स्थान बदलने पर: पहले व्यक्ति के स्थान में वृद्धि = दूसरे व्यक्ति के स्थान में वृद्धि।",
            fileUrl = "https://jaybajrangakhada.org/materials/reasoning_ch08_order_ranking.pdf",
            fileName = "08_Order_and_Ranking_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 23,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Ranking and Order Shortcuts",
            videoUrl = "https://youtube.com/results?search_query=order+and+ranking+reasoning",
            practiceQuestionsCount = 25,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        ),
        Chapter(
            id = 24,
            subjectName = "Reasoning",
            chapterNumber = 9,
            chapterName = "वेन आरेख (Venn Diagram)",
            chapterOrder = 9,
            description = "तार्किक वेन आरेख, ज्यामितीय वेन आरेख (त्रिभुज, वृत्त, आयत में संख्या पहचानना)",
            notesContent = "• सम्पूर्ण समावेशन (All): जैसे भारत, एशिया, विश्व।\n• आंशिक सम्बन्ध (Some): जैसे शिक्षक, लेखक, पुरुष।\n• कोई सम्बन्ध नहीं (No): जैसे कुर्सी, मेज, सेब।",
            fileUrl = "https://jaybajrangakhada.org/materials/reasoning_ch09_venn_diagram.pdf",
            fileName = "09_Venn_Diagram_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 24,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Venn Diagram Complete Concept",
            videoUrl = "https://youtube.com/results?search_query=venn+diagram+reasoning",
            practiceQuestionsCount = 25,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        ),
        Chapter(
            id = 25,
            subjectName = "Reasoning",
            chapterNumber = 10,
            chapterName = "न्याय निगमन (Syllogism)",
            chapterOrder = 10,
            description = "कथन एवं निष्कर्ष, सभी/कुछ/कोई नहीं सम्बन्धी नियम, संभावना (Possibility) वाले प्रश्न",
            notesContent = "• 'सभी A, B हैं' तथा 'सभी B, C हैं' ⇒ 'सभी A, C हैं'।\n• 'कुछ A, B हैं' ⇒ 'कुछ B, A हैं'।\n• 'कोई A, B नहीं है' ⇒ 'कोई B, A नहीं है'।\n• नकारात्मक निष्कर्ष सकारात्मक कथन से नहीं निकलता।",
            fileUrl = "https://jaybajrangakhada.org/materials/reasoning_ch10_syllogism.pdf",
            fileName = "10_Syllogism_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 25,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Syllogism 100/50 and Venn Method",
            videoUrl = "https://youtube.com/results?search_query=syllogism+reasoning",
            practiceQuestionsCount = 30,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        ),
        Chapter(
            id = 26,
            subjectName = "Reasoning",
            chapterNumber = 11,
            chapterName = "घड़ी एवं कैलेंडर (Clock & Calendar)",
            chapterOrder = 11,
            description = "सुइयों के बीच कोण, अतिव्यापन (0°, 90°, 180°), विषम दिन (Odd Days), लीप वर्ष, जन्मतिथि का दिन ज्ञात करना",
            notesContent = "• घड़ी में कोण θ = |(30 × H) - (11/2 × M)|.\n• साधारण वर्ष = 365 दिन (1 विषम दिन); लीप वर्ष = 366 दिन (2 विषम दिन).\n• शताब्दी कोड: 1600 (6), 1700 (4), 1800 (2), 1900 (0), 2000 (6).\n• 400 वर्षों में 0 विषम दिन होते हैं।",
            fileUrl = "https://jaybajrangakhada.org/materials/reasoning_ch11_clock_calendar.pdf",
            fileName = "11_Clock_and_Calendar_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 26,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Clock and Calendar Magical Tricks",
            videoUrl = "https://youtube.com/results?search_query=clock+calendar+reasoning",
            practiceQuestionsCount = 30,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        ),
        Chapter(
            id = 27,
            subjectName = "Reasoning",
            chapterNumber = 12,
            chapterName = "पासा एवं घन (Dice & Cube)",
            chapterOrder = 12,
            description = "मानक पासा, साधारण पासा, खुला पासा (Open Dice), 2 व 3 पासे में विपरीत सतह ज्ञात करना, रंगे हुए घन काटना",
            notesContent = "• मानक पासा: विपरीत सतहों का योग 7 होता है (1↔6, 2↔5, 3↔4).\n• एक सतह समान होने पर Clockwise नियम लागू करें।\n• खुला पासा: एक खाना छोड़कर विपरीत सतह होती है।\n• 3 तरफ रंगे छोटे घन सदैव 8 होते हैं (शीर्ष कोनों पर)।",
            fileUrl = "https://jaybajrangakhada.org/materials/reasoning_ch12_dice_cube.pdf",
            fileName = "12_Dice_and_Cube_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 27,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Dice and Cube Complete Masterclass",
            videoUrl = "https://youtube.com/results?search_query=dice+and+cube+reasoning",
            practiceQuestionsCount = 25,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        ),
        Chapter(
            id = 28,
            subjectName = "Reasoning",
            chapterNumber = 13,
            chapterName = "लुप्त पद ज्ञात करना (Missing Number)",
            chapterOrder = 13,
            description = "मैट्रिक्स, वृत्त, त्रिभुज, वर्ग में अज्ञात संख्या खोजना",
            notesContent = "• पंक्ति (Row-wise) अथवा स्तंभ (Column-wise) में संक्रिया देखें।\n• सम्भावित पैटर्न: a² + b² = c, (a + b) × k = c, a³ - b² = c.",
            fileUrl = "https://jaybajrangakhada.org/materials/reasoning_ch13_missing_number.pdf",
            fileName = "13_Missing_Number_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 28,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Missing Number Pattern Recognition",
            videoUrl = "https://youtube.com/results?search_query=missing+number+tricks",
            practiceQuestionsCount = 30,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        ),
        Chapter(
            id = 29,
            subjectName = "Reasoning",
            chapterNumber = 14,
            chapterName = "दर्पण एवं जल प्रतिबिंब (Mirror & Water Image)",
            chapterOrder = 14,
            description = "दर्पण प्रतिबिंब (दायां ↔ बायां परिवर्तन), जल प्रतिबिंब (ऊपर ↔ नीचे परिवर्तन), घड़ी समय का दर्पण प्रतिबिंब",
            notesContent = "• दर्पण प्रतिबिंब: क्षैतिज परिवर्तन (Left ↔ Right), Top/Bottom स्थिर।\n• घड़ी का दर्पण प्रतिबिंब = 11:60 में से दिया समय घटाएं (या 23:60).\n• जल प्रतिबिंब: ऊर्ध्वाधर परिवर्तन (Top ↔ Bottom), Left/Right स्थिर।\n• जल प्रतिबिंब = 18:30 में से घटाएं।",
            fileUrl = "https://jaybajrangakhada.org/materials/reasoning_ch14_mirror_water_image.pdf",
            fileName = "14_Mirror_and_Water_Image_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 29,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Mirror and Water Image Tricks",
            videoUrl = "https://youtube.com/results?search_query=mirror+and+water+image+reasoning",
            practiceQuestionsCount = 25,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        ),
        Chapter(
            id = 30,
            subjectName = "Reasoning",
            chapterNumber = 15,
            chapterName = "कागज मोड़ना एवं काटना (Paper Folding & Cutting)",
            chapterOrder = 15,
            description = "कागज के मोड़ों की दिशा, कट का विस्तार, आकृति पूर्ण करना",
            notesContent = "• कागज खोलने की दिशा में क्रमिक दर्पण/जल प्रतिबिंब की भाँति आकृति बनाएं।\n• जिस क्रम में कागज मोड़ा गया है, उसके ठीक उल्टे क्रम में खोलें।",
            fileUrl = "https://jaybajrangakhada.org/materials/reasoning_ch15_paper_folding_cutting.pdf",
            fileName = "15_Paper_Folding_Cutting_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 30,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Non-Verbal Paper Cutting & Folding",
            videoUrl = "https://youtube.com/results?search_query=paper+folding+cutting+reasoning",
            practiceQuestionsCount = 25,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        )
    )

    private fun getHindiChapters(): List<Chapter> = listOf(
        Chapter(
            id = 31,
            subjectName = "Hindi",
            chapterNumber = 1,
            chapterName = "हिंदी वर्णमाला एवं विराम चिन्ह",
            chapterOrder = 1,
            description = "स्वर (ह्रस्व, दीर्घ, प्लुत), व्यंजन (स्पर्श, अंतःस्थ, ऊष्म, संयुक्त), अल्पप्राण-महाप्राण, घोष-अघोष, 14 विराम चिन्ह",
            notesContent = "• कुल वर्ण: 52 (11 स्वर + 41 व्यंजन/अयोगवाह).\n• स्पर्श व्यंजन: क से म (25).\n• अंतःस्थ: य, र, ल, व; ऊष्म: श, ष, स, ह.\n• संयुक्त व्यंजन: क्ष (क्+ष्), त्र (त्+र्), ज्ञ (ज्+ञ्), श्र (श्+र्).\n• अल्पप्राण: वर्ग का 1, 3, 5 वां वर्ण; महाप्राण: 2, 4 वां वर्ण।",
            fileUrl = "https://jaybajrangakhada.org/materials/hindi_ch01_varnamala.pdf",
            fileName = "01_Hindi_Varnamala_Viram_Chinh.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 31,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Hindi Varnamala Complete Foundation",
            videoUrl = "https://youtube.com/results?search_query=hindi+varnamala+police",
            practiceQuestionsCount = 35,
            isCompleted = true,
            progressPercentage = 100,
            isTodayTarget = false
        ),
        Chapter(
            id = 32,
            subjectName = "Hindi",
            chapterNumber = 2,
            chapterName = "संधि एवं संधि विच्छेद",
            chapterOrder = 2,
            description = "स्वर संधि (दीर्घ, गुण, वृद्धि, यण, अयादि), व्यंजन संधि, विसर्ग संधि के नियम एवं उदाहरण",
            notesContent = "• दीर्घ संधि: अ/आ + अ/आ = आ (उदा. विद्या + आलय = विद्यालय).\n• गुण संधि: अ/आ + इ/ई = ए, उ/ऊ = ओ, ऋ = अर् (उदा. देव + इंद्र = देवेंद्र).\n• वृद्धि संधि: अ/आ + ए/ऐ = ऐ, ओ/औ = औ (उदा. एक + एक = एकैक).\n• यण संधि: इ/ई + भिन्न स्वर = य्, उ/ऊ = व्, ऋ = र्.\n• अयादि संधि: ए = अय्, ऐ = आय्, ओ = अव्, औ = आव् (उदा. पो + अन = पवन).",
            fileUrl = "https://jaybajrangakhada.org/materials/hindi_ch02_sandhi.pdf",
            fileName = "02_Sandhi_and_Vichchhed_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 32,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Sandhi All Types Easy Short Tricks",
            videoUrl = "https://youtube.com/results?search_query=sandhi+trick+hindi",
            practiceQuestionsCount = 30,
            isCompleted = false,
            progressPercentage = 70,
            isTodayTarget = false
        ),
        Chapter(
            id = 33,
            subjectName = "Hindi",
            chapterNumber = 3,
            chapterName = "समास",
            chapterOrder = 3,
            description = "समास के 6 भेद: अव्ययीभाव, तत्पुरुष (कारक विभक्ति सहित), कर्मधारय, द्विगु, द्वन्द्व, बहुव्रीहि",
            notesContent = "• अव्ययीभाव: पहला पद अव्यय/उपसर्ग (उदा. प्रतिदिन, आजन्म, यथाशक्ति).\n• तत्पुरुष: उत्तर पद प्रधान, कारक लोप (उदा. राजपुत्र = राजा का पुत्र).\n• कर्मधारय: विशेषण-विशेष्य सम्बन्ध (उदा. नीलकमल, चरणकमल).\n• द्विगु: पहला पद संख्यावाची (उदा. चौराहा, त्रिफला, नवरत्न).\n• द्वन्द्व: दोनों पद प्रधान, बीच में योजक (उदा. माता-पिता, दिन-रात).\n• बहुव्रीहि: तीसरा अर्थ प्रधान (उदा. दशानन = रावण, लंबोदर = गणेश).",
            fileUrl = "https://jaybajrangakhada.org/materials/hindi_ch03_samas.pdf",
            fileName = "03_Samas_All_Types_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 33,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Samas Short Trick Masterclass",
            videoUrl = "https://youtube.com/results?search_query=samas+trick+hindi+grammar",
            practiceQuestionsCount = 30,
            isCompleted = false,
            progressPercentage = 50,
            isTodayTarget = false
        ),
        Chapter(
            id = 34,
            subjectName = "Hindi",
            chapterNumber = 4,
            chapterName = "उपसर्ग एवं प्रत्यय",
            chapterOrder = 4,
            description = "संस्कृत, हिंदी एवं उर्दू के उपसर्ग, कृत् प्रत्यय (क्रिया से) एवं तद्धित प्रत्यय (संज्ञा/सर्वनाम से)",
            notesContent = "• उपसर्ग: शब्द के आरम्भ में जुड़कर अर्थ बदलते हैं (अति, अनु, प्रति, अप, सम्).\n• प्रत्यय: शब्द के अंत में जुड़ते हैं.\n• कृत् प्रत्यय: क्रिया के अंत में (जैसे: लिख् + आवट = लिखावट).\n• तद्धित प्रत्यय: संज्ञा/विशेषण के अंत में (जैसे: मानव + ता = मानवता).",
            fileUrl = "https://jaybajrangakhada.org/materials/hindi_ch04_upsarg_pratyay.pdf",
            fileName = "04_Upsarg_and_Pratyay_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 34,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Upsarg and Pratyay Hindi Tricks",
            videoUrl = "https://youtube.com/results?search_query=upsarg+pratyay+hindi",
            practiceQuestionsCount = 25,
            isCompleted = false,
            progressPercentage = 40,
            isTodayTarget = false
        ),
        Chapter(
            id = 35,
            subjectName = "Hindi",
            chapterNumber = 5,
            chapterName = "तत्सम, तद्भव एवं देशज-विदेशज शब्द",
            chapterOrder = 5,
            description = "संस्कृत मूल (तत्सम), विकृत रूप (तद्भव), देशज शब्द (लोटा, पगड़ी), आगत/विदेशज शब्द (अरबी, फारसी, अंग्रेजी)",
            notesContent = "• तत्सम पहचान: क्ष, त्र, ज्ञ, श्र, ष, ऋ, ण वर्ण युक्त शब्द.\n• तद्भव पहचान: चंद्रबिंदु (ँ), ख, छ, ब का प्रयोग।\n• उदाहरण: अग्नि (तत्सम) ↔ आग (तद्भव), दुग्ध ↔ दूध, कर्ण ↔ कान, अक्षि ↔ आँख।",
            fileUrl = "https://jaybajrangakhada.org/materials/hindi_ch05_tatsam_tadbhav.pdf",
            fileName = "05_Tatsam_Tadbhav_Deshaj_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 35,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Tatsam Tadbhav Best Rules",
            videoUrl = "https://youtube.com/results?search_query=tatsam+tadbhav+tricks",
            practiceQuestionsCount = 30,
            isCompleted = false,
            progressPercentage = 30,
            isTodayTarget = false
        ),
        Chapter(
            id = 36,
            subjectName = "Hindi",
            chapterNumber = 6,
            chapterName = "पर्यायवाची शब्द",
            chapterOrder = 6,
            description = "महत्वपूर्ण पर्यायवाची: जल, कमल, बादल, समुद्र, सूर्य, चंद्रमा, अग्नि, गंगा, हाथी, सिंह, अश्व",
            notesContent = "• जल: नीर, तोय, वारि, अंबु, सलिल.\n• 'जल' के पर्यायवाची में 'ज' लगाने से 'कमल' (जलज, वारिज, अंबुज).\n• 'जल' के पर्यायवाची में 'द' लगाने से 'बादल' (जलद, वारिद, अंबुद).\n• 'जल' के पर्यायवाची में 'धि' लगाने से 'समुद्र' (जलधि, वारिधि, अंबुधि).",
            fileUrl = "https://jaybajrangakhada.org/materials/hindi_ch06_paryayvachi.pdf",
            fileName = "06_Paryayvachi_Shabd_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 36,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Top 100 Important Paryayvachi",
            videoUrl = "https://youtube.com/results?search_query=hindi+paryayvachi+top+100",
            practiceQuestionsCount = 40,
            isCompleted = false,
            progressPercentage = 40,
            isTodayTarget = false
        ),
        Chapter(
            id = 37,
            subjectName = "Hindi",
            chapterNumber = 7,
            chapterName = "विलोम शब्द",
            chapterOrder = 7,
            description = "परीक्षा उपयोगी विपरीतार्थक शब्द संग्रह: अनुराग, प्रत्यक्ष, उत्कर्ष, स्थावर, तिमिर",
            notesContent = "• अनुराग का विराग, प्रत्यक्ष का परोक्ष, उत्कर्ष का अपकर्ष.\n• स्थावर का जंगम, तिमिर का प्रकाश, अज्ञ का विज्ञ.\n• जंगम का स्थावर (बार-बार पूछा जाने वाला प्रश्न).\n• 'अ' या 'अन' उपसर्ग जोड़कर बनने वाले विलोम।",
            fileUrl = "https://jaybajrangakhada.org/materials/hindi_ch07_vilom_shabd.pdf",
            fileName = "07_Vilom_Shabd_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 37,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Most Repeated Vilom Shabd",
            videoUrl = "https://youtube.com/results?search_query=vilom+shabd+hindi+police",
            practiceQuestionsCount = 35,
            isCompleted = false,
            progressPercentage = 30,
            isTodayTarget = false
        ),
        Chapter(
            id = 38,
            subjectName = "Hindi",
            chapterNumber = 8,
            chapterName = "अनेकार्थी शब्द एवं समरूपी भिन्नार्थक शब्द",
            chapterOrder = 8,
            description = "एक शब्द के अनेक अर्थ (कनक = सोना/धतूरा), सुनने में समान अर्थ भिन्न (अनल = आग, अनिल = हवा)",
            notesContent = "• कनक: सोना, धतूरा, पलाश, गेहूं.\n• द्विज: ब्राह्मण, पक्षी, दांत.\n• अनल (आग) ↔ अनिल (हवा).\n• अंश (कंधा) ↔ अंस (भाग).\n• तरंग (लहर) ↔ तुरंग (घोड़ा).",
            fileUrl = "https://jaybajrangakhada.org/materials/hindi_ch08_anekarthi_shabd.pdf",
            fileName = "08_Anekarthi_and_Samrupi_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 38,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Anekarthi & Shruti Sambhinnarthak Shabd",
            videoUrl = "https://youtube.com/results?search_query=anekarthi+shabd+hindi",
            practiceQuestionsCount = 25,
            isCompleted = false,
            progressPercentage = 20,
            isTodayTarget = false
        ),
        Chapter(
            id = 39,
            subjectName = "Hindi",
            chapterNumber = 9,
            chapterName = "वाक्यांश के लिए एक शब्द",
            chapterOrder = 9,
            description = "अनेक शब्दों के लिए एक शब्द: जो सब कुछ जानता हो, जिसे जीता न जा सके, जंगल की आग",
            notesContent = "• जो सब कुछ जानता हो = सर्वज्ञ.\n• जो कम जानता हो = अल्पज्ञ.\n• जिसे जीता न जा सके = अजेय.\n• जंगल की आग = दावाग्नि (दावानल); पेट की आग = जठराग्नि; समुद्र की आग = बड़वाग्नि.\n• मोक्ष की इच्छा रखने वाला = मुमुक्षु.",
            fileUrl = "https://jaybajrangakhada.org/materials/hindi_ch09_vakyansh_ek_shabd.pdf",
            fileName = "09_Vakyansh_Ke_Liye_Ek_Shabd.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 39,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Top 100 One Word Substitution Hindi",
            videoUrl = "https://youtube.com/results?search_query=vakyansh+ke+liye+ek+shabd",
            practiceQuestionsCount = 35,
            isCompleted = false,
            progressPercentage = 20,
            isTodayTarget = false
        ),
        Chapter(
            id = 40,
            subjectName = "Hindi",
            chapterNumber = 10,
            chapterName = "मुहावरे एवं लोकोक्तियाँ",
            chapterOrder = 10,
            description = "प्रसिद्ध मुहावरे (अंगूठा दिखाना, 9 दो 11 होना, कान भरना) एवं लोकोक्तियाँ व कहावतें",
            notesContent = "• अंगूठा दिखाना = साफ मना करना.\n• नौ दो ग्यारह होना = भाग जाना.\n• ईंट का जवाब पत्थर से देना = करारा जवाब देना.\n• नाच न जाने आँगन टेढ़ा = अपनी अयोग्यता छिपाने के लिए दूसरों में दोष निकालना.\n• अधजल गगरी छलकत जाए = अल्पज्ञानी अधिक दिखावा करता है।",
            fileUrl = "https://jaybajrangakhada.org/materials/hindi_ch10_muhavare_lokokti.pdf",
            fileName = "10_Muhavare_and_Lokoktiyan_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 40,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Important Muhavare and Lokoktiyan",
            videoUrl = "https://youtube.com/results?search_query=hindi+muhavare+lokoktiyan",
            practiceQuestionsCount = 30,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        ),
        Chapter(
            id = 41,
            subjectName = "Hindi",
            chapterNumber = 11,
            chapterName = "वाक्य शुद्धि एवं वर्तनी शुद्धि",
            chapterOrder = 11,
            description = "लिंग, वचन, कारक, सर्वनाम, पदक्रम सम्बन्धी अशुद्धियाँ एवं शुद्ध वर्तनी (उज्ज्वल, कवयित्री, शृंगार, पूजनीय)",
            notesContent = "• शुद्ध वर्तनी: उज्ज्वल (उ + ज् + ज् + व + ल), कवयित्री, शृंगार, पूजनीय (न कि पूज्यनीय), आशीर्वाद, जीजीविषा.\n• वाक्य शुद्धि: 'अनेक' का 'अनेकों' नहीं होता ('वहाँ अनेकों लोग थे' गलत ⇒ 'वहाँ अनेक लोग थे' सही).",
            fileUrl = "https://jaybajrangakhada.org/materials/hindi_ch11_vakya_vartani_shuddhi.pdf",
            fileName = "11_Vakya_and_Vartani_Shuddhi.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 41,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Vakya and Vartani Shuddhi Rules",
            videoUrl = "https://youtube.com/results?search_query=vartani+shuddhi+hindi",
            practiceQuestionsCount = 30,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        ),
        Chapter(
            id = 42,
            subjectName = "Hindi",
            chapterNumber = 12,
            chapterName = "रस, छंद एवं अलंकार",
            chapterOrder = 12,
            description = "रस के 9 भेद व स्थायी भाव, दोहा-सोरठा-चौपाई के लक्षण व मात्रा गणना, शब्दालंकार एवं अर्थालंकार",
            notesContent = "• रस: श्रृंगार (रति), हास्य (हास), करुण (शोक), वीर (उत्साह), रौद्र (क्रोध), भयानक (भय), वीभत्स (जुगुप्सा), अद्भुत (विस्मय), शांत (निर्वेद).\n• चौपाई: 4 चरण, प्रत्येक में 16 मात्राएं।\n• दोहा: 13, 11, 13, 11 मात्राएं; सोरठा: 11, 13, 11, 13 मात्राएं (दोहे का उल्टा).\n• अनुप्रास: वर्ण की आवृत्ति; यमक: शब्द की आवृत्ति भिन्न अर्थ (कनक कनक ते सौ गुनी); श्लेष: एक शब्द के अनेक अर्थ चिपके हों।",
            fileUrl = "https://jaybajrangakhada.org/materials/hindi_ch12_ras_chhand_alankar.pdf",
            fileName = "12_Ras_Chhand_Alankar_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 42,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Ras Chhand Alankar Full Concept",
            videoUrl = "https://youtube.com/results?search_query=ras+chhand+alankar+tricks",
            practiceQuestionsCount = 35,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        )
    )

    private fun getGkChapters(): List<Chapter> = listOf(
        Chapter(
            id = 43,
            subjectName = "GK / GS",
            chapterNumber = 1,
            chapterName = "प्राचीन भारत का इतिहास",
            chapterOrder = 1,
            description = "सिंधु घाटी सभ्यता (हड़प्पा, मोहनजोदड़ो), वैदिक काल (चार वेद), बौद्ध एवं जैन धर्म, मौर्य साम्राज्य (चंद्रगुप्त, अशोक), गुप्त साम्राज्य (स्वर्ण युग)",
            notesContent = "• हड़प्पा (दयाराम साहनी 1921, रावी नदी तट).\n• मोहनजोदड़ो (राखालदास बनर्जी 1922, विशाल स्नानागार, सिंधु नदी).\n• चार वेद: ऋग्वेद (सबसे प्राचीन, गायत्री मंत्र), यजुर्वेद, सामवेद (संगीत), अथर्ववेद (जादू-टोना/औषधि).\n• गौतम बुद्ध: जन्म लुम्बिनी, ज्ञान बोधगया, प्रथम उपदेश सारनाथ, महापरिनिर्वाण कुशीनगर.\n• मौर्य वंश संस्थापक चंद्रगुप्त मौर्य (चाणक्य के सहयोग से); सम्राट अशोक का कलिंग युद्ध (261 ईसा पूर्व).",
            fileUrl = "https://jaybajrangakhada.org/materials/gk_ch01_ancient_history.pdf",
            fileName = "01_Ancient_Indian_History_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 43,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Ancient History Fast Revision for Defence",
            videoUrl = "https://youtube.com/results?search_query=ancient+indian+history+police",
            practiceQuestionsCount = 40,
            isCompleted = true,
            progressPercentage = 100,
            isTodayTarget = true
        ),
        Chapter(
            id = 44,
            subjectName = "GK / GS",
            chapterNumber = 2,
            chapterName = "मध्यकालीन भारत का इतिहास",
            chapterOrder = 2,
            description = "दिल्ली सल्तनत (गुलाम, खिलजी, तुगलक, सैयद, लोदी वंश), मुगल साम्राज्य (बाबर से औरंगजेब), विजयनगर साम्राज्य, मराठा साम्राज्य (छत्रपति शिवाजी महाराज)",
            notesContent = "• तराइन का प्रथम युद्ध (1191) - पृथ्वीराज चौहान विजयी; द्वितीय युद्ध (1192) - गोरी विजयी.\n• गुलाम वंश संस्थापक: कुतुबुद्दीन ऐबक (1206); कुतुबमीनार निर्माण शुरू कराया.\n• पानीपत की पहली लड़ाई (1526) - बाबर ने इब्राहिम लोदी को हराया, मुगल साम्राज्य की नींव रखी।\n• पानीपत की दूसरी लड़ाई (1556) - अकबर vs हेमू.\n• पानीपत की तीसरी लड़ाई (1761) - अहमद शाह अब्दाली vs मराठा।",
            fileUrl = "https://jaybajrangakhada.org/materials/gk_ch02_medieval_history.pdf",
            fileName = "02_Medieval_Indian_History_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 44,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Medieval History Delhi Sultanate & Mughals",
            videoUrl = "https://youtube.com/results?search_query=medieval+history+gk",
            practiceQuestionsCount = 35,
            isCompleted = false,
            progressPercentage = 60,
            isTodayTarget = false
        ),
        Chapter(
            id = 45,
            subjectName = "GK / GS",
            chapterNumber = 3,
            chapterName = "आधुनिक भारत का इतिहास एवं स्वतंत्रता संग्राम",
            chapterOrder = 3,
            description = "यूरोपीय कंपनियों का आगमन, 1857 की क्रांति, भारतीय राष्ट्रीय कांग्रेस (1885), गांधी युग, प्रमुख जन आंदोलन (असहयोग, सविनय अवज्ञा, भारत छोड़ो), प्रमुख गवर्नर जनरल",
            notesContent = "• 1857 की क्रांति: बैरकपुर छावनी (मंगल पांडे), शुरुआत 10 मई मेरठ से.\n• भारतीय राष्ट्रीय कांग्रेस स्थापना: 28 दिसंबर 1885 (A.O. Hume), प्रथम अध्यक्ष डब्ल्यू. सी. बनर्जी.\n• बंगाल विभाजन (1905, लॉर्ड कर्जन); जलियांवाला बाग हत्याकांड (13 अप्रैल 1919, अमृतसर, जनरल डायर).\n• 1920 असहयोग आंदोलन; 1930 दांडी मार्च (नमक सत्याग्रह); 1942 भारत छोड़ो आंदोलन ('करो या मरो').\n• स्वतंत्र भारत के प्रथम गवर्नर जनरल: लॉर्ड माउंटबेटन; प्रथम भारतीय: सी. राजगोपालाचारी।",
            fileUrl = "https://jaybajrangakhada.org/materials/gk_ch03_modern_history.pdf",
            fileName = "03_Modern_History_Freedom_Struggle.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 45,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Modern History & Freedom Movement",
            videoUrl = "https://youtube.com/results?search_query=modern+indian+history+national+movement",
            practiceQuestionsCount = 45,
            isCompleted = false,
            progressPercentage = 50,
            isTodayTarget = false
        ),
        Chapter(
            id = 46,
            subjectName = "GK / GS",
            chapterNumber = 4,
            chapterName = "भारत का भूगोल",
            chapterOrder = 4,
            description = "भौगोलिक स्थिति एवं विस्तार, पर्वत श्रृंखलाएं (हिमालय, अरावली, पश्चिमी घाट), नदियां व जलप्रपात, राष्ट्रीय उद्यान व वन्यजीव अभयारण्य, खनिज व कृषि",
            notesContent = "• भारत का क्षेत्रफल 32,87,263 वर्ग किमी (विश्व का 2.42%, 7वां स्थान).\n• सबसे लंबी नदी: गंगा (2525 किमी); दक्षिण भारत की गंगा: गोदावरी.\n• सबसे ऊंची चोटी: K2 (गॉडविन ऑस्टिन 8611 मी), भारत में स्थित कंचनजंगा (8586 मी).\n• सबसे प्राचीन पर्वत श्रृंखला: अरावली.\n• जिम कॉर्बेट (उत्तराखंड) भारत का पहला राष्ट्रीय उद्यान (1936).\n• कर्क रेखा भारत के 8 राज्यों से गुजरती है: गुजरात, राजस्थान, MP, छत्तीसगढ़, झारखंड, पश्चिम बंगाल, त्रिपुरा, मिजोरम।",
            fileUrl = "https://jaybajrangakhada.org/materials/gk_ch04_indian_geography.pdf",
            fileName = "04_Indian_Geography_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 46,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Indian Geography Map Tricks & Notes",
            videoUrl = "https://youtube.com/results?search_query=indian+geography+tricks",
            practiceQuestionsCount = 40,
            isCompleted = false,
            progressPercentage = 50,
            isTodayTarget = false
        ),
        Chapter(
            id = 47,
            subjectName = "GK / GS",
            chapterNumber = 5,
            chapterName = "विश्व का सामान्य भूगोल",
            chapterOrder = 5,
            description = "सौरमंडल (ग्रह, उपग्रह), महाद्वीप व महासागर, प्रमुख जलसंधियां, विश्व की प्रमुख नदियां, पर्वत व मरुस्थल",
            notesContent = "• सौरमंडल: सबसे बड़ा ग्रह बृहस्पति, सबसे चमकीला व गर्म ग्रह शुक्र (भोर का तारा), लाल ग्रह मंगल.\n• सबसे बड़ा महाद्वीप: एशिया; सबसे छोटा: ऑस्ट्रेलिया.\n• सबसे गहरा महासागर: प्रशांत महासागर (मारियाना गर्त 11,022 मी).\n• विश्व की सबसे लंबी नदी: नील (अफ्रीका); सबसे बड़ी नदी (जलप्रवाह): अमेज़न.\n• विश्व का सबसे बड़ा मरुस्थल: सहारा (अफ्रीका); सबसे ऊंचा पर्वत शिखर: माउंट एवरेस्ट (8848.86 मी).",
            fileUrl = "https://jaybajrangakhada.org/materials/gk_ch05_world_geography.pdf",
            fileName = "05_World_Geography_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 47,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "World Geography Top Questions",
            videoUrl = "https://youtube.com/results?search_query=world+geography+gk",
            practiceQuestionsCount = 30,
            isCompleted = false,
            progressPercentage = 30,
            isTodayTarget = false
        ),
        Chapter(
            id = 48,
            subjectName = "GK / GS",
            chapterNumber = 6,
            chapterName = "भारतीय संविधान एवं राजव्यवस्था",
            chapterOrder = 6,
            description = "संविधान निर्माण, प्रस्तावना, मूल अधिकार (भाग 3), नीति निदेशक तत्व (भाग 4), राष्ट्रपति, संसद (लोकसभा, राज्यसभा), सर्वोच्च न्यायालय, प्रमुख अनुच्छेद व संशोधन",
            notesContent = "• संविधान सभा की प्रथम बैठक: 9 दिसंबर 1946 (सच्चिदानंद सिन्हा अस्थायी अध्यक्ष); स्थायी अध्यक्ष: डॉ. राजेंद्र प्रसाद.\n• प्रारूप समिति अध्यक्ष: डॉ. बी.आर. अम्बेडकर.\n• लागू: 26 जनवरी 1950 (मूल संविधान में 395 अनुच्छेद, 8 अनुसूचियां, 22 भाग).\n• मूल अधिकार (अनुच्छेद 12-35) - 6 मौलिक अधिकार; अनुच्छेद 32 को 'संविधान की आत्मा' कहा गया.\n• राष्ट्रपति आयु: 35 वर्ष; लोकसभा सदस्य आयु: 25 वर्ष; राज्यसभा सदस्य आयु: 30 वर्ष।",
            fileUrl = "https://jaybajrangakhada.org/materials/gk_ch06_indian_polity.pdf",
            fileName = "06_Indian_Polity_Constitution_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 48,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Polity Master Video Articles & Amendments",
            videoUrl = "https://youtube.com/results?search_query=indian+polity+constitution+tricks",
            practiceQuestionsCount = 45,
            isCompleted = true,
            progressPercentage = 100,
            isTodayTarget = true
        ),
        Chapter(
            id = 49,
            subjectName = "GK / GS",
            chapterNumber = 7,
            chapterName = "भारतीय अर्थव्यवस्था",
            chapterOrder = 7,
            description = "राष्ट्रीय आय, GDP, नीति आयोग, RBI एवं बैंकिंग प्रणाली, मुद्रास्फीति (Inflation), बजट एवं पंचवर्षीय योजनाएं",
            notesContent = "• RBI स्थापना: 1 अप्रैल 1935 (हिल्टन यंग आयोग की सिफारिश पर), राष्ट्रीयकरण: 1 जनवरी 1949.\n• नीति आयोग (NITI Aayog): 1 जनवरी 2015 को योजना आयोग के स्थान पर (अध्यक्ष - प्रधानमंत्री).\n• प्रथम पंचवर्षीय योजना (1951-56): हेराल्ड डोमर मॉडल (कृषि पर बल).\n• द्वितीय पंचवर्षीय योजना (1956-61): पी.सी. महालनोबिस मॉडल (भारी उद्योग पर बल).\n• भारत में GST लागू: 1 जुलाई 2017 (101वां संविधान संशोधन).",
            fileUrl = "https://jaybajrangakhada.org/materials/gk_ch07_indian_economy.pdf",
            fileName = "07_Indian_Economy_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 49,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Indian Economy Top GK Concepts",
            videoUrl = "https://youtube.com/results?search_query=indian+economy+gk",
            practiceQuestionsCount = 30,
            isCompleted = false,
            progressPercentage = 20,
            isTodayTarget = false
        ),
        Chapter(
            id = 50,
            subjectName = "GK / GS",
            chapterNumber = 8,
            chapterName = "भौतिक विज्ञान (Physics)",
            chapterOrder = 8,
            description = "SI मात्रक, गति एवं बल के नियम (न्यूटन के 3 नियम), कार्य, ऊर्जा एवं शक्ति, प्रकाश (दर्पण, लेंस, अपवर्तन), ध्वनि एवं तरंग",
            notesContent = "• SI मात्रक: बल = न्यूटन, कार्य/ऊर्जा = जूल, शक्ति = वाट, आवृत्ति = हर्ट्ज़, विद्युत धारा = एम्पीयर, ज्योति तीव्रता = कैंडेला.\n• न्यूटन का प्रथम नियम = जड़त्व का नियम (गैलीलियो का नियम); द्वितीय नियम = F = m × a; तृतीय नियम = क्रिया-प्रतिक्रिया का नियम.\n• प्रकाश का वेग निर्वात में: 3 × 10⁸ m/s.\n• वाहनों के साइड मिरर में: उत्तल दर्पण (Convex Mirror).\n• ध्वनि तरंगें अनुदैर्ध्य (Longitudinal) होती हैं; निर्वात में गमन नहीं कर सकतीं।",
            fileUrl = "https://jaybajrangakhada.org/materials/gk_ch08_physics.pdf",
            fileName = "08_General_Physics_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 50,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "General Physics Top Formulae & Questions",
            videoUrl = "https://youtube.com/results?search_query=general+science+physics+gk",
            practiceQuestionsCount = 35,
            isCompleted = false,
            progressPercentage = 40,
            isTodayTarget = false
        ),
        Chapter(
            id = 51,
            subjectName = "GK / GS",
            chapterNumber = 9,
            chapterName = "रसायन विज्ञान (Chemistry)",
            chapterOrder = 9,
            description = "तत्व, यौगिक, मिश्रण, परमाणु संरचना, अम्ल-क्षार-लवण (pH मान), धातु एवं अधातु, प्रमुख रासायनिक सूत्र",
            notesContent = "• pH मान: शुद्ध जल = 7 (उदासीन), रक्त = 7.4, सिरका = 2.4-3.4, दूध = 6.6.\n• अम्ल नीले लिटमस को लाल करता है; क्षार लाल लिटमस को नीला करता है।\n• रासायनिक सूत्र: धावन सोडा = Na₂CO₃·10H₂O; बेकिंग सोडा = NaHCO₃; प्लास्टर ऑफ पेरिस = CaSO₄·½H₂O; विरंजक चूर्ण = CaOCl₂.\n• एलपीजी (LPG) के मुख्य घटक: प्रोपेन एवं ब्यूटेन (गंध हेतु एथिल मरकैप्टन).",
            fileUrl = "https://jaybajrangakhada.org/materials/gk_ch09_chemistry.pdf",
            fileName = "09_General_Chemistry_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 51,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "General Chemistry Formulas and Concepts",
            videoUrl = "https://youtube.com/results?search_query=chemistry+general+science+gk",
            practiceQuestionsCount = 35,
            isCompleted = false,
            progressPercentage = 30,
            isTodayTarget = false
        ),
        Chapter(
            id = 52,
            subjectName = "GK / GS",
            chapterNumber = 10,
            chapterName = "जीव विज्ञान (Biology)",
            chapterOrder = 10,
            description = "कोशिका संरचना (माइटोकॉन्ड्रिया), मानव शरीर तंत्र (पाचन, श्वसन, परिसंचरण), रक्त समूह (ABO, Rh कारक), विटामिन एवं उनके रासायनिक नाम, प्रमुख रोग व कारक",
            notesContent = "• कोशिका का पावर हाउस: माइटोकॉन्ड्रिया (Mitochondria); आत्महत्या की थैली: लाइसोसोम.\n• विटामिन रासायनिक नाम व कमी से रोग:\n  - Vit A: रेटिनॉल (रतौंधी)\n  - Vit B1: थायमीन (बेरी-बेरी)\n  - Vit C: एस्कॉर्बिक एसिड (स्कर्वी)\n  - Vit D: कैल्सिफेरॉल (रिकेट्स)\n  - Vit K: रक्त का थक्का जमना.\n• सर्वदाता रक्त समूह: O- (O negative); सर्वग्राही: AB+.",
            fileUrl = "https://jaybajrangakhada.org/materials/gk_ch10_biology.pdf",
            fileName = "10_General_Biology_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 52,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Biology Master Class Vitamins and Diseases",
            videoUrl = "https://youtube.com/results?search_query=biology+vitamins+diseases+gk",
            practiceQuestionsCount = 40,
            isCompleted = false,
            progressPercentage = 40,
            isTodayTarget = false
        ),
        Chapter(
            id = 53,
            subjectName = "GK / GS",
            chapterNumber = 11,
            chapterName = "पर्यावरण एवं पारिस्थितिकी",
            chapterOrder = 11,
            description = "पारिस्थितिकी तंत्र (Ecosystem), खाद्य श्रृंखला व खाद्य जाल, ओजोन परत (मॉन्ट्रियल प्रोटोकॉल), ग्रीनहाउस प्रभाव, जैव विविधता हॉटस्पॉट",
            notesContent = "• पारिस्थितिकी तंत्र शब्द के जनक: ए. जी. टांसले (1935).\n• खाद्य श्रृंखला में ऊर्जा का 10% नियम: लिंडमैन (1942).\n• ओजोन परत समताप मंडल (Stratosphere) में स्थित है; ओजोन दिवस: 16 सितंबर.\n• मुख्य ग्रीनहाउस गैसें: CO₂, मीथेन (CH₄), नाइट्रस ऑक्साइड, जलवाष्प.\n• भारत के मुख्य हॉटस्पॉट: पश्चिमी घाट, पूर्वी हिमालय, इंडो-बर्मा.",
            fileUrl = "https://jaybajrangakhada.org/materials/gk_ch11_environment.pdf",
            fileName = "11_Environment_and_Ecology_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 53,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Environment and Ecology Top Notes",
            videoUrl = "https://youtube.com/results?search_query=environment+ecology+gk",
            practiceQuestionsCount = 25,
            isCompleted = false,
            progressPercentage = 10,
            isTodayTarget = false
        ),
        Chapter(
            id = 54,
            subjectName = "GK / GS",
            chapterNumber = 12,
            chapterName = "भारत के प्रमुख खेलकूद एवं पुरस्कार",
            chapterOrder = 12,
            description = "ओलंपिक खेल, एशियाई खेल, राष्ट्रमंडल खेल, क्रिकेट (IPL / ICC), मेजर ध्यानचंद खेल रत्न पुरस्कार, अर्जुन पुरस्कार, द्रोणाचार्य पुरस्कार",
            notesContent = "• भारत का राष्ट्रीय खेल दिवस: 29 अगस्त (मेजर ध्यानचंद की जयंती पर).\n• भारत का सर्वोच्च खेल पुरस्कार: मेजर ध्यानचंद खेल रत्न पुरस्कार (राशि 25 लाख रु).\n• प्रथम खेल रत्न विजेता: विश्वनाथन आनंद (शतरंज, 1991-92).\n• ओलंपिक छल्ले के 5 रंग 5 महाद्वीपों को दर्शाते हैं: नीला (यूरोप), पीला (एशिया), काला (अफ्रीका), हरा (ऑस्ट्रेलिया), लाल (अमेरिका).",
            fileUrl = "https://jaybajrangakhada.org/materials/gk_ch12_sports_awards.pdf",
            fileName = "12_Sports_and_Awards_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 54,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Sports GK & Major Awards for Defence",
            videoUrl = "https://youtube.com/results?search_query=sports+gk+awards",
            practiceQuestionsCount = 30,
            isCompleted = false,
            progressPercentage = 20,
            isTodayTarget = false
        ),
        Chapter(
            id = 55,
            subjectName = "GK / GS",
            chapterNumber = 13,
            chapterName = "राष्ट्रीय एवं अंतर्राष्ट्रीय प्रमुख दिवस",
            chapterOrder = 13,
            description = "जनवरी से दिसंबर तक के सभी महत्वपूर्ण दिवस (थल सेना दिवस, वायु सेना दिवस, नौसेना दिवस, संविधान दिवस, पर्यावरण दिवस)",
            notesContent = "• 15 जनवरी: थल सेना दिवस (Army Day)\n• 8 अक्टूबर: वायु सेना दिवस (Air Force Day)\n• 4 दिसंबर: नौसेना दिवस (Navy Day)\n• 26 जनवरी: गणतंत्र दिवस; 15 अगस्त: स्वतंत्रता दिवस\n• 5 जून: विश्व पर्यावरण दिवस; 21 जून: अंतर्राष्ट्रीय योग दिवस\n• 26 नवंबर: संविधान दिवस एवं राष्ट्रीय दुग्ध दिवस\n• 10 दिसंबर: अंतर्राष्ट्रीय मानवाधिकार दिवस।",
            fileUrl = "https://jaybajrangakhada.org/materials/gk_ch13_important_days.pdf",
            fileName = "13_Important_National_International_Days.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 55,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "All Important National & International Days Tricks",
            videoUrl = "https://youtube.com/results?search_query=important+days+gk+tricks",
            practiceQuestionsCount = 30,
            isCompleted = false,
            progressPercentage = 30,
            isTodayTarget = false
        ),
        Chapter(
            id = 56,
            subjectName = "GK / GS",
            chapterNumber = 14,
            chapterName = "भारत के प्रमुख मेले, त्यौहार एवं लोकनृत्य",
            chapterOrder = 14,
            description = "शास्त्रीय नृत्य (8 शैलियाँ), प्रमुख राज्यों के लोकनृत्य (बिहू, गरबा, भांगड़ा, घूमर, राउत नाचा), कुंभ मेला व प्रसिद्ध उत्सव (हॉर्नबिल, पोंगल, ओणम)",
            notesContent = "• 8 शास्त्रीय नृत्य: भरतनाट्यम (तमिलनाडु), कत्थक (उत्तर प्रदेश), कथकली व मोहिनीअट्टम (केरल), कुचिपुड़ी (आंध्र प्रदेश), ओडिसी (ओडिशा), मणिपुरी (मणिपुर), सत्रिया (असम).\n• प्रसिद्ध लोकनृत्य: राउत नाचा व पंथी (छत्तीसगढ़), गरबा व डांडिया (गुजरात), भांगड़ा व गिद्दा (पंजाब), घूमर व कालबेलिया (राजस्थान), बिहू (असम).\n• त्यौहार: पोंगल (तमिलनाडु), ओणम (केरल), हॉर्नबिल उत्सव (नागालैंड), बस्तर दशहरा (छत्तीसगढ़).",
            fileUrl = "https://jaybajrangakhada.org/materials/gk_ch14_fairs_festivals_dances.pdf",
            fileName = "14_Indian_Dances_Fairs_Festivals.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 56,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Folk Dances & Festivals of India",
            videoUrl = "https://youtube.com/results?search_query=indian+folk+dances+festivals",
            practiceQuestionsCount = 35,
            isCompleted = false,
            progressPercentage = 20,
            isTodayTarget = false
        ),
        Chapter(
            id = 57,
            subjectName = "GK / GS",
            chapterNumber = 15,
            chapterName = "महत्वपूर्ण पुस्तकें एवं उनके लेखक",
            chapterOrder = 15,
            description = "प्राचीन पुस्तकें (अर्थशास्त्र, राजतरंगिणी, मुद्राराक्षस), आधुनिक साहित्य (गोदान, गीतांजलि, डिस्कवरी ऑफ इंडिया, विंग्स ऑफ फायर)",
            notesContent = "• अर्थशास्त्र: चाणक्य (कौटिल्य); मुद्राराक्षस: विशाखदत्त; राजतरंगिणी: कल्हण.\n• इंडिका: मेगस्थनीज; हर्षचरित व कादंबरी: बाणभट्ट.\n• गीतांजलि: रवींद्रनाथ टैगोर (1913 नोबेल पुरस्कार).\n• डिस्कवरी ऑफ इंडिया: जवाहरलाल नेहरू.\n• विंग्स ऑफ फायर (Wings of Fire): डॉ. ए.पी.जे. अब्दुल कलाम.\n• गोदान, गबन, कर्मभूमि: मुंशी प्रेमचंद।",
            fileUrl = "https://jaybajrangakhada.org/materials/gk_ch15_books_authors.pdf",
            fileName = "15_Important_Books_and_Authors.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 57,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Top Books and Authors Complete List",
            videoUrl = "https://youtube.com/results?search_query=important+books+authors+gk",
            practiceQuestionsCount = 30,
            isCompleted = false,
            progressPercentage = 10,
            isTodayTarget = false
        ),
        Chapter(
            id = 58,
            subjectName = "GK / GS",
            chapterNumber = 16,
            chapterName = "समसामयिकी (Current Affairs - 2026)",
            chapterOrder = 16,
            description = "राष्ट्रीय व अंतर्राष्ट्रीय घटनाक्रम, सैन्य युद्धाभ्यास, प्रमुख नियुक्तियाँ (CDS, सेना प्रमुख), सरकारी योजनाएं, नोबेल पुरस्कार एवं G20/BRICS सम्मेलन",
            notesContent = "• भारत के चीफ ऑफ डिफेंस स्टाफ (CDS), थल सेनाध्यक्ष, वायु सेनाध्यक्ष व नौसेनाध्यक्ष.\n• प्रमुख संयुक्त युद्धाभ्यास: वज्र प्रहार व युद्धाभ्यास (भारत-USA), सूर्य किरण (भारत-नेपाल), संप्रति (भारत-बांग्लादेश), वरुण व गरुड़ (भारत-फ्रांस).\n• भारत के नवीनतम रक्षा उपकरण: तेजस लड़ाकू विमान, अग्नि-5 मिसाइल, INS विक्रांत (विमानवाहक पोत).\n• चंद्रयान मिशन एवं आदित्य L1 सौर मिशन की सफलताएं।",
            fileUrl = "https://jaybajrangakhada.org/materials/gk_ch16_current_affairs_2026.pdf",
            fileName = "16_Current_Affairs_2026_Defence.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 58,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Defence Current Affairs 2026 Master Video",
            videoUrl = "https://youtube.com/results?search_query=defence+current+affairs+2026",
            practiceQuestionsCount = 40,
            isCompleted = false,
            progressPercentage = 25,
            isTodayTarget = false
        )
    )

    private fun getComputerChapters(): List<Chapter> = listOf(
        Chapter(
            id = 59,
            subjectName = "Computer",
            chapterNumber = 1,
            chapterName = "कंप्यूटर का परिचय एवं इतिहास",
            chapterOrder = 1,
            description = "कंप्यूटर का विकास, पीढ़ियाँ (1st से 5th Generation), चार्ल्स बैबेज, इनपुट व आउटपुट डिवाइस",
            notesContent = "• कंप्यूटर के जनक: चार्ल्स बैबेज (एनालिटिकल इंजन 1837).\n• कंप्यूटर की पीढ़ियां:\n  - 1st Gen (1940-56): वैक्यूम ट्यूब\n  - 2nd Gen (1956-63): ट्रांजिस्टर\n  - 3rd Gen (1964-71): IC (इंटीग्रेटेड सर्किट)\n  - 4th Gen (1971-वर्तमान): माइक्रोप्रोसेसर (VLSI/ULSI)\n  - 5th Gen (वर्तमान-भविष्य): AI (आर्टिफिशियल इंटेलिजेंस).\n• इनपुट डिवाइस: कीबोर्ड, माउस, स्कैनर, OMR, OCR, MICR, बारकोड रीडर.\n• आउटपुट डिवाइस: मॉनिटर, प्रिंटर, प्लॉटर, स्पीकर।",
            fileUrl = "https://jaybajrangakhada.org/materials/comp_ch01_intro_history.pdf",
            fileName = "01_Computer_Intro_History_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 59,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Computer Generations & Input Output Devices",
            videoUrl = "https://youtube.com/results?search_query=computer+generations+input+output",
            practiceQuestionsCount = 30,
            isCompleted = true,
            progressPercentage = 100,
            isTodayTarget = false
        ),
        Chapter(
            id = 60,
            subjectName = "Computer",
            chapterNumber = 2,
            chapterName = "कंप्यूटर की संरचना (Hardware & Software)",
            chapterOrder = 2,
            description = "CPU के भाग (ALU, CU, Registers), मदरबोर्ड, सिस्टम सॉफ्टवेयर (OS - Windows, Linux), एप्लीकेशन सॉफ्टवेयर (MS Office)",
            notesContent = "• CPU (Central Processing Unit) - कंप्यूटर का मस्तिष्क.\n• ALU (Arithmetic Logic Unit): गणितीय एवं तार्किक गणनाएं.\n• CU (Control Unit): सभी हार्डवेयर व निर्देशों का नियंत्रण.\n• सिस्टम सॉफ्टवेयर: Operating System (Windows, Linux, Android, iOS), Device Drivers.\n• एप्लीकेशन सॉफ्टवेयर: MS Word, MS Excel, Web Browser, Photoshop.",
            fileUrl = "https://jaybajrangakhada.org/materials/comp_ch02_hardware_software.pdf",
            fileName = "02_Computer_Hardware_Software_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 60,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "CPU Architecture & Software Basics",
            videoUrl = "https://youtube.com/results?search_query=computer+hardware+software+basics",
            practiceQuestionsCount = 25,
            isCompleted = false,
            progressPercentage = 50,
            isTodayTarget = false
        ),
        Chapter(
            id = 61,
            subjectName = "Computer",
            chapterNumber = 3,
            chapterName = "मेमोरी एवं स्टोरेज (RAM, ROM, SSD)",
            chapterOrder = 3,
            description = "प्राथमिक मेमोरी (RAM - अस्थाई, ROM - स्थाई, Cache Memory), द्वितीयक मेमोरी (HDD, SSD, Pen Drive), मेमोरी इकाइयां (Bit, Byte, KB, MB, GB, TB)",
            notesContent = "• मेमोरी इकाइयाँ:\n  - 1 Nibble = 4 Bits\n  - 1 Byte = 8 Bits\n  - 1 KB = 1024 Bytes\n  - 1 MB = 1024 KB\n  - 1 GB = 1024 MB\n  - 1 TB = 1024 GB.\n• RAM (Random Access Memory): Volatile (अस्थाई मेमोरी).\n• ROM (Read Only Memory): Non-Volatile (स्थाई मेमोरी, BIOS संग्रहीत).\n• सबसे तेज मेमोरी: Registers > Cache Memory > RAM.",
            fileUrl = "https://jaybajrangakhada.org/materials/comp_ch03_memory_storage.pdf",
            fileName = "03_Memory_and_Storage_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 61,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Computer Memory Units RAM ROM Cache",
            videoUrl = "https://youtube.com/results?search_query=computer+memory+units+tricks",
            practiceQuestionsCount = 30,
            isCompleted = false,
            progressPercentage = 30,
            isTodayTarget = false
        ),
        Chapter(
            id = 62,
            subjectName = "Computer",
            chapterNumber = 4,
            chapterName = "इंटरनेट, ईमेल एवं साइबर सुरक्षा",
            chapterOrder = 4,
            description = "नेटवर्क के प्रकार (LAN, MAN, WAN), IP Address, URL, WWW, वेब ब्राउज़र, ईमेल प्रोटोकॉल (SMTP, POP3, IMAP), वायरस, मालवेयर व फ़ायरवॉल",
            notesContent = "• LAN (Local Area Network), MAN (Metropolitan), WAN (Wide Area Network - Internet).\n• WWW (World Wide Web) के जनक: टिम बर्नर्स ली (1989).\n• URL (Uniform Resource Locator) - वेबसाइट का अद्वितीय पता.\n• ईमेल प्रोटोकॉल: SMTP (भेजने हेतु - Simple Mail Transfer Protocol), POP3/IMAP (प्राप्त करने हेतु).\n• साइबर सुरक्षा: फ़ायरवॉल अनाधिकृत नेटवर्क पहुंच को रोकता है; वायरस, ट्रोजन हॉर्स, रैनसमवेयर मालवेयर के प्रकार हैं।",
            fileUrl = "https://jaybajrangakhada.org/materials/comp_ch04_internet_cyber_security.pdf",
            fileName = "04_Internet_Email_Cyber_Security.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 62,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Internet Protocols and Cyber Security",
            videoUrl = "https://youtube.com/results?search_query=internet+protocols+cyber+security",
            practiceQuestionsCount = 30,
            isCompleted = false,
            progressPercentage = 20,
            isTodayTarget = false
        ),
        Chapter(
            id = 63,
            subjectName = "Computer",
            chapterNumber = 5,
            chapterName = "महत्वपूर्ण शॉर्टकट कीज एवं फुल फॉर्म्स (Shortcuts & Abbreviations)",
            chapterOrder = 5,
            description = "Windows एवं MS Office की शॉर्टकट कुंजियाँ (Ctrl+C, Ctrl+V, Ctrl+Z, F5, Alt+Tab), 50+ अति महत्वपूर्ण कंप्यूटर संक्षिप्त नाम (Full Forms)",
            notesContent = "• शॉर्टकट कीज:\n  - Ctrl + A (Select All), Ctrl + C (Copy), Ctrl + X (Cut), Ctrl + V (Paste)\n  - Ctrl + Z (Undo), Ctrl + Y (Redo), Ctrl + S (Save), Ctrl + P (Print)\n  - F5 (Refresh), F7 (Spelling Check), Alt + F4 (Close Program).\n• फुल फॉर्म्स:\n  - CPU: Central Processing Unit\n  - HTTP: HyperText Transfer Protocol\n  - HTTPS: HyperText Transfer Protocol Secure\n  - HTML: HyperText Markup Language\n  - PDF: Portable Document Format\n  - USB: Universal Serial Bus\n  - Wi-Fi: Wireless Fidelity\n  - GUI: Graphical User Interface.",
            fileUrl = "https://jaybajrangakhada.org/materials/comp_ch05_shortcuts_fullforms.pdf",
            fileName = "05_Shortcut_Keys_Full_Forms_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi",
            isPublished = true,
            displayOrder = 63,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Top 50 Computer Shortcuts and Full Forms",
            videoUrl = "https://youtube.com/results?search_query=computer+shortcuts+full+forms+gk",
            practiceQuestionsCount = 35,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        )
    )

    private fun getEnglishChapters(): List<Chapter> = listOf(
        Chapter(
            id = 64,
            subjectName = "English",
            chapterNumber = 1,
            chapterName = "Parts of Speech (शब्द भेद)",
            chapterOrder = 1,
            description = "8 Parts of Speech की पहचान: Noun, Pronoun, Verb, Adverb, Adjective, Preposition, Conjunction, Interjection",
            notesContent = "• Eight Parts of Speech in English:\n1. Noun: Person, place, thing, or idea (उदा: Ram, Delhi, Bravery).\n2. Pronoun: Used in place of a noun (उदा: he, she, it, they, who).\n3. Verb: Action or state of being (उदा: run, write, is, are, was).\n4. Adjective: Modifies a noun/pronoun (उदा: brave soldier, honest man).\n5. Adverb: Modifies a verb, adjective, or another adverb (उदा: runs fast, very good).\n6. Preposition: Shows relationship of time/place/direction (उदा: in, on, at, under, between).\n7. Conjunction: Joins words, phrases, or clauses (उदा: and, but, because, although).\n8. Interjection: Expresses strong emotion (उदा: Wow!, Alas!, Bravo!).\n• Exam Tip: वाक्य में शब्द के कार्य (function) के आधार पर part of speech तय होता है, केवल स्पेलिंग से नहीं।",
            fileUrl = "https://jaybajrangakhada.org/materials/eng_ch01_parts_of_speech.pdf",
            fileName = "01_Parts_of_Speech_English_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi / English",
            isPublished = true,
            displayOrder = 64,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Parts of Speech Complete Masterclass",
            videoUrl = "https://youtube.com/results?search_query=parts+of+speech+english+grammar",
            practiceQuestionsCount = 30,
            isCompleted = true,
            progressPercentage = 100,
            isTodayTarget = true
        ),
        Chapter(
            id = 65,
            subjectName = "English",
            chapterNumber = 2,
            chapterName = "Noun & Its Classification (संज्ञा एवं नियम)",
            chapterOrder = 2,
            description = "Proper, Common, Collective, Abstract, Material Nouns; Singular/Plural Rules; Noun Gender & Apostrophe ('s) Rules",
            notesContent = "• Types of Noun:\n  1. Proper Noun: विशेष नाम (Arjun, Ganga - सदैव Capital letter से शुरू).\n  2. Common Noun: जातिवाचक (soldier, city, book).\n  3. Collective Noun: समूहवाचक (Army, Fleet, Crowd, Jury).\n  4. Material Noun: पदार्थवाचक (gold, iron, water - सामान्यतः Uncountable).\n  5. Abstract Noun: भाववाचक (honesty, courage, freedom, youth).\n• Important Exam Rules:\n  - Uncountable Nouns का Plural नहीं बनता (furniture, advice, information, scenery, luggage, baggage).\n  - कुछ Nouns हमेशा Plural रहते हैं: scissors, trousers, spectacles, pliers, binoculars, barracks, premises.\n  - देखने में Plural लेकिन प्रयोग में Singular: Mathematics, Physics, News, Politics, Innings, Rickets.\n  - Collective Noun यदि एक मत हो तो Singular Verb (The jury was unanimous), यदि मतभेद हो तो Plural Verb (The jury were divided).\n  - निर्जीव वस्तुओं के साथ Apostrophe ('s) नहीं लगाते (Table's leg गलत, leg of the table सही)।",
            fileUrl = "https://jaybajrangakhada.org/materials/eng_ch02_noun_rules.pdf",
            fileName = "02_Noun_Rules_English_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi / English",
            isPublished = true,
            displayOrder = 65,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Noun Complete Rules and Spotting Errors",
            videoUrl = "https://youtube.com/results?search_query=noun+english+grammar+rules",
            practiceQuestionsCount = 35,
            isCompleted = false,
            progressPercentage = 40,
            isTodayTarget = false
        ),
        Chapter(
            id = 66,
            subjectName = "English",
            chapterNumber = 3,
            chapterName = "Pronoun & Agreement Rules (सर्वनाम)",
            chapterOrder = 3,
            description = "Personal Pronouns (Order 231 vs 123), Relative Pronouns (Who, Whom, Whose, Which, That), Reflexive Pronouns",
            notesContent = "• Order of Personal Pronouns:\n  - सामान्य अच्छी बातों/सामान्य संदर्भ में क्रम: 231 (Second Person, Third Person, First Person: You, he and I are friends).\n  - भूल स्वीकारने, दोष या गलती में क्रम: 123 (I, you and he are guilty).\n• Subjective vs Objective Case:\n  - Let, between, except, but के बाद Objective Case (Let him and me go; Between you and me).\n  - 'Than' और 'As' के बाद तुलना यदि Subject से हो तो Subjective case (He is taller than I).\n• Relative Pronoun Rules:\n  - Who = Subject (सजीव हेतु), Whom = Object (सजीव हेतु), Which = निर्जीव व जानवरों हेतु।\n  - 'That' का प्रयोग: Superlative degree, all, the only, the same, none के बाद केवल 'That' आता है (This is the best book that I have read).\n• One must do one's duty (his duty गलत है)।",
            fileUrl = "https://jaybajrangakhada.org/materials/eng_ch03_pronoun_rules.pdf",
            fileName = "03_Pronoun_Rules_English_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi / English",
            isPublished = true,
            displayOrder = 66,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Pronoun Master Rules for Competitive Exams",
            videoUrl = "https://youtube.com/results?search_query=pronoun+english+rules+competitive+exams",
            practiceQuestionsCount = 30,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        ),
        Chapter(
            id = 67,
            subjectName = "English",
            chapterNumber = 4,
            chapterName = "Tenses & Structures (काल एवं वाक्य संरचना)",
            chapterOrder = 4,
            description = "Present, Past & Future Tenses (Simple, Continuous, Perfect, Perfect Continuous); Signal Words & Conditionals",
            notesContent = "• Present Tense:\n  - Simple Present: S + V1/V5 + O (सार्वभौमिक सत्य, आदत, दिनचर्या - daily, always, usually, never).\n  - Present Continuous: S + is/am/are + V4 (कार्य वर्तमान में जारी - now, at present, currently).\n  - Present Perfect: S + has/have + V3 (कार्य पूर्ण हो चुका - already, just, yet, recently).\n  - Present Perfect Continuous: S + has/have been + V4 + since/for + time (since = निश्चित समय, for = समयावधि).\n• Past Tense:\n  - Simple Past: S + V2 + O (भूतकाल का समय सूचक: yesterday, ago, last year, in 1947).\n  - Past Continuous: S + was/were + V4 (When/While).\n  - Past Perfect: S + had + V3 (भूतकाल में दो कार्यों में पहला कार्य had + V3, दूसरा V2: The patient had died before the doctor arrived).\n• Conditional Sentences:\n  1. If + Simple Present, Simple Future (If you work hard, you will succeed).\n  2. If + Simple Past (V2 / were), would + V1 (If I were a king, I would help the poor).\n  3. If + had + V3, would have + V3 (If he had run fast, he would have won the race).",
            fileUrl = "https://jaybajrangakhada.org/materials/eng_ch04_tenses_structures.pdf",
            fileName = "04_Tenses_Structures_English_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi / English",
            isPublished = true,
            displayOrder = 67,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Tenses Complete Concept & Conditional Sentences",
            videoUrl = "https://youtube.com/results?search_query=tenses+rules+english+grammar",
            practiceQuestionsCount = 40,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        ),
        Chapter(
            id = 68,
            subjectName = "English",
            chapterNumber = 5,
            chapterName = "Subject-Verb Agreement (कर्त्ता-क्रिया सामंजस्य)",
            chapterOrder = 5,
            description = "Syntax के 15 स्वर्णिम नियम: Singular Subject = Singular Verb, Plural Subject = Plural Verb, Either/Or, As well as",
            notesContent = "• Rule 1: दो Singular Subjects 'and' से जुड़े हों तो Plural Verb (Ram and Shyam are going). परन्तु यदि एक ही व्यक्ति या विचार दर्शाएं तो Singular Verb (Bread and butter is a wholesome food; Slow and steady wins the race).\n• Rule 2: जब दो Subjects 'as well as', 'with', 'along with', 'together with', 'accompanied by', 'in addition to' से जुड़े हों तो Verb पहले Subject के अनुसार आती है (The Captain, along with his soldiers, was present).\n• Rule 3: जब दो Subjects 'either...or', 'neither...nor', 'not only...but also' से जुड़े हों तो Verb निकटतम (दूसरे) Subject के अनुसार आती है (Neither the teacher nor the students were ready).\n• Rule 4: 'Each of', 'One of', 'Neither of', 'Either of' के बाद Noun हमेशा Plural लेकिन Verb हमेशा Singular होती है (One of my friends is a commando).\n• Rule 5: 'A number of' के साथ Plural Verb (A number of cadets were selected), लेकिन 'The number of' के साथ Singular Verb (The number of cadets is fifty).\n• Rule 6: More than one + Singular Noun + Singular Verb (More than one cadet was rewarded).",
            fileUrl = "https://jaybajrangakhada.org/materials/eng_ch05_subject_verb_agreement.pdf",
            fileName = "05_Subject_Verb_Agreement_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi / English",
            isPublished = true,
            displayOrder = 68,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Subject-Verb Agreement Golden Rules",
            videoUrl = "https://youtube.com/results?search_query=subject+verb+agreement+english+rules",
            practiceQuestionsCount = 35,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        ),
        Chapter(
            id = 69,
            subjectName = "English",
            chapterNumber = 6,
            chapterName = "Prepositions & Fixed Prepositions (पूर्वसर्ग)",
            chapterOrder = 6,
            description = "At, In, On, Into, Onto, Between, Among, Beside, Besides; Defence Exam Fixed Prepositions",
            notesContent = "• Position & Time Rules:\n  - In vs Into: In = स्थिरता/अंदर (in the room); Into = गतिशीलता के साथ प्रवेश (jumped into the river).\n  - Between vs Among: Between = 2 व्यक्तियों/वस्तुओं हेतु; Among = 2 से अधिक हेतु।\n  - Beside vs Besides: Beside = बगल में/पास में; Besides = के अतिरिक्त/के अलावा।\n  - At vs In (Time): At = निश्चित समय (at 5 PM, at night); In = महीना/वर्ष (in July, in 2026); On = दिन/दिनांक (on Monday, on 15th August).\n• Crucial Fixed Prepositions (कंठस्थ करें):\n  - Senior / Junior / Superior / Inferior / Preferable + TO (than कभी नहीं आता!)\n  - Abstain / Refrain / Prevent / Prohibit + FROM + V4\n  - Accused / Fond / Aware / Capable / Afraid + OF\n  - Good / Bad / Expert + AT (He is good at mathematics)\n  - Die of disease (उदा: died of cholera), Die from cause (उदा: died from overeating/wound)\n  - Rely / Depend / Congratulate + ON\n  - Look after (देखभाल करना), Look into (जांच करना), Look for (तलाश करना).",
            fileUrl = "https://jaybajrangakhada.org/materials/eng_ch06_prepositions_fixed.pdf",
            fileName = "06_Prepositions_Fixed_English_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi / English",
            isPublished = true,
            displayOrder = 69,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Prepositions and Fixed Prepositions Tricks",
            videoUrl = "https://youtube.com/results?search_query=prepositions+fixed+prepositions+english",
            practiceQuestionsCount = 40,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        ),
        Chapter(
            id = 70,
            subjectName = "English",
            chapterNumber = 7,
            chapterName = "Articles & Determiners (A, An, The एवं लोप नियम)",
            chapterOrder = 7,
            description = "Indefinite Articles (A, An का स्वर ध्वनि पर आधारित प्रयोग), Definite Article (The), Omission of Articles",
            notesContent = "• 'A' vs 'An' का सही नियम:\n  - अंग्रेजी अक्षरों (A, E, I, O, U) से नहीं, बल्कि हिंदी स्वर उच्चारण (अ, आ, इ, ई, उ, ऊ, ए, ऐ, ओ, औ) से तय होता है!\n  - 'An' स्वर ध्वनि से पहले: An honest man (ऑनेस्ट), An hour (आवर), An heir, An MLA (एम.एल.ए.), An MP, An FIR (एफ.आई.आर.), An umbrella.\n  - 'A' व्यंजन ध्वनि से पहले: A European (यूरोपियन - य), A university (य), A unique book, A one-rupee note (व - व्यंजन), A union.\n• Article 'The' का प्रयोग:\n  - नदियों, पहाड़ों की श्रृंखलाओं, महासागरों, धार्मिक ग्रंथों (The Ganga, The Himalayas, The Pacific, The Geeta).\n  - ऐतिहासिक इमारतों, अखबारों, दिशाओं, राजनीतिक दलों (The Taj Mahal, The Hindu, The East).\n  - Superlative Degree से पहले (The tallest boy, The most intelligent).\n  - Parallel structure में (The higher you go, the cooler you feel).\n• Omission of Articles (The न लगाएं):\n  - भाषाओं के नाम से पहले (English, Hindi), खेलों के नाम (Cricket, Football), भोजन के समय (Breakfast, Dinner), बीमारियों (Cholera, Fever), धातुओं (Gold is precious).",
            fileUrl = "https://jaybajrangakhada.org/materials/eng_ch07_articles_rules.pdf",
            fileName = "07_Articles_Rules_English_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi / English",
            isPublished = true,
            displayOrder = 70,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Articles A An The Complete Rules & Errors",
            videoUrl = "https://youtube.com/results?search_query=articles+a+an+the+english+rules",
            practiceQuestionsCount = 30,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        ),
        Chapter(
            id = 71,
            subjectName = "English",
            chapterNumber = 8,
            chapterName = "Active & Passive Voice (वाच्य परिवर्तन)",
            chapterOrder = 8,
            description = "Voice परिवर्तन के सामान्य सूत्र, Tense-wise Chart, Modals एवं Imperative Sentences का Voice बदलना",
            notesContent = "• Passive Voice का मूल नियम: Object → Subject + Helping Verb + V3 + by + Subject → Object.\n• Tense Transformation Chart:\n  - Simple Present (V1/V5) → is/am/are + V3\n  - Present Continuous (is/am/are + V4) → is/am/are + being + V3\n  - Present Perfect (has/have + V3) → has/have been + V3\n  - Simple Past (V2) → was/were + V3\n  - Past Continuous (was/were + V4) → was/were + being + V3\n  - Past Perfect (had + V3) → had been + V3\n  - Simple Future (will/shall + V1) → will/shall be + V3\n  - Future Perfect (will have + V3) → will have been + V3\n  - Modals (can/could/may/must + V1) → Modal + be + V3.\n• Imperative Sentences:\n  - Order/Command: Let + object + be + V3 (Shut the door → Let the door be shut).\n  - Advice: You are advised to + V1 (Work hard → You are advised to work hard).\n  - Request: Please/Kindly हटाकर You are requested to + V1.",
            fileUrl = "https://jaybajrangakhada.org/materials/eng_ch08_active_passive_voice.pdf",
            fileName = "08_Active_Passive_Voice_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi / English",
            isPublished = true,
            displayOrder = 71,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Active and Passive Voice Rules and Shortcuts",
            videoUrl = "https://youtube.com/results?search_query=active+passive+voice+tricks",
            practiceQuestionsCount = 35,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        ),
        Chapter(
            id = 72,
            subjectName = "English",
            chapterNumber = 9,
            chapterName = "Direct & Indirect Speech / Narration (कथन)",
            chapterOrder = 9,
            description = "Reporting Verb, Inverted Commas हटाना, Tense एवं Pronoun परिवर्तन, Time & Place शब्दों का रूपांतरण",
            notesContent = "• मूलभूत नियम:\n  - यदि Reporting Verb Present या Future में हो (says/will say), तो Reported Speech का Tense नहीं बदलता!\n  - यदि Reporting Verb Past में हो (said/said to), तो Tense निम्नानुसार बदलता है:\n    • Simple Present → Simple Past\n    • Present Continuous → Past Continuous\n    • Present Perfect → Past Perfect\n    • Simple Past (V2) → Past Perfect (had + V3)\n    • Past Continuous → Past Perfect Continuous\n    • will/shall → would/should, can → could, may → might.\n• Pronoun Change Rule (SON / 123):\n  - 1st Person (I, we) → Reporting Verb के Subject के अनुसार\n  - 2nd Person (you) → Reporting Verb के Object के अनुसार\n  - 3rd Person (he, she, it, they) → No Change.\n• Time and Place Changes:\n  - this → that, these → those, here → there, now → then, today → that day, yesterday → the previous day, tomorrow → the next day.\n• Universal Truth / Habitual Action होने पर Past Tense में भी Tense No Change रहता है (He said, 'The sun rises in the east' → He said that the sun rises in the east).",
            fileUrl = "https://jaybajrangakhada.org/materials/eng_ch09_direct_indirect_speech.pdf",
            fileName = "09_Direct_Indirect_Speech_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi / English",
            isPublished = true,
            displayOrder = 72,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Direct and Indirect Speech Narration Tricks",
            videoUrl = "https://youtube.com/results?search_query=direct+indirect+speech+rules+english",
            practiceQuestionsCount = 35,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        ),
        Chapter(
            id = 73,
            subjectName = "English",
            chapterNumber = 10,
            chapterName = "Adjectives & Degrees of Comparison (विशेषण)",
            chapterOrder = 10,
            description = "Positive, Comparative, Superlative Degrees; Than vs To; Order of Adjectives; Common Grammatical Pitfalls",
            notesContent = "• Three Degrees of Comparison:\n  1. Positive Degree: सामान्य गुण (tall, brave, good, difficult).\n  2. Comparative Degree: दो के बीच तुलना (taller, braver, better, more difficult + THAN).\n  3. Superlative Degree: सभी में श्रेष्ठ (the tallest, the bravest, the best, the most difficult).\n• Important Rules:\n  - Latin Adjectives (Senior, Junior, Prior, Anterior, Posterior, Superior, Inferior) के साथ 'than' नहीं 'to' लगाते हैं (He is senior to me).\n  - Preferable और Prefer के साथ 'to' आता है (Tea is preferable to coffee).\n  - Double Comparative या Double Superlative का प्रयोग गलत है (more taller गलत है, केवल taller सही है).\n  - Any other का प्रयोग: He is taller than any other boy in the class (other लगाना अनिवार्य है).\n  - Little (न के बराबर), A little (थोड़ा/सकारात्मक), The little (जो कुछ थोड़ा बचा है) - Uncountable हेतु।\n  - Few (न के बराबर), A few (कुछ), The few (जो कुछ गिने-चुने) - Countable Plural हेतु।",
            fileUrl = "https://jaybajrangakhada.org/materials/eng_ch10_adjectives_degrees.pdf",
            fileName = "10_Adjectives_Degrees_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi / English",
            isPublished = true,
            displayOrder = 73,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Adjectives and Degrees of Comparison Masterclass",
            videoUrl = "https://youtube.com/results?search_query=adjectives+degrees+of+comparison+rules",
            practiceQuestionsCount = 30,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        ),
        Chapter(
            id = 74,
            subjectName = "English",
            chapterNumber = 11,
            chapterName = "Adverbs & Inversion Rules (क्रियाविशेषण)",
            chapterOrder = 11,
            description = "Adverbs of Manner, Time, Frequency, Degree; Law of Inversion (Hardly, Scarcely, Seldom, Never से शुरुआत)",
            notesContent = "• Order of Adverbs in Sentence: M - P - T (Manner, Place, Time: He sang beautifully (M) at the akhada (P) yesterday (T)).\n• Frequency Adverbs (always, never, seldom, often, rarely): Main Verb से पहले तथा Helping Verb के बाद आते हैं (He always speaks the truth; He is never late).\n• Law of Inversion (वाक्य के प्रारंभ में नकारात्मक Adverb आने पर पहले Helping Verb फिर Subject आता है):\n  - Hardly / Scarcely had he reached the ground when it began to rain.\n  - Seldom have I seen such discipline.\n  - No sooner did the race start than the cadets sprinted.\n  - Not only did he win the gold medal, but he also broke the record.\n• 'Too...to' का प्रयोग: He is too weak to walk (वह इतना कमजोर है कि चल नहीं सकता).\n• 'Fairly' का प्रयोग सकारात्मक गुणों हेतु (fairly good), 'Rather' का प्रयोग अप्रिय गुणों हेतु (rather bad, rather hot).",
            fileUrl = "https://jaybajrangakhada.org/materials/eng_ch11_adverbs_inversion.pdf",
            fileName = "11_Adverbs_Inversion_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi / English",
            isPublished = true,
            displayOrder = 74,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Adverbs and Law of Inversion Grammar Rules",
            videoUrl = "https://youtube.com/results?search_query=adverbs+law+of+inversion+rules",
            practiceQuestionsCount = 25,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        ),
        Chapter(
            id = 75,
            subjectName = "English",
            chapterNumber = 12,
            chapterName = "Conjunctions & Connectors (संयोजक शब्द)",
            chapterOrder = 12,
            description = "Correlative Conjunctions (Either...or, Neither...nor, Not only...but also, Scarcely...when, No sooner...than)",
            notesContent = "• Important Correlative Pairs (परीक्षा में बार-बार पूछे जाने वाले):\n  1. Neither ... NOR (or नहीं आता)\n  2. Either ... OR\n  3. Not only ... BUT ALSO\n  4. Both ... AND (as well as नहीं आता)\n  5. Although / Though ... YET (but नहीं आता)\n  6. Hardly / Scarcely ... WHEN / BEFORE (than नहीं आता)\n  7. No sooner ... THAN (then या when नहीं आता)\n  8. Lest ... SHOULD (lest के साथ negative word 'not' कभी नहीं आता! उदा: Run fast lest you should miss the train).\n  9. Between ... AND (to नहीं आता: Between 10 AM and 5 PM).\n  10. From ... TO (From 10 AM to 5 PM).\n• Until vs Unless:\n  - Until = समय सूचक (जब तक नहीं)\n  - Unless = शर्त सूचक (यदि नहीं)\n  - दोनों स्वयं Negative हैं, इसलिए इनके साथ 'not' नहीं आता।",
            fileUrl = "https://jaybajrangakhada.org/materials/eng_ch12_conjunctions_rules.pdf",
            fileName = "12_Conjunctions_Rules_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi / English",
            isPublished = true,
            displayOrder = 75,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Conjunctions Correlative Pairs and Common Errors",
            videoUrl = "https://youtube.com/results?search_query=conjunctions+pairs+english+grammar",
            practiceQuestionsCount = 30,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        ),
        Chapter(
            id = 76,
            subjectName = "English",
            chapterNumber = 13,
            chapterName = "High Frequency Synonyms (समानार्थी शब्द)",
            chapterOrder = 13,
            description = "सेना, अर्धसैनिक बल एवं पुलिस भर्ती परीक्षाओं में पिछले 10 वर्षों में सर्वाधिक पूछे गए 50+ Synonyms हिंदी अर्थ सहित",
            notesContent = "• Top Defence & Police Exam Synonyms:\n1. Abandon = Leave / Forsake (त्याग देना)\n2. Abundant = Plentiful / Ample (प्रचुर / अत्यधिक)\n3. Accurate = Precise / Correct (सटीक)\n4. Adversity = Misfortune / Hardship (विपत्ति / संकट)\n5. Authentic = Genuine / Real (प्रामाणिक)\n6. Benevolent = Kind / Generous (परोपकारी / दयालु)\n7. Candid = Frank / Honest (स्पष्टवादी)\n8. Cease = Stop / Terminate (रोकना / समाप्त करना)\n9. Courageous = Brave / Valorous (साहसी / वीर)\n10. Diligent = Hardworking / Industrious (परिश्रमी)\n11. Eliminate = Remove / Eradicate (हटाना / निकालना)\n12. Feasible = Practical / Viable (संभव / व्यावहारिक)\n13. Grateful = Thankful / Obliged (कृतज्ञ)\n14. Hostile = Unfriendly / Antagonistic (शत्रुतापूर्ण)\n15. Immaculate = Pure / Spotless (निर्मल / बेदाग)\n16. Lethal = Deadly / Fatal (जानलेवा / घातक)\n17. Mendacious = Untruthful / False (झूठा)\n18. Obsolete = Outdated / Archaic (अप्रचलित)\n19. Placid = Calm / Peaceful (शांत)\n20. Reluctant = Unwilling / Hesitant (अनिच्छुक)\n21. Scanty = Meagre / Inadequate (अल्प / न्यून)\n22. Tranquil = Serene / Quiet (शांत / स्थिर)\n23. Vigilant = Watchful / Alert (सतर्क / जागरूक)\n24. Zenith = Peak / Apex / Summit (शिखर / उच्चतम बिंदु)\n25. Zeal = Enthusiasm / Passion (उत्साह / जोश).",
            fileUrl = "https://jaybajrangakhada.org/materials/eng_ch13_synonyms_vocab.pdf",
            fileName = "13_Synonyms_Vocabulary_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi / English",
            isPublished = true,
            displayOrder = 76,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Top 50 Synonyms for Defence and Police Exams",
            videoUrl = "https://youtube.com/results?search_query=synonyms+for+defence+police+exams",
            practiceQuestionsCount = 50,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        ),
        Chapter(
            id = 77,
            subjectName = "English",
            chapterNumber = 14,
            chapterName = "High Frequency Antonyms (विलोम शब्द)",
            chapterOrder = 14,
            description = "प्रतियोगी परीक्षाओं के अति-महत्वपूर्ण Antonyms (Opposite Words) हिंदी अर्थ एवं परीक्षा उदाहरण सहित",
            notesContent = "• Top Antonyms (विपरीतार्थक शब्द):\n1. Acquit (दोषमुक्त करना) × Condemn / Convict (दोषी ठहराना)\n2. Ancient (प्राचीन) × Modern (आधुनिक)\n3. Arrogant (घमंडी) × Humble / Modest (विनम्र)\n4. Barren (बंजर / अनुपजाऊ) × Fertile (उपजाऊ)\n5. Boon (वरदान) × Bane / Curse (अभिशाप)\n6. Captivity (कैद / बंधन) × Freedom / Liberty (स्वतंत्रता)\n7. Courage (साहस) × Cowardice (कायरता)\n8. Despair (निराशा) × Hope (आशा)\n9. Eminent (प्रसिद्ध / प्रख्यात) × Unknown / Obscure (अज्ञात)\n10. Frugal (मितव्ययी / कम खर्च करने वाला) × Extravagant (फिजूलखर्च)\n11. Guilty (दोषी) × Innocent (निर्दोष)\n12. Hopeful (आशावादी) × Pessimistic (निराशावादी)\n13. Ignorance (अज्ञानता) × Knowledge (ज्ञान)\n14. Lenient (उदार / नरम) × Strict / Severe (कठोर)\n15. Mourn (शोक मनाना) × Rejoice / Celebrate (हर्षोल्लास मनाना)\n16. Optimist (आशावादी) × Pessimist (निराशावादी)\n17. Permanent (स्थाई) × Temporary / Transient (अस्थाई)\n18. Rigorous (कड़ा / कठोर) × Easy / Flexible (लचीला)\n19. Shallow (उथला) × Deep / Profound (गहरा)\n20. Trivial (तुच्छ / नगण्य) × Significant / Important (महत्वपूर्ण)\n21. Voluntary (ऐच्छिक) × Compulsory / Mandatory (अनिवार्य)\n22. Zenith (शीर्ष) × Nadir (निम्नतम बिंदु).",
            fileUrl = "https://jaybajrangakhada.org/materials/eng_ch14_antonyms_vocab.pdf",
            fileName = "14_Antonyms_Vocabulary_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi / English",
            isPublished = true,
            displayOrder = 77,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Top Antonyms for SSC GD, Army GD & Police Exams",
            videoUrl = "https://youtube.com/results?search_query=antonyms+opposite+words+defence+exams",
            practiceQuestionsCount = 50,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        ),
        Chapter(
            id = 78,
            subjectName = "English",
            chapterNumber = 15,
            chapterName = "One Word Substitution & Idioms (अनेक शब्दों के लिए एक शब्द व मुहावरे)",
            chapterOrder = 15,
            description = "100+ One Word Substitutions और सर्वाधिक प्रचलित Idioms & Phrases हिंदी भावार्थ एवं उदाहरण सहित",
            notesContent = "• High-Scoring One Word Substitutions:\n1. A person who looks at the bright side of things = Optimist (आशावादी)\n2. A person who looks at the dark side of things = Pessimist (निराशावादी)\n3. One who believes in God = Theist (आस्तिक)\n4. One who does not believe in God = Atheist (नास्तिक)\n5. Present everywhere = Omnipresent (सर्वव्यापी)\n6. All powerful = Omnipotent (सर्वशक्तिमान)\n7. Knowing everything = Omniscient (सर्वज्ञ)\n8. A speech delivered without previous preparation = Extempore (तात्कालिक भाषण)\n9. A cure for all diseases = Panacea (रामबाण औषधि)\n10. One who loves mankind = Philanthropist (मानव प्रेमी / परोपकारी)\n11. One who hates mankind = Misanthrope (मानव द्वेषी)\n12. That which cannot be read = Illegible (अपठनीय)\n13. That which cannot be heard = Inaudible (अश्रव्य)\n14. Capable of being easily broken = Brittle (भंगुर)\n15. Life history of a man written by himself = Autobiography (आत्म-कथा)\n16. Life history written by someone else = Biography (जीवनी)\n17. One who eats no animal flesh = Vegetarian (शाकाहारी)\n18. One who can speak two languages = Bilingual (द्विभाषी).\n• Essential Idioms & Phrases:\n1. A piece of cake = Very easy task (बहुत सरल कार्य)\n2. At the eleventh hour = At the last moment (अंतिम क्षण में)\n3. Break the ice = To start a conversation (वार्तालाप शुरू करना)\n4. Burn the midnight oil = Work/study hard late into the night (देर रात तक कड़ी मेहनत करना)\n5. By leaps and bounds = Very rapidly (दिन दूनी रात चौगुनी गति से)\n6. Once in a blue moon = Very rarely (कभी-कभार / ईद का चाँद)\n7. Through thick and thin = In all circumstances / difficulties (हर सुख-दुख में साथ निभाना)\n8. Spill the beans = Reveal a secret (रहस्य उजागर करना)\n9. Hit the nail on the head = Say or do exactly the right thing (सटीक बात कहना)\n10. Crocodile tears = False sorrow (दिखावे के आंसू).",
            fileUrl = "https://jaybajrangakhada.org/materials/eng_ch15_one_word_idioms.pdf",
            fileName = "15_One_Word_Substitution_Idioms_Notes.pdf",
            examType = "All Defence & Police Exams",
            language = "Hindi / English",
            isPublished = true,
            displayOrder = 78,
            createdAt = "2026-08-30",
            updatedAt = "2026-08-30",
            videoTitle = "Top 100 One Word Substitutions & Idioms",
            videoUrl = "https://youtube.com/results?search_query=one+word+substitution+idioms+defence",
            practiceQuestionsCount = 50,
            isCompleted = false,
            progressPercentage = 0,
            isTodayTarget = false
        )
    )
}
