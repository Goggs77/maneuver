$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing

$projectRoot = Split-Path -Parent $PSScriptRoot
$textureRoot = Join-Path $projectRoot "src/main/resources/assets/maneuver/textures/block"
$assetRoot = Join-Path $projectRoot "src/main/resources/assets/maneuver"
New-Item -ItemType Directory -Force -Path $textureRoot | Out-Null

function Color([int]$r, [int]$g, [int]$b) {
    return [System.Drawing.Color]::FromArgb(255, $r, $g, $b)
}

function New-Texture([string]$name, [scriptblock]$pixel) {
    $bitmap = [System.Drawing.Bitmap]::new(16, 16)
    for ($y = 0; $y -lt 16; $y++) {
        for ($x = 0; $x -lt 16; $x++) {
            $bitmap.SetPixel($x, $y, (& $pixel $x $y))
        }
    }
    $path = Join-Path $textureRoot "$name.png"
    $bitmap.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $bitmap.Dispose()
}

function Desaturate-Texture([string]$sourceName, [string]$targetName) {
    $sourcePath = Join-Path $textureRoot "$sourceName.png"
    $targetPath = Join-Path $textureRoot "$targetName.png"
    $source = [System.Drawing.Bitmap]::new($sourcePath)
    $target = [System.Drawing.Bitmap]::new(16, 16)
    for ($y = 0; $y -lt 16; $y++) {
        for ($x = 0; $x -lt 16; $x++) {
            $c = $source.GetPixel($x, $y)
            $grey = [int](0.24 * $c.R + 0.59 * $c.G + 0.17 * $c.B)
            $dark = [int]($grey * 0.46)
            $target.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(255, $dark, [int]($dark * 0.92), [int]($dark * 1.08)))
        }
    }
    $target.Save($targetPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $target.Dispose()
    $source.Dispose()
}

$purpleDark = Color 25 12 42
$purpleMid = Color 67 37 105
$purpleLight = Color 132 89 207
$amethyst = Color 190 143 255
$glow = Color 238 219 255
$void = Color 9 5 17

New-Texture "gravity_field_top" {
    param($x, $y)
    $dx = $x - 7.5
    $dy = $y - 7.5
    $distance = [Math]::Sqrt($dx * $dx + $dy * $dy)
    if ($x -eq 0 -or $y -eq 0 -or $x -eq 15 -or $y -eq 15) { return $void }
    if ($distance -ge 4.4 -and $distance -le 5.5) { return $amethyst }
    if ($distance -le 1.4) { return $glow }
    if ($distance -le 2.2) { return $purpleLight }
    if (($x -eq 7 -or $x -eq 8) -and ($y -eq 2 -or $y -eq 3 -or $y -eq 12 -or $y -eq 13)) { return $amethyst }
    if (($y -eq 7 -or $y -eq 8) -and ($x -eq 2 -or $x -eq 3 -or $x -eq 12 -or $x -eq 13)) { return $amethyst }
    if ((($x + $y) % 5) -eq 0) { return $purpleLight }
    if ((($x * 3 + $y) % 7) -eq 0) { return $purpleMid }
    return $purpleDark
}

New-Texture "gravity_field_side" {
    param($x, $y)
    if ($x -eq 0 -or $y -eq 0 -or $x -eq 15 -or $y -eq 15) { return $void }
    if ($y -eq 7 -or $y -eq 8) {
        if ($x -ge 3 -and $x -le 12) { return $amethyst }
    }
    if (($x -eq 7 -or $x -eq 8) -and $y -ge 2 -and $y -le 13) { return $purpleLight }
    if ($x -eq 2 -or $x -eq 13) { return $purpleMid }
    if ((($x * 5 + $y * 2) % 9) -eq 0) { return $amethyst }
    if ((($x + $y * 3) % 4) -eq 0) { return $purpleMid }
    return $purpleDark
}

New-Texture "gravity_field_bottom" {
    param($x, $y)
    $dx = $x - 7.5
    $dy = $y - 7.5
    $distance = [Math]::Sqrt($dx * $dx + $dy * $dy)
    if ($x -eq 0 -or $y -eq 0 -or $x -eq 15 -or $y -eq 15) { return $void }
    if ($distance -le 1.2) { return $glow }
    if ($distance -le 3.2) { return $amethyst }
    if ($distance -le 5.8 -and (($x + $y) % 3) -eq 0) { return $purpleLight }
    if ((($x * 2 + $y * 5) % 11) -eq 0) { return $purpleMid }
    return $purpleDark
}

$blueDark = Color 12 20 42
$blueMid = Color 35 59 94
$blueLight = Color 77 125 173
$ice = Color 161 218 255
$wool = Color 218 209 235

New-Texture "damper_top" {
    param($x, $y)
    $dx = $x - 7.5
    $dy = $y - 7.5
    $distance = [Math]::Sqrt($dx * $dx + $dy * $dy)
    if ($x -eq 0 -or $y -eq 0 -or $x -eq 15 -or $y -eq 15) { return $void }
    if ($distance -le 1.4) { return $wool }
    if ($distance -ge 2.5 -and $distance -le 3.4) { return $ice }
    if ($distance -ge 4.8 -and $distance -le 5.7) { return $blueLight }
    if (($x -eq 2 -or $x -eq 13) -and ($y -eq 2 -or $y -eq 13)) { return $amethyst }
    if ((($x + $y) % 4) -eq 0) { return $blueMid }
    return $blueDark
}

New-Texture "damper_side" {
    param($x, $y)
    if ($x -eq 0 -or $y -eq 0 -or $x -eq 15 -or $y -eq 15) { return $void }
    $wave = [Math]::Abs(($y - 7.5) + [Math]::Sin($x * 0.72) * 2.3)
    if ($wave -le 0.7) { return $ice }
    if ($wave -le 1.8) { return $blueLight }
    if ($x -eq 2 -or $x -eq 13) { return $amethyst }
    if ((($x * 3 + $y) % 7) -eq 0) { return $blueMid }
    return $blueDark
}

New-Texture "damper_bottom" {
    param($x, $y)
    $dx = $x - 7.5
    $dy = $y - 7.5
    $distance = [Math]::Sqrt($dx * $dx + $dy * $dy)
    if ($x -eq 0 -or $y -eq 0 -or $x -eq 15 -or $y -eq 15) { return $void }
    if ($distance -le 1.0) { return $amethyst }
    if ($distance -ge 2.2 -and $distance -le 3.0) { return $blueLight }
    if ($distance -ge 4.2 -and $distance -le 5.0) { return $wool }
    if ((($x * 4 + $y) % 9) -eq 0) { return $blueMid }
    return $blueDark
}

Desaturate-Texture "gravity_field_top" "gravity_field_top_off"
Desaturate-Texture "gravity_field_side" "gravity_field_side_off"
Desaturate-Texture "gravity_field_bottom" "gravity_field_bottom_off"
Desaturate-Texture "damper_top" "damper_top_off"
Desaturate-Texture "damper_side" "damper_side_off"
Desaturate-Texture "damper_bottom" "damper_bottom_off"

$top = [System.Drawing.Bitmap]::new((Join-Path $textureRoot "gravity_field_top.png"))
$icon = [System.Drawing.Bitmap]::new(128, 128)
$graphics = [System.Drawing.Graphics]::FromImage($icon)
$graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
$graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
$graphics.Clear($void)
$graphics.DrawImage($top, 8, 8, 112, 112)
for ($i = 0; $i -lt 7; $i++) {
    $sparkX = 12 + (($i * 37) % 104)
    $sparkY = 10 + (($i * 53) % 24)
    $graphics.FillRectangle([System.Drawing.Brushes]::White, $sparkX, $sparkY, 2, 2)
}
$graphics.Dispose()
New-Item -ItemType Directory -Force -Path $assetRoot | Out-Null
$icon.Save((Join-Path $assetRoot "icon.png"), [System.Drawing.Imaging.ImageFormat]::Png)
$icon.Dispose()
$top.Dispose()

Write-Output "Generated amethyst block textures and icon."
