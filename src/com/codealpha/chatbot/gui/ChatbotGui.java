package com.codealpha.chatbot.gui;

import com.codealpha.chatbot.engine.ChatbotEngine;
import com.codealpha.chatbot.model.BotResponse;
import com.codealpha.chatbot.model.FAQEntry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Modern Java Swing Desktop GUI for the CodeAlpha AI Chatbot.
 * Features customizable glassmorphism dark theme, NLP diagnostics HUD,
 * interactive quick suggestions, Knowledge Base manager, and chat export.
 */
public class ChatbotGui extends JFrame {
    private final ChatbotEngine engine;

    // UI Components
    private JPanel chatPanel;
    private JScrollPane chatScrollPane;
    private JTextField inputField;
    private JButton sendButton;
    private JPanel chipsPanel;

    // NLP HUD Components
    private JLabel intentLabel;
    private JProgressBar confidenceBar;
    private JLabel sentimentLabel;
    private JLabel algorithmLabel;
    private JLabel entitiesLabel;
    private JLabel turnCountLabel;

    // Theme Colors
    private static final Color BG_DARK = new Color(15, 23, 42); // slate-900
    private static final Color CARD_DARK = new Color(30, 41, 59); // slate-800
    private static final Color ACCENT_BLUE = new Color(59, 130, 246); // blue-500
    private static final Color USER_BUBBLE_BG = new Color(37, 99, 235); // blue-600
    private static final Color BOT_BUBBLE_BG = new Color(51, 65, 85); // slate-700
    private static final Color TEXT_WHITE = new Color(248, 250, 252);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);

    public ChatbotGui(ChatbotEngine engine) {
        super("CodeAlpha AI Chatbot — Intelligent Assistant");
        this.engine = engine;
        initializeUI();
    }

    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setMinimumSize(new Dimension(850, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());

        // Header Panel
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Main Center Split (Chat on Left, NLP HUD on Right)
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBackground(BG_DARK);

        JPanel chatContainer = createChatContainer();
        JPanel hudContainer = createNlpHudPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatContainer, hudContainer);
        splitPane.setResizeWeight(0.72);
        splitPane.setDividerSize(4);
        splitPane.setBackground(CARD_DARK);
        splitPane.setBorder(null);

        centerContainer.add(splitPane, BorderLayout.CENTER);
        add(centerContainer, BorderLayout.CENTER);

        // Add Initial Bot Welcome Message
        SwingUtilities.invokeLater(() -> {
            addBotMessage("👋 **Welcome to CodeAlpha AI Assistant!**\n\n" +
                "I am equipped with a Java NLP pipeline, TF-IDF vectorizer, and Naive Bayes ML intent classification.\n" +
                "Ask me anything about **CodeAlpha tasks**, **Java OOP**, **data structures**, **algorithms**, or use `teach:` to train me with new facts!",
                "Greeting", 1.0, "System_Init", null);
            updateChips(java.util.Arrays.asList("CodeAlpha Tasks", "Explain OOP in Java", "What is TF-IDF?", "Tell me a joke"));
        });
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD_DARK);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titlePanel.setOpaque(false);

        JLabel logo = new JLabel("🤖");
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textPanel.setOpaque(false);

        JLabel title = new JLabel("CodeAlpha AI Chatbot");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_WHITE);

        JLabel subtitle = new JLabel("Natural Language Processing & Machine Learning Engine");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(TEXT_MUTED);

        textPanel.add(title);
        textPanel.add(subtitle);
        titlePanel.add(logo);
        titlePanel.add(textPanel);

        // Action Buttons on Right
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton trainBtn = createStyledButton("🎓 Teach Bot", new Color(16, 185, 129));
        trainBtn.addActionListener(e -> showTeachDialog());

        JButton faqBtn = createStyledButton("📚 View FAQs", new Color(14, 165, 233));
        faqBtn.addActionListener(e -> showFaqListDialog());

        JButton exportBtn = createStyledButton("💾 Export", new Color(99, 102, 241));
        exportBtn.addActionListener(e -> exportChatHistory());

        JButton clearBtn = createStyledButton("🗑️ Clear", new Color(239, 68, 68));
        clearBtn.addActionListener(e -> clearChat());

        actions.add(trainBtn);
        actions.add(faqBtn);
        actions.add(exportBtn);
        actions.add(clearBtn);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JPanel createChatContainer() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);

        // Chat Message Area
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(BG_DARK);
        chatPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        chatScrollPane = new JScrollPane(chatPanel);
        chatScrollPane.setBorder(null);
        chatScrollPane.setBackground(BG_DARK);
        chatScrollPane.getViewport().setBackground(BG_DARK);
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Bottom Input Container
        JPanel bottomContainer = new JPanel(new BorderLayout(0, 8));
        bottomContainer.setBackground(BG_DARK);
        bottomContainer.setBorder(new EmptyBorder(8, 16, 16, 16));

        // Quick Suggestion Chips Panel
        chipsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        chipsPanel.setBackground(BG_DARK);

        // Input Field and Send Button
        JPanel inputBar = new JPanel(new BorderLayout(8, 0));
        inputBar.setBackground(CARD_DARK);
        inputBar.setBorder(new EmptyBorder(6, 12, 6, 6));

        inputField = new JTextField();
        inputField.setBackground(CARD_DARK);
        inputField.setForeground(TEXT_WHITE);
        inputField.setCaretColor(TEXT_WHITE);
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setBorder(null);
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });

        sendButton = createStyledButton("Send ➔", ACCENT_BLUE);
        sendButton.addActionListener(e -> sendMessage());

        inputBar.add(inputField, BorderLayout.CENTER);
        inputBar.add(sendButton, BorderLayout.EAST);

        bottomContainer.add(chipsPanel, BorderLayout.NORTH);
        bottomContainer.add(inputBar, BorderLayout.SOUTH);

        panel.add(chatScrollPane, BorderLayout.CENTER);
        panel.add(bottomContainer, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createNlpHudPanel() {
        JPanel hud = new JPanel();
        hud.setLayout(new BoxLayout(hud, BoxLayout.Y_AXIS));
        hud.setBackground(CARD_DARK);
        hud.setBorder(new EmptyBorder(16, 16, 16, 16));
        hud.setPreferredSize(new Dimension(300, 0));

        JLabel hudTitle = new JLabel("🧠 NLP Live Inspector");
        hudTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        hudTitle.setForeground(TEXT_WHITE);
        hudTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel hudSubtitle = new JLabel("Real-time NLP diagnostics & ML signals");
        hudSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hudSubtitle.setForeground(TEXT_MUTED);
        hudSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        hud.add(hudTitle);
        hud.add(hudSubtitle);
        hud.add(Box.createVerticalStrut(16));

        // Metric 1: Intent
        intentLabel = new JLabel("Intent: Waiting...");
        formatHudLabel(intentLabel);
        hud.add(intentLabel);
        hud.add(Box.createVerticalStrut(12));

        // Metric 2: Confidence
        JLabel confTitle = new JLabel("Confidence Score:");
        formatHudSubTitle(confTitle);
        hud.add(confTitle);

        confidenceBar = new JProgressBar(0, 100);
        confidenceBar.setValue(0);
        confidenceBar.setStringPainted(true);
        confidenceBar.setForeground(ACCENT_BLUE);
        confidenceBar.setBackground(BG_DARK);
        confidenceBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        confidenceBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        hud.add(confidenceBar);
        hud.add(Box.createVerticalStrut(12));

        // Metric 3: Sentiment
        sentimentLabel = new JLabel("Sentiment: Neutral 😐");
        formatHudLabel(sentimentLabel);
        hud.add(sentimentLabel);
        hud.add(Box.createVerticalStrut(12));

        // Metric 4: Algorithm
        algorithmLabel = new JLabel("Algorithm: Hybrid ML/Rules");
        formatHudLabel(algorithmLabel);
        hud.add(algorithmLabel);
        hud.add(Box.createVerticalStrut(12));

        // Metric 5: Entities
        entitiesLabel = new JLabel("<html>Entities: <i>None detected</i></html>");
        formatHudLabel(entitiesLabel);
        hud.add(entitiesLabel);
        hud.add(Box.createVerticalStrut(12));

        // Metric 6: Turns & Memory
        turnCountLabel = new JLabel("Conversation Turns: 0");
        formatHudLabel(turnCountLabel);
        hud.add(turnCountLabel);

        hud.add(Box.createVerticalGlue());

        // Quick tip at bottom
        JLabel tipLabel = new JLabel("<html><small>💡 <b>Tip:</b> Try typing <code>teach: Q -&gt; A</code> to teach the bot new facts dynamically!</small></html>");
        tipLabel.setForeground(TEXT_MUTED);
        tipLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        hud.add(tipLabel);

        return hud;
    }

    private void formatHudLabel(JLabel lbl) {
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT_WHITE);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void formatHudSubTitle(JLabel lbl) {
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void sendMessage() {
        String query = inputField.getText().trim();
        if (query.isEmpty()) return;

        inputField.setText("");
        addUserMessage(query);

        // Process in background worker to keep GUI silky smooth
        SwingWorker<BotResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected BotResponse doInBackground() {
                return engine.processQuery(query);
            }

            @Override
            protected void done() {
                try {
                    BotResponse resp = get();
                    addBotMessage(resp.getText(), resp.getIntent().getDisplayName(), resp.getConfidence(), resp.getAlgorithmUsed(), resp.getUserSentiment());
                    updateNlpHud(resp);
                    updateChips(resp.getSuggestedChips());
                } catch (Exception e) {
                    addBotMessage("Error processing query: " + e.getMessage(), "Error", 0.0, "ErrorHandler", null);
                }
            }
        };
        worker.execute();
    }

    private void addUserMessage(String message) {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1000));

        JPanel bubble = new JPanel(new BorderLayout());
        bubble.setBackground(USER_BUBBLE_BG);
        bubble.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel textLbl = new JLabel("<html><p style='width: 320px; color: #FFFFFF; font-family: Segoe UI; font-size: 13px;'>" + escapeHtml(message) + "</p></html>");
        JLabel timeLbl = new JLabel(LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
        timeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLbl.setForeground(new Color(200, 220, 255));
        timeLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        bubble.add(textLbl, BorderLayout.CENTER);
        bubble.add(timeLbl, BorderLayout.SOUTH);
        wrapper.add(bubble);

        chatPanel.add(wrapper);
        chatPanel.revalidate();
        chatPanel.repaint();
        scrollToBottom();
    }

    private void addBotMessage(String message, String intent, double confidence, String algorithm, com.codealpha.chatbot.nlp.SentimentAnalyzer.SentimentResult sentiment) {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1000));

        JPanel bubble = new JPanel(new BorderLayout(0, 4));
        bubble.setBackground(BOT_BUBBLE_BG);
        bubble.setBorder(new EmptyBorder(12, 14, 12, 14));

        String formattedHtml = formatMarkdownToHtml(message);
        JLabel textLbl = new JLabel("<html><p style='width: 380px; color: #F8FAFC; font-family: Segoe UI; font-size: 13px;'>" + formattedHtml + "</p></html>");

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        String badgeText = String.format("⚡ %s (%.0f%%) • %s", intent, confidence * 100, algorithm);
        JLabel badge = new JLabel(badgeText);
        badge.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        badge.setForeground(TEXT_MUTED);

        JLabel timeLbl = new JLabel(LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
        timeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLbl.setForeground(TEXT_MUTED);

        footer.add(badge, BorderLayout.WEST);
        footer.add(timeLbl, BorderLayout.EAST);

        bubble.add(textLbl, BorderLayout.CENTER);
        bubble.add(footer, BorderLayout.SOUTH);
        wrapper.add(bubble);

        chatPanel.add(wrapper);
        chatPanel.revalidate();
        chatPanel.repaint();
        scrollToBottom();
    }

    private void updateNlpHud(BotResponse resp) {
        intentLabel.setText("Intent: " + resp.getIntent().getDisplayName());
        int confPercent = (int) (resp.getConfidence() * 100);
        confidenceBar.setValue(confPercent);
        confidenceBar.setString(confPercent + "%");

        if (resp.getUserSentiment() != null) {
            sentimentLabel.setText(String.format("Sentiment: %s %s (%.2f)",
                resp.getUserSentiment().getLabel(), resp.getUserSentiment().getEmoji(), resp.getUserSentiment().getScore()));
        }

        algorithmLabel.setText("Algorithm: " + resp.getAlgorithmUsed());

        if (!resp.getExtractedEntities().isEmpty()) {
            StringBuilder sb = new StringBuilder("<html>Entities:<br>");
            for (String ent : resp.getExtractedEntities()) {
                sb.append("• <font color='#38bdf8'>").append(escapeHtml(ent)).append("</font><br>");
            }
            sb.append("</html>");
            entitiesLabel.setText(sb.toString());
        } else {
            entitiesLabel.setText("<html>Entities: <i>None detected</i></html>");
        }

        turnCountLabel.setText("Conversation Turns: " + engine.getConversationContext().getTurnCount());
    }

    private void updateChips(List<String> chips) {
        chipsPanel.removeAll();
        if (chips != null) {
            for (String chip : chips) {
                JButton chipBtn = new JButton(chip);
                chipBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                chipBtn.setForeground(new Color(203, 213, 225));
                chipBtn.setBackground(new Color(51, 65, 85));
                chipBtn.setFocusPainted(false);
                chipBtn.setBorder(new EmptyBorder(4, 10, 4, 10));
                chipBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                chipBtn.addActionListener(e -> {
                    inputField.setText(chip);
                    sendMessage();
                });
                chipsPanel.add(chipBtn);
            }
        }
        chipsPanel.revalidate();
        chipsPanel.repaint();
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void showTeachDialog() {
        JDialog dialog = new JDialog(this, "🎓 Teach CodeAlpha Bot", true);
        dialog.setSize(500, 350);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(CARD_DARK);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(3, 1, 8, 8));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(16, 16, 16, 16));

        JTextField qField = new JTextField();
        JTextArea aArea = new JTextArea(3, 20);
        aArea.setLineWrap(true);
        aArea.setWrapStyleWord(true);
        JTextField cField = new JTextField("Custom Knowledge");

        JPanel p1 = new JPanel(new BorderLayout(4, 4));
        p1.setOpaque(false);
        JLabel l1 = new JLabel("Question / Query:");
        l1.setForeground(TEXT_WHITE);
        p1.add(l1, BorderLayout.NORTH);
        p1.add(qField, BorderLayout.CENTER);

        JPanel p2 = new JPanel(new BorderLayout(4, 4));
        p2.setOpaque(false);
        JLabel l2 = new JLabel("Answer / Response:");
        l2.setForeground(TEXT_WHITE);
        p2.add(l2, BorderLayout.NORTH);
        p2.add(new JScrollPane(aArea), BorderLayout.CENTER);

        JPanel p3 = new JPanel(new BorderLayout(4, 4));
        p3.setOpaque(false);
        JLabel l3 = new JLabel("Category:");
        l3.setForeground(TEXT_WHITE);
        p3.add(l3, BorderLayout.NORTH);
        p3.add(cField, BorderLayout.CENTER);

        form.add(p1);
        form.add(p2);
        form.add(p3);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton saveBtn = createStyledButton("Save & Train Model", new Color(16, 185, 129));
        saveBtn.addActionListener(e -> {
            String q = qField.getText().trim();
            String a = aArea.getText().trim();
            String c = cField.getText().trim();
            if (q.isEmpty() || a.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Question and Answer cannot be empty!", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            FAQEntry entry = new FAQEntry("custom_" + System.currentTimeMillis(), c, q, a);
            engine.getKnowledgeBase().addEntry(entry);
            engine.getIntentClassifier().addCustomTrainingData(com.codealpha.chatbot.model.Intent.FAQ_QUERY, q);
            JOptionPane.showMessageDialog(dialog, "Bot successfully trained with new FAQ entry!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });

        actions.add(saveBtn);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showFaqListDialog() {
        JDialog dialog = new JDialog(this, "📚 Knowledge Base FAQs", true);
        dialog.setSize(650, 450);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(CARD_DARK);

        List<FAQEntry> list = engine.getKnowledgeBase().getAllEntries();
        String[] cols = {"Category", "Question", "Answer"};
        String[][] data = new String[list.size()][3];
        for (int i = 0; i < list.size(); i++) {
            FAQEntry e = list.get(i);
            data[i][0] = e.getCategory();
            data[i][1] = e.getQuestion();
            data[i][2] = e.getAnswer();
        }

        JTable table = new JTable(data, cols);
        table.setBackground(BG_DARK);
        table.setForeground(TEXT_WHITE);
        table.setRowHeight(24);
        table.getTableHeader().setBackground(CARD_DARK);
        table.getTableHeader().setForeground(TEXT_WHITE);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new EmptyBorder(10, 10, 10, 10));
        scroll.setBackground(BG_DARK);

        dialog.add(scroll);
        dialog.setVisible(true);
    }

    private void exportChatHistory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("CodeAlpha_Chat_History.txt"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter writer = new FileWriter(chooser.getSelectedFile())) {
                writer.write("=== CodeAlpha AI Chatbot Transcript ===\n");
                writer.write("Export Date: " + LocalDateTime.now() + "\n\n");
                for (com.codealpha.chatbot.model.ChatMessage msg : engine.getConversationContext().getHistory()) {
                    writer.write(String.format("[%s] %s: %s\n\n",
                        msg.getFormattedTime(), msg.getSender(), msg.getContent()));
                }
                JOptionPane.showMessageDialog(this, "Chat transcript saved successfully!", "Export", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to export chat: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearChat() {
        chatPanel.removeAll();
        engine.getConversationContext().clearHistory();
        chatPanel.revalidate();
        chatPanel.repaint();
        addBotMessage("Chat history cleared. How can I assist you now?", "System", 1.0, "Reset", null);
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String formatMarkdownToHtml(String text) {
        if (text == null) return "";
        String escaped = escapeHtml(text);
        // Replace bold **text** with <b>text</b>
        escaped = escaped.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
        // Replace italics *text* with <i>text</i>
        escaped = escaped.replaceAll("\\*(.*?)\\*", "<i>$1</i>");
        // Replace inline `code`
        escaped = escaped.replaceAll("`(.*?)`", "<code style='background:#1e293b; color:#38bdf8; padding:2px 4px;'>$1</code>");
        // Replace newlines
        escaped = escaped.replace("\n", "<br>");
        return escaped;
    }
}
