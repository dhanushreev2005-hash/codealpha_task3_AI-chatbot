package com.codealpha.chatbot.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Knowledge Base FAQ Entry data model.
 */
public class FAQEntry {
    private final String id;
    private final String category;
    private final String question;
    private final String answer;
    private final List<String> alternativeQuestions;
    private final List<String> tags;

    public FAQEntry(String id, String category, String question, String answer) {
        this(id, category, question, answer, new ArrayList<>(), new ArrayList<>());
    }

    public FAQEntry(String id, String category, String question, String answer, List<String> alternativeQuestions, List<String> tags) {
        this.id = id;
        this.category = category;
        this.question = question;
        this.answer = answer;
        this.alternativeQuestions = alternativeQuestions != null ? alternativeQuestions : new ArrayList<>();
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    public String getId() { return id; }
    public String getCategory() { return category; }
    public String getQuestion() { return question; }
    public String getAnswer() { return answer; }
    public List<String> getAlternativeQuestions() { return alternativeQuestions; }
    public List<String> getTags() { return tags; }

    /**
     * Returns combined text representation for TF-IDF training vector.
     */
    public String getAllSearchableText() {
        StringBuilder sb = new StringBuilder();
        sb.append(question).append(" ");
        for (String alt : alternativeQuestions) {
            sb.append(alt).append(" ");
        }
        for (String tag : tags) {
            sb.append(tag).append(" ");
        }
        sb.append(category);
        return sb.toString().trim();
    }
}
