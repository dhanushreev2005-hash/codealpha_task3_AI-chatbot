package com.codealpha.chatbot.nlp;

import java.util.*;

/**
 * Sentiment Analyzer utilizing an enriched lexicon of positive and negative tokens,
 * negation modifiers ("not", "never", "hardly"), booster words ("very", "extremely", "super"),
 * and emoticons/emojis to score user sentiment.
 */
public class SentimentAnalyzer {

    public enum SentimentLevel {
        VERY_POSITIVE("Very Positive", "😄", "#10b981"),
        POSITIVE("Positive", "🙂", "#22c55e"),
        NEUTRAL("Neutral", "😐", "#64748b"),
        NEGATIVE("Negative", "🙁", "#f97316"),
        VERY_NEGATIVE("Very Negative", "😡", "#ef4444");

        private final String label;
        private final String emoji;
        private final String color;

        SentimentLevel(String label, String emoji, String color) {
            this.label = label;
            this.emoji = emoji;
            this.color = color;
        }

        public String getLabel() { return label; }
        public String getEmoji() { return emoji; }
        public String getColor() { return color; }
    }

    public static class SentimentResult {
        private final SentimentLevel level;
        private final double score; // -1.0 to +1.0

        public SentimentResult(SentimentLevel level, double score) {
            this.level = level;
            this.score = Math.max(-1.0, Math.min(1.0, score));
        }

        public SentimentLevel getLevel() { return level; }
        public double getScore() { return score; }
        public String getEmoji() { return level.getEmoji(); }
        public String getLabel() { return level.getLabel(); }

        @Override
        public String toString() {
            return String.format("%s %s (Score: %.2f)", level.getEmoji(), level.getLabel(), score);
        }
    }

    private static final Map<String, Double> LEXICON = new HashMap<>();
    private static final Set<String> NEGATIONS = new HashSet<>(Arrays.asList(
        "not", "never", "no", "hardly", "barely", "scarcely", "cannot", "cant", "dont", "wont", "isnt", "arent", "neither"
    ));
    private static final Map<String, Double> BOOSTERS = new HashMap<>();

    static {
        // Boosters
        BOOSTERS.put("very", 1.5);
        BOOSTERS.put("extremely", 1.8);
        BOOSTERS.put("super", 1.6);
        BOOSTERS.put("really", 1.4);
        BOOSTERS.put("absolutely", 1.7);
        BOOSTERS.put("highly", 1.5);
        BOOSTERS.put("so", 1.3);
        BOOSTERS.put("totally", 1.4);
        BOOSTERS.put("somewhat", 0.7);
        BOOSTERS.put("slightly", 0.6);
        BOOSTERS.put("a bit", 0.7);

        // Positive terms
        LEXICON.put("good", 0.5);
        LEXICON.put("great", 0.8);
        LEXICON.put("excellent", 0.9);
        LEXICON.put("amazing", 0.95);
        LEXICON.put("awesome", 0.9);
        LEXICON.put("fantastic", 0.9);
        LEXICON.put("wonderful", 0.85);
        LEXICON.put("love", 0.85);
        LEXICON.put("like", 0.4);
        LEXICON.put("enjoy", 0.6);
        LEXICON.put("happy", 0.7);
        LEXICON.put("glad", 0.6);
        LEXICON.put("helpful", 0.7);
        LEXICON.put("thank", 0.5);
        LEXICON.put("thanks", 0.5);
        LEXICON.put("thankyou", 0.6);
        LEXICON.put("brilliant", 0.85);
        LEXICON.put("perfect", 0.95);
        LEXICON.put("best", 0.8);
        LEXICON.put("nice", 0.45);
        LEXICON.put("impressive", 0.75);
        LEXICON.put("cool", 0.5);
        LEXICON.put("appreciate", 0.65);
        LEXICON.put("beautiful", 0.8);
        LEXICON.put("pleased", 0.6);
        LEXICON.put("smart", 0.6);
        LEXICON.put("intelligent", 0.7);
        LEXICON.put("genius", 0.85);
        LEXICON.put("superb", 0.85);
        LEXICON.put("outstanding", 0.9);
        LEXICON.put("valuable", 0.6);
        LEXICON.put("clarity", 0.5);

        // Negative terms
        LEXICON.put("bad", -0.5);
        LEXICON.put("poor", -0.6);
        LEXICON.put("terrible", -0.9);
        LEXICON.put("horrible", -0.95);
        LEXICON.put("awful", -0.9);
        LEXICON.put("hate", -0.85);
        LEXICON.put("dislike", -0.5);
        LEXICON.put("angry", -0.7);
        LEXICON.put("annoyed", -0.6);
        LEXICON.put("useless", -0.8);
        LEXICON.put("stupid", -0.8);
        LEXICON.put("dumb", -0.75);
        LEXICON.put("worst", -0.9);
        LEXICON.put("slow", -0.4);
        LEXICON.put("error", -0.4);
        LEXICON.put("bug", -0.4);
        LEXICON.put("broken", -0.6);
        LEXICON.put("fail", -0.6);
        LEXICON.put("failed", -0.6);
        LEXICON.put("wrong", -0.5);
        LEXICON.put("confusing", -0.5);
        LEXICON.put("unhelpful", -0.7);
        LEXICON.put("sucks", -0.8);
        LEXICON.put("boring", -0.5);
        LEXICON.put("sad", -0.6);
        LEXICON.put("frustrated", -0.75);
        LEXICON.put("garbage", -0.85);
        LEXICON.put("rubbish", -0.8);
    }

    private final Tokenizer tokenizer;

    public SentimentAnalyzer() {
        this.tokenizer = new Tokenizer();
    }

    public SentimentResult analyze(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new SentimentResult(SentimentLevel.NEUTRAL, 0.0);
        }

        List<String> tokens = tokenizer.tokenize(text);
        if (tokens.isEmpty()) {
            return new SentimentResult(SentimentLevel.NEUTRAL, 0.0);
        }

        double totalScore = 0.0;
        int scoredTokens = 0;
        double currentBooster = 1.0;
        boolean isNegated = false;

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);

            if (NEGATIONS.contains(token)) {
                isNegated = !isNegated;
                continue;
            }

            if (BOOSTERS.containsKey(token)) {
                currentBooster = BOOSTERS.get(token);
                continue;
            }

            if (LEXICON.containsKey(token)) {
                double baseScore = LEXICON.get(token);
                double score = baseScore * currentBooster;
                if (isNegated) {
                    score = -score * 0.8; // Flip polarity when negated
                }
                totalScore += score;
                scoredTokens++;

                // Reset modifiers
                currentBooster = 1.0;
                isNegated = false;
            }
        }

        double finalScore = 0.0;
        if (scoredTokens > 0) {
            finalScore = totalScore / Math.sqrt(tokens.size());
            finalScore = Math.max(-1.0, Math.min(1.0, finalScore));
        }

        SentimentLevel level;
        if (finalScore >= 0.45) {
            level = SentimentLevel.VERY_POSITIVE;
        } else if (finalScore >= 0.12) {
            level = SentimentLevel.POSITIVE;
        } else if (finalScore <= -0.45) {
            level = SentimentLevel.VERY_NEGATIVE;
        } else if (finalScore <= -0.12) {
            level = SentimentLevel.NEGATIVE;
        } else {
            level = SentimentLevel.NEUTRAL;
        }

        return new SentimentResult(level, finalScore);
    }
}
