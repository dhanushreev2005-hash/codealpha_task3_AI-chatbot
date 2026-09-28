package com.codealpha.chatbot.nlp;

import java.util.*;

/**
 * Tokenizer component that splits text into word tokens, filters stop words,
 * applies stemming, and produces n-grams for phrase-based NLP scoring.
 */
public class Tokenizer {
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "a", "about", "above", "after", "again", "against", "all", "am", "an", "and",
        "any", "are", "as", "at", "be", "because", "been", "before", "being", "below",
        "between", "both", "but", "by", "could", "did", "do", "does", "doing", "down",
        "during", "each", "few", "for", "from", "further", "had", "has", "have",
        "having", "he", "her", "here", "hers", "herself", "him", "himself", "his",
        "how", "i", "if", "in", "into", "is", "it", "its", "itself", "me", "more",
        "most", "my", "myself", "no", "nor", "not", "of", "off", "on", "once", "only",
        "or", "other", "ought", "our", "ours", "ourselves", "out", "over", "own",
        "same", "she", "should", "so", "some", "such", "than", "that", "the", "their",
        "theirs", "them", "themselves", "then", "there", "these", "they", "this",
        "those", "through", "to", "too", "under", "until", "up", "very", "was", "we",
        "were", "what", "when", "where", "which", "while", "who", "whom", "why",
        "with", "would", "you", "your", "yours", "yourself", "yourselves"
    ));

    // Question words are important for intent recognition, so we can selectively keep them
    private static final Set<String> QUESTION_WORDS = new HashSet<>(Arrays.asList(
        "what", "when", "where", "which", "who", "whom", "why", "how"
    ));

    private final PorterStemmer stemmer;

    public Tokenizer() {
        this.stemmer = new PorterStemmer();
    }

    /**
     * Splits raw text into cleaned, lowercase word tokens.
     */
    public List<String> tokenize(String text) {
        if (text == null || text.trim().isEmpty()) return Collections.emptyList();
        String normalized = TextNormalizer.normalize(text);
        String cleaned = TextNormalizer.stripPunctuation(normalized);
        if (cleaned.isEmpty()) return Collections.emptyList();

        String[] parts = cleaned.split("\\s+");
        List<String> tokens = new ArrayList<>();
        for (String p : parts) {
            if (!p.isEmpty()) {
                tokens.add(p);
            }
        }
        return tokens;
    }

    /**
     * Tokenizes and removes English stop words, optionally preserving question keywords.
     */
    public List<String> tokenizeAndFilter(String text, boolean preserveQuestionWords) {
        List<String> rawTokens = tokenize(text);
        List<String> filtered = new ArrayList<>();
        for (String token : rawTokens) {
            if (preserveQuestionWords && QUESTION_WORDS.contains(token)) {
                filtered.add(token);
            } else if (!STOP_WORDS.contains(token)) {
                filtered.add(token);
            }
        }
        return filtered;
    }

    /**
     * Tokenizes, filters stop words, and applies the Porter Stemmer algorithm.
     */
    public List<String> tokenizeStemmed(String text, boolean preserveQuestionWords) {
        List<String> filtered = tokenizeAndFilter(text, preserveQuestionWords);
        List<String> stemmed = new ArrayList<>();
        for (String token : filtered) {
            stemmed.add(stemmer.stemWord(token));
        }
        return stemmed;
    }

    /**
     * Generates unigrams, bigrams, and trigrams from tokens.
     */
    public List<String> generateNGrams(List<String> tokens, int maxN) {
        List<String> nGrams = new ArrayList<>(tokens);
        int size = tokens.size();
        for (int n = 2; n <= maxN; n++) {
            for (int i = 0; i <= size - n; i++) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < n; j++) {
                    sb.append(tokens.get(i + j));
                    if (j < n - 1) sb.append("_");
                }
                nGrams.add(sb.toString());
            }
        }
        return nGrams;
    }

    public PorterStemmer getStemmer() {
        return stemmer;
    }
}
