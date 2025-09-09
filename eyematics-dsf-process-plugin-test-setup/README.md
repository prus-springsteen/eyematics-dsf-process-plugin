# Docker Test Setup
Build the project from the root directory of this repository by executing the following command.

```sh
mvn clean package
```

The following entries are required in the `hosts` file of your computer so that the FHIR servers of the simulated organizations can be accessed in your web browser. On Linux and Mac this file is located at `/etc/hosts`. On Windows you can find it at `C:\Windows\System32\drivers\etc\hosts`.
```
127.0.0.1	dic-a
127.0.0.1	dic-b
127.0.0.1	dic-c
127.0.0.1	dic-d
127.0.0.1	keycloak
```

You may start every container - in terms of logging - manually or just use the PowerShell file 'start_eyematics_dev_process.ps1' file if you are using windows.
All process can be started by the execution of the file:
```sh
.\start_eyematics_dev_process.ps1
```

Otherwise, start the development setup by using following command:
```sh
docker compose up -d && docker compose logs -f
```

## Hardware
The minimum hardware requirements to run all simulated organizations and [Blaze FHIR® Servers](https://samply.github.io/blaze/) as part of the Docker dev-setup is 24 GB of RAM.