package com.example.data.recruitment

enum class EligibilityStatus(val labelHindi: String, val badgeColorHex: String) {
    LIKELY_ELIGIBLE("संभावित योग्य", "#16A34A"), // Green
    MAY_BE_ELIGIBLE("संभवतः योग्य – चेक नोटिफिकेशन", "#D97706"), // Amber
    NEEDS_IMPROVEMENT("सुधार आवश्यक", "#EA580C"), // Orange/Red
    INSUFFICIENT_DATA("प्रोफाइल डेटा अपूर्ण", "#64748B") // Gray
}

enum class StandardStatus(val labelHindi: String) {
    ACHIEVED("लक्ष्य प्राप्त (उत्कृष्ट)"),
    NEEDS_IMPROVEMENT("सुधार आवश्यक"),
    NO_DATA("डेटा उपलब्ध नहीं")
}

data class EligibilityCriterionResult(
    val criterionName: String,
    val isMet: Boolean?, // true: met, false: not met, null: data missing / check notif
    val studentValue: String,
    val requiredValue: String,
    val statusTextHindi: String,
    val guidanceHindi: String
)

data class StudentEligibilityReport(
    val studentId: String,
    val recruitmentName: String,
    val category: String,
    val overallStatus: EligibilityStatus,
    val overallScorePercentage: Int,
    val criteria: List<EligibilityCriterionResult>,
    val actionAdviceHindi: String,
    val disclaimerText: String = "अंतिम पात्रता नवीनतम आधिकारिक भर्ती अधिसूचना (Official Notification) पर निर्भर करती है।"
)

data class PhysicalStandardComparisonItem(
    val metricName: String,
    val iconEmoji: String,
    val studentCurrentValue: String,
    val targetRequirement: String,
    val status: StandardStatus,
    val progressRatio: Float, // 0.0 to 1.0+
    val coachingAdviceHindi: String
)

data class RecruitmentCategoryProfile(
    val id: String,
    val nameHindi: String,
    val nameEnglish: String,
    val organization: String,
    val badgeLabel: String,
    val iconEmoji: String,
    val minAge: Double = 17.5,
    val maxAge: Double = 23.0,
    val ageRelaxationNotesHindi: String = "आरक्षित वर्गों (OBC/SC/ST) हेतु नियमानुसार 3 से 5 वर्ष की छूट",
    val requiredEducationHindi: String,
    val minEducationLevelRank: Int = 2, // 1: 8th, 2: 10th, 3: 12th, 4: Graduate
    val minHeightMaleCm: Double = 168.0,
    val minHeightFemaleCm: Double = 157.0,
    val minChestMaleCm: Double = 77.0,
    val minChestExpansionCm: Double = 5.0,
    val target1600mSeconds: Int = 345, // 5:45 min (345s)
    val targetPullups: Int = 10,
    val targetLongJumpFeet: Double = 9.0,
    val targetHighJumpFeet: Double = 4.0,
    val targetShotPutMeters: Double = 7.5,
    val runningDescriptionHindi: String,
    val physicalEfficiencyOverviewHindi: String,
    val writtenExamPatternHindi: String,
    val selectionProcessHindi: String,
    val requiredDocumentsHindi: List<String>,
    val medicalRequirementsHindi: List<String>,
    val preparationTipsHindi: List<String>,
    val officialWebsiteUrl: String
)
