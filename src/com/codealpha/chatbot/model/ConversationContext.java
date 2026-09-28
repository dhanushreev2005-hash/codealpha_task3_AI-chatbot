package com.codealpha.chatbot.model;

import com.codealpha.chatbot.nlp.SentimentAnalyzer.SentimentResult;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Manages conversational state, session context, user memory, and sentiment tracking across turns.
 */
public class ConversationContext {
    private String userName;
    private int turnCount;
    private Intent lastIntent;
    private String lastTopic;
    private final LocalDateTime sessionStartTime;
    private final List<ChatMessage> history;
    private final List<SentimentResult> sentimentHistory;
    private final Map<String, Object> memoryVariables;

    public ConversationContext() {
        this.userName = null;
        this.turnCount = 0;
        this.lastIntent = null;
        this.lastTopic = null;
        this.sessionStartTime = LocalDateTime.now();
        this.history = new ArrayList<>();
        this.sentimentHistory = new ArrayList<>();
        this.memoryVariables = new HashMap<>();
    }

    public void addTurn(ChatMessage userMsg, ChatMessage botMsg) {
        turnCount++;
        history.add(userMsg);
        history.add(botMsg);
        if (userMsg.getSentiment() != null) {
            sentimentHistory.add(userMsg.getSentiment());
        }
        if (botMsg.getDetectedIntent() != null) {
            this.lastIntent = botMsg.getDetectedIntent();
        }
    }

    public void clearHistory() {
        history.clear();
        sentimentHistory.clear();
        turnCount = 0;
        lastIntent = null;
        lastTopic = null;
        memoryVariables.clear();
    }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public int getTurnCount() { return turnCount; }
    public Intent getLastIntent() { return lastIntent; }
    public void setLastIntent(Intent lastIntent) { this.lastIntent = lastIntent; }
    public String getLastTopic() { return lastTopic; }
    public void setLastTopic(String lastTopic) { this.lastTopic = lastTopic; }
    public LocalDateTime getSessionStartTime() { return sessionStartTime; }
    public List<ChatMessage> getHistory() { return Collections.unmodifiableList(history); }
    public List<SentimentResult> getSentimentHistory() { return Collections.unmodifiableList(sentimentHistory); }
    public Map<String, Object> getMemoryVariables() { return memoryVariables; }

    public double getAverageSentimentScore() {
        if (sentimentHistory.isEmpty()) return 0.0;
        double sum = 0.0;
        for (SentimentResult sr : sentimentHistory) {
            sum += sr.getScore();
        }
        return sum / sentimentHistory.size();
    }
}
