/**
 * CodeAlpha AI Chatbot — Frontend Application Logic
 * Integrates REST API communication, real-time NLP diagnostics HUD,
 * Speech Synthesis & Voice Input, Web Audio synthesizers, and FAQ training.
 */

document.addEventListener('DOMContentLoaded', () => {
    // --- State Variables ---
    let conversationHistory = [];
    let isWaitingForResponse = false;
    let soundEnabled = true;
    let speechSynthEnabled = false;
    let recognition = null;
    let isListening = false;

    // --- DOM Elements ---
    const chatMessages = document.getElementById('chat-messages');
    const chatInput = document.getElementById('chat-input');
    const btnSend = document.getElementById('btn-send');
    const btnMic = document.getElementById('btn-mic');
    const quickChips = document.getElementById('quick-chips');

    // Header & HUD Buttons
    const btnToggleHud = document.getElementById('btn-toggle-hud');
    const btnCloseHud = document.getElementById('btn-close-hud');
    const nlpInspector = document.getElementById('nlp-inspector');
    const btnKnowledgeBase = document.getElementById('btn-knowledge-base');
    const btnStats = document.getElementById('btn-stats');
    const btnExport = document.getElementById('btn-export');
    const btnClear = document.getElementById('btn-clear');
    const btnSoundToggle = document.getElementById('btn-sound-toggle');
    const soundIcon = document.getElementById('sound-icon');

    // NLP HUD Elements
    const hudIntentTag = document.getElementById('hud-intent-tag');
    const hudIntentDesc = document.getElementById('hud-intent-desc');
    const hudConfidenceVal = document.getElementById('hud-confidence-val');
    const hudConfidenceBar = document.getElementById('hud-confidence-bar');
    const hudAlgorithmUsed = document.getElementById('hud-algorithm-used');
    const hudSentimentBadge = document.getElementById('hud-sentiment-badge');
    const hudSentimentIndicator = document.getElementById('hud-sentiment-indicator');
    const hudEntitiesList = document.getElementById('hud-entities-list');
    const hudLatency = document.getElementById('hud-latency');
    const hudTurns = document.getElementById('hud-turns');
    const hudContextUser = document.getElementById('hud-context-user');

    // Modals
    const modalKnowledge = document.getElementById('modal-knowledge');
    const modalStats = document.getElementById('modal-stats');
    const formTrain = document.getElementById('form-train');
    const faqContainer = document.getElementById('faq-container');
    const faqCount = document.getElementById('faq-count');
    const faqSearch = document.getElementById('faq-search');
    const toast = document.getElementById('toast');

    // Stats Elements
    const statTotalFaqs = document.getElementById('stat-total-faqs');
    const statVocabSize = document.getElementById('stat-vocab-size');
    const statTurns = document.getElementById('stat-turns');
    const statUptime = document.getElementById('stat-uptime');

    // --- Web Audio API Synth for UI Sounds ---
    const audioCtx = (typeof window.AudioContext !== 'undefined' || typeof window.webkitAudioContext !== 'undefined')
        ? new (window.AudioContext || window.webkitAudioContext)() : null;

    function playChime(type) {
        if (!soundEnabled || !audioCtx) return;
        try {
            if (audioCtx.state === 'suspended') {
                audioCtx.resume();
            }
            const osc = audioCtx.createOscillator();
            const gain = audioCtx.createGain();
            osc.connect(gain);
            gain.connect(audioCtx.destination);

            const now = audioCtx.currentTime;

            if (type === 'send') {
                osc.type = 'sine';
                osc.frequency.setValueAtTime(440, now);
                osc.frequency.exponentialRampToValueAtTime(880, now + 0.12);
                gain.gain.setValueAtTime(0.06, now);
                gain.gain.exponentialRampToValueAtTime(0.001, now + 0.12);
                osc.start(now);
                osc.stop(now + 0.12);
            } else if (type === 'receive') {
                osc.type = 'triangle';
                osc.frequency.setValueAtTime(587.33, now);
                osc.frequency.setValueAtTime(880, now + 0.08);
                gain.gain.setValueAtTime(0.07, now);
                gain.gain.exponentialRampToValueAtTime(0.001, now + 0.2);
                osc.start(now);
                osc.stop(now + 0.2);
            }
        } catch (e) {
            // Audio context not allowed before interaction
        }
    }

    // --- Voice Recognition (Speech-to-Text) ---
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (SpeechRecognition) {
        recognition = new SpeechRecognition();
        recognition.continuous = false;
        recognition.interimResults = false;
        recognition.lang = 'en-US';

        recognition.onstart = () => {
            isListening = true;
            btnMic.classList.add('listening');
            showToast('🎙️ Listening... Speak now');
        };

        recognition.onresult = (e) => {
            const transcript = e.results[0][0].transcript;
            chatInput.value = transcript;
            btnMic.classList.remove('listening');
            isListening = false;
            sendMessage();
        };

        recognition.onerror = (e) => {
            btnMic.classList.remove('listening');
            isListening = false;
            showToast('Voice recognition error: ' + e.error);
        };

        recognition.onend = () => {
            btnMic.classList.remove('listening');
            isListening = false;
        };

        btnMic.addEventListener('click', () => {
            if (!isListening) {
                try {
                    recognition.start();
                } catch (err) {
                    recognition.stop();
                }
            } else {
                recognition.stop();
            }
        });
    } else {
        btnMic.style.display = 'none';
    }

    // --- Text to Speech (Voice Output) ---
    function speakText(text) {
        if (!('speechSynthesis' in window)) return;
        window.speechSynthesis.cancel();
        // Clean markdown symbols for natural reading
        const cleanText = text.replace(/[*_`#\-\+]/g, '').replace(/\[(.*?)\]\(.*?\)/g, '$1');
        const utterance = new SpeechSynthesisUtterance(cleanText);
        utterance.rate = 1.0;
        utterance.pitch = 1.0;
        window.speechSynthesis.speak(utterance);
    }

    // --- Initial Welcome Message ---
    const welcomeText = `👋 **Welcome to CodeAlpha AI Assistant!**\n\n` +
        `I am an intelligent conversational bot built with a Java NLP pipeline, TF-IDF vectorizer, and Naive Bayes machine learning.\n\n` +
        `Here is what you can ask me:\n` +
        `• **CodeAlpha Internship**: Tasks (1-4), submission steps, criteria, perks.\n` +
        `• **Java & OOP**: Polymorphism, Inheritance, Encapsulation, JVM, GC, Collections, Streams.\n` +
        `• **AI & NLP**: Tokenization, Stemming, TF-IDF, Naive Bayes.\n` +
        `• **Math & Utilities**: \`calculate 25 * 4 + 10\`, \`what time is it\`, \`weather in London\`.\n` +
        `• **Teach Bot**: Use \`teach: Question -> Answer\` to teach me something new!`;

    renderBotMessage(welcomeText, {
        intent: 'Greeting',
        confidence: 1.0,
        algorithm: 'System_Init',
        sentiment: { label: 'Neutral', emoji: '😐', score: 0.0, color: '#64748b' },
        entities: ['System_Init'],
        responseTimeMs: 1
    }, false);

    renderChips(['CodeAlpha Tasks', 'Explain OOP in Java', 'What is TF-IDF?', 'Tell me a joke']);

    // --- Send Message Flow ---
    async function sendMessage() {
        const text = chatInput.value.trim();
        if (!text || isWaitingForResponse) return;

        // Auto-resize input
        chatInput.value = '';
        chatInput.style.height = 'auto';

        // Render User Bubble
        renderUserMessage(text);
        playChime('send');

        isWaitingForResponse = true;
        const typingEl = showTypingIndicator();

        try {
            const response = await fetch('/api/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ message: text })
            });

            if (!response.ok) {
                throw new Error(`Server returned HTTP ${response.status}`);
            }

            const data = await response.json();
            removeTypingIndicator(typingEl);

            renderBotMessage(data.text, data, true);
            updateNlpHud(data);
            if (data.suggestedChips && data.suggestedChips.length > 0) {
                renderChips(data.suggestedChips);
            }
            playChime('receive');
        } catch (error) {
            removeTypingIndicator(typingEl);
            renderBotMessage(`⚠️ **Error communicating with Java backend:** ${error.message}\nMake sure the server is running on http://localhost:8080`, {
                intent: 'Error',
                confidence: 0.0,
                algorithm: 'ErrorHandler',
                sentiment: null,
                entities: [],
                responseTimeMs: 0
            }, false);
        } finally {
            isWaitingForResponse = false;
        }
    }

    // --- Rendering Functions ---

    function renderUserMessage(text) {
        const wrapper = document.createElement('div');
        wrapper.className = 'msg-wrapper user';

        const timeStr = getCurrentTimeFormatted();

        wrapper.innerHTML = `
            <div class="msg-avatar">U</div>
            <div class="msg-bubble-container">
                <div class="msg-bubble">${escapeHtml(text)}</div>
                <div class="msg-meta">
                    <span class="msg-time">${timeStr}</span>
                </div>
            </div>
        `;

        chatMessages.appendChild(wrapper);
        scrollToBottom();
        conversationHistory.push({ sender: 'USER', text, time: timeStr });
    }

    function renderBotMessage(text, meta, animate = false) {
        const wrapper = document.createElement('div');
        wrapper.className = 'msg-wrapper bot';

        const timeStr = getCurrentTimeFormatted();
        const intentName = meta.intent || 'Unknown';
        const confidencePct = Math.round((meta.confidence || 0) * 100);
        const algoName = meta.algorithm || 'RuleMatcher';

        const formattedHtml = parseMarkdown(text);

        wrapper.innerHTML = `
            <div class="msg-avatar">🤖</div>
            <div class="msg-bubble-container">
                <div class="msg-bubble">${formattedHtml}</div>
                <div class="msg-meta">
                    <span class="msg-nlp-badge">⚡ ${intentName} (${confidencePct}%) • ${algoName}</span>
                    <span class="msg-time">${timeStr}</span>
                    <button class="msg-speak-btn" title="Read aloud">🔊</button>
                </div>
            </div>
        `;

        // Attach TTS handler
        const speakBtn = wrapper.querySelector('.msg-speak-btn');
        speakBtn.addEventListener('click', () => speakText(text));

        chatMessages.appendChild(wrapper);
        scrollToBottom();
        conversationHistory.push({ sender: 'BOT', text, meta, time: timeStr });
    }

    function showTypingIndicator() {
        const wrapper = document.createElement('div');
        wrapper.className = 'msg-wrapper bot typing-wrapper';
        wrapper.innerHTML = `
            <div class="msg-avatar">🤖</div>
            <div class="msg-bubble typing-bubble">
                <span class="typing-dot"></span>
                <span class="typing-dot"></span>
                <span class="typing-dot"></span>
            </div>
        `;
        chatMessages.appendChild(wrapper);
        scrollToBottom();
        return wrapper;
    }

    function removeTypingIndicator(el) {
        if (el && el.parentNode) {
            el.parentNode.removeChild(el);
        }
    }

    function renderChips(chips) {
        quickChips.innerHTML = '';
        if (!chips || chips.length === 0) return;

        chips.forEach(chip => {
            const btn = document.createElement('button');
            btn.className = 'chip-btn';
            btn.textContent = chip;
            btn.addEventListener('click', () => {
                chatInput.value = chip;
                sendMessage();
            });
            quickChips.appendChild(btn);
        });
    }

    function updateNlpHud(data) {
        if (!data) return;

        // Intent
        hudIntentTag.textContent = data.intent || 'Unknown';
        hudIntentDesc.textContent = getIntentDescription(data.intent);

        // Confidence
        const confPct = Math.round((data.confidence || 0) * 100);
        hudConfidenceVal.textContent = confPct + '%';
        hudConfidenceBar.style.width = Math.min(100, Math.max(0, confPct)) + '%';
        hudAlgorithmUsed.textContent = data.algorithm || 'ML_NaiveBayes';

        // Sentiment
        if (data.sentiment) {
            hudSentimentBadge.textContent = `${data.sentiment.emoji} ${data.sentiment.label}`;
            // Convert score (-1 to +1) to percentage (0% to 100%)
            const posPct = ((data.sentiment.score + 1.0) / 2.0) * 100;
            hudSentimentIndicator.style.left = `${Math.max(5, Math.min(95, posPct))}%`;
        }

        // Entities
        hudEntitiesList.innerHTML = '';
        if (data.entities && data.entities.length > 0) {
            data.entities.forEach(ent => {
                const span = document.createElement('span');
                span.className = 'entity-pill';
                span.textContent = ent;
                hudEntitiesList.appendChild(span);
            });
        } else {
            hudEntitiesList.innerHTML = '<span class="entity-pill">None detected</span>';
        }

        // Mini Stats
        hudLatency.textContent = (data.responseTimeMs || 1) + ' ms';
        hudTurns.textContent = data.turnCount || 1;
        hudContextUser.textContent = data.userName || 'Guest';
    }

    function getIntentDescription(intent) {
        const map = {
            'Greeting': 'General greetings & welcome pleasantries',
            'Farewell': 'Session exit & goodbye phrases',
            'Thanks': 'Expressions of gratitude & appreciation',
            'Bot Identity': 'Chatbot origins, system specs, & stack',
            'Bot Capabilities': 'Features, utilities, & supported actions',
            'Help': 'Usage manual & command guidance',
            'Time and Date': 'System clock & current calendar date',
            'Math Calculation': 'Arithmetic & scientific AST evaluation',
            'Weather': 'Real-time simulated weather forecasting',
            'CodeAlpha Internship': 'Internship rules, tasks, & submission perks',
            'Java Programming': 'Java SE concepts, OOP, memory, concurrency',
            'AI & NLP': 'Tokenization, TF-IDF, Naive Bayes ML',
            'Tech Joke': 'Programming & computer humor',
            'Motivational Quote': 'Inspirational dev quotes',
            'Teach Bot': 'Dynamic Q&A knowledge learning',
            'FAQ Query': 'Matched knowledge base question',
            'Unknown / Fallback': 'Low confidence query fallback'
        };
        return map[intent] || 'Natural Language Query Processing';
    }

    // --- Markdown Parser Helper ---
    function parseMarkdown(text) {
        if (!text) return '';
        let escaped = escapeHtml(text);

        // Code blocks: ```java ... ```
        escaped = escaped.replace(/```([a-zA-Z]*)\n([\s\S]*?)```/g, (match, lang, code) => {
            return `<pre><code>${code.trim()}</code></pre>`;
        });

        // Inline code: `code`
        escaped = escaped.replace(/`([^`]+)`/g, '<code>$1</code>');

        // Bold: **text**
        escaped = escaped.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');

        // Italic: *text*
        escaped = escaped.replace(/\*([^*]+)\*/g, '<em>$1</em>');

        // Bullet lists
        const lines = escaped.split('\n');
        let inList = false;
        let result = [];

        for (let line of lines) {
            if (line.trim().startsWith('• ') || line.trim().startsWith('- ') || line.trim().startsWith('* ')) {
                if (!inList) {
                    result.push('<ul>');
                    inList = true;
                }
                const item = line.trim().substring(2);
                result.push(`<li>${item}</li>`);
            } else {
                if (inList) {
                    result.push('</ul>');
                    inList = false;
                }
                if (line.trim().length > 0) {
                    result.push(`<p>${line}</p>`);
                }
            }
        }
        if (inList) result.push('</ul>');

        return result.join('');
    }

    function escapeHtml(str) {
        return str
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

    function getCurrentTimeFormatted() {
        const now = new Date();
        return now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }

    function scrollToBottom() {
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    function showToast(msg) {
        toast.textContent = msg;
        toast.classList.add('show');
        setTimeout(() => toast.classList.remove('show'), 3000);
    }

    // --- Knowledge Base & FAQ Modal Management ---
    async function loadFaqs() {
        try {
            const resp = await fetch('/api/faq');
            const data = await resp.json();
            faqCount.textContent = data.length;
            renderFaqAccordion(data);
        } catch (e) {
            faqContainer.innerHTML = `<p style="color:var(--accent-rose);">Failed to load FAQs: ${e.message}</p>`;
        }
    }

    function renderFaqAccordion(list) {
        faqContainer.innerHTML = '';
        if (!list || list.length === 0) {
            faqContainer.innerHTML = '<p style="color:var(--text-dim); text-align:center; padding:12px;">No FAQs found.</p>';
            return;
        }

        list.forEach(item => {
            const el = document.createElement('div');
            el.className = 'faq-item';
            el.innerHTML = `
                <div class="faq-item-header">
                    <span class="faq-q-text">${escapeHtml(item.question)}</span>
                    <span class="faq-cat-badge">${escapeHtml(item.category)}</span>
                </div>
                <div class="faq-item-body">${escapeHtml(item.answer)}</div>
            `;

            el.querySelector('.faq-item-header').addEventListener('click', () => {
                el.classList.toggle('open');
            });

            faqContainer.appendChild(el);
        });
    }

    faqSearch.addEventListener('input', (e) => {
        const query = e.target.value.toLowerCase();
        const items = faqContainer.querySelectorAll('.faq-item');
        items.forEach(item => {
            const q = item.querySelector('.faq-q-text').textContent.toLowerCase();
            const a = item.querySelector('.faq-item-body').textContent.toLowerCase();
            if (q.includes(query) || a.includes(query)) {
                item.style.display = 'block';
            } else {
                item.style.display = 'none';
            }
        });
    });

    formTrain.addEventListener('submit', async (e) => {
        e.preventDefault();
        const question = document.getElementById('train-question').value.trim();
        const answer = document.getElementById('train-answer').value.trim();
        const category = document.getElementById('train-category').value;

        if (!question || !answer) return;

        const btn = document.getElementById('btn-submit-train');
        btn.disabled = true;
        btn.innerHTML = '<span>⏳ Training Engine...</span>';

        try {
            const resp = await fetch('/api/train', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ question, answer, category })
            });

            const resData = await resp.json();
            if (resData.success) {
                showToast('🎉 FAQ Learned & Model Retrained!');
                formTrain.reset();
                loadFaqs();
            } else {
                showToast('⚠️ Training failed: ' + resData.error);
            }
        } catch (err) {
            showToast('⚠️ Error: ' + err.message);
        } finally {
            btn.disabled = false;
            btn.innerHTML = '<span>⚡ Save & Retrain NLP Engine</span>';
        }
    });

    // --- Analytics Stats Modal ---
    async function loadStats() {
        try {
            const resp = await fetch('/api/stats');
            const data = await resp.json();
            statTotalFaqs.textContent = data.totalFaqs;
            statVocabSize.textContent = data.vocabSize;
            statTurns.textContent = data.turnCount;
            statUptime.textContent = `${data.uptimeSec}s`;
        } catch (e) {
            console.error('Failed to load stats:', e);
        }
    }

    // --- Export Conversation ---
    btnExport.addEventListener('click', () => {
        if (conversationHistory.length === 0) {
            showToast('No messages to export yet!');
            return;
        }

        let content = `# CodeAlpha AI Chatbot Transcript\nExport Date: ${new Date().toLocaleString()}\n\n---\n\n`;
        conversationHistory.forEach(msg => {
            content += `### [${msg.time}] ${msg.sender}\n${msg.text}\n\n`;
        });

        const blob = new Blob([content], { type: 'text/markdown;charset=utf-8' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `CodeAlpha_Chat_${Date.now()}.md`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
        showToast('💾 Chat transcript exported!');
    });

    // --- Clear Conversation ---
    btnClear.addEventListener('click', async () => {
        if (!confirm('Clear current conversation history?')) return;
        chatMessages.innerHTML = '';
        conversationHistory = [];
        try {
            await fetch('/api/clear', { method: 'POST' });
        } catch (e) {}

        renderBotMessage('Conversation reset! How can I help you next?', {
            intent: 'Greeting',
            confidence: 1.0,
            algorithm: 'System_Reset',
            sentiment: null,
            entities: [],
            responseTimeMs: 1
        }, false);
        showToast('✨ Conversation cleared');
    });

    // --- Sound Toggle ---
    btnSoundToggle.addEventListener('click', () => {
        soundEnabled = !soundEnabled;
        soundIcon.textContent = soundEnabled ? '🔊' : '🔇';
        showToast(soundEnabled ? '🔊 Audio effects enabled' : '🔇 Audio muted');
    });

    // --- NLP HUD Toggle ---
    btnToggleHud.addEventListener('click', () => {
        nlpInspector.classList.toggle('closed');
    });

    btnCloseHud.addEventListener('click', () => {
        nlpInspector.classList.add('closed');
    });

    // --- Modal Triggers ---
    btnKnowledgeBase.addEventListener('click', () => {
        modalKnowledge.classList.add('open');
        loadFaqs();
    });

    btnStats.addEventListener('click', () => {
        modalStats.classList.add('open');
        loadStats();
    });

    document.querySelectorAll('.modal-close').forEach(btn => {
        btn.addEventListener('click', () => {
            const modalId = btn.getAttribute('data-modal');
            document.getElementById(modalId).classList.remove('open');
        });
    });

    window.addEventListener('click', (e) => {
        if (e.target.classList.contains('modal-backdrop')) {
            e.target.classList.remove('open');
        }
    });

    // --- Event Listeners for Input ---
    btnSend.addEventListener('click', sendMessage);

    chatInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });

    // Auto-grow textarea
    chatInput.addEventListener('input', () => {
        chatInput.style.height = 'auto';
        chatInput.style.height = Math.min(chatInput.scrollHeight, 120) + 'px';
    });
});
