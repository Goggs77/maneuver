$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$mainSources = Get-ChildItem -Path (Join-Path $projectRoot "src/main/java") -Filter "*.java" -Recurse
$clientSources = Get-ChildItem -Path (Join-Path $projectRoot "src/client/java") -Filter "*.java" -Recurse
$errors = [System.Collections.Generic.List[string]]::new()

foreach ($source in $mainSources) {
    $text = [System.IO.File]::ReadAllText($source.FullName, [System.Text.Encoding]::UTF8)
    if ($text -match "net\.minecraft\.client|com\.goggs\.maneuver\.client") {
        $errors.Add("Common source references client-only code: $($source.FullName)")
    }
}

if (!($clientSources | Where-Object Name -eq "ManeuverClient.java")) {
    $errors.Add("ManeuverClient.java is missing from src/client/java.")
}

$modJson = [System.IO.File]::ReadAllText(
    (Join-Path $projectRoot "src/main/resources/fabric.mod.json"),
    [System.Text.Encoding]::UTF8
) | ConvertFrom-Json

if ($modJson.environment -ne "*") {
    $errors.Add("fabric.mod.json must support both client and dedicated-server environments.")
}
if ($modJson.entrypoints.main -notcontains "com.goggs.maneuver.Maneuver") {
    $errors.Add("Common entrypoint is missing.")
}
if ($modJson.entrypoints.client -notcontains "com.goggs.maneuver.ManeuverClient") {
    $errors.Add("Client entrypoint is missing.")
}

if ($errors.Count -gt 0) {
    $errors | ForEach-Object { Write-Error $_ }
    throw "Physical-side validation failed."
}

Write-Output "Physical-side validation passed: $($mainSources.Count) common sources, $($clientSources.Count) client sources."

