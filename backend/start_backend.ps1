$ErrorActionPreference = "Stop"

# Use script's parent directory
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
if (-not $ScriptDir) { $ScriptDir = Get-Location }

# Path to venv python
$Path = Join-Path $ScriptDir ".venv\Scripts\python.exe"

# If venv doesn't exist locally, check the hardcoded path from previous version as fallback
$FallbackPath = "C:\Users\Administrator\Desktop\android\backend\.venv\Scripts\python.exe"

if (Test-Path $Path) {
    Write-Host "Using VirtualEnv Python: $Path"
    $PythonExec = $Path
} elseif (Test-Path $FallbackPath) {
    Write-Host "Using Fallback VirtualEnv Python: $FallbackPath"
    $PythonExec = $FallbackPath
} else {
    Write-Host "VirtualEnv Python not found. Using system python."
    $PythonExec = "python"
}

Write-Host "Starting Django server from $ScriptDir..."
Push-Location $ScriptDir
try {
    & $PythonExec manage.py runserver 0.0.0.0:8000
} finally {
    Pop-Location
}
