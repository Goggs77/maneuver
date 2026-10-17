$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
Push-Location $projectRoot
try {
    $minecraftJava = Join-Path $env:APPDATA ".minecraft/runtime/java-runtime-epsilon"
    if (Test-Path -LiteralPath (Join-Path $minecraftJava "bin/javac.exe")) {
        $env:JAVA_HOME = $minecraftJava
    }
    if (!$env:JAVA_HOME) {
        throw "Set JAVA_HOME to a JDK 25 installation before building."
    }

    & (Join-Path $PSScriptRoot "check.ps1")
    & (Join-Path $PSScriptRoot "manual-build.ps1")
}
finally {
    Pop-Location
}
