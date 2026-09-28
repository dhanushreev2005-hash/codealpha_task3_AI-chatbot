package com.codealpha.chatbot.nlp;

/**
 * Computes Levenshtein Distance and fuzzy similarity score between two strings
 * for typo tolerance, spell-checking and near-match intent recognition.
 */
public class LevenshteinDistance {

    /**
     * Computes the Levenshtein edit distance between two strings.
     */
    public static int compute(String s1, String s2) {
        if (s1 == null && s2 == null) return 0;
        if (s1 == null) return s2.length();
        if (s2 == null) return s1.length();

        int len1 = s1.length();
        int len2 = s2.length();

        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) dp[i][0] = i;
        for (int j = 0; j <= len2; j++) dp[0][j] = j;

        for (int i = 1; i <= len1; i++) {
            char c1 = s1.charAt(i - 1);
            for (int j = 1; j <= len2; j++) {
                char c2 = s2.charAt(j - 1);
                if (Character.toLowerCase(c1) == Character.toLowerCase(c2)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    int replace = dp[i - 1][j - 1] + 1;
                    int insert = dp[i][j - 1] + 1;
                    int delete = dp[i - 1][j] + 1;
                    dp[i][j] = Math.min(replace, Math.min(insert, delete));
                }
            }
        }
        return dp[len1][len2];
    }

    /**
     * Returns a similarity ratio between 0.0 (completely distinct) and 1.0 (exact match).
     */
    public static double similarityRatio(String s1, String s2) {
        if (s1 == null && s2 == null) return 1.0;
        if (s1 == null || s2 == null) return 0.0;
        if (s1.equalsIgnoreCase(s2)) return 1.0;

        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 1.0;

        int distance = compute(s1, s2);
        return 1.0 - ((double) distance / maxLen);
    }
}
