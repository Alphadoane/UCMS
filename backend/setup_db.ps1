param(
    [string]$RootUser = "root",
    [Parameter(Mandatory=$true)][string]$RootPassword,
    [string]$Database = "test",
    [string]$MysqlPath = "",
    [string]$SetupFile = "setup.sql"
)

$ErrorActionPreference = "Stop"

function Resolve-MysqlExe {
    param([string]$PathHint)
    if ($PathHint -and (Test-Path $PathHint)) { return $PathHint }
    $candidates = @(
        "$Env:ProgramFiles\MySQL\MySQL Server 8.0\bin\mysql.exe",
        "$Env:ProgramFiles(x86)\MySQL\MySQL Server 8.0\bin\mysql.exe",
        "mysql.exe"
    )
    foreach ($c in $candidates) {
        if (Get-Command $c -ErrorAction SilentlyContinue) { return $c }
        if (Test-Path $c) { return $c }
    }
    throw "mysql.exe not found. Install MySQL or specify -MysqlPath."
}

$mysql = Resolve-MysqlExe -PathHint $MysqlPath

$setupPath = Join-Path $PSScriptRoot $SetupFile
if (-not (Test-Path $setupPath)) {
    throw "Setup file not found: $setupPath"
}

Write-Host "Using mysql at: $mysql"
Write-Host "Applying schema and seed from: $setupPath"

& $mysql -u $RootUser -p$RootPassword < $setupPath
if ($LASTEXITCODE -ne 0) { throw "MySQL setup failed with exit code $LASTEXITCODE" }

Write-Host "Database '$Database' initialized successfully."
