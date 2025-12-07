# Build script for Feedback Management Backend

# Find JDK 17
$jdk17Paths = @(
    "C:\Program Files\Microsoft\jdk-17.0.10.7-hotspot",
    "C:\Program Files\Eclipse Adoptium\jdk-17*",
    "C:\Program Files\Java\jdk-17*",
    "$env:LOCALAPPDATA\Programs\Eclipse Adoptium\jdk-17*"
)

$jdk17Home = $null
foreach ($path in $jdk17Paths) {
    $found = Get-ChildItem $path -ErrorAction SilentlyContinue | Where-Object { $_.PSIsContainer } | Select-Object -First 1
    if ($found -and (Test-Path "$($found.FullName)\bin\java.exe")) {
        $jdk17Home = $found.FullName
        break
    }
}

if (-not $jdk17Home) {
    Write-Host "JDK 17 not found. Please install JDK 17+ first." -ForegroundColor Red
    Write-Host "Run: winget install Microsoft.OpenJDK.17" -ForegroundColor Yellow
    exit 1
}

Write-Host "Found JDK 17 at: $jdk17Home" -ForegroundColor Green

# Set JAVA_HOME
$env:JAVA_HOME = $jdk17Home
$env:Path = "$jdk17Home\bin;$env:Path"

# Verify Java version
Write-Host "`nJava version:" -ForegroundColor Cyan
java -version

# Run Maven build
Write-Host "`nBuilding project..." -ForegroundColor Cyan
.\mvnw.cmd clean install

if ($LASTEXITCODE -eq 0) {
    Write-Host "`nBuild successful!" -ForegroundColor Green
} else {
    Write-Host "`nBuild failed!" -ForegroundColor Red
    exit 1
}

