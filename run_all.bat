@echo off
echo ======================================================================
echo 🚀 Launching CodeAlpha AI Chatbot (Web Server + Swing GUI)...
echo ======================================================================

if not exist bin\com\codealpha\chatbot\Main.class (
    call build.bat
)

java -cp bin com.codealpha.chatbot.Main --all
pause
