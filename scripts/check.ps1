$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot

& (Join-Path $PSScriptRoot "test-physics.ps1")
& (Join-Path $PSScriptRoot "validate-resources.ps1")
& (Join-Path $PSScriptRoot "validate-sides.ps1")
