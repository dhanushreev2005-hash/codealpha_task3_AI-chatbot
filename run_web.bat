@echo off
echo ======================================================================
echo 🌐 Starting CodeAlpha AI Chatbot Web Server...
echo ======================================================================

if not exist bin\com\codealpha\chatbot\Main.class (
    call build.bat
)

java -cp bin com.codealpha.chatbot.Main --web
pause
