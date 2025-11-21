# EyeMatics DSF Process Plugin
This repository contains the plugin for transferring data between DICs participating in the EyeMatics project for the [Data Sharing Framework](https://github.com/datasharingframework/dsf) (DSF).

## Documentation
The documentation of the process including the description, the deployment and configuration guides as well as instructions on how to start a process instance can be found in the [wiki](https://imigitlab.uni-muenster.de/MeDIC/eyematics/eyematics-dsf-process-plugin/-/wikis/home).
The main documentation for the DSF can be found under [dsf.dev](https://dsf.dev).

## Build
Prerequisite: Java 17, Maven >= 3.6

Build the project from the root directory of this repository by executing the following command. 

```sh
mvn clean package
```

## Testing
You can test the processes by following the [README](eyematics-dsf-process-plugin-test-setup/README.md) in
the `eyematics-dsf-process-plugin-test-setup` directory.

## Acknowledgements
The EyeMatics DSF process plugin is developed based on the DSF Community. Here the periodical support by Simon ([@schwzr](https://github.com/schwzr)) needs to be highlighted. 

Furthermore, existing processes and code is obtained from following repositories:
- [MII Process Data Transfer](https://github.com/medizininformatik-initiative/mii-process-data-transfer) — Partially the data transferring processes as starting point for the EyeMatics process. Many thanks to [@wetret](https://github.com/wetret) and [@hhund](https://github.com/hhund) for the work.
- [MII Process Data Sharing](https://github.com/medizininformatik-initiative/mii-process-data-sharing) — Especially error handling and sub-processes are inspired from this repository. Many thanks to [@wetret](https://github.com/wetret), [@hhund](https://github.com/hhund) and [@nhaldorn](https://github.com/nhaldorn) for the work.
- [Feasibility Processes](https://github.com/medizininformatik-initiative/mii-process-feasibility) — The development environment, as also the project specific enhancement of it needs to be mentioned. Many thanks to [@EmteZogaf](https://github.com/EmteZogaf) and [@alexanderkiel](https://github.com/alexanderkiel) for the work.
- [NUM Process Dashboard Report](https://github.com/medizininformatik-initiative/dsf-plugin-numdashboard) - The questionnaire implementation is based on this repository. Many thanks to [@Tim-Steinbach-UKB](https://github.com/Tim-Steinbach-UKB), [@quastkj](https://github.com/quastkj) and [@kleinertp](https://github.com/kleinertp) for the work.
- [RDP Processes](https://github.com/num-codex/codex-processes-ap1) — The fTTP client could be found in this repository and is taken - with modifications. Many thanks to [@wetret](https://github.com/wetret), [@hhund](https://github.com/hhund) and [@schwzr](https://github.com/schwzr) for the work.
- [MII Processes Common](https://github.com/medizininformatik-initiative/mii-processes-common) — FHIR® clients, cryptography, constants and the DataSetStatusGenerator are also used from this repository with little modifications. Many thanks to [@wetret](https://github.com/wetret) and [@hhund](https://github.com/hhund) for the work.

## License
All code is published under the [Apache-2.0 License](LICENSE).