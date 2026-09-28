package com.codealpha.chatbot.cli;

import com.codealpha.chatbot.engine.ChatbotEngine;
import com.codealpha.chatbot.model.BotResponse;
import com.codealpha.chatbot.model.FAQEntry;

import java.util.List;
import java.util.Scanner;

/**
 * Interactive Console / Terminal Interface for the CodeAlpha AI Chatbot.
 */
public class ChatbotCli {
    private final ChatbotEngine engine;

    // ANSI Color codes
    private static final String RESET = "\u001B[0m";
    private static final String CYAN = "\u001B[36m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String BOLD = "\u001B[1m";

    public ChatbotCli(ChatbotEngine engine) {
        this.engine = engine;
    }

    public void start() {
        printBanner();
        Scanner scanner = new Scanner(System.in);

        System.out.println(GREEN + "Type your questions below. Type '/help' for commands or '/exit' to quit.\n" + RESET);

        while (true) {
            System.out.print(CYAN + BOLD + "You > " + RESET);
            String line = scanner.nextLine();
            if (line == null) break;
            line = line.trim();

            if (line.isEmpty()) continue;

            if (line.equalsIgnoreCase("/exit") || line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) {
                System.out.println(PURPLE + "\n👋 Thank you for using CodeAlpha AI Chatbot! Happy coding!" + RESET);
                break;
            }

            if (line.equalsIgnoreCase("/help")) {
                printHelp();
                continue;
            }

            if (line.equalsIgnoreCase("/faq") || line.equalsIgnoreCase("/faqs")) {
                printFaqs();
                continue;
            }

            if (line.equalsIgnoreCase("/stats")) {
                printStats();
                continue;
            }

            if (line.equalsIgnoreCase("/clear")) {
                engine.getConversationContext().clearHistory();
                System.out.println(YELLOW + "✨ Conversation context cleared!\n" + RESET);
                continue;
            }

            // Process query through ChatbotEngine
            BotResponse response = engine.processQuery(line);

            // Display Bot Output
            System.out.println(PURPLE + BOLD + "\n🤖 Bot: " + RESET + response.getText());
            System.out.println(YELLOW + "   [NLP: " + response.getIntent().getDisplayName() +
                               " (" + (int)(response.getConfidence() * 100) + "%) | " +
                               response.getAlgorithmUsed() + " | " +
                               (response.getUserSentiment() != null ? response.getUserSentiment().toString() : "Neutral") +
                               " | " + response.getResponseTimeMs() + "ms]" + RESET);

            if (!response.getSuggestedChips().isEmpty()) {
                System.out.print(CYAN + "   💡 Suggestions: " + RESET);
                for (int i = 0; i < response.getSuggestedChips().size(); i++) {
                    System.out.print("[" + response.getSuggestedChips().get(i) + "]");
                    if (i < response.getSuggestedChips().size() - 1) System.out.print(" ");
                }
                System.out.println();
            }
            System.out.println();
        }
    }

    private void printBanner() {
        System.out.println(CYAN + BOLD + "======================================================================" + RESET);
        System.out.println(CYAN + BOLD + "           🤖 CODEALPHA ARTIFICIAL INTELLIGENCE CHATBOT               " + RESET);
        System.out.println(CYAN + "      Natural Language Processing • Machine Learning • Java SE        " + RESET);
        System.out.println(CYAN + BOLD + "======================================================================" + RESET);
    }

    private void printHelp() {
        System.out.println(YELLOW + "\n--- Chatbot CLI Commands ---");
        System.out.println(" • /help        : Display this commands manual");
        System.out.println(" • /faq         : List all loaded knowledge base FAQs");
        System.out.println(" • /stats       : Show NLP engine & conversation statistics");
        System.out.println(" • /clear       : Reset conversation turns and memory");
        System.out.println(" • teach: Q->A  : Dynamically train the bot with a new Q&A pair");
        System.out.println(" • /exit        : Quit CLI application\n" + RESET);
    }

    private void printFaqs() {
        List<FAQEntry> entries = engine.getKnowledgeBase().getAllEntries();
        System.out.println(YELLOW + "\n--- Knowledge Base FAQs (" + entries.size() + " total) ---" + RESET);
        for (int i = 0; i < entries.size(); i++) {
            FAQEntry e = entries.get(i);
            System.out.println(GREEN + (i + 1) + ". [" + e.getCategory() + "] " + RESET + e.getQuestion());
        }
        System.out.println();
    }

    private void printStats() {
        System.out.println(YELLOW + "\n--- Engine Statistics ---" + RESET);
        System.out.println(" • Total FAQs Loaded: " + engine.getKnowledgeBase().getEntryCount());
        System.out.println(" • Vocabulary Size: " + engine.getKnowledgeBase().getVectorizer().getVocabularySize() + " terms");
        System.out.println(" • Conversation Turns: " + engine.getConversationContext().getTurnCount());
        System.out.println(" • Avg Sentiment: " + String.format("%.2f", engine.getConversationContext().getAverageSentimentScore()));
        System.out.println();
    }
}
