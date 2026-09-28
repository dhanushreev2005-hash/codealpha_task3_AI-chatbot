@echo off
echo ======================================================================
echo 🤖 Compiling CodeAlpha AI Chatbot (Pure Java SE)...
echo ======================================================================

if not exist bin mkdir bin

javac -d bin src/com/codealpha/chatbot/model/*.java src/com/codealpha/chatbot/nlp/*.java src/com/codealpha/chatbot/engine/*.java src/com/codealpha/chatbot/server/*.java src/com/codealpha/chatbot/gui/*.java src/com/codealpha/chatbot/cli/*.java src/com/codealpha/chatbot/*.java

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Compilation successful! Class files built in bin/
) else (
    echo [ERROR] Compilation failed!
)
