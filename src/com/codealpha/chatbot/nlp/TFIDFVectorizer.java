package com.codealpha.chatbot.nlp;

import java.util.*;

/**
 * TF-IDF Vectorizer for Information Retrieval and semantic similarity calculation.
 * Computes Term Frequency (TF) and Inverse Document Frequency (IDF) over a corpus.
 */
public class TFIDFVectorizer {
    private final Tokenizer tokenizer;
    private final Map<String, Double> idfMap;
    private final Set<String> vocabulary;
    private int totalDocuments;

    public TFIDFVectorizer() {
        this.tokenizer = new Tokenizer();
        this.idfMap = new HashMap<>();
        this.vocabulary = new HashSet<>();
        this.totalDocuments = 0;
    }

    /**
     * Fits the vectorizer on a corpus of text documents.
     */
    public synchronized void fit(List<String> documents) {
        idfMap.clear();
        vocabulary.clear();
        totalDocuments = documents.size();

        if (totalDocuments == 0) return;

        Map<String, Integer> docFreq = new HashMap<>();

        for (String doc : documents) {
            List<String> tokens = tokenizer.tokenizeStemmed(doc, true);
            // Also add bigrams for richer phrase-level representation
            List<String> ngrams = tokenizer.generateNGrams(tokens, 2);
            Set<String> uniqueTerms = new HashSet<>(ngrams);

            for (String term : uniqueTerms) {
                vocabulary.add(term);
                docFreq.put(term, docFreq.getOrDefault(term, 0) + 1);
            }
        }

        // Compute smooth IDF: log((N + 1) / (df + 1)) + 1
        for (Map.Entry<String, Integer> entry : docFreq.entrySet()) {
            String term = entry.getKey();
            int df = entry.getValue();
            double idf = Math.log(((double) totalDocuments + 1.0) / ((double) df + 1.0)) + 1.0;
            idfMap.put(term, idf);
        }
    }

    /**
     * Transforms a text string into a sparse TF-IDF vector (Map<Term, Weight>).
     */
    public Map<String, Double> transform(String text) {
        Map<String, Double> vector = new HashMap<>();
        if (text == null || text.trim().isEmpty()) return vector;

        List<String> tokens = tokenizer.tokenizeStemmed(text, true);
        List<String> ngrams = tokenizer.generateNGrams(tokens, 2);

        if (ngrams.isEmpty()) return vector;

        // Compute Term Frequencies (TF) with sublinear scaling: 1 + log(tf)
        Map<String, Integer> termCounts = new HashMap<>();
        for (String term : ngrams) {
            termCounts.put(term, termCounts.getOrDefault(term, 0) + 1);
        }

        for (Map.Entry<String, Integer> entry : termCounts.entrySet()) {
            String term = entry.getKey();
            int count = entry.getValue();
            double tf = 1.0 + Math.log(count);
            // If term not in IDF map (out of vocabulary), default to fallback IDF
            double idf = idfMap.getOrDefault(term, Math.log((double) totalDocuments + 2.0) + 1.0);
            vector.put(term, tf * idf);
        }

        return vector;
    }

    public Tokenizer getTokenizer() {
        return tokenizer;
    }

    public int getVocabularySize() {
        return vocabulary.size();
    }

    public Set<String> getVocabulary() {
        return Collections.unmodifiableSet(vocabulary);
    }
}
