package com.codealpha.chatbot.engine;

import com.codealpha.chatbot.model.ConversationContext;
import com.codealpha.chatbot.model.Intent;
import com.codealpha.chatbot.nlp.EntityExtractor.ExtractedEntities;
import com.codealpha.chatbot.nlp.SentimentAnalyzer.SentimentResult;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Generates natural conversational responses, contextual suggestions,
 * and dynamic answers for utilities, jokes, quotes, and greetings.
 */
public class ResponseGenerator {

    private static final List<String> GREETINGS = Arrays.asList(
        "Hello! I am your CodeAlpha AI Assistant. How can I assist you with Java programming, internship tasks, or general tech queries today?",
        "Hi there! Welcome back. What would you like to explore today — Java concepts, internship guidance, or NLP algorithms?",
        "Hey! Great to see you. How can I help you today?"
    );

    private static final List<String> FAREWELLS = Arrays.asList(
        "Goodbye! Best of luck with your CodeAlpha projects and coding journey. Feel free to come back anytime!",
        "Farewell! Keep coding, learning, and building great software. Have a wonderful day!",
        "Bye! Don't hesitate to reach out if you need help with your Java tasks again."
    );

    private static final List<String> TECH_JOKES = Arrays.asList(
        "Why do Java developers wear glasses? Because they don't C#! 😂",
        "There are 10 types of people in the world: those who understand binary, and those who don't.",
        "A SQL query walks into a bar, walks up to two tables and asks: 'Can I join you?' 🍺",
        "Why do programmers prefer dark mode? Because light attracts bugs! 🐛",
        "How many programmers does it take to change a light bulb? None, it's a hardware problem! 💡",
        "Real programmers count from 0. ☕",
        "Java is to JavaScript what Car is to Carpet."
    );

    private static final List<String> MOTIVATIONAL_QUOTES = Arrays.asList(
        "\"Talk is cheap. Show me the code.\" — Linus Torvalds",
        "\"Programs must be written for people to read, and only incidentally for machines to execute.\" — Harold Abelson",
        "\"The best error message is the one that never shows up.\" — Thomas Fuchs",
        "\"First, solve the problem. Then, write the code.\" — John Johnson",
        "\"Experience is the name everyone gives to their mistakes.\" — Oscar Wilde",
        "\"Simplicity is prerequisite for reliability.\" — Edsger W. Dijkstra"
    );

    private static final Random RANDOM = new Random();

    public String generateNameIntro(String name) {
        if (name != null && !name.isEmpty()) {
            return "Nice to meet you, **" + name + "**! 👋 I'm your CodeAlpha AI Assistant. How can I help you today with your Java programming or internship tasks?";
        }
        return "Nice to meet you! How can I assist you today?";
    }

    public String generateFeedbackPositiveResponse() {
        String[] options = {
            "Thank you so much for the kind words! 😊 I'm thrilled to help you excel in your Java coding journey.",
            "You're awesome! 🎉 I really appreciate your positive feedback. Feel free to ask more questions anytime.",
            "Glad to hear that! 🚀 Helping you build great projects is my top priority!"
        };
        return options[RANDOM.nextInt(options.length)];
    }

    public String generateFeedbackNegativeResponse() {
        return "I'm sorry to hear that! 🙁 I am continuously improving. You can teach me the correct answer with `teach: Question -> Answer`, or rephrase your question so I can assist you better.";
    }

    public String generateGreeting(ConversationContext context, ExtractedEntities entities) {
        String name = (entities != null && entities.getPersonName() != null) ? entities.getPersonName() : context.getUserName();
        if (name != null && !name.isEmpty()) {
            return "Hello, " + name + "! 👋 How can I help you with your Java projects, internship tasks, or code queries today?";
        }
        return GREETINGS.get(RANDOM.nextInt(GREETINGS.size()));
    }

    public String generateFarewell(ConversationContext context) {
        String name = context.getUserName();
        if (name != null) {
            return "Goodbye, " + name + "! Keep coding and best of luck with your CodeAlpha projects! 🚀";
        }
        return FAREWELLS.get(RANDOM.nextInt(FAREWELLS.size()));
    }

    public String generateThanksResponse() {
        String[] options = {
            "You're very welcome! Let me know if you need any more explanations or code assistance.",
            "Happy to help! Feel free to ask anytime you have questions on Java, OOP, or algorithms.",
            "Glad I could help! Keep up the great work on your internship tasks! 🚀"
        };
        return options[RANDOM.nextInt(options.length)];
    }

    public String generateBotIdentity() {
        return "🤖 **CodeAlpha AI Assistant (v2.0)**\n\n" +
               "I am an intelligent Java-based Chatbot engineered for the CodeAlpha Internship Program.\n" +
               "• **Core Engine**: Pure Java SE with zero external runtime dependencies.\n" +
               "• **NLP Stack**: Porter Stemmer, Tokenizer, Stopwords Filter, N-Gram TF-IDF Vectorizer, Cosine Similarity, and Levenshtein Distance.\n" +
               "• **ML Layer**: Multinomial Naive Bayes Classifier with Laplace smoothing for Intent Recognition.\n" +
               "• **Features**: Dynamic Knowledge Base, 'Teach Bot' dynamic training, Math Expression Engine, Real-time Sentiment Analysis, and Dual Swing GUI + Modern Web interface!";
    }

    public String generateBotCapabilities() {
        return "✨ **What I Can Do For You:**\n\n" +
               "1. 💡 **CodeAlpha Guidance**: Answer questions about internship tasks (Task 1 to 4), submission criteria, GitHub repos, and certificates.\n" +
               "2. ☕ **Java & OOP Explanations**: Explain OOP pillars, Collections (HashMap, ArrayList), Multithreading, JVM, Garbage Collection, Streams, and Lambdas.\n" +
               "3. 🧠 **AI & NLP Concepts**: Clarify Tokenization, TF-IDF, Naive Bayes, Cosine Similarity, and Machine Learning pipelines.\n" +
               "4. ➕ **Math Calculator**: Evaluate expressions like `calculate 25 * 4`, `sqrt(144) + 12`, `2^8`.\n" +
               "5. 🎓 **Teach Bot Mode**: Teach me new FAQs dynamically via `teach: Question -> Answer`.\n" +
               "6. 🌦️ **Weather & Time**: Provide current time, date, and simulated city forecasts.\n" +
               "7. 🎭 **Fun & Motivation**: Share tech jokes and coding quotes!";
    }

    public String generateHelp() {
        return "📖 **Chatbot Quick Help & Command Guide:**\n\n" +
               "• Ask questions directly, e.g.:\n" +
               "  - *'What are the CodeAlpha tasks?'*\n" +
               "  - *'Explain OOP pillars in Java'*\n" +
               "  - *'What is TF-IDF in NLP?'*\n" +
               "  - *'How to submit CodeAlpha projects?'*\n" +
               "• **Math**: `calculate (50 + 25) * 3` or `what is sqrt(256)`\n" +
               "• **Time / Date**: `what time is it?` or `current date`\n" +
               "• **Teach Bot**: `teach: What is JDK? -> JDK is the Java Development Kit.`\n" +
               "• **Humor & Wisdom**: `tell me a joke` or `inspire me`\n" +
               "• **Clear Chat**: Click 'Clear Chat' or type `/clear`";
    }

    public String generateTimeDate() {
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
        DateTimeFormatter ttf = DateTimeFormatter.ofPattern("hh:mm:ss a");

        return "🕒 **Current System Clock & Date:**\n" +
               "• **Time**: " + time.format(ttf) + "\n" +
               "• **Date**: " + date.format(dtf);
    }

    public String generateWeather(String location) {
        if (location == null || location.isEmpty()) {
            location = "your current location";
        }
        int temp = 20 + RANDOM.nextInt(12);
        String[] conditions = {"Sunny with clear skies ☀️", "Partly Cloudy ⛅", "Light Breeze 🍃", "Pleasant & Mild 🌤️"};
        String cond = conditions[RANDOM.nextInt(conditions.length)];

        return String.format("🌦️ **Weather Forecast for %s**:\n• **Condition**: %s\n• **Temperature**: %d°C (%d°F)\n• **Humidity**: %d%%\n• **Wind**: %d km/h",
            location, cond, temp, (int)(temp * 1.8 + 32), 45 + RANDOM.nextInt(35), 8 + RANDOM.nextInt(15));
    }

    public String generateJoke() {
        return "😄 " + TECH_JOKES.get(RANDOM.nextInt(TECH_JOKES.size()));
    }

    public String generateMotivationalQuote() {
        return "💡 " + MOTIVATIONAL_QUOTES.get(RANDOM.nextInt(MOTIVATIONAL_QUOTES.size()));
    }

    public String generateFallback(String userQuery, SentimentResult sentiment) {
        if (sentiment != null && sentiment.getScore() < -0.4) {
            return "I sincerely apologize that I couldn't provide the exact answer you were looking for. " +
                   "I am continuously learning! You can teach me the answer right now by typing:\n\n" +
                   "`teach: " + userQuery + " -> [Your Answer Here]`\n\n" +
                   "Or ask me about CodeAlpha tasks, Java OOP, or math calculations.";
        }

        return "🤔 I am not completely sure about that query yet, but I am learning every day!\n\n" +
               "You can:\n" +
               "1. Rephrase your question (e.g. *'What are the 4 OOP pillars?'* or *'CodeAlpha tasks'*)\n" +
               "2. Teach me the answer with:\n" +
               "   `teach: " + (userQuery.length() > 30 ? userQuery.substring(0, 30) + "..." : userQuery) + " -> [Answer]`\n" +
               "3. Explore recommended topics below 👇";
    }

    public List<String> getSuggestedChipsForIntent(Intent intent) {
        List<String> chips = new ArrayList<>();
        switch (intent) {
            case GREETING:
            case BOT_IDENTITY:
            case BOT_CAPABILITIES:
            case HELP:
                chips.add("CodeAlpha Tasks");
                chips.add("Explain OOP in Java");
                chips.add("What is TF-IDF?");
                chips.add("Tell me a joke");
                break;
            case CODEALPHA_INTERNSHIP:
                chips.add("How to submit tasks?");
                chips.add("Internship perks");
                chips.add("Completion criteria");
                chips.add("Task 3 details");
                break;
            case PROGRAMMING_JAVA:
                chips.add("HashMap vs ArrayList");
                chips.add("Multithreading in Java");
                chips.add("Java Streams & Lambda");
                chips.add("JVM vs JRE vs JDK");
                break;
            case AI_NLP_CONCEPTS:
                chips.add("What is TF-IDF?");
                chips.add("What is Naive Bayes?");
                chips.add("NLP Pipeline steps");
                chips.add("Porter Stemmer algorithm");
                break;
            default:
                chips.add("Java OOP Pillars");
                chips.add("CodeAlpha FAQ");
                chips.add("Calculate 2^10");
                chips.add("Inspire me");
                break;
        }
        return chips;
    }
}
