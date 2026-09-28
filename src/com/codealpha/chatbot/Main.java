package com.codealpha.chatbot;

import com.codealpha.chatbot.cli.ChatbotCli;
import com.codealpha.chatbot.engine.ChatbotEngine;
import com.codealpha.chatbot.gui.ChatbotGui;
import com.codealpha.chatbot.server.ChatbotHttpServer;

import javax.swing.*;
import java.awt.Desktop;
import java.net.URI;

/**
 * Main application entry point for CodeAlpha AI Chatbot.
 * Supports launching as Web Server + UI, Swing Desktop GUI, or CLI console.
 */
public class Main {
    private static final int DEFAULT_PORT = 8088;

    public static void main(String[] args) {
        ChatbotEngine engine = new ChatbotEngine();

        String mode = "web";
        int port = DEFAULT_PORT;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i].toLowerCase();
            if (arg.equals("--gui") || arg.equals("-g")) {
                mode = "gui";
            } else if (arg.equals("--cli") || arg.equals("-c")) {
                mode = "cli";
            } else if (arg.equals("--web") || arg.equals("-w")) {
                mode = "web";
            } else if (arg.equals("--all") || arg.equals("-a")) {
                mode = "all";
            } else if (arg.equals("--port") || arg.equals("-p")) {
                if (i + 1 < args.length) {
                    try {
                        port = Integer.parseInt(args[++i]);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        switch (mode) {
            case "gui":
                launchGui(engine);
                break;
            case "cli":
                launchCli(engine);
                break;
            case "all":
                launchWebServer(engine, port, false);
                launchGui(engine);
                break;
            case "web":
            default:
                launchWebServer(engine, port, true);
                break;
        }
    }

    private static void launchGui(ChatbotEngine engine) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            ChatbotGui gui = new ChatbotGui(engine);
            gui.setVisible(true);
        });
    }

    private static void launchCli(ChatbotEngine engine) {
        ChatbotCli cli = new ChatbotCli(engine);
        cli.start();
    }

    private static void launchWebServer(ChatbotEngine engine, int initialPort, boolean openBrowser) {
        int port = initialPort;
        boolean started = false;
        for (int attempt = 0; attempt < 5; attempt++) {
            try {
                ChatbotHttpServer server = new ChatbotHttpServer(port, engine);
                server.start();
                started = true;
                break;
            } catch (Exception e) {
                System.err.println("Port " + port + " is busy, trying next port...");
                port++;
            }
        }

        if (started) {
            if (openBrowser && Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URI("http://localhost:" + port));
                } catch (Exception ignored) {}
            }
        } else {
            System.err.println("Could not bind HTTP server. Falling back to Swing GUI...");
            launchGui(engine);
        }
    }
}
