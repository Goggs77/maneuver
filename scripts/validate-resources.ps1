$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing

$projectRoot = Split-Path -Parent $PSScriptRoot
$resources = Join-Path $projectRoot "src/main/resources"
$assetRoot = Join-Path $resources "assets/maneuver"
$errors = [System.Collections.Generic.List[string]]::new()

function Read-Json([string]$path) {
    $text = [System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8)
    return $text | ConvertFrom-Json
}

$jsonFiles = Get-ChildItem -Path $resources -Filter "*.json" -Recurse
foreach ($file in $jsonFiles) {
    try {
        Read-Json $file.FullName | Out-Null
    }
    catch {
        $errors.Add("Invalid JSON: $($file.FullName): $($_.Exception.Message)")
    }
}

$screenObjects = Get-ChildItem -Path (Join-Path $resources "data/maneuver") -Filter "*.json" -Recurse
foreach ($file in $screenObjects) {
    $json = Read-Json $file.FullName
    if ($null -ne $json.'%s') {
        $errors.Add("Unresolved translation placeholder in data: $($file.FullName)")
    }
}

$blockstateFiles = Get-ChildItem -Path (Join-Path $assetRoot "blockstates") -Filter "*.json"
foreach ($file in $blockstateFiles) {
    $blockstate = Read-Json $file.FullName
    foreach ($variant in $blockstate.variants.PSObject.Properties) {
        $modelPath = $variant.Value.model
        if (!$modelPath -or !$modelPath.StartsWith("maneuver:")) {
            $errors.Add("Blockstate $($file.Name) has a non-mod model: $modelPath")
            continue
        }
        $relative = $modelPath.Substring("maneuver:".Length) + ".json"
        $modelFile = Join-Path (Join-Path $assetRoot "models") $relative
        if (!(Test-Path -LiteralPath $modelFile)) {
            $errors.Add("Missing model $modelPath referenced by $($file.Name)")
            continue
        }
        $model = Read-Json $modelFile
        foreach ($texture in $model.textures.PSObject.Properties) {
            $texturePath = $texture.Value
            if ($texturePath.StartsWith("maneuver:")) {
                $textureRelative = $texturePath.Substring("maneuver:".Length) + ".png"
                $textureFile = Join-Path (Join-Path $assetRoot "textures") $textureRelative
                if (!(Test-Path -LiteralPath $textureFile)) {
                    $errors.Add("Missing texture $texturePath referenced by $modelPath")
                }
            }
        }
    }
}

$itemFiles = Get-ChildItem -Path (Join-Path $assetRoot "items") -Filter "*.json"
foreach ($file in $itemFiles) {
    $item = Read-Json $file.FullName
    $modelPath = $item.model.model
    if (!$modelPath -or !$modelPath.StartsWith("maneuver:")) {
        $errors.Add("Item model $($file.Name) has a non-mod model: $modelPath")
        continue
    }
    $relative = $modelPath.Substring("maneuver:".Length) + ".json"
    $modelFile = Join-Path (Join-Path $assetRoot "models") $relative
    if (!(Test-Path -LiteralPath $modelFile)) {
        $errors.Add("Missing item model $modelPath referenced by $($file.Name)")
    }
}

$recipeFiles = Get-ChildItem -Path (Join-Path $resources "data/maneuver/recipe") -Filter "*.json"
foreach ($file in $recipeFiles) {
    $recipe = Read-Json $file.FullName
    if (!$recipe.result.id.StartsWith("maneuver:")) {
        $errors.Add("Recipe $($file.Name) has an unexpected result: $($recipe.result.id)")
        continue
    }
    $itemId = $recipe.result.id.Substring("maneuver:".Length)
    if (!(Test-Path -LiteralPath (Join-Path (Join-Path $assetRoot "items") "$itemId.json"))) {
        $errors.Add("Recipe $($file.Name) produces an item without a model definition: $itemId")
    }
}

$blockTextures = Get-ChildItem -Path (Join-Path $assetRoot "textures/block") -Filter "*.png"
foreach ($texture in $blockTextures) {
    $bitmap = [System.Drawing.Bitmap]::new($texture.FullName)
    if ($bitmap.Width -ne 16 -or $bitmap.Height -ne 16) {
        $errors.Add("Block texture $($texture.Name) is not 16x16.")
    }
    $bitmap.Dispose()
}

$iconPath = Join-Path $assetRoot "icon.png"
if (!(Test-Path -LiteralPath $iconPath)) {
    $errors.Add("Missing mod icon.")
}
else {
    $icon = [System.Drawing.Bitmap]::new($iconPath)
    if ($icon.Width -ne 128 -or $icon.Height -ne 128) {
        $errors.Add("Mod icon is not 128x128.")
    }
    $icon.Dispose()
}

if ($errors.Count -gt 0) {
    $errors | ForEach-Object { Write-Error $_ }
    throw "Resource validation failed with $($errors.Count) error(s)."
}

Write-Output "Resource validation passed: $($jsonFiles.Count) JSON files, $($blockTextures.Count) block textures, $($recipeFiles.Count) recipes."

