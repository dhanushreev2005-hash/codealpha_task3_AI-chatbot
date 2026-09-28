package com.codealpha.chatbot.nlp;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Entity Extractor for identifying names, mathematical expressions, locations,
 * programming concepts, and teaching patterns from raw user queries.
 */
public class EntityExtractor {

    private static final Pattern NAME_PATTERN = Pattern.compile(
        "\\b(?:my name is|i am called|call me|i am|this is)\\s+([a-zA-Z]{2,20})\\b",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern WEATHER_LOCATION_PATTERN = Pattern.compile(
        "\\b(?:weather|temperature|forecast|climate)\\s+(?:in|for|at)\\s+([a-zA-Z\\s]{2,30})",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern TEACH_PATTERN_ARROW = Pattern.compile(
        "^(?:teach|learn|add faq|train)\\s*:\\s*(.+?)\\s*(?:->|=>|\\|)\\s*(.+)$",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern TEACH_PATTERN_QA = Pattern.compile(
        "^(?:teach|learn|add faq|train)\\s*:\\s*q\\s*:\\s*(.+?)\\s*a\\s*:\\s*(.+)$",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern MATH_PATTERN = Pattern.compile(
        "(?:calculate|eval|what is|compute|solve)?\\s*([0-9\\.\\s\\+\\-\\*/\\^\\(\\)\\%]|sqrt|sin|cos|tan|log|abs)+",
        Pattern.CASE_INSENSITIVE
    );

    public static class ExtractedEntities {
        private String personName;
        private String location;
        private String mathExpression;
        private String teachQuestion;
        private String teachAnswer;
        private final List<String> techKeywords = new ArrayList<>();
        private final List<String> allEntityTags = new ArrayList<>();

        public String getPersonName() { return personName; }
        public String getLocation() { return location; }
        public String getMathExpression() { return mathExpression; }
        public String getTeachQuestion() { return teachQuestion; }
        public String getTeachAnswer() { return teachAnswer; }
        public List<String> getTechKeywords() { return techKeywords; }
        public List<String> getAllEntityTags() { return allEntityTags; }
    }

    private static final Set<String> KNOWN_TECH_KEYWORDS = new HashSet<>(Arrays.asList(
        "java", "oop", "polymorphism", "inheritance", "encapsulation", "abstraction",
        "class", "object", "interface", "abstract", "multithreading", "thread", "concurrency",
        "synchronization", "collections", "arraylist", "hashmap", "treeset", "linkedlist",
        "stream", "lambda", "jvm", "jre", "jdk", "garbage collection", "gc", "exception",
        "try-catch", "spring", "hibernate", "nlp", "machine learning", "ai", "chatbot",
        "tokenization", "tf-idf", "stemming", "cosine similarity", "naive bayes", "neural network"
    ));

    public ExtractedEntities extract(String text) {
        ExtractedEntities entities = new ExtractedEntities();
        if (text == null || text.trim().isEmpty()) return entities;

        String trimmed = text.trim();

        // 1. Check Teach / Train command patterns
        Matcher teachArrow = TEACH_PATTERN_ARROW.matcher(trimmed);
        if (teachArrow.matches()) {
            entities.teachQuestion = teachArrow.group(1).trim();
            entities.teachAnswer = teachArrow.group(2).trim();
            entities.allEntityTags.add("TeachQ: \"" + entities.teachQuestion + "\"");
            entities.allEntityTags.add("TeachA: \"" + entities.teachAnswer + "\"");
            return entities;
        }

        Matcher teachQa = TEACH_PATTERN_QA.matcher(trimmed);
        if (teachQa.matches()) {
            entities.teachQuestion = teachQa.group(1).trim();
            entities.teachAnswer = teachQa.group(2).trim();
            entities.allEntityTags.add("TeachQ: \"" + entities.teachQuestion + "\"");
            entities.allEntityTags.add("TeachA: \"" + entities.teachAnswer + "\"");
            return entities;
        }

        // 2. Extract Person Name
        Matcher nameMatcher = NAME_PATTERN.matcher(trimmed);
        if (nameMatcher.find()) {
            String candidate = nameMatcher.group(1).trim();
            // Discard common words that might follow "i am"
            String lower = candidate.toLowerCase();
            if (!lower.equals("fine") && !lower.equals("good") && !lower.equals("happy") &&
                !lower.equals("sad") && !lower.equals("here") && !lower.equals("ready") &&
                !lower.equals("trying") && !lower.equals("looking") && !lower.equals("learning")) {
                entities.personName = Character.toUpperCase(candidate.charAt(0)) + candidate.substring(1);
                entities.allEntityTags.add("Name: " + entities.personName);
            }
        }

        // 3. Extract Weather Location
        Matcher locMatcher = WEATHER_LOCATION_PATTERN.matcher(trimmed);
        if (locMatcher.find()) {
            entities.location = locMatcher.group(1).trim();
            entities.allEntityTags.add("Location: " + entities.location);
        }

        // 4. Extract Math Expression if query contains operators and numbers
        if (hasMathIntent(trimmed)) {
            String mathCandidate = extractPureMathString(trimmed);
            if (mathCandidate != null && !mathCandidate.isEmpty()) {
                entities.mathExpression = mathCandidate;
                entities.allEntityTags.add("MathExpr: " + mathCandidate);
            }
        }

        // 5. Extract Tech & Domain Keywords
        String lowerText = trimmed.toLowerCase();
        for (String kw : KNOWN_TECH_KEYWORDS) {
            if (lowerText.matches(".*\\b" + Pattern.quote(kw) + "\\b.*")) {
                entities.techKeywords.add(kw);
                entities.allEntityTags.add("Tech: " + kw);
            }
        }

        return entities;
    }

    private boolean hasMathIntent(String text) {
        String lower = text.toLowerCase();
        boolean hasCalcWord = lower.contains("calculate") || lower.contains("what is") ||
                              lower.contains("eval") || lower.contains("solve") ||
                              lower.contains("sqrt") || lower.contains("sin") ||
                              lower.contains("cos") || lower.contains("power");
        boolean hasOperators = text.matches(".*[\\+\\-\\*/\\^%].*");
        boolean hasDigits = text.matches(".*[0-9].*");

        return (hasCalcWord && hasDigits) || (hasDigits && hasOperators);
    }

    private String extractPureMathString(String text) {
        String clean = text.replaceAll("(?i)\\b(calculate|eval|what is|compute|solve|the value of|please|result of|equals)\\b", "");
        clean = clean.replaceAll("[\\?\\!]", "").trim();
        if (clean.matches(".*[0-9].*") && clean.matches("^[0-9\\.\\s\\+\\-\\*/\\^\\(\\)\\%a-z]+$")) {
            return clean;
        }
        return null;
    }
}
