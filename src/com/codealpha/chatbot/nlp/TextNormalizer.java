package com.codealpha.chatbot.nlp;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Text normalizer for standardizing queries, expanding contractions,
 * removing special noise characters and normalizing whitespace.
 */
public class TextNormalizer {
    private static final Map<String, String> CONTRACTIONS = new HashMap<>();

    static {
        CONTRACTIONS.put("what's", "what is");
        CONTRACTIONS.put("whats", "what is");
        CONTRACTIONS.put("how's", "how is");
        CONTRACTIONS.put("hows", "how is");
        CONTRACTIONS.put("where's", "where is");
        CONTRACTIONS.put("wheres", "where is");
        CONTRACTIONS.put("who's", "who is");
        CONTRACTIONS.put("whos", "who is");
        CONTRACTIONS.put("there's", "there is");
        CONTRACTIONS.put("i'm", "i am");
        CONTRACTIONS.put("im", "i am");
        CONTRACTIONS.put("you're", "you are");
        CONTRACTIONS.put("youre", "you are");
        CONTRACTIONS.put("he's", "he is");
        CONTRACTIONS.put("she's", "she is");
        CONTRACTIONS.put("it's", "it is");
        CONTRACTIONS.put("we're", "we are");
        CONTRACTIONS.put("they're", "they are");
        CONTRACTIONS.put("i've", "i have");
        CONTRACTIONS.put("ive", "i have");
        CONTRACTIONS.put("you've", "you have");
        CONTRACTIONS.put("we've", "we have");
        CONTRACTIONS.put("they've", "they have");
        CONTRACTIONS.put("i'll", "i will");
        CONTRACTIONS.put("you'll", "you will");
        CONTRACTIONS.put("he'll", "he will");
        CONTRACTIONS.put("she'll", "she will");
        CONTRACTIONS.put("we'll", "we will");
        CONTRACTIONS.put("they'll", "they will");
        CONTRACTIONS.put("i'd", "i would");
        CONTRACTIONS.put("you'd", "you would");
        CONTRACTIONS.put("he'd", "he would");
        CONTRACTIONS.put("she'd", "she would");
        CONTRACTIONS.put("we'd", "we would");
        CONTRACTIONS.put("they'd", "they would");
        CONTRACTIONS.put("can't", "cannot");
        CONTRACTIONS.put("cant", "cannot");
        CONTRACTIONS.put("don't", "do not");
        CONTRACTIONS.put("dont", "do not");
        CONTRACTIONS.put("doesn't", "does not");
        CONTRACTIONS.put("doesnt", "does not");
        CONTRACTIONS.put("didn't", "did not");
        CONTRACTIONS.put("didnt", "did not");
        CONTRACTIONS.put("won't", "will not");
        CONTRACTIONS.put("wont", "will not");
        CONTRACTIONS.put("isn't", "is not");
        CONTRACTIONS.put("isnt", "is not");
        CONTRACTIONS.put("aren't", "are not");
        CONTRACTIONS.put("arent", "are not");
        CONTRACTIONS.put("wasn't", "was not");
        CONTRACTIONS.put("wasnt", "was not");
        CONTRACTIONS.put("weren't", "were not");
        CONTRACTIONS.put("werent", "were not");
        CONTRACTIONS.put("haven't", "have not");
        CONTRACTIONS.put("havent", "have not");
        CONTRACTIONS.put("hasn't", "has not");
        CONTRACTIONS.put("hasnt", "has not");
        CONTRACTIONS.put("hadn't", "had not");
        CONTRACTIONS.put("hadnt", "had not");
        CONTRACTIONS.put("wouldn't", "would not");
        CONTRACTIONS.put("shouldn't", "should not");
        CONTRACTIONS.put("couldn't", "could not");
        CONTRACTIONS.put("let's", "let us");
        CONTRACTIONS.put("lets", "let us");
        CONTRACTIONS.put("that's", "that is");
        CONTRACTIONS.put("thats", "that is");
    }

    private static final Pattern PUNCTUATION_PATTERN = Pattern.compile("[^a-zA-Z0-9\\s\\+\\-\\*/\\^\\(\\)\\.]");

    public static String normalize(String text) {
        if (text == null) return "";
        String lower = text.trim().toLowerCase();
        
        // Expand contractions
        String[] words = lower.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String w = words[i];
            // Remove trailing punctuation from word for lookup
            String cleanWord = w.replaceAll("[^a-zA-Z0-9']", "");
            if (CONTRACTIONS.containsKey(cleanWord)) {
                sb.append(CONTRACTIONS.get(cleanWord));
            } else if (CONTRACTIONS.containsKey(w)) {
                sb.append(CONTRACTIONS.get(w));
            } else {
                sb.append(w);
            }
            if (i < words.length - 1) sb.append(" ");
        }

        String result = sb.toString();
        // Replace multiple whitespace
        result = result.replaceAll("\\s+", " ").trim();
        return result;
    }

    public static String stripPunctuation(String text) {
        if (text == null) return "";
        return PUNCTUATION_PATTERN.matcher(text).replaceAll(" ").replaceAll("\\s+", " ").trim();
    }
}
