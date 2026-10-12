param([switch]$PromptForPassword)

$ErrorActionPreference = 'Stop'
$projectDirectory = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $projectDirectory
New-Item -ItemType Directory -Force -Path (Join-Path $projectDirectory 'target') | Out-Null
$statusPath = Join-Path $projectDirectory 'target/database-setup-status.json'

function Write-SetupStatus([string]$state) {
    @{ status = $state; database = 'eventproof' } | ConvertTo-Json | Set-Content -LiteralPath $statusPath -Encoding UTF8
}

try {
    $Host.UI.RawUI.WindowTitle = 'EventProof - MySQL sample database setup'
    Write-Host 'EVENTPROOF - Sample database setup' -ForegroundColor Cyan
    Write-Host 'Creates eventproof and its tables. Sample rows are added only when all tables are empty.'
    Write-Host 'Your password stays in this PowerShell session and is not saved to a file.'
    if (-not $env:DB_URL) { $env:DB_URL = 'jdbc:mysql://localhost:3306/eventproof' }
    if (-not $env:DB_USER) { $env:DB_USER = 'root' }
    Write-Host ('MySQL user: ' + $env:DB_USER)

    if ($PromptForPassword -or $null -eq $env:DB_PASSWORD) {
        Write-SetupStatus 'awaiting-password'
        $securePassword = Read-Host 'Enter your MySQL password' -AsSecureString
        $passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
        try { $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer) }
        finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer); $securePassword.Dispose() }
    }

    Write-SetupStatus 'running'
    & mvn compile exec:java '-Dexec.mainClass=com.eventproof.database.DemoDatabaseSetup' 2>&1 |
        Tee-Object -FilePath (Join-Path $projectDirectory 'target/database-setup.log')
    if ($LASTEXITCODE -ne 0) { throw 'Database setup failed. Check the MySQL username/password and the messages above.' }
    Write-SetupStatus 'ready'
    Write-Host ''
    Write-Host 'Database and sample records verified. You can show the three tables in MySQL Workbench.' -ForegroundColor Green
    Write-Host 'This terminal retains your DB variables. Run mvn javafx:run here when needed.'
} catch {
    Write-SetupStatus 'failed'
    Write-Host $_.Exception.Message -ForegroundColor Red
}
