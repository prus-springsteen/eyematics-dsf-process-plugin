$dev_setup_path = ".\eyematics-dsf-process-plugin-test-setup\docker-compose.yml"

$fhir_server = @("dic-a-fhir","dic-b-fhir","dic-c-fhir","dic-d-fhir")   
$bpe_server = @("dic-a-bpe","dic-b-bpe","dic-c-bpe","dic-d-bpe")   
$fhir_data_repositories = @("dic-a-fhir-data-repository","dic-b-fhir-data-repository","dic-c-fhir-data-repository","dic-d-fhir-data-repository")  
$fhir_aggregated_data_repositories = @("dic-a-fhir-aggregated-data-repository","dic-b-fhir-aggregated-data-repository","dic-c-fhir-aggregated-data-repository","dic-d-fhir-aggregated-data-repository")

foreach ($fs in $fhir_server) {
    Start-Process powershell -ArgumentList "-NoExit","-Command","`$Host.UI.RawUI.WindowTitle = '$fs'; docker compose -f `"$dev_setup_path`" up $fs"
    Start-Sleep -Seconds 5
}

foreach ($bs in $bpe_server) {
    Start-Process powershell -ArgumentList "-NoExit","-Command","`$Host.UI.RawUI.WindowTitle = '$bs'; docker compose -f `"$dev_setup_path`" up $bs"
    Start-Sleep -Seconds 5
}

foreach ($dr in $fhir_data_repositories) {
    Start-Process powershell -ArgumentList "-NoExit","-Command","`$Host.UI.RawUI.WindowTitle = '$dr'; docker compose -f `"$dev_setup_path`" up $dr"
}

foreach ($ar in $fhir_aggregated_data_repositories) {
    Start-Process powershell -ArgumentList "-NoExit","-Command","`$Host.UI.RawUI.WindowTitle = '$ar'; docker compose -f `"$dev_setup_path`" up $ar"
}
  