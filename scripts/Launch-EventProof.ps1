param([switch]$Verify, [switch]$PromptForPassword)

$ErrorActionPreference = 'Stop'
$projectDirectory = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $projectDirectory
New-Item -ItemType Directory -Force -Path (Join-Path $projectDirectory 'target') | Out-Null
$statusPath = Join-Path $projectDirectory 'target/launch-status.json'
function Write-LaunchStatus([string]$state) {
    @{ status = $state } | ConvertTo-Json | Set-Content -LiteralPath $statusPath -Encoding UTF8
}
function Invoke-Maven([string[]]$MavenArguments, [string]$LogName) {
    $previousPreference = $ErrorActionPreference
    try {
        # Windows PowerShell 5 treats native stderr as an error record. Maven and
        # Java write harmless diagnostics there; use the process exit code.
        $ErrorActionPreference = 'Continue'
        & mvn @MavenArguments 2>&1 | ForEach-Object { $_.ToString() } |
            Tee-Object -FilePath (Join-Path $projectDirectory ('target/' + $LogName))
        $mavenExitCode = $LASTEXITCODE
    } finally { $ErrorActionPreference = $previousPreference }
    if ($mavenExitCode -ne 0) { throw ('Maven failed with exit code ' + $mavenExitCode + '. Review ' + $LogName + '.') }
}
try {
    $Host.UI.RawUI.WindowTitle = 'EventProof - Database verification and launch'
    Write-Host 'EVENTPROOF - Stage 3' -ForegroundColor Cyan
    if (-not $env:DB_URL) { $env:DB_URL = 'jdbc:mysql://localhost:3306/eventproof' }
    if (-not $env:DB_USER) { $env:DB_USER = 'root' }
    Write-Host ('MySQL user: ' + $env:DB_USER)
    Write-Host 'Your password stays in this session and is never saved to a file.'
    if ($PromptForPassword -or $null -eq $env:DB_PASSWORD) {
        Write-LaunchStatus 'awaiting-password'
        $securePassword = Read-Host 'Enter your MySQL password' -AsSecureString
        $passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
        try { $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer) }
        finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer); $securePassword.Dispose() }
    }
    if ($Verify) {
        Write-LaunchStatus 'verifying'
        $previousFlag = $env:EVENTPROOF_DB_TESTS
        try {
            $env:EVENTPROOF_DB_TESTS = 'true'
            Invoke-Maven -MavenArguments @('-B', 'test') -LogName 'stage3-verification.log'
        } finally { $env:EVENTPROOF_DB_TESTS = $previousFlag }
    }
    Write-LaunchStatus 'launching'
    Invoke-Maven -MavenArguments @('-B', 'compile', 'javafx:run') -LogName 'application.log'
    Write-LaunchStatus 'closed'
} catch {
    Write-LaunchStatus 'failed'
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}
