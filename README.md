# CodeAlpha AI Chatbot — Natural Language Processing & Machine Learning Engine

> **Task 3: Artificial Intelligence Chatbot** for the **CodeAlpha Java Programming Internship**.  
> Built in **Pure Java SE 26** with **zero external runtime dependencies**, featuring an end-to-end NLP pipeline, TF-IDF vector indexing, Naive Bayes Intent Classification, dynamic "Teach Bot" memory persistence, dual Modern Swing GUI & Glassmorphic Web App.

---

## 🌟 Key Features

1. **Natural Language Processing (NLP) Pipeline**:
   - **Porter Stemmer Algorithm**: Morphological root-word extraction.
   - **Contractions Expansion & Sanitization**: Normalizes inputs (e.g. *"what's"* -> *"what is"*).
   - **Stopwords Filter & N-Gram Generation**: Unigrams, bigrams, and trigrams.
   - **TF-IDF Vectorizer & Cosine Similarity**: High-precision semantic knowledge base search.
   - **Levenshtein Distance**: Fuzzy typo and spelling error tolerance.
   - **Lexicon Sentiment Analyzer**: Intensity boosters, negation flippers, polarity scoring (-1.0 to +1.0) & emoji badges.
2. **Machine Learning & Rule Hybrid Engine**:
   - **Multinomial Naive Bayes Classifier**: Probabilistic intent classification with Laplace smoothing.
   - **AST Math Expression Evaluator**: Safely solves arithmetic, trigonometry, square roots, powers.
   - **Dynamic "Teach Bot" Learning**: Live FAQ training that retrains vectors and persists to disk.
3. **Multi-Interface Deployment**:
   - 🌐 **Modern Glassmorphic Web UI**: REST API (`com.sun.net.httpserver`), speech-to-text voice input, text-to-speech voice synthesis, live NLP Inspector HUD, suggested reply chips, and export to Markdown/JSON.
   - 🖥️ **Desktop Swing GUI**: Dark glassmorphic layout, bubble messages, live NLP metrics HUD, interactive training dialog.
   - 💻 **Terminal CLI**: Interactive console with ANSI colored diagnostics.

---

## 🚀 How to Run in Visual Studio Code (Step-by-Step)

### Option A: Using the VS Code Built-in Terminal (Quickest)

1. Open **VS Code**.
2. Go to **File > Open Folder...** and select `E:\code alpha 3` (or your project folder).
3. Open the integrated terminal in VS Code:
   - Press <kbd>Ctrl</kbd> + <kbd>`</kbd> (backtick) OR click **Terminal > New Terminal**.
4. Run any of the following commands:

#### 1. Compile the project:
```bash
.\build.bat
```

#### 2. Launch your preferred interface:
- **Web App (Recommended)**:
  ```bash
  .\run_web.bat
  ```
  *Then open your browser at [http://localhost:8088](http://localhost:8088)*

- **Desktop Swing GUI**:
  ```bash
  .\run_gui.bat
  ```

- **Interactive Terminal CLI**:
  ```bash
  .\run_cli.bat
  ```

- **Both Web Server & GUI Together**:
  ```bash
  .\run_all.bat
  ```

---

### Option B: Using VS Code Java Run & Debug (F5)

1. Ensure the **Extension Pack for Java** is installed in VS Code.
2. Press <kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>D</kbd> to open the **Run & Debug** panel.
3. Select any configuration from the top dropdown:
   - `Launch AI Chatbot (Web Server + UI)`
   - `Launch AI Chatbot (Desktop Swing GUI)`
   - `Launch AI Chatbot (CLI Terminal Mode)`
   - `Launch AI Chatbot (Web + GUI Simultaneous)`
4. Press <kbd>F5</kbd> or click the green **Play** button.

---

### Option C: Manual Command Line (javac & java)

```bash
# 1. Compile all Java sources
javac -d bin src/com/codealpha/chatbot/model/*.java src/com/codealpha/chatbot/nlp/*.java src/com/codealpha/chatbot/engine/*.java src/com/codealpha/chatbot/server/*.java src/com/codealpha/chatbot/gui/*.java src/com/codealpha/chatbot/cli/*.java src/com/codealpha/chatbot/*.java

# 2. Run in Web mode (port 8088)
java -cp bin com.codealpha.chatbot.Main --web

# Or run GUI mode
java -cp bin com.codealpha.chatbot.Main --gui

# Or run CLI mode
java -cp bin com.codealpha.chatbot.Main --cli
```

---

## 📂 Project Structure

```
code alpha 3/
├── .vscode/
│   └── launch.json                # VS Code F5 Run/Debug configuration
├── bin/                           # Compiled Java .class bytecode
├── data/
│   └── knowledge_base.txt         # Dynamically persisted FAQ entries
├── src/
│   └── com/codealpha/chatbot/
│       ├── Main.java              # Entry point dispatcher (Web, GUI, CLI)
│       ├── cli/
│       │   └── ChatbotCli.java    # Interactive console interface
│       ├── engine/
│       │   ├── ChatbotEngine.java # Master NLP & ML orchestrator
│       │   ├── KnowledgeBase.java # FAQ storage & TF-IDF indexing
│       │   ├── MathEvaluator.java # AST recursive descent math parser
│       │   └── ResponseGenerator.java # Dynamic response builder
│       ├── gui/
│       │   └── ChatbotGui.java    # Desktop Swing GUI with NLP HUD
│       ├── model/
│       │   ├── BotResponse.java   # Response DTO with NLP diagnostics
│       │   ├── ChatMessage.java   # Chat turn model
│       │   ├── ConversationContext.java # Memory & session state
│       │   ├── FAQEntry.java      # FAQ model
│       │   └── Intent.java        # Intent taxonomy enum
│       ├── nlp/
│       │   ├── CosineSimilarity.java # Vector cosine distance
│       │   ├── EntityExtractor.java  # Name, math, location extraction
│       │   ├── IntentClassifier.java # Naive Bayes ML classifier
│       │   ├── LevenshteinDistance.java # Fuzzy typo distance
│       │   ├── PorterStemmer.java    # English root word stemmer
│       │   ├── SentimentAnalyzer.java # Lexicon sentiment polarity
│       │   ├── TextNormalizer.java   # Contractions & noise cleaner
│       │   └── Tokenizer.java        # Stopwords & N-gram tokenizer
│       └── server/
│           ├── ChatbotHttpServer.java # Lightweight HTTP REST server
│           └── JsonUtil.java          # Pure Java JSON serializer/parser
├── web/
│   ├── index.html                 # Glassmorphic Web App UI
│   ├── style.css                  # Modern CSS Design System
│   └── app.js                     # REST client, Web Audio & NLP HUD
├── build.bat                      # Build script
├── run_web.bat                    # Web runner
├── run_gui.bat                    # GUI runner
├── run_cli.bat                    # CLI runner
└── README.md                      # Documentation
```

---

## 🎓 CodeAlpha Submission Guidelines

1. **GitHub Repository Name**: `CodeAlpha_AIChatbot`
2. **LinkedIn Post**: Share a demo video highlighting:
   - NLP Pipeline (TF-IDF & Naive Bayes)
   - Dynamic FAQ Training with `teach:`
   - Web & GUI interfaces
   - Tag **@CodeAlpha** with your GitHub repo link
3. **Submit**: Fill out the official CodeAlpha Submission Form.
