package com.codealpha.chatbot.nlp;

import java.util.Map;

/**
 * Computes Cosine Similarity between sparse term frequency / TF-IDF vectors.
 */
public class CosineSimilarity {

    /**
     * Calculates cosine similarity between two sparse vectors represented as Map<String, Double>.
     * Returns a score between 0.0 (no similarity) and 1.0 (identical vector direction).
     */
    public static double compute(Map<String, Double> vec1, Map<String, Double> vec2) {
        if (vec1 == null || vec2 == null || vec1.isEmpty() || vec2.isEmpty()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (Map.Entry<String, Double> entry : vec1.entrySet()) {
            double val = entry.getValue();
            normA += val * val;
            if (vec2.containsKey(entry.getKey())) {
                dotProduct += val * vec2.get(entry.getKey());
            }
        }

        for (double val : vec2.values()) {
            normB += val * val;
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
