package com.codealpha.chatbot.nlp;

import com.codealpha.chatbot.model.Intent;
import java.util.*;

/**
 * Machine Learning & Rule-based Hybrid Intent Classifier.
 * Utilizes Multinomial Naive Bayes log-likelihood scoring combined with TF-IDF vector projections
 * to classify user queries into discrete intent categories with probabilistic confidence metrics.
 */
public class IntentClassifier {

    public static class ClassificationResult {
        private final Intent intent;
        private final double confidence; // 0.0 to 1.0
        private final String algorithm;
        private final Map<Intent, Double> probabilityDistribution;

        public ClassificationResult(Intent intent, double confidence, String algorithm, Map<Intent, Double> probabilityDistribution) {
            this.intent = intent;
            this.confidence = Math.max(0.0, Math.min(1.0, confidence));
            this.algorithm = algorithm;
            this.probabilityDistribution = probabilityDistribution;
        }

        public Intent getIntent() { return intent; }
        public double getConfidence() { return confidence; }
        public String getAlgorithm() { return algorithm; }
        public Map<Intent, Double> getProbabilityDistribution() { return probabilityDistribution; }
    }

    private final Tokenizer tokenizer;
    private final Map<Intent, List<String>> trainingCorpus;
    private final Map<Intent, Map<String, Integer>> intentWordCounts;
    private final Map<Intent, Integer> intentTotalWords;
    private final Map<Intent, Double> priorProbabilities;
    private final Set<String> vocabulary;
    private int totalTrainingDocuments;

    public IntentClassifier() {
        this.tokenizer = new Tokenizer();
        this.trainingCorpus = new HashMap<>();
        this.intentWordCounts = new HashMap<>();
        this.intentTotalWords = new HashMap<>();
        this.priorProbabilities = new HashMap<>();
        this.vocabulary = new HashSet<>();
        this.totalTrainingDocuments = 0;

        initializeDefaultTrainingData();
        trainModel();
    }

    private void initializeDefaultTrainingData() {
        // GREETING
        addExamples(Intent.GREETING, Arrays.asList(
            "hello", "hi", "hey", "good morning", "good afternoon", "good evening",
            "greetings", "hey there", "howdy", "sup", "hi chatbot", "hello there bot"
        ));

        // FAREWELL
        addExamples(Intent.FAREWELL, Arrays.asList(
            "bye", "goodbye", "see you later", "farewell", "catch you later", "exit", "quit", "see ya", "talk to you soon"
        ));

        // THANKS
        addExamples(Intent.THANKS, Arrays.asList(
            "thanks", "thank you", "thank you very much", "thanks a lot", "much appreciated", "cheers", "awesome thanks"
        ));

        // BOT IDENTITY
        addExamples(Intent.BOT_IDENTITY, Arrays.asList(
            "who are you", "what is your name", "who created you", "tell me about yourself",
            "are you an ai", "what are you", "who made this bot", "introduce yourself"
        ));

        // BOT CAPABILITIES
        addExamples(Intent.BOT_CAPABILITIES, Arrays.asList(
            "what can you do", "what are your features", "what are your capabilities",
            "how can you help me", "show capabilities", "what do you know", "list features"
        ));

        // HELP
        addExamples(Intent.HELP, Arrays.asList(
            "help", "help me", "commands", "how to use this bot", "guide me", "i need help", "show help manual", "what commands exist"
        ));

        // TIME & DATE
        addExamples(Intent.TIME_DATE, Arrays.asList(
            "what time is it", "current time", "tell me the time", "what is today's date",
            "what day is it", "current date", "what is the date today", "what year is this", "clock time"
        ));

        // MATH CALCULATION
        addExamples(Intent.MATH_CALCULATION, Arrays.asList(
            "calculate 25 * 4", "what is 50 + 100", "compute 1024 / 8", "solve sqrt(144)",
            "eval 2^10", "math 45 - 12", "what is 15 percent of 200", "calculate expression"
        ));

        // WEATHER
        addExamples(Intent.WEATHER, Arrays.asList(
            "what is the weather", "weather in London", "temperature today", "is it raining",
            "forecast for New York", "climate in Tokyo", "weather report", "how is the weather outside"
        ));

        // CODEALPHA INTERNSHIP
        addExamples(Intent.CODEALPHA_INTERNSHIP, Arrays.asList(
            "codealpha", "internship details", "codealpha internship", "task 1", "task 2", "task 3", "task 4",
            "how to submit tasks", "github repo name", "codealpha perks", "completion certificate",
            "letter of recommendation", "submission form", "internship instructions", "how many tasks are required"
        ));

        // JAVA PROGRAMMING
        addExamples(Intent.PROGRAMMING_JAVA, Arrays.asList(
            "what is java", "explain oop in java", "what is polymorphism", "what is inheritance",
            "difference between interface and abstract class", "what is jvm", "garbage collection in java",
            "what is hashmap", "arraylist vs linkedlist", "multithreading in java", "java streams",
            "exception handling in java", "java lambda expressions", "how does spring boot work"
        ));

        // AI & NLP
        addExamples(Intent.AI_NLP_CONCEPTS, Arrays.asList(
            "what is nlp", "what is natural language processing", "how does a chatbot work",
            "what is machine learning", "what is tf-idf", "explain tokenization", "what is stemming",
            "what is cosine similarity", "what is naive bayes", "difference between supervised and unsupervised learning",
            "what is deep learning", "what is artificial intelligence"
        ));

        // TECH JOKE
        addExamples(Intent.TECH_JOKE, Arrays.asList(
            "tell me a joke", "programming joke", "tech humor", "make me laugh", "give me a coding joke", "funny developer joke"
        ));

        // MOTIVATIONAL QUOTE
        addExamples(Intent.MOTIVATIONAL_QUOTE, Arrays.asList(
            "inspire me", "give me a motivational quote", "inspirational quote", "quote of the day", "encourage me", "coding inspiration"
        ));

        // TEACH BOT
        addExamples(Intent.TEACH_BOT, Arrays.asList(
            "teach:", "learn:", "add faq:", "train bot", "teach the bot something new", "i want to teach you"
        ));

        // FEEDBACK POSITIVE
        addExamples(Intent.FEEDBACK_POSITIVE, Arrays.asList(
            "you are great", "you are very smart", "good bot", "nice work", "i love you", "great answer", "you are awesome"
        ));

        // FEEDBACK NEGATIVE
        addExamples(Intent.FEEDBACK_NEGATIVE, Arrays.asList(
            "you are bad", "useless bot", "stupid answer", "you did not help", "terrible bot", "i hate this answer"
        ));
    }

    private void addExamples(Intent intent, List<String> examples) {
        trainingCorpus.computeIfAbsent(intent, k -> new ArrayList<>()).addAll(examples);
    }

    /**
     * Trains the Naive Bayes model on the current corpus.
     */
    public synchronized void trainModel() {
        intentWordCounts.clear();
        intentTotalWords.clear();
        priorProbabilities.clear();
        vocabulary.clear();
        totalTrainingDocuments = 0;

        for (List<String> docs : trainingCorpus.values()) {
            totalTrainingDocuments += docs.size();
        }

        if (totalTrainingDocuments == 0) return;

        for (Map.Entry<Intent, List<String>> entry : trainingCorpus.entrySet()) {
            Intent intent = entry.getKey();
            List<String> docs = entry.getValue();

            priorProbabilities.put(intent, (double) docs.size() / totalTrainingDocuments);

            Map<String, Integer> wordCounts = new HashMap<>();
            int totalWords = 0;

            for (String doc : docs) {
                List<String> tokens = tokenizer.tokenizeStemmed(doc, true);
                List<String> ngrams = tokenizer.generateNGrams(tokens, 2);
                for (String term : ngrams) {
                    vocabulary.add(term);
                    wordCounts.put(term, wordCounts.getOrDefault(term, 0) + 1);
                    totalWords++;
                }
            }

            intentWordCounts.put(intent, wordCounts);
            intentTotalWords.put(intent, totalWords);
        }
    }

    /**
     * Classifies a user query into an Intent using Naive Bayes log-likelihood with Laplace smoothing.
     */
    public ClassificationResult classify(String userQuery) {
        if (userQuery == null || userQuery.trim().isEmpty()) {
            return new ClassificationResult(Intent.UNKNOWN_FALLBACK, 0.0, "EmptyQuery", Collections.emptyMap());
        }

        String normalized = TextNormalizer.normalize(userQuery);
        String lower = normalized.toLowerCase().trim();

        // 1. Fast exact & regex rule matches
        if (lower.startsWith("teach:") || lower.startsWith("learn:") || lower.startsWith("add faq:") || lower.startsWith("train:")) {
            return new ClassificationResult(Intent.TEACH_BOT, 1.0, "ExactRule", Collections.singletonMap(Intent.TEACH_BOT, 1.0));
        }

        if (lower.matches("^(hi|hello|hey|greetings|howdy|sup)\\b.*")) {
            return new ClassificationResult(Intent.GREETING, 0.98, "RuleMatcher", Collections.singletonMap(Intent.GREETING, 0.98));
        }

        if (lower.matches("^(bye|goodbye|exit|quit|see ya|farewell)\\b.*")) {
            return new ClassificationResult(Intent.FAREWELL, 0.98, "RuleMatcher", Collections.singletonMap(Intent.FAREWELL, 0.98));
        }

        if (lower.matches("^(thanks|thank you|thx|much appreciated)\\b.*")) {
            return new ClassificationResult(Intent.THANKS, 0.95, "RuleMatcher", Collections.singletonMap(Intent.THANKS, 0.95));
        }

        if (lower.contains("time") && (lower.contains("what") || lower.contains("current") || lower.contains("tell me") || lower.contains("now"))) {
            return new ClassificationResult(Intent.TIME_DATE, 0.95, "RuleMatcher", Collections.singletonMap(Intent.TIME_DATE, 0.95));
        }

        if (lower.contains("date") && (lower.contains("today") || lower.contains("what") || lower.contains("current"))) {
            return new ClassificationResult(Intent.TIME_DATE, 0.95, "RuleMatcher", Collections.singletonMap(Intent.TIME_DATE, 0.95));
        }

        if (lower.contains("weather") || lower.contains("forecast") || lower.contains("temperature in")) {
            return new ClassificationResult(Intent.WEATHER, 0.95, "RuleMatcher", Collections.singletonMap(Intent.WEATHER, 0.95));
        }

        if (lower.contains("joke") || lower.contains("funny") || lower.contains("make me laugh")) {
            return new ClassificationResult(Intent.TECH_JOKE, 0.95, "RuleMatcher", Collections.singletonMap(Intent.TECH_JOKE, 0.95));
        }

        if (lower.contains("quote") || lower.contains("motivat") || lower.contains("inspire")) {
            return new ClassificationResult(Intent.MOTIVATIONAL_QUOTE, 0.95, "RuleMatcher", Collections.singletonMap(Intent.MOTIVATIONAL_QUOTE, 0.95));
        }

        // Check for math pattern: starts with calculate/eval or purely digits/operators
        if (lower.startsWith("calculate") || lower.startsWith("eval") || lower.startsWith("solve") ||
            (lower.matches(".*[0-9].*") && lower.matches("^[0-9\\.\\s\\+\\-\\*/\\^\\(\\)\\%a-z]+$") && lower.matches(".*[\\+\\-\\*/\\^%].*"))) {
            return new ClassificationResult(Intent.MATH_CALCULATION, 0.96, "RuleMatcher", Collections.singletonMap(Intent.MATH_CALCULATION, 0.96));
        }

        // 2. Machine Learning: Multinomial Naive Bayes
        List<String> tokens = tokenizer.tokenizeStemmed(userQuery, true);
        List<String> queryNgrams = tokenizer.generateNGrams(tokens, 2);

        if (queryNgrams.isEmpty()) {
            return new ClassificationResult(Intent.UNKNOWN_FALLBACK, 0.1, "Fallback", Collections.emptyMap());
        }

        int vocabSize = Math.max(vocabulary.size(), 1);
        Map<Intent, Double> logPosteriors = new HashMap<>();
        double maxLog = Double.NEGATIVE_INFINITY;

        for (Intent intent : trainingCorpus.keySet()) {
            double prior = priorProbabilities.getOrDefault(intent, 1.0 / trainingCorpus.size());
            double logProb = Math.log(prior);

            Map<String, Integer> wordCounts = intentWordCounts.getOrDefault(intent, Collections.emptyMap());
            int totalWords = intentTotalWords.getOrDefault(intent, 0);
            double denominator = totalWords + vocabSize;

            for (String term : queryNgrams) {
                int count = wordCounts.getOrDefault(term, 0);
                // Laplace smoothing (+1)
                double termProb = (count + 1.0) / denominator;
                logProb += Math.log(termProb);
            }

            logPosteriors.put(intent, logProb);
            if (logProb > maxLog) {
                maxLog = logProb;
            }
        }

        // Softmax normalization over log likelihoods
        Map<Intent, Double> probabilities = new HashMap<>();
        double sumExp = 0.0;
        for (Map.Entry<Intent, Double> entry : logPosteriors.entrySet()) {
            double expVal = Math.exp(entry.getValue() - maxLog);
            probabilities.put(entry.getKey(), expVal);
            sumExp += expVal;
        }

        Intent bestIntent = Intent.UNKNOWN_FALLBACK;
        double bestProb = 0.0;

        for (Map.Entry<Intent, Double> entry : probabilities.entrySet()) {
            double normalizedProb = sumExp > 0 ? entry.getValue() / sumExp : 0.0;
            entry.setValue(normalizedProb);
            if (normalizedProb > bestProb) {
                bestProb = normalizedProb;
                bestIntent = entry.getKey();
            }
        }

        // If confidence is too weak, check fallback threshold
        if (bestProb < 0.25) {
            return new ClassificationResult(Intent.UNKNOWN_FALLBACK, bestProb, "ML_NaiveBayes_LowConfidence", probabilities);
        }

        return new ClassificationResult(bestIntent, bestProb, "ML_NaiveBayes", probabilities);
    }

    public void addCustomTrainingData(Intent intent, String utterance) {
        if (intent == null || utterance == null || utterance.trim().isEmpty()) return;
        trainingCorpus.computeIfAbsent(intent, k -> new ArrayList<>()).add(utterance.trim());
        trainModel();
    }
}
