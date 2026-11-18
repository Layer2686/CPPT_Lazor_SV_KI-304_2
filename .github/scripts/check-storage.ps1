$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
$jar = (Resolve-Path 'LAB_05/target/lab05.jar').Path
$inputFile = (Resolve-Path 'LAB_05/data/queries-example.csv').Path
$directory = Join-Path (Resolve-Path 'LAB_05/target').Path 'ci-storage'
New-Item -ItemType Directory -Path $directory -Force | Out-Null
$csv = Join-Path $directory 'weather.csv'
$json = Join-Path $directory 'weather.json'
$report = Join-Path $directory 'report.txt'
$restored = Join-Path $directory 'restored.csv'
$importedReport = Join-Path $directory 'imported-report.txt'

$output = @(& java '-Dstdout.encoding=UTF-8' '-Dstderr.encoding=UTF-8' -jar $jar --input $inputFile --output $report --export-csv $csv --export-json $json --round-trip)
if ($LASTEXITCODE -ne 0 -or $output -cnotcontains 'Round-trip: true (записів: 7)') {
    throw 'LAB_05 real JAR export/round-trip failed'
}
$csvText = Get-Content -LiteralPath $csv -Raw -Encoding utf8
if (-not $csvText.StartsWith("дата,вологість,тиск,температура,вітер`n") -or $csvText.Contains("`r")) {
    throw 'CSV header, UTF-8 or deterministic LF failed'
}
$example = Get-Content -LiteralPath 'LAB_05/data/export-example.csv' -Raw -Encoding utf8
if ($csvText -cne $example.Replace("`r`n", "`n")) {
    throw 'Actual exported CSV differs from committed generated example'
}
$objects = @(Get-Content -LiteralPath $json -Raw -Encoding utf8 | ConvertFrom-Json)
if ($objects.Count -ne 7 -or $objects[0].'дата' -cne '2026-09-01' -or
    $objects[0].'температура' -ne 10 -or $objects[0].'вітер' -ne 7.5) {
    throw 'JSON structure, numeric values or Unicode names failed'
}
$imported = @(& java '-Dstdout.encoding=UTF-8' '-Dstderr.encoding=UTF-8' -jar $jar --import-csv $csv --output $importedReport --export-csv $restored --round-trip --danger --queries)
if ($LASTEXITCODE -ne 0 -or $imported -cnotcontains 'Round-trip: true (записів: 7)' -or
    $imported -cnotcontains 'Максимальний індекс небезпеки: 40.00') {
    throw 'LAB_05 real JAR import/analysis failed'
}
if ($csvText -cne (Get-Content -LiteralPath $restored -Raw -Encoding utf8) -or
    (Get-Content -LiteralPath $report -Raw -Encoding utf8) -cne
    (Get-Content -LiteralPath $importedReport -Raw -Encoding utf8)) {
    throw 'Round-trip values/order/duplicate dates or standard report changed'
}
Write-Output 'Storage CLI passed: UTF-8/LF CSV, parsed JSON, full round-trip and identical report.'
