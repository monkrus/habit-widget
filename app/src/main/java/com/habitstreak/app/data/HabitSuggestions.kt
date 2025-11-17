package com.habitstreak.app.data

/**
 * Curated list of popular habit suggestions for new users
 */
object HabitSuggestions {

    data class SuggestedHabit(
        val name: String,
        val emoji: String,
        val category: String
    )

    val suggestions = listOf(
        // Health & Fitness
        SuggestedHabit("Drink water", "💧", "Health & Fitness"),
        SuggestedHabit("Exercise", "💪", "Health & Fitness"),
        SuggestedHabit("Go for a walk", "🚶", "Health & Fitness"),
        SuggestedHabit("Stretch", "🧘", "Health & Fitness"),
        SuggestedHabit("Take vitamins", "💊", "Health & Fitness"),
        SuggestedHabit("Track calories", "🥗", "Health & Fitness"),

        // Wellness & Mind
        SuggestedHabit("Meditate", "🧘‍♀️", "Wellness & Mind"),
        SuggestedHabit("Journal", "📓", "Wellness & Mind"),
        SuggestedHabit("Gratitude practice", "🙏", "Wellness & Mind"),
        SuggestedHabit("Deep breathing", "😌", "Wellness & Mind"),
        SuggestedHabit("Get 8 hours sleep", "😴", "Wellness & Mind"),
        SuggestedHabit("No phone before bed", "📵", "Wellness & Mind"),

        // Learning & Growth
        SuggestedHabit("Read", "📚", "Learning & Growth"),
        SuggestedHabit("Learn a language", "🌍", "Learning & Growth"),
        SuggestedHabit("Practice instrument", "🎸", "Learning & Growth"),
        SuggestedHabit("Watch educational content", "🎓", "Learning & Growth"),
        SuggestedHabit("Listen to podcast", "🎧", "Learning & Growth"),
        SuggestedHabit("Take online course", "💻", "Learning & Growth"),

        // Productivity
        SuggestedHabit("Make bed", "🛏️", "Productivity"),
        SuggestedHabit("Plan tomorrow", "📝", "Productivity"),
        SuggestedHabit("Clean workspace", "🧹", "Productivity"),
        SuggestedHabit("Inbox zero", "📧", "Productivity"),
        SuggestedHabit("Focus time", "🎯", "Productivity"),
        SuggestedHabit("Review goals", "✅", "Productivity"),

        // Social & Relationships
        SuggestedHabit("Call family", "☎️", "Social & Relationships"),
        SuggestedHabit("Text a friend", "💬", "Social & Relationships"),
        SuggestedHabit("Quality time with loved ones", "❤️", "Social & Relationships"),
        SuggestedHabit("Random act of kindness", "🤝", "Social & Relationships"),

        // Creative
        SuggestedHabit("Draw", "🎨", "Creative"),
        SuggestedHabit("Write", "✍️", "Creative"),
        SuggestedHabit("Take photos", "📸", "Creative"),
        SuggestedHabit("Practice hobby", "🎭", "Creative"),

        // Self-Care
        SuggestedHabit("Skincare routine", "🧴", "Self-Care"),
        SuggestedHabit("Take a break", "☕", "Self-Care"),
        SuggestedHabit("Unplug from tech", "🔌", "Self-Care"),
        SuggestedHabit("Spend time in nature", "🌳", "Self-Care"),
    )

    fun getByCategory(): Map<String, List<SuggestedHabit>> {
        return suggestions.groupBy { it.category }
    }

    fun getPopular(count: Int = 10): List<SuggestedHabit> {
        return listOf(
            SuggestedHabit("Drink water", "💧", "Health & Fitness"),
            SuggestedHabit("Exercise", "💪", "Health & Fitness"),
            SuggestedHabit("Read", "📚", "Learning & Growth"),
            SuggestedHabit("Meditate", "🧘‍♀️", "Wellness & Mind"),
            SuggestedHabit("Journal", "📓", "Wellness & Mind"),
            SuggestedHabit("Go for a walk", "🚶", "Health & Fitness"),
            SuggestedHabit("Get 8 hours sleep", "😴", "Wellness & Mind"),
            SuggestedHabit("Make bed", "🛏️", "Productivity"),
            SuggestedHabit("Gratitude practice", "🙏", "Wellness & Mind"),
            SuggestedHabit("Stretch", "🧘", "Health & Fitness"),
        ).take(count)
    }
}
