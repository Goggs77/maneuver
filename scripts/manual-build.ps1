param(
    [string]$MinecraftJar = "I:\Goggs\bin\Executables\.minecraft\versions\26.2-Fabric 0.19.3\26.2-Fabric 0.19.3.jar",
    [string]$FabricApiJar = "I:\Goggs\bin\Executables\.minecraft\versions\26.2-Fabric 0.19.3\mods\fabric-api-0.158.0+26.2.jar"
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$env:TEMP = Join-Path $projectRoot ".tmp"
$env:TMP = $env:TEMP
New-Item -ItemType Directory -Force -Path $env:TEMP | Out-Null
$javaHome = "C:\Users\Adminliu\AppData\Roaming\.minecraft\runtime\java-runtime-epsilon"
$javac = Join-Path $javaHome "bin/javac.exe"
$jarTool = Join-Path $javaHome "bin/jar.exe"

if (!(Test-Path -LiteralPath $MinecraftJar)) {
    throw "Minecraft 26.2 jar not found: $MinecraftJar"
}
if (!(Test-Path -LiteralPath $FabricApiJar)) {
    throw "Fabric API 26.2 jar not found: $FabricApiJar"
}

$buildRoot = Join-Path $env:TEMP ("maneuver-manual-build-" + [Guid]::NewGuid().ToString("N"))
$tempRoot = [System.IO.Path]::GetFullPath($env:TEMP)
$resolvedBuild = [System.IO.Path]::GetFullPath($buildRoot)
if (!$resolvedBuild.StartsWith($tempRoot, [StringComparison]::OrdinalIgnoreCase)) {
    throw "Refusing to build outside the temp directory."
}

$classes = Join-Path $resolvedBuild "classes"
$stubs = Join-Path $resolvedBuild "stubs"
$apiExtract = Join-Path $resolvedBuild "fabric-api"
$modVersion = (Get-Content -LiteralPath (Join-Path $projectRoot "gradle.properties") |
    Where-Object { $_ -match "^mod_version=" } |
    Select-Object -First 1).Split("=")[1].Trim()
New-Item -ItemType Directory -Force -Path $classes, $stubs, $apiExtract | Out-Null

Push-Location $apiExtract
try {
    & $jarTool xf $FabricApiJar
}
finally {
    Pop-Location
}

$libraryJars = Get-ChildItem -Path "I:\Goggs\bin\Executables\.minecraft\libraries" -Filter "*.jar" -Recurse |
    ForEach-Object FullName
$fabricModules = Get-ChildItem -Path (Join-Path $apiExtract "META-INF/jars") -Filter "*.jar" |
    ForEach-Object FullName
$baseClassPath = (@($MinecraftJar) + $libraryJars + $fabricModules) -join ";"

$stubSources = Get-ChildItem -Path (Join-Path $PSScriptRoot "compile-stub") -Filter "*.java" -Recurse |
    ForEach-Object FullName
& $javac -encoding UTF-8 -cp $baseClassPath -d $stubs $stubSources
if ($LASTEXITCODE -ne 0) {
    throw "Compile-only menu facade failed."
}

$classPath = (@($stubs, $MinecraftJar) + $libraryJars + $fabricModules) -join ";"
$sources = @(
    Get-ChildItem -Path (Join-Path $projectRoot "src/main/java") -Filter "*.java" -Recurse |
        ForEach-Object FullName
    Get-ChildItem -Path (Join-Path $projectRoot "src/client/java") -Filter "*.java" -Recurse |
        ForEach-Object FullName
)

& $javac -encoding UTF-8 -cp $classPath -d $classes $sources
if ($LASTEXITCODE -ne 0) {
    throw "Java compilation failed."
}

Copy-Item -Path (Join-Path $projectRoot "src/main/resources/*") -Destination $classes -Recurse -Force
$modJsonPath = Join-Path $classes "fabric.mod.json"
$modJson = [System.IO.File]::ReadAllText($modJsonPath)
$modJson = $modJson.Replace('${version}', $modVersion)
[System.IO.File]::WriteAllText($modJsonPath, $modJson, [System.Text.UTF8Encoding]::new($false))

$outputDirectory = Join-Path $projectRoot "build/libs"
New-Item -ItemType Directory -Force -Path $outputDirectory | Out-Null
$outputJar = Join-Path $outputDirectory "maneuver-$modVersion.jar"
& $jarTool --create --file $outputJar -C $classes .
if ($LASTEXITCODE -ne 0) {
    throw "Jar packaging failed."
}

Get-Item -LiteralPath $outputJar | Select-Object FullName, Length, LastWriteTime
[System.IO.Directory]::Delete($resolvedBuild, $true)
