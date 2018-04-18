# Ballerina Kubernetes Extension
 
Annotation based kubernetes extension implementation for ballerina. 

[![Build Status](https://wso2.org/jenkins/job/ballerinax/job/kubernetes/badge/icon)](https://wso2.org/jenkins/job/ballerinax/job/kubernetes/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
## Features:
- Kubernetes deployment support. 
- Kubernetes service support.
- Kubernetes liveness probe support
- Kubernetes ingress support.
- Kubernetes horizontal pod autoscaler support.
- Docker image generation. 
- Docker push support with remote docker registry.
- Kubernetes secret support.
- Kubernetes config map support
- Kubernetes persistent volume claim support


## Supported Annotations:

### @kubernetes:Deployment{}
- Supported with ballerina services or endpoints.

|**Annotation Name**|**Description**|**Default value**|
|--|--|--|
|name|Name of the deployment|\<outputfilename\>-deployment|
|labels|Labels for deployment|"app: \<outputfilename\>"|
|replicas|Number of replicas|1|
|enableLiveness|Enable or disable liveness probe|disable|
|initialDelaySeconds|Initial delay in seconds before performing the first probe|10s|
|periodSeconds|Liveness probe interval|5s|
|livenessPort|Port which the Liveness probe check|\<ServicePort\>|
|imagePullPolicy|Docker image pull policy|IfNotPresent|
|image|Docker image with tag|<output file name>:latest|
|env|List of environment variables|null|
|buildImage|Building docker image|true|
|copyFiles|Copy external files for Docker image|null|
|dockerHost|Docker host IP and docker PORT.(e.g "tcp://192.168.99.100:2376")|null|
|dockerCertPath|Docker cert path|null|
|push|Push docker image to registry. This can only be true if image build is true.|false|
|username|Username for the docker registry|null|
|password|Password for the docker registry|null|
|baseImage|Base image to create the docker image|ballerina/ballerina:latest|

### @kubernetes:Service{}
- Supported with ballerina endpoints.

|**Annotation Name**|**Description**|**Default value**|
|--|--|--|
|name|Name of the Service|\<ballerina service name\>-service|
|labels|Labels for service|"app: \<outputfilename\>"|
|serviceType|Service type of the service|ClusterIP|
|port|Service port|Port of the ballerina service|

### @kubernetes:Ingress{}
- Supported with ballerina endpoints.

|**Annotation Name**|**Description**|**Default value**|
|--|--|--|
|name|Name of the Ingress|\<ballerina service name\>-ingress
|labels|Labels for service|"app: \<outputfilename\>"
|hostname|Host name of the ingress|\<ballerina service name\>.com
|path|Resource path.|/
|targetPath|This will use for URL rewrite.|null
|ingressClass|Ingress class|nginx
|enableTLS|Enable ingress TLS|false

### @kubernetes:HPA{}
- Supported with ballerina services.

|**Annotation Name**|**Description**|**Default value**|
|--|--|--|
|name|Name of the Horizontal Pod Autoscaler|\<ballerina service name\>-hpa|
|labels|Labels for service|"app: \<outputfilename\>"|
|minReplicas|Minimum number of replicas|No of replicas in deployment|
|maxReplicas|Maximum number of replicas|minReplicas+1|
|cpuPrecentage|CPU percentage to start scaling|50|

### @kubernetes:Secret{}
- Supported with ballerina service.

|**Annotation Name**|**Description**|**Default value**|
|--|--|--|
|name|Name secret mount|\<service_name\>-secret|
|mountPath|Path to mount on container|null|
|readOnly|Is mount read only|true|
|data|Paths to data files|null|

### @kubernetes:ConfigMap{}
- Supported with ballerina services.

|**Annotation Name**|**Description**|**Default value**|
|--|--|--|
|name|Name config map mount|\<service_name\>-config-map|
|mountPath|Path to mount on container|null|
|readOnly|Is mount read only|true|
|ballerinaConf|Ballerina conf file location|null|
|data|Paths to data files|null|

### @kubernetes:PersistentVolumeClaim{}
- Supported with ballerina services.

|**Annotation Name**|**Description**|**Default value**|
|--|--|--|
|name|Name secret mount|null|
|mountPath|Path to mount on container|null|
|readOnly|Is mount read only|false|
|accessMode|Access mode|ReadWriteOnce|
|volumeClaimSize|Size of the volume claim|null|

### @kubernetes:Job{}
- Supported with ballerina main function.

|**Annotation Name**|**Description**|**Default value**|
|--|--|--|
|name| Name of the job|\<output file name\>-job|
|labels| Labels for job|"app: \<outputfilename\>"|
|restartPolicy| Restart policy|Never|
|backoffLimit| Backoff limit|3|
|activeDeadlineSeconds| Active deadline seconds|20|
|schedule| Schedule for cron jobs|none|
|imagePullPolicy|Docker image pull policy|IfNotPresent|
|image|Docker image with tag|\<output file name\>:latest|
|env|List of environment variables|null|
|buildImage|Building docker image|true|
|dockerHost|Docker host IP and docker PORT.(e.g "tcp://192.168.99.100:2376")|null|
|dockerCertPath|Docker cert path|null|
|push|Push docker image to registry. This can only be true if image build is true.|false|
|username|Username for the docker registry|null|
|password|Password for the docker registry|null|
|baseImage|Base image to create the docker image|ballerina/ballerina:latest|


## How to run

1. Download and install JDK 8 or later
2. Get a clone or download the source from this repository (https://github.com/ballerinax/docker)
3. Run the Maven command ``mvn clean  install`` from within the ``kubernetes`` directory.
4. Copy ``target/kubernetes-extension-0.962.0.jar`` file to ``<BALLERINA_HOME>/bre/lib`` directory.
5. Run ``ballerina build <.bal filename>`` to generate artifacts.

The docker artifacts will be created in a folder called target with following structure.
```bash
    └── kubernetes
    │           ├── deployment.yaml
    │           ├── ingress.yaml
    │           ├── secret.yaml
    │           ├── config_map.yaml
    │           ├── volume_claim.yaml
    │           ├── service.yaml
    │           └── docker
    │	        	  └── Dockerfile
    │  	
    └── outputfilename.balx	
```

### Annotation Usage Sample:
```ballerina
import ballerina/http;
import ballerinax/kubernetes;

@kubernetes:Service{name:"hello"}
endpoint http:Listener helloEP {
    port:9090
};

@kubernetes:Deployment{
    enableLiveness:"enable"
}
@kubernetes:Ingress{
    hostname:"abc.com"
}
@http:serviceConfig {
    basePath:"/helloWorld"
}
service<http:Service> helloWorld bind helloEP {
    sayHello (endpoint outboundEP, http:Request request) {
        http:Response response = new;
        response.setStringPayload("Hello, World from service helloWorld ! ");
        _ = outboundEP -> respond(response);
    }
}
```
**Refer [samples](samples) for more info.**
