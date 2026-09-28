package com.codealpha.chatbot.engine;

import com.codealpha.chatbot.model.FAQEntry;
import com.codealpha.chatbot.nlp.CosineSimilarity;
import com.codealpha.chatbot.nlp.LevenshteinDistance;
import com.codealpha.chatbot.nlp.TFIDFVectorizer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Knowledge Base holding curated and dynamically learned FAQ items.
 * Integrates TF-IDF vector indexing, semantic search, and file persistence.
 */
public class KnowledgeBase {

    public static class MatchResult {
        private final FAQEntry entry;
        private final double score;
        private final String matchType;

        public MatchResult(FAQEntry entry, double score, String matchType) {
            this.entry = entry;
            this.score = score;
            this.matchType = matchType;
        }

        public FAQEntry getEntry() { return entry; }
        public double getScore() { return score; }
        public String getMatchType() { return matchType; }
    }

    private final List<FAQEntry> entries;
    private final TFIDFVectorizer vectorizer;
    private final List<Map<String, Double>> documentVectors;
    private final Path storagePath;

    public KnowledgeBase() {
        this.entries = new ArrayList<>();
        this.vectorizer = new TFIDFVectorizer();
        this.documentVectors = new ArrayList<>();
        this.storagePath = Paths.get("data", "knowledge_base.txt");

        loadDefaultFAQs();
        loadPersistedFAQs();
        rebuildIndex();
    }

    private void loadDefaultFAQs() {
        // --- CodeAlpha Internship Domain ---
        addEntryInternal(new FAQEntry(
            "ca_overview",
            "CodeAlpha Internship",
            "What is the CodeAlpha Java Programming Internship?",
            "The CodeAlpha Java Programming Internship is an in-depth program focusing on Java development and object-oriented programming (OOP). It empowers students to master core Java fundamentals, data structures, multithreading, file handling, backend development, and building real-world enterprise applications.",
            Arrays.asList("tell me about codealpha", "codealpha overview", "what is codealpha"),
            Arrays.asList("codealpha", "internship", "overview", "java")
        ));

        addEntryInternal(new FAQEntry(
            "ca_tasks",
            "CodeAlpha Internship",
            "What are the tasks in the CodeAlpha Java Internship?",
            "The 4 Java internship tasks are:\n" +
            "1. **Task 1: Student Grade Tracker** - Input & manage student grades, calculate average/highest/lowest scores using ArrayLists, summary reporting (Console/GUI).\n" +
            "2. **Task 2: Stock Trading Platform** - Simulate stock market data, buy/sell operations, portfolio tracking, and file/DB persistence.\n" +
            "3. **Task 3: Artificial Intelligence Chatbot** - Java-based chatbot with NLP techniques, machine learning/rule-based logic, FAQ training, and modern Web/GUI interface.\n" +
            "4. **Task 4: Hotel Reservation System** - Search, book & manage hotel rooms with categorization, payment simulation, and booking persistence.",
            Arrays.asList("list all tasks", "internship tasks", "what projects do i have to do", "task 1 task 2 task 3 task 4"),
            Arrays.asList("tasks", "list", "projects", "grade tracker", "stock trading", "ai chatbot", "hotel reservation")
        ));

        addEntryInternal(new FAQEntry(
            "ca_criteria",
            "CodeAlpha Internship",
            "What is the criteria for completing the CodeAlpha internship?",
            "To be eligible for the internship completion certificate, participants must complete a minimum of **2 or 3 out of the 4 tasks**. Submitting only one task will be considered incomplete and certificates will not be issued.",
            Arrays.asList("how many tasks do i need to finish", "completion criteria", "minimum tasks required", "passing criteria"),
            Arrays.asList("criteria", "completion", "minimum", "certificate", "rules")
        ));

        addEntryInternal(new FAQEntry(
            "ca_submission",
            "CodeAlpha Internship",
            "How do I submit my CodeAlpha projects?",
            "Submission Process:\n" +
            "1. Upload complete source code to a public GitHub repository named: `CodeAlpha_ProjectName` (e.g. `CodeAlpha_AIChatbot`).\n" +
            "2. Record and post a video explanation/demo of your working project on LinkedIn tagging **@CodeAlpha** with your GitHub repo link.\n" +
            "3. Submit your completed task details via the official Google Submission Form shared in your WhatsApp group.",
            Arrays.asList("how to submit task", "submission instructions", "github repository name", "submission form link", "where to submit"),
            Arrays.asList("submission", "github", "linkedin", "video", "form", "whatsapp")
        ));

        addEntryInternal(new FAQEntry(
            "ca_perks",
            "CodeAlpha Internship",
            "What perks do interns receive from CodeAlpha?",
            "CodeAlpha Internship Perks include:\n" +
            "• Internship Offer Letter\n" +
            "• Completion Certificate (QR Verified)\n" +
            "• Unique ID Certificate\n" +
            "• Letter of Recommendation (LoR) based on performance\n" +
            "• Job Opportunities & Placement Support\n" +
            "• Resume Building Support",
            Arrays.asList("what are the internship perks", "benefits of codealpha", "will i get certificate and lor", "internship certificate"),
            Arrays.asList("perks", "certificate", "letter of recommendation", "offer letter", "placement")
        ));

        // --- Core Java Domain ---
        addEntryInternal(new FAQEntry(
            "java_oop",
            "Java Programming",
            "What are the 4 Pillars of Object-Oriented Programming (OOP) in Java?",
            "The 4 fundamental pillars of OOP in Java are:\n" +
            "1. **Encapsulation**: Bundling data (variables) and methods within a class while restricting direct access using private fields and public getters/setters.\n" +
            "2. **Inheritance**: Mechanism where a child class acquires fields and methods of a parent class (`extends` keyword) to promote code reusability.\n" +
            "3. **Polymorphism**: Ability of an object or method to take multiple forms (Compile-time Method Overloading & Runtime Method Overriding).\n" +
            "4. **Abstraction**: Hiding internal implementation details and showing only essential features to the user using Abstract Classes and Interfaces.",
            Arrays.asList("explain oop concepts", "what is oop in java", "four pillars of oop", "encapsulation inheritance polymorphism abstraction"),
            Arrays.asList("oop", "encapsulation", "inheritance", "polymorphism", "abstraction")
        ));

        addEntryInternal(new FAQEntry(
            "java_jvm",
            "Java Programming",
            "What is the difference between JDK, JRE, and JVM?",
            "• **JVM (Java Virtual Machine)**: The abstract engine that executes compiled Java bytecode (`.class` files) and provides platform independence ('Write Once, Run Anywhere').\n" +
            "• **JRE (Java Runtime Environment)**: JVM + Core Java Class Libraries necessary to *run* compiled Java applications.\n" +
            "• **JDK (Java Development Kit)**: Complete software development package containing JRE + Development Tools (`javac` compiler, `javadoc`, debugger, jar utility).",
            Arrays.asList("jdk vs jre vs jvm", "difference between jdk and jre", "what is jvm", "java virtual machine"),
            Arrays.asList("jvm", "jre", "jdk", "architecture", "bytecode")
        ));

        addEntryInternal(new FAQEntry(
            "java_collections",
            "Java Programming",
            "What is the Java Collections Framework and when do you use HashMap vs ArrayList?",
            "The Java Collections Framework provides a standardized architecture for storing and manipulating groups of objects:\n" +
            "• **ArrayList**: Dynamic resizable array implementing `List`. Fast random access by index O(1), ideal for ordered lists where lookups dominate.\n" +
            "• **LinkedList**: Doubly-linked list implementing `List` and `Deque`. Fast insertions/deletions O(1) when node pointer is known.\n" +
            "• **HashMap**: Hash table based `Map` storing Key-Value pairs with O(1) average lookup, insert, and delete performance.\n" +
            "• **HashSet**: Set implementation backed by HashMap guaranteeing unique elements with O(1) average operations.",
            Arrays.asList("hashmap vs arraylist", "what is java collections framework", "list set map in java", "collections hierarchy"),
            Arrays.asList("collections", "arraylist", "linkedlist", "hashmap", "hashset", "map", "list")
        ));

        addEntryInternal(new FAQEntry(
            "java_multithreading",
            "Java Programming",
            "How does Multithreading and Concurrency work in Java?",
            "Multithreading allows concurrent execution of two or more threads to maximize CPU utilization. In Java, threads can be created by:\n" +
            "1. Extending `Thread` class and overriding `run()`.\n" +
            "2. Implementing `Runnable` interface (preferred for loose coupling).\n" +
            "3. Using `Callable<V>` and `ExecutorService` (modern Thread Pools) for asynchronous tasks returning futures.\n" +
            "Thread safety is achieved using `synchronized` blocks/methods, `volatile` variables, and atomic locks from `java.util.concurrent`.",
            Arrays.asList("explain multithreading in java", "how to create a thread in java", "what is executor service", "concurrency in java"),
            Arrays.asList("multithreading", "thread", "concurrency", "runnable", "executorservice", "synchronized")
        ));

        addEntryInternal(new FAQEntry(
            "java_gc",
            "Java Programming",
            "How does Garbage Collection (GC) work in Java?",
            "Java Garbage Collection automatically reclaims heap memory allocated to objects that are no longer reachable from any active GC Roots (local variables, static fields, active threads). Key collectors include G1GC, ZGC, and Parallel GC, utilizing generational heap management (Young Generation: Eden & Survivor spaces, and Old Generation).",
            Arrays.asList("what is garbage collector in java", "how does gc work", "memory management in java", "automatic garbage collection"),
            Arrays.asList("garbage collection", "gc", "memory", "heap", "g1gc", "zgc")
        ));

        addEntryInternal(new FAQEntry(
            "java_streams",
            "Java Programming",
            "What are Java Streams and Lambda Expressions?",
            "Introduced in Java 8:\n" +
            "• **Lambda Expressions**: Anonymous functions (`(params) -> { body }`) that provide a concise syntax to implement Functional Interfaces (`Predicate`, `Function`, `Consumer`, `Supplier`).\n" +
            "• **Streams API**: Functional pipeline to process sequences of elements supporting declarative operations like `filter()`, `map()`, `sorted()`, `reduce()`, and `collect()` without mutating the underlying data source.",
            Arrays.asList("what are java streams", "what is lambda expression in java", "java 8 features", "stream map filter reduce"),
            Arrays.asList("streams", "lambda", "functional programming", "java 8", "filter", "map")
        ));

        addEntryInternal(new FAQEntry(
            "java_exceptions",
            "Java Programming",
            "What is the difference between Checked and Unchecked Exceptions in Java?",
            "• **Checked Exceptions**: Subclasses of `Exception` (excluding `RuntimeException`). Checked at compile-time and must be handled using `try-catch` or declared with `throws` (e.g. `IOException`, `SQLException`).\n" +
            "• **Unchecked Exceptions**: Subclasses of `RuntimeException` and `Error`. Occur at runtime and do not require explicit declaration (e.g. `NullPointerException`, `ArrayIndexOutOfBoundsException`, `IllegalArgumentException`).",
            Arrays.asList("checked vs unchecked exceptions", "exception handling in java", "try catch finally java", "runtime exception vs exception"),
            Arrays.asList("exceptions", "checked", "unchecked", "try-catch", "runtimeexception")
        ));

        // --- AI & NLP Domain ---
        addEntryInternal(new FAQEntry(
            "ai_nlp_overview",
            "AI & NLP",
            "What is Natural Language Processing (NLP)?",
            "Natural Language Processing (NLP) is a branch of Artificial Intelligence that enables computers to understand, interpret, and generate human language. Standard NLP pipelines include:\n" +
            "1. **Tokenization**: Splitting text into words/sentences.\n" +
            "2. **Normalization & Cleaning**: Lowercasing, removing noise, expanding contractions.\n" +
            "3. **Stopwords Removal & Stemming/Lemmatization**: Reducing words to root forms (e.g., 'running' -> 'run').\n" +
            "4. **Vectorization**: Converting text into mathematical vectors (Bag of Words, TF-IDF, Word Embeddings).\n" +
            "5. **Intent Classification & Entity Recognition**: Extracting meaning and parameters.",
            Arrays.asList("what is nlp", "explain natural language processing", "how does nlp pipeline work", "nlp components"),
            Arrays.asList("nlp", "natural language processing", "tokenization", "stemming", "pipeline")
        ));

        addEntryInternal(new FAQEntry(
            "ai_tfidf",
            "AI & NLP",
            "What is TF-IDF and how is it used in chatbots?",
            "**TF-IDF (Term Frequency-Inverse Document Frequency)** is a numerical statistic used in NLP to evaluate how important a word is to a document within a collection:\n" +
            "• **Term Frequency (TF)**: Measures frequency of a word in a specific text.\n" +
            "• **Inverse Document Frequency (IDF)**: Penalizes common words across all documents (like 'is', 'the') and boosts rare, informative keywords.\n" +
            "By computing TF-IDF vectors for user questions and FAQ entries, the chatbot calculates **Cosine Similarity** to instantly find the most relevant answer!",
            Arrays.asList("what is tf-idf", "explain tf idf", "term frequency inverse document frequency", "how does tfidf work"),
            Arrays.asList("tf-idf", "tfidf", "vectorizer", "term frequency", "cosine similarity")
        ));

        addEntryInternal(new FAQEntry(
            "ai_naive_bayes",
            "AI & NLP",
            "What is Naive Bayes classification in AI?",
            "**Naive Bayes** is a probabilistic machine learning algorithm based on Bayes' Theorem with the 'naive' assumption of feature independence given the class. In text classification, it calculates the posterior probability $P(Intent | Query)$ using word occurrence frequencies and prior probabilities to categorize inputs with high accuracy and lightning-fast inference.",
            Arrays.asList("what is naive bayes", "explain naive bayes classifier", "machine learning in chatbot", "probabilistic classification"),
            Arrays.asList("naive bayes", "bayes theorem", "machine learning", "intent classification", "ml")
        ));
    }

    private void addEntryInternal(FAQEntry entry) {
        entries.add(entry);
    }

    public synchronized void addEntry(FAQEntry entry) {
        entries.add(entry);
        persistFAQ(entry);
        rebuildIndex();
    }

    public synchronized void rebuildIndex() {
        List<String> documents = new ArrayList<>();
        for (FAQEntry entry : entries) {
            documents.add(entry.getAllSearchableText());
        }

        vectorizer.fit(documents);
        documentVectors.clear();

        for (String doc : documents) {
            documentVectors.add(vectorizer.transform(doc));
        }
    }

    /**
     * Searches the Knowledge Base using TF-IDF vector cosine similarity and Levenshtein fuzzy matching.
     */
    public MatchResult search(String query) {
        if (query == null || query.trim().isEmpty() || entries.isEmpty()) {
            return null;
        }

        String normalizedQuery = query.toLowerCase().trim();

        // 1. Check exact or high-ratio Levenshtein match on question and alternatives
        for (FAQEntry entry : entries) {
            if (entry.getQuestion().equalsIgnoreCase(query)) {
                return new MatchResult(entry, 1.0, "ExactMatch");
            }
            double dist = LevenshteinDistance.similarityRatio(normalizedQuery, entry.getQuestion().toLowerCase());
            if (dist > 0.88) {
                return new MatchResult(entry, dist, "FuzzyLevenshtein");
            }
            for (String alt : entry.getAlternativeQuestions()) {
                double altDist = LevenshteinDistance.similarityRatio(normalizedQuery, alt.toLowerCase());
                if (altDist > 0.88) {
                    return new MatchResult(entry, altDist, "FuzzyLevenshteinAlt");
                }
            }
        }

        // 2. TF-IDF Cosine Similarity Search
        Map<String, Double> queryVector = vectorizer.transform(query);
        if (queryVector.isEmpty()) return null;

        double bestScore = 0.0;
        FAQEntry bestMatch = null;

        for (int i = 0; i < entries.size(); i++) {
            Map<String, Double> docVec = documentVectors.get(i);
            double sim = CosineSimilarity.compute(queryVector, docVec);
            if (sim > bestScore) {
                bestScore = sim;
                bestMatch = entries.get(i);
            }
        }

        if (bestMatch != null && bestScore >= 0.28) {
            return new MatchResult(bestMatch, bestScore, "TF-IDF_CosineSimilarity");
        }

        return null;
    }

    private void persistFAQ(FAQEntry entry) {
        try {
            if (!Files.exists(storagePath.getParent())) {
                Files.createDirectories(storagePath.getParent());
            }
            String line = String.format("%s\t%s\t%s\t%s\n",
                escapeField(entry.getId()),
                escapeField(entry.getCategory()),
                escapeField(entry.getQuestion()),
                escapeField(entry.getAnswer())
            );
            Files.write(storagePath, line.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Warning: Could not persist FAQ to disk: " + e.getMessage());
        }
    }

    private void loadPersistedFAQs() {
        if (!Files.exists(storagePath)) return;
        try {
            List<String> lines = Files.readAllLines(storagePath, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\t");
                if (parts.length >= 4) {
                    String id = unescapeField(parts[0]);
                    String cat = unescapeField(parts[1]);
                    String q = unescapeField(parts[2]);
                    String a = unescapeField(parts[3]);
                    entries.add(new FAQEntry(id, cat, q, a));
                }
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load persisted FAQs: " + e.getMessage());
        }
    }

    private String escapeField(String text) {
        if (text == null) return "";
        return text.replace("\t", "\\t").replace("\n", "\\n").replace("\r", "");
    }

    private String unescapeField(String text) {
        if (text == null) return "";
        return text.replace("\\t", "\t").replace("\\n", "\n");
    }

    public List<FAQEntry> getAllEntries() {
        return Collections.unmodifiableList(entries);
    }

    public int getEntryCount() {
        return entries.size();
    }

    public TFIDFVectorizer getVectorizer() {
        return vectorizer;
    }
}
