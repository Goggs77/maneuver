$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$env:TEMP = Join-Path $projectRoot ".tmp"
$env:TMP = $env:TEMP
New-Item -ItemType Directory -Force -Path $env:TEMP | Out-Null
$javaHome = if ($env:JAVA_HOME) { $env:JAVA_HOME } else { "E:\Java\jdk21" }
$javac = Join-Path $javaHome "bin/javac.exe"
$java = Join-Path $javaHome "bin/java.exe"

if (!(Test-Path -LiteralPath $javac) -or !(Test-Path -LiteralPath $java)) {
    throw "A JDK 21+ is required. Set JAVA_HOME or install one at E:\Java\jdk21."
}

$buildRoot = Join-Path $env:TEMP ("maneuver-physics-" + [Guid]::NewGuid().ToString("N"))
$resolvedTemp = [System.IO.Path]::GetFullPath($env:TEMP)
$resolvedBuild = [System.IO.Path]::GetFullPath($buildRoot)
if (!$resolvedBuild.StartsWith($resolvedTemp, [StringComparison]::OrdinalIgnoreCase)) {
    throw "Refusing to use a build directory outside the temp root."
}

New-Item -ItemType Directory -Force -Path $resolvedBuild | Out-Null
try {
    $sources = @(
        (Join-Path $projectRoot "src/main/java/com/goggs/maneuver/physics/FieldBounds.java"),
        (Join-Path $projectRoot "src/main/java/com/goggs/maneuver/physics/GravityVector.java"),
        (Join-Path $projectRoot "src/main/java/com/goggs/maneuver/physics/DamperMath.java"),
        (Join-Path $projectRoot "src/main/java/com/goggs/maneuver/physics/CeilingFlightMath.java"),
        (Join-Path $projectRoot "src/main/java/com/goggs/maneuver/physics/FallDamageMath.java"),
        (Join-Path $projectRoot "scripts/physics/PhysicsSelfTest.java")
    )
    & $javac -encoding UTF-8 -d $resolvedBuild $sources
    if ($LASTEXITCODE -ne 0) {
        throw "javac failed with exit code $LASTEXITCODE"
    }
    & $java -cp $resolvedBuild PhysicsSelfTest
    if ($LASTEXITCODE -ne 0) {
        throw "physics test failed with exit code $LASTEXITCODE"
    }
}
finally {
    if (Test-Path -LiteralPath $resolvedBuild) {
        Remove-Item -LiteralPath $resolvedBuild -Recurse -Force
    }
}
