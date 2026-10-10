param(
    [string]$PostgresBin = 'C:\Program Files\PostgreSQL\18\bin',
    [string]$HostName = 'localhost',
    [int]$Port = 5432,
    [string]$AdminUser = 'postgres'
)

$ErrorActionPreference = 'Stop'
$psqlPath = Join-Path $PostgresBin 'psql.exe'
if (-not (Test-Path -LiteralPath $psqlPath)) {
    throw "psql.exe not found: $psqlPath"
}

# psql prompts locally for the administrator password; it is not stored in a file.
& $psqlPath -h $HostName -p $Port -U $AdminUser -d postgres -W -v ON_ERROR_STOP=1 -f (Join-Path $PSScriptRoot 'setup.sql')
if ($LASTEXITCODE -ne 0) {
    throw 'Database setup failed. Check the administrator password and PostgreSQL connection.'
}
Write-Host 'Setup completed: hogwarts / student. Existing objects were left unchanged.'
