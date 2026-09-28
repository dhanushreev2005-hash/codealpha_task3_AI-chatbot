package com.codealpha.chatbot.model;

/**
 * Enumeration of recognized intents within the NLP engine.
 */
public enum Intent {
    GREETING("Greeting", "General greetings and pleasantries"),
    FAREWELL("Farewell", "Goodbyes and session exits"),
    THANKS("Thanks", "Expressions of gratitude"),
    BOT_IDENTITY("Bot Identity", "Questions regarding the chatbot identity and origin"),
    BOT_CAPABILITIES("Bot Capabilities", "Inquiries about features, abilities, and commands"),
    HELP("Help", "Requests for assistance or command listing"),
    TIME_DATE("Time and Date", "Inquiries about current time, date, day, or year"),
    MATH_CALCULATION("Math Calculation", "Arithmetic, algebraic or scientific math queries"),
    WEATHER("Weather", "Weather inquiries and forecasts"),
    CODEALPHA_INTERNSHIP("CodeAlpha Internship", "CodeAlpha tasks, rules, perks, submission"),
    PROGRAMMING_JAVA("Java Programming", "Java language syntax, OOP, concurrency, collections"),
    AI_NLP_CONCEPTS("AI & NLP", "Machine learning, tokenization, TF-IDF, neural networks"),
    TECH_JOKE("Tech Joke", "Programming and computer science humor"),
    MOTIVATIONAL_QUOTE("Motivational Quote", "Inspirational quotes for developers"),
    TEACH_BOT("Teach Bot", "User teaching the bot new custom FAQ answers"),
    FAQ_QUERY("FAQ Query", "General domain knowledge and pre-trained FAQ questions"),
    FEEDBACK_POSITIVE("Positive Feedback", "Compliments or positive appraisal"),
    FEEDBACK_NEGATIVE("Negative Feedback", "Criticism or dissatisfaction"),
    UNKNOWN_FALLBACK("Unknown / Fallback", "Query not matched with high confidence");

    private final String displayName;
    private final String description;

    Intent(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
