#!/usr/bin/env sh

echo -n "Waiting for FHIR server to be online..."
i=0
status_code=0
while true; do
	status_code=$(curl -s -o /dev/null -w "%{http_code}" "http://${dic}-fhir-local-data-repository:${port}/fhir/metadata")
	if [ "$status_code" -eq 200 ]; then
		echo "DONE"
		break
	fi
	sleep 5
	i=$((i+1))
	if [ $i -ge 120 ]; then
		echo "FAILED AFTER $i ATTEMPTS."
		break
	fi
done

if [ "$status_code" -eq 200 ]; then
	echo -n "Uncompressing JSON files..."
	tar -xzvf /tmp/${resourceFile} -C /tmp/ >/dev/null 2>&1
	if [ $? -eq 0 ]; then
		echo "DONE"
	else
		echo "FAILED"
		exit  1
	fi
	for bundle in /tmp/*.json
	do
		echo -n "Adding Bundle ($bundle) to FHIR server..."
		status_code=$(curl -X POST -s -o /dev/null -w "%{http_code}" -H "Content-Type: application/fhir+json" -d @$bundle "http://${dic}-fhir-local-data-repository:${port}/fhir")
			if [ "$status_code" -ne 200 ]; then
				echo "FAILED"
			else
				echo "DONE"
			fi
	done
fi
exit 1