@echo off
echo Starting Django Server...
start "Django Server" cmd /k "python manage.py runserver"

echo Waiting for server to initialize...
timeout /t 5 /nobreak > nul

echo Starting ngrok on port 8000...
start "ngrok" cmd /k "ngrok http 8000"

echo.
echo Both processes have been started in separate windows.
pause
