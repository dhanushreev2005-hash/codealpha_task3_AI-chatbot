package com.codealpha.chatbot.server;

import java.util.*;

/**
 * Lightweight, robust JSON parser and serializer in pure Java SE with zero dependencies.
 */
public class JsonUtil {

    public static String getString(String json, String key) {
        if (json == null || key == null) return null;
        json = json.trim();

        // Search for "key" followed by ':'
        int keyIndex = json.indexOf("\"" + key + "\"");
        if (keyIndex == -1) return null;

        int colonIndex = json.indexOf(":", keyIndex + key.length() + 2);
        if (colonIndex == -1) return null;

        int valStart = colonIndex + 1;
        while (valStart < json.length() && Character.isWhitespace(json.charAt(valStart))) {
            valStart++;
        }

        if (valStart >= json.length()) return null;

        if (json.charAt(valStart) == '"') {
            // String value
            StringBuilder sb = new StringBuilder();
            boolean escaped = false;
            for (int i = valStart + 1; i < json.length(); i++) {
                char c = json.charAt(i);
                if (escaped) {
                    if (c == 'n') sb.append('\n');
                    else if (c == 't') sb.append('\t');
                    else if (c == 'r') sb.append('\r');
                    else if (c == 'b') sb.append('\b');
                    else if (c == 'f') sb.append('\f');
                    else if (c == '"') sb.append('"');
                    else if (c == '\\') sb.append('\\');
                    else if (c == '/') sb.append('/');
                    else sb.append(c);
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    return sb.toString();
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        } else {
            // Non-string value (number, bool, etc.)
            int valEnd = valStart;
            while (valEnd < json.length() && json.charAt(valEnd) != ',' && json.charAt(valEnd) != '}' && !Character.isWhitespace(json.charAt(valEnd))) {
                valEnd++;
            }
            return json.substring(valStart, valEnd).trim();
        }
    }

    public static String escape(String s) {
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
                    if (c < 32) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append("\"");
        return sb.toString();
    }
}
