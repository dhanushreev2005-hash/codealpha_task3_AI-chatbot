package com.codealpha.chatbot.server;

import com.codealpha.chatbot.engine.ChatbotEngine;
import com.codealpha.chatbot.model.BotResponse;
import com.codealpha.chatbot.model.FAQEntry;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Embedded Lightweight Java HTTP Web Server providing REST APIs and serving
 * the modern Web UI frontend.
 */
public class ChatbotHttpServer {
    private final int port;
    private final ChatbotEngine engine;
    private final long startTime;
    private HttpServer server;

    public ChatbotHttpServer(int port, ChatbotEngine engine) {
        this.port = port;
        this.engine = engine;
        this.startTime = System.currentTimeMillis();
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Static file handler & API routing
        server.createContext("/api/chat", new ChatApiHandler());
        server.createContext("/api/faq", new FaqApiHandler());
        server.createContext("/api/train", new TrainApiHandler());
        server.createContext("/api/stats", new StatsApiHandler());
        server.createContext("/api/clear", new ClearApiHandler());
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
        System.out.println("=================================================");
        System.out.println("🤖 CodeAlpha AI Chatbot Web Server Started!");
        System.out.println("🌐 Open in your browser: http://localhost:" + port);
        System.out.println("=================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    // --- API Handlers ---

    private class ChatApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method Not Allowed");
                return;
            }

            String body = readRequestBody(exchange);
            String message = JsonUtil.getString(body, "message");
            if (message == null) message = "";

            BotResponse response = engine.processQuery(message);

            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"text\":").append(JsonUtil.escape(response.getText())).append(",");
            json.append("\"intent\":").append(JsonUtil.escape(response.getIntent().getDisplayName())).append(",");
            json.append("\"confidence\":").append(String.format(java.util.Locale.US, "%.3f", response.getConfidence())).append(",");
            
            if (response.getUserSentiment() != null) {
                json.append("\"sentiment\":{")
                    .append("\"label\":").append(JsonUtil.escape(response.getUserSentiment().getLabel())).append(",")
                    .append("\"emoji\":").append(JsonUtil.escape(response.getUserSentiment().getEmoji())).append(",")
                    .append("\"score\":").append(String.format(java.util.Locale.US, "%.3f", response.getUserSentiment().getScore()))
                    .append("},");
            } else {
                json.append("\"sentiment\":null,");
            }

            json.append("\"algorithm\":").append(JsonUtil.escape(response.getAlgorithmUsed())).append(",");
            
            // Entities
            json.append("\"entities\":[");
            for (int i = 0; i < response.getExtractedEntities().size(); i++) {
                json.append(JsonUtil.escape(response.getExtractedEntities().get(i)));
                if (i < response.getExtractedEntities().size() - 1) json.append(",");
            }
            json.append("],");

            // Suggested chips
            json.append("\"suggestedChips\":[");
            for (int i = 0; i < response.getSuggestedChips().size(); i++) {
                json.append(JsonUtil.escape(response.getSuggestedChips().get(i)));
                if (i < response.getSuggestedChips().size() - 1) json.append(",");
            }
            json.append("],");

            json.append("\"responseTimeMs\":").append(response.getResponseTimeMs()).append(",");
            json.append("\"userName\":").append(JsonUtil.escape(engine.getConversationContext().getUserName())).append(",");
            json.append("\"turnCount\":").append(engine.getConversationContext().getTurnCount());
            json.append("}");

            sendJsonResponse(exchange, 200, json.toString());
        }
    }

    private class FaqApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            List<FAQEntry> entries = engine.getKnowledgeBase().getAllEntries();
            StringBuilder json = new StringBuilder();
            json.append("[");
            for (int i = 0; i < entries.size(); i++) {
                FAQEntry e = entries.get(i);
                json.append("{")
                    .append("\"id\":").append(JsonUtil.escape(e.getId())).append(",")
                    .append("\"category\":").append(JsonUtil.escape(e.getCategory())).append(",")
                    .append("\"question\":").append(JsonUtil.escape(e.getQuestion())).append(",")
                    .append("\"answer\":").append(JsonUtil.escape(e.getAnswer()))
                    .append("}");
                if (i < entries.size() - 1) json.append(",");
            }
            json.append("]");

            sendJsonResponse(exchange, 200, json.toString());
        }
    }

    private class TrainApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method Not Allowed");
                return;
            }

            String body = readRequestBody(exchange);
            String question = JsonUtil.getString(body, "question");
            String answer = JsonUtil.getString(body, "answer");
            String category = JsonUtil.getString(body, "category");
            if (category == null || category.isEmpty()) category = "User Trained";

            if (question == null || question.trim().isEmpty() || answer == null || answer.trim().isEmpty()) {
                sendError(exchange, 400, "Question and Answer cannot be empty");
                return;
            }

            String id = "train_" + System.currentTimeMillis();
            FAQEntry entry = new FAQEntry(id, category, question.trim(), answer.trim());
            engine.getKnowledgeBase().addEntry(entry);
            engine.getIntentClassifier().addCustomTrainingData(com.codealpha.chatbot.model.Intent.FAQ_QUERY, question.trim());

            sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"FAQ successfully learned and model retrained!\"}");
        }
    }

    private class StatsApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            long uptimeSec = (System.currentTimeMillis() - startTime) / 1000;
            int totalFaqs = engine.getKnowledgeBase().getEntryCount();
            int vocabSize = engine.getKnowledgeBase().getVectorizer().getVocabularySize();
            int turnCount = engine.getConversationContext().getTurnCount();
            double avgSentiment = engine.getConversationContext().getAverageSentimentScore();

            String json = String.format(java.util.Locale.US,
                "{\"uptimeSec\":%d,\"totalFaqs\":%d,\"vocabSize\":%d,\"turnCount\":%d,\"avgSentiment\":%.2f}",
                uptimeSec, totalFaqs, vocabSize, turnCount, avgSentiment);

            sendJsonResponse(exchange, 200, json);
        }
    }

    private class ClearApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            engine.getConversationContext().clearHistory();
            sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Conversation context cleared!\"}");
        }
    }

    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }

            // Remove leading slash
            String cleanPath = path.startsWith("/") ? path.substring(1) : path;
            Path filePath = Paths.get("web", cleanPath);

            if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
                // Fallback to index.html for SPA-like experience
                filePath = Paths.get("web", "index.html");
            }

            if (!Files.exists(filePath)) {
                sendError(exchange, 404, "Not Found");
                return;
            }

            String mimeType = getMimeType(filePath.toString());
            byte[] fileBytes = Files.readAllBytes(filePath);

            exchange.getResponseHeaders().set("Content-Type", mimeType);
            exchange.getResponseHeaders().set("Cache-Control", "no-cache");
            exchange.sendResponseHeaders(200, fileBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(fileBytes);
            }
        }
    }

    // --- Helpers ---

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            return bos.toString(StandardCharsets.UTF_8);
        }
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        String json = "{\"error\":" + JsonUtil.escape(message) + "}";
        sendJsonResponse(exchange, statusCode, json);
    }

    private String getMimeType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=utf-8";
        if (path.endsWith(".css")) return "text/css; charset=utf-8";
        if (path.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (path.endsWith(".json")) return "application/json; charset=utf-8";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".ico")) return "image/x-icon";
        return "text/plain; charset=utf-8";
    }

    private String toJsonString(String s) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    private String extractJsonValue(String json, String key) {
        if (json == null) return null;
        // Simple regex-based extractor for standard JSON key-values
        String patternStr = "\"" + key + "\"\\s*:\\s*\"([^\"]*)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(patternStr);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            String val = m.group(1);
            return val.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return null;
    }
}
