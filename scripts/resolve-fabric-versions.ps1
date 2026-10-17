param(
    [string]$MinecraftVersion = "26.2"
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$propertiesPath = Join-Path $projectRoot "gradle.properties"

Write-Output "Resolving Fabric versions for Minecraft $MinecraftVersion..."
$games = Invoke-RestMethod -Uri "https://meta.fabricmc.net/v2/versions/game" -TimeoutSec 30
if (!($games | Where-Object { $_.version -eq $MinecraftVersion })) {
    throw "Fabric meta does not list Minecraft $MinecraftVersion."
}

$loaders = Invoke-RestMethod -Uri "https://meta.fabricmc.net/v2/versions/loader/$MinecraftVersion" -TimeoutSec 30
$loader = $loaders | Select-Object -First 1
if (!$loader) {
    throw "No Fabric Loader build was returned for $MinecraftVersion."
}

$modrinthQuery = [Uri]::EscapeDataString("[`"$MinecraftVersion`"]")
$apiVersions = Invoke-RestMethod `
    -Uri "https://api.modrinth.com/v2/project/fabric-api/version?game_versions=$modrinthQuery&loaders=%5B%22fabric%22%5D" `
    -Headers @{ "User-Agent" = "maneuver-version-resolver/1.0" } `
    -TimeoutSec 30
$fabricApi = $apiVersions |
    Where-Object { $_.version_number -like "*+$MinecraftVersion" } |
    Select-Object -First 1
if (!$fabricApi) {
    throw "No Fabric API release was returned for $MinecraftVersion."
}

$loomMetadata = [xml](Invoke-WebRequest `
    -UseBasicParsing `
    -Uri "https://maven.fabricmc.net/net/fabricmc/fabric-loom/maven-metadata.xml" `
    -TimeoutSec 30).Content
$loomVersion = $loomMetadata.metadata.versioning.release
if (!$loomVersion) {
    throw "Could not determine the current Fabric Loom release."
}

$content = [System.IO.File]::ReadAllText($propertiesPath)
$content = $content -replace "(?m)^minecraft_version=.*$", "minecraft_version=$MinecraftVersion"
$content = $content -replace "(?m)^loader_version=.*$", "loader_version=$($loader.loader.version)"
$content = $content -replace "(?m)^fabric_version=.*$", "fabric_version=$($fabricApi.version_number)"
$content = $content -replace "(?m)^loom_version=.*$", "loom_version=$loomVersion"
[System.IO.File]::WriteAllText($propertiesPath, $content, [System.Text.UTF8Encoding]::new($false))

Write-Output "Minecraft:  $MinecraftVersion"
Write-Output "Loader:     $($loader.loader.version)"
Write-Output "Fabric API: $($fabricApi.version_number)"
Write-Output "Loom:       $loomVersion"
