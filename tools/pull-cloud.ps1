param([switch]$CheckOnly)
$ErrorActionPreference = 'Stop'
$repoPath = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$expectedRemote = 'https://github.com/15841307074/sun.git'

function Invoke-CloudGit {
    param([string[]]$GitArgs)
    $result = & git -c "safe.directory=$repoPath" -C $repoPath @GitArgs
    if ($LASTEXITCODE -ne 0) { throw "Git command failed: $($GitArgs[0])" }
    return $result
}

$branch = Invoke-CloudGit -GitArgs @('branch', '--show-current')
if ($branch -ne 'main') { throw 'Cloud checkout must be on main before pulling.' }
$remote = Invoke-CloudGit -GitArgs @('remote', 'get-url', 'origin')
if ($remote -ne $expectedRemote) { throw 'Cloud checkout remote does not match the configured repository.' }
$status = @(Invoke-CloudGit -GitArgs @('status', '--porcelain=v1', '--untracked-files=all'))
if ($status.Count -ne 0) { throw 'Cloud checkout has uncommitted files. Commit or back them up before pulling.' }
if ($CheckOnly) {
    Write-Output 'Cloud checkout is clean, on main, and connected to the expected remote.'
    exit 0
}
Invoke-CloudGit -GitArgs @('pull', '--ff-only', 'origin', 'main')
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
