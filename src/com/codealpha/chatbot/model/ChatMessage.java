package com.codealpha.chatbot.model;

import com.codealpha.chatbot.nlp.SentimentAnalyzer.SentimentResult;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model representing a single chat message in the conversation.
 */
public class ChatMessage {
    public enum Sender {
        USER,
        BOT,
        SYSTEM
    }

    private final String id;
    private final Sender sender;
    private final String content;
    private final LocalDateTime timestamp;
    private final Intent detectedIntent;
    private final double confidence;
    private final SentimentResult sentiment;

    public ChatMessage(Sender sender, String content) {
        this(sender, content, Intent.UNKNOWN_FALLBACK, 1.0, null);
    }

    public ChatMessage(Sender sender, String content, Intent detectedIntent, double confidence, SentimentResult sentiment) {
        this.id = "msg_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.detectedIntent = detectedIntent;
        this.confidence = confidence;
        this.sentiment = sentiment;
    }

    public String getId() { return id; }
    public Sender getSender() { return sender; }
    public String getContent() { return content; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getFormattedTime() {
        return timestamp.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }
    public Intent getDetectedIntent() { return detectedIntent; }
    public double getConfidence() { return confidence; }
    public SentimentResult getSentiment() { return sentiment; }
}
