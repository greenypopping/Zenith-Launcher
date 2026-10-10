package com.example.ui.components

object ZenQuotes {
    val quotes = listOf(
        "Simplicity is the ultimate sophistication." to "Leonardo da Vinci",
        "Silence is a source of great strength." to "Lao Tzu",
        "You have power over your mind, not outside events." to "Marcus Aurelius",
        "The soul becomes dyed with the color of its thoughts." to "Marcus Aurelius",
        "In the midst of movement, keep stillness inside of you." to "Deepak Chopra",
        "Do less, with full attention." to "Zen Proverb",
        "When you realize nothing is lacking, the whole world belongs to you." to "Lao Tzu",
        "Within you, there is a stillness and a sanctuary." to "Hermann Hesse"
    )

    fun getQuote(index: Int): Pair<String, String> {
        return quotes[index % quotes.size]
    }
}
