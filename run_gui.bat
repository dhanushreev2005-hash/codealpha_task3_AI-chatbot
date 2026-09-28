@echo off
echo ======================================================================
echo 🖥️ Launching CodeAlpha AI Chatbot Swing GUI...
echo ======================================================================

if not exist bin\com\codealpha\chatbot\Main.class (
    call build.bat
)

java -cp bin com.codealpha.chatbot.Main --gui
