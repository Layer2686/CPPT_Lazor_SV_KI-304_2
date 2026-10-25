param(
    [Parameter(Mandatory = $true)]
    [string] $BuildNumber
)

$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
$reports = @{}
$consoles = @{}

foreach ($name in @('lab01', 'lab02', 'lab03')) {
    $module = $name.ToUpperInvariant().Replace('LAB', 'LAB_')
    $jarPath = (Resolve-Path -LiteralPath "$module/target/$name.jar").Path
    $inputPath = (Resolve-Path -LiteralPath "$module/data/input.csv").Path

    # Версія читається з POM; LAB_01 успадковує її від parent.
    [xml] $pom = Get-Content -LiteralPath "$module/pom.xml" -Raw
    $version = [string] $pom.project.version
    if ([string]::IsNullOrWhiteSpace($version)) {
        $version = [string] $pom.project.parent.version
    }
    if ([string]::IsNullOrWhiteSpace($version)) {
        throw "Maven version is missing for $module"
    }
    $expectedVersion = "$name $version (build $BuildNumber)"
    $actualVersion = @(& java -jar $jarPath --version)
    if ($LASTEXITCODE -ne 0 -or $actualVersion.Count -ne 1 -or $actualVersion[0] -cne $expectedVersion) {
        throw "Version check failed for $name; expected '$expectedVersion'"
    }
    Write-Output "Version check passed: $expectedVersion"

    if ($name -eq 'lab03' -and $env:GITHUB_REF_TYPE -eq 'tag') {
        if ($env:GITHUB_REF_NAME -cne "v$version") {
            throw "Tag '$env:GITHUB_REF_NAME' differs from LAB_03 version '$version'"
        }
    }

    $help = @(& java '-Dstdout.encoding=UTF-8' '-Dstderr.encoding=UTF-8' -jar $jarPath --help)
    if ($LASTEXITCODE -ne 0) {
        throw "Help command failed for $name"
    }
    $expectedHelp = @(
        'Метеостанція: аналіз вимірювань із CSV'
        '--help          показати цю довідку'
        '--version       показати версію програми'
        '--input ШЛЯХ    вказати вхідний CSV-файл'
        '--output ШЛЯХ   вказати файл для звіту'
    )
    foreach ($line in $expectedHelp) {
        if ($help -cnotcontains $line) {
            throw "Help line is missing for ${name}: $line"
        }
    }
    Write-Output "Help check passed: $name"

    if ($name -eq 'lab03' -and $help -cnotcontains '--danger        показати додатковий аналіз небезпеки') {
        throw 'LAB_03 help does not describe --danger'
    }

    # Файл без батьківського каталогу перевіряється з тимчасової робочої папки.
    $testDirectory = Join-Path $env:RUNNER_TEMP "$name-report"
    New-Item -ItemType Directory -Path $testDirectory -Force | Out-Null
    Push-Location $testDirectory
    try {
        $output = @(& java '-Dstdout.encoding=UTF-8' '-Dstderr.encoding=UTF-8' -jar $jarPath --input $inputPath --output report.txt)
        if ($LASTEXITCODE -ne 0) {
            throw "JAR command failed for $name"
        }
        $report = Get-Content -LiteralPath 'report.txt' -Raw -Encoding utf8
    } finally {
        Pop-Location
    }
    $expectedLines = @(
        'Мінімальна температура: -5.50 °C'
        'Максимальна швидкість вітру: 7.50 м/с'
        'Середній рівень вологості: 70.00 %'
        'Кількість коректних записів: 3'
    )
    foreach ($line in $expectedLines) {
        if ($output -cnotcontains $line) {
            throw "Expected report line is missing for ${name}: $line"
        }
    }
    $consoleText = ($output -join [Environment]::NewLine) + [Environment]::NewLine
    if ([string]::IsNullOrEmpty($report) -or -not $consoleText.EndsWith($report)) {
        throw "Console and UTF-8 file reports differ for $name"
    }
    $reports[$name] = $report
    $consoles[$name] = $consoleText
    Write-Output "JAR check passed: $name; UTF-8 report.txt without a parent directory."
}

foreach ($name in @('lab02', 'lab03')) {
    if ($reports['lab01'] -cne $reports[$name] -or $consoles['lab01'] -cne $consoles[$name]) {
        throw "LAB_01 and $name produce different results for the reference CSV"
    }
}
Write-Output 'Compatibility check passed: LAB_01, LAB_02 and LAB_03 console/file reports are identical.'

# Новий режим перевіряється окремо; старі лабораторні не мають цього ключа.
$dangerJar = (Resolve-Path -LiteralPath 'LAB_03/target/lab03.jar').Path
$dangerInput = (Resolve-Path -LiteralPath 'LAB_03/data/danger-example.csv').Path
$plainFile = Join-Path $env:RUNNER_TEMP 'lab03-danger/plain.txt'
$dangerFile = Join-Path $env:RUNNER_TEMP 'lab03-danger/danger.txt'
$plainOutput = @(& java '-Dstdout.encoding=UTF-8' '-Dstderr.encoding=UTF-8' -jar $dangerJar --input $dangerInput --output $plainFile)
if ($LASTEXITCODE -ne 0) {
    throw 'LAB_03 baseline command for danger fixture failed'
}
$dangerOutput = @(& java '-Dstdout.encoding=UTF-8' '-Dstderr.encoding=UTF-8' -jar $dangerJar --danger --input $dangerInput --output $dangerFile)
if ($LASTEXITCODE -ne 0) {
    throw 'LAB_03 --danger command failed'
}
$dangerLines = @(
    'Додатковий аналіз небезпеки:'
    '2026-09-01: Звичайне вимірювання, індекс 7.50'
    '2026-09-02: Штормове вимірювання, індекс 40.00'
    'Максимальний індекс небезпеки: 40.00'
)
foreach ($line in $dangerLines) {
    if ($dangerOutput -cnotcontains $line) {
        throw "LAB_03 danger analysis line is missing: $line"
    }
}
$plainText = ($plainOutput -join [Environment]::NewLine) + [Environment]::NewLine
$dangerText = ($dangerOutput -join [Environment]::NewLine) + [Environment]::NewLine
$plainReport = Get-Content -LiteralPath $plainFile -Raw -Encoding utf8
$dangerReport = Get-Content -LiteralPath $dangerFile -Raw -Encoding utf8
if (-not $dangerText.StartsWith($plainText) -or $plainReport -cne $dangerReport -or $dangerReport.Contains('індекс')) {
    throw 'LAB_03 --danger changed the standard console/file report'
}
Write-Output 'Danger CLI check passed: extra console analysis; standard UTF-8 file unchanged.'
