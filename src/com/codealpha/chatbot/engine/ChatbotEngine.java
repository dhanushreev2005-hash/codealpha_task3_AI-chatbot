package com.codealpha.chatbot.engine;

import com.codealpha.chatbot.model.*;
import com.codealpha.chatbot.nlp.*;
import com.codealpha.chatbot.nlp.EntityExtractor.ExtractedEntities;
import com.codealpha.chatbot.nlp.SentimentAnalyzer.SentimentResult;

import java.util.*;

/**
 * Main AI Chatbot Engine coordinating the NLP pipeline, Machine Learning intent classifier,
 * Knowledge Base semantic search, conversational context, and dynamic response generation.
 */
public class ChatbotEngine {
    private final Tokenizer tokenizer;
    private final SentimentAnalyzer sentimentAnalyzer;
    private final EntityExtractor entityExtractor;
    private final IntentClassifier intentClassifier;
    private final KnowledgeBase knowledgeBase;
    private final ResponseGenerator responseGenerator;
    private final ConversationContext conversationContext;

    public ChatbotEngine() {
        this.tokenizer = new Tokenizer();
        this.sentimentAnalyzer = new SentimentAnalyzer();
        this.entityExtractor = new EntityExtractor();
        this.intentClassifier = new IntentClassifier();
        this.knowledgeBase = new KnowledgeBase();
        this.responseGenerator = new ResponseGenerator();
        this.conversationContext = new ConversationContext();
    }

    /**
     * Processes a user query end-to-end through the NLP pipeline and returns a structured BotResponse.
     */
    public synchronized BotResponse processQuery(String userQuery) {
        long startTime = System.currentTimeMillis();

        if (userQuery == null || userQuery.trim().isEmpty()) {
            return new BotResponse("Please enter a message or question so I can assist you!",
                Intent.UNKNOWN_FALLBACK, 1.0, new SentimentResult(SentimentAnalyzer.SentimentLevel.NEUTRAL, 0.0),
                "EmptyValidation", Collections.emptyList(), Collections.singletonList("Show Help"), 1);
        }

        String rawInput = userQuery.trim();

        // 1. NLP Step: Sentiment Analysis
        SentimentResult sentiment = sentimentAnalyzer.analyze(rawInput);

        // 2. NLP Step: Entity Extraction
        ExtractedEntities entities = entityExtractor.extract(rawInput);
        if (entities.getPersonName() != null) {
            conversationContext.setUserName(entities.getPersonName());
        }

        // 3. NLP Step: Intent Classification
        IntentClassifier.ClassificationResult classification = intentClassifier.classify(rawInput);
        Intent intent = classification.getIntent();
        double confidence = classification.getConfidence();
        String algorithmUsed = classification.getAlgorithm();

        String responseText;

        // 4. Branching Logic based on Entities & Intent

        // A. Handle "Teach Bot" (dynamic training)
        if (entities.getTeachQuestion() != null && entities.getTeachAnswer() != null) {
            String newQ = entities.getTeachQuestion();
            String newA = entities.getTeachAnswer();
            String id = "custom_" + System.currentTimeMillis();

            FAQEntry newEntry = new FAQEntry(id, "Custom / User Trained", newQ, newA,
                Collections.singletonList(newQ), Arrays.asList("custom", "user-trained"));
            knowledgeBase.addEntry(newEntry);
            intentClassifier.addCustomTrainingData(Intent.FAQ_QUERY, newQ);

            responseText = String.format("🎉 **Knowledge Successfully Learned!**\n\n" +
                "• **Question**: *\"%s\"*\n• **Stored Answer**: *\"%s\"*\n\n" +
                "I have updated my TF-IDF vector index and Naive Bayes model. You can test asking this question right away!",
                newQ, newA);
            intent = Intent.TEACH_BOT;
            confidence = 1.0;
            algorithmUsed = "DynamicKnowledgeTrainer";
        }
        // B. Handle Person Introduction (e.g. "My name is ...", "I am ...")
        else if (entities.getPersonName() != null && (rawInput.toLowerCase().contains("name is") || rawInput.toLowerCase().startsWith("i am ") || rawInput.toLowerCase().startsWith("call me "))) {
            responseText = responseGenerator.generateNameIntro(entities.getPersonName());
            intent = Intent.GREETING;
            confidence = 0.98;
            algorithmUsed = "EntityRecognition_Name";
        }
        // C. Handle Math Calculation
        else if (entities.getMathExpression() != null) {
            try {
                double result = MathEvaluator.eval(entities.getMathExpression());
                String formatted = (result == (long) result) ? String.format("%d", (long) result) : String.format("%.4f", result);
                responseText = String.format("🔢 **Calculation Result:**\n`%s` = **%s**", entities.getMathExpression().trim(), formatted);
                intent = Intent.MATH_CALCULATION;
                confidence = 0.99;
                algorithmUsed = "MathEvaluator_AST";
            } catch (Exception e) {
                responseText = "⚠️ Could not evaluate math expression: *" + e.getMessage() + "*\nPlease verify your numbers and operators (e.g. `calculate 45 * 12` or `sqrt(144)`).";
                intent = Intent.MATH_CALCULATION;
                confidence = 0.7;
                algorithmUsed = "MathEvaluator_Error";
            }
        }
        // D. Handle Weather
        else if (intent == Intent.WEATHER || entities.getLocation() != null) {
            responseText = responseGenerator.generateWeather(entities.getLocation());
            confidence = 0.95;
            algorithmUsed = "WeatherSimulationService";
        }
        // E. Handle Greetings
        else if (intent == Intent.GREETING) {
            responseText = responseGenerator.generateGreeting(conversationContext, entities);
        }
        // F. Handle Farewells
        else if (intent == Intent.FAREWELL) {
            responseText = responseGenerator.generateFarewell(conversationContext);
        }
        // G. Handle Thanks
        else if (intent == Intent.THANKS) {
            responseText = responseGenerator.generateThanksResponse();
        }
        // H. Handle Positive Feedback & Compliments
        else if (intent == Intent.FEEDBACK_POSITIVE || (sentiment.getLevel() == SentimentAnalyzer.SentimentLevel.VERY_POSITIVE && (rawInput.toLowerCase().contains("love") || rawInput.toLowerCase().contains("great") || rawInput.toLowerCase().contains("awesome") || rawInput.toLowerCase().contains("fast") || rawInput.toLowerCase().contains("smart")))) {
            responseText = responseGenerator.generateFeedbackPositiveResponse();
            intent = Intent.FEEDBACK_POSITIVE;
            confidence = 0.95;
            algorithmUsed = "SentimentBooster_PositiveFeedback";
        }
        // I. Handle Negative Feedback
        else if (intent == Intent.FEEDBACK_NEGATIVE || (sentiment.getLevel() == SentimentAnalyzer.SentimentLevel.VERY_NEGATIVE && (rawInput.toLowerCase().contains("bad") || rawInput.toLowerCase().contains("useless") || rawInput.toLowerCase().contains("hate")))) {
            responseText = responseGenerator.generateFeedbackNegativeResponse();
            intent = Intent.FEEDBACK_NEGATIVE;
            confidence = 0.95;
            algorithmUsed = "SentimentBooster_NegativeFeedback";
        }
        // J. Handle Bot Identity & Capabilities
        else if (intent == Intent.BOT_IDENTITY) {
            responseText = responseGenerator.generateBotIdentity();
        } else if (intent == Intent.BOT_CAPABILITIES) {
            responseText = responseGenerator.generateBotCapabilities();
        } else if (intent == Intent.HELP) {
            responseText = responseGenerator.generateHelp();
        }
        // K. Handle Time & Date
        else if (intent == Intent.TIME_DATE) {
            responseText = responseGenerator.generateTimeDate();
            algorithmUsed = "SystemClockProvider";
        }
        // L. Handle Jokes & Motivation
        else if (intent == Intent.TECH_JOKE) {
            responseText = responseGenerator.generateJoke();
        } else if (intent == Intent.MOTIVATIONAL_QUOTE) {
            responseText = responseGenerator.generateMotivationalQuote();
        }
        // J. Knowledge Base FAQ Semantic Search
        else {
            KnowledgeBase.MatchResult match = knowledgeBase.search(rawInput);
            if (match != null && match.getScore() >= 0.28) {
                responseText = match.getEntry().getAnswer();
                confidence = match.getScore();
                algorithmUsed = "KnowledgeBase_" + match.getMatchType();
                if (match.getEntry().getCategory().contains("CodeAlpha")) {
                    intent = Intent.CODEALPHA_INTERNSHIP;
                } else if (match.getEntry().getCategory().contains("Java")) {
                    intent = Intent.PROGRAMMING_JAVA;
                } else if (match.getEntry().getCategory().contains("AI")) {
                    intent = Intent.AI_NLP_CONCEPTS;
                } else {
                    intent = Intent.FAQ_QUERY;
                }
            } else {
                // Fallback
                responseText = responseGenerator.generateFallback(rawInput, sentiment);
                confidence = Math.max(0.15, confidence);
                intent = Intent.UNKNOWN_FALLBACK;
            }
        }

        long responseTimeMs = Math.max(1, System.currentTimeMillis() - startTime);
        List<String> suggestedChips = responseGenerator.getSuggestedChipsForIntent(intent);

        BotResponse response = new BotResponse(
            responseText,
            intent,
            confidence,
            sentiment,
            algorithmUsed,
            entities.getAllEntityTags(),
            suggestedChips,
            responseTimeMs
        );

        // Update conversation context memory
        ChatMessage userMsg = new ChatMessage(ChatMessage.Sender.USER, rawInput, intent, confidence, sentiment);
        ChatMessage botMsg = new ChatMessage(ChatMessage.Sender.BOT, responseText, intent, confidence, null);
        conversationContext.addTurn(userMsg, botMsg);

        return response;
    }

    public KnowledgeBase getKnowledgeBase() {
        return knowledgeBase;
    }

    public ConversationContext getConversationContext() {
        return conversationContext;
    }

    public IntentClassifier getIntentClassifier() {
        return intentClassifier;
    }

    public Tokenizer getTokenizer() {
        return tokenizer;
    }

    public SentimentAnalyzer getSentimentAnalyzer() {
        return sentimentAnalyzer;
    }
}
