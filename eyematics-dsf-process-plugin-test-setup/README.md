# Docker Test (Development) Setup
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

Start the development setup by using following command:
```sh
docker compose up -d && docker compose logs -f
```

## Hardware
The minimum hardware requirements to run all simulated organizations and [Blaze FHIR® Servers](https://samply.github.io/blaze/) as part of the Docker dev-setup is 24 GB of RAM.

## Instances, IPs and Ports

### DIC-A

| Component                                        | IP and Port                |
|--------------------------------------------------|----------------------------|
| FHIR (NGINX-FHIR-FRONTEND)                       | 172.20.0.66                |
| BPE (NGINX-BPE-FRONTEND)                         | 172.20.0.114               |
| FHIR (PORTS)                                     | 127.0.0.1:5000:5000        |
| FHIR (FRONTEND)                                  | 172.20.0.67                |
| BPE (PORTS)                                      | 127.0.0.1:5003:5003        |
| BPE (FRONTEND)                                   | 172.20.0.115               |
| FHIR (NETWORK-FHIR-FRONTEND)                     | 172.20.0.64/28             |
| BPE (NETWORK-BPE-FRONTEND)                       | 172.20.0.112/28            |
| DIC-A-FHIR-LOCAL-DATA-REPOSITORY                 | 172.0.0.1:8070:8070        |
| DIC-A-FHIR-DEMONSTRATOR-DATA-REPOSITORY          | 172.0.0.1:8071:8071        |

### DIC-B

| Component                                        | IP and Port                |
|--------------------------------------------------|----------------------------|
| FHIR (NGINX-FHIR-FRONTEND)                       | 172.20.0.98                |
| BPE (NGINX-BPE-FRONTEND)                         | 172.20.0.146               |
| FHIR (PORTS)                                     | 127.0.0.1:5002:5002        |
| FHIR (FRONTEND)                                  | 172.20.0.99                |
| BPE (PORTS)                                      | 127.0.0.1:5005:5005        |
| BPE (FRONTEND)                                   | 172.20.0.147               |
| FHIR (NETWORK-FHIR-FRONTEND)                     | 172.20.0.96/28             |
| BPE (NETWORK-BPE-FRONTEND)                       | 172.20.0.144/28            |
| DIC-B-FHIR-LOCAL-DATA-REPOSITORY                 | 172.0.0.1:8072:8072        |
| DIC-B-FHIR-DEMONSTRATOR-DATA-REPOSITORY          | 172.0.0.1:8073:8073        |

### DIC-C

| Component                                        | IP and Port                |
|--------------------------------------------------|----------------------------|
| FHIR (NGINX-FHIR-FRONTEND)                       | 172.20.0.82                |
| BPE (NGINX-BPE-FRONTEND)                         | 172.20.0.130               |
| FHIR (PORTS)                                     | 127.0.0.1:5001:5001        |
| FHIR (FRONTEND)                                  | 172.20.0.83                |
| BPE (PORTS)                                      | 127.0.0.1:5004:5004        |
| BPE (FRONTEND)                                   | 172.20.0.131               |
| FHIR (NETWORK-FHIR-FRONTEND)                     | 172.20.0.80/28             |
| BPE (NETWORK-BPE-FRONTEND)                       | 172.20.0.128/28            |
| DIC-C-FHIR-LOCAL-DATA-REPOSITORY                 | 172.0.0.1:8074:8074        |
| DIC-C-FHIR-DEMONSTRATOR-DATA-REPOSITORY          | 172.0.0.1:8075:8075        |

### DIC-D

| Component                                        | IP and Port                |
|--------------------------------------------------|----------------------------|
| FHIR (NGINX-FHIR-FRONTEND)                       | 172.20.0.188               |
| BPE (NGINX-BPE-FRONTEND)                         | 172.20.0.236               |
| FHIR (PORTS)                                     | 127.0.0.1:5006:5006        |
| FHIR (FRONTEND)                                  | 172.20.0.189               |
| BPE (PORTS)                                      | 127.0.0.1:5009:5009        |
| BPE (FRONTEND)                                   | 172.20.0.237               |
| FHIR (NETWORK-FHIR-FRONTEND)                     | 172.20.0.176/28            |
| BPE (NETWORK-BPE-FRONTEND)                       | 172.20.0.224/28            |
| DIC-D-FHIR-LOCAL-DATA-REPOSITORY                 | 172.0.0.1:8076:8076        |
| DIC-D-FHIR-DEMONSTRATOR-DATA-REPOSITORY          | 172.0.0.1:8077:8077        |