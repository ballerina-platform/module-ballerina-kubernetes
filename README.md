# Ballerina Kubernetes Extension
 
Annotation based kubernetes extension implementation for ballerina. 

[![Build Status](https://wso2.org/jenkins/job/ballerinax/job/kubernetes/badge/icon)](https://wso2.org/jenkins/job/ballerinax/job/kubernetes/)
[![Travis (.org)](https://img.shields.io/travis/ballerinax/kubernetes.svg?logo=travis)](https://travis-ci.org/ballerinax/kubernetes)
[![codecov](https://codecov.io/gh/ballerinax/kubernetes/branch/master/graph/badge.svg)](https://codecov.io/gh/ballerinax/kubernetes)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
## Features:
- Kubernetes deployment support. 
- Kubernetes service support.
- Kubernetes liveness probe support
- Kubernetes readiness probe support
- Kubernetes ingress support.
- Kubernetes horizontal pod autoscaler support.
- Docker image generation. 
- Docker push support with remote docker registry.
- Kubernetes secret support.
- Kubernetes config map support.
- Kubernetes persistent volume claim support.
- Kubernetes resource quotas.
- Istio gateways support.
- Istio virtual services support.
- OpenShift build config and image stream support.
- OpenShift route support.

**Refer [samples](samples) for more info.**

## Supported Annotations:

### @kubernetes:Deployment{}
- Supported with ballerina services or listeners.

|**Annotation Name**|**Description**|**Default value**|
|--|--|--|
|name|Name of the deployment|<OUTPUT_FILE_NAME>-deployment|
|labels|Labels for deployment|{ app: <OUTPUT_FILE_NAME> }|
|annotations|Annotations for deployment|{}|
|dockerHost|Docker host IP and docker PORT.(e.g "tcp://192.168.99.100:2376")|DOCKER_HOST environment variable. If DOCKER_HOST is unavailable, use "unix:///var/run/docker.sock" for Unix or use "npipe:////./pipe/docker_engine" for Windows 10 or use "localhost:2375"|
|dockerCertPath|Docker cert path|DOCKER_CERT_PATH environment variable|
|registry|Docker registry url|null|
|username|Username for the docker registry|null|
|password|Password for the docker registry|null|
|baseImage|Base image to create the docker image|ballerina/ballerina-runtime:<BALLERINA_VERSION>|
|image|Docker image with tag|<OUTPUT_FILE_NAME>:latest|
|buildImage|Building docker image|true|
|push|Push docker image to registry. This will be effective if image buildImage field is true|false|
|copyFiles|Copy external files for Docker image|null|
|singleYAML|Generate a single yaml file for all k8s resources|true|
|namespace|Namespace of the deployment|null|
|replicas|Number of replicas|1|
|livenessProbe|Enable or disable liveness probe|false|
|readinessProbe|Enable or disable readiness probe|false|
|imagePullPolicy|Docker image pull policy|IfNotPresent|
|env|List of environment variables|null|
|podAnnotations|Pod annotations|{}|
|buildExtension|Extension for building docker images and artifacts|null|
|dependsOn|Listeners this deployment Depends on|null|
|imagePullSecrets|Image pull secrets value|null|

### @kubernetes:Service{}
- Supported with ballerina listeners.

|**Annotation Name**|**Description**|**Default value**|
|--|--|--|
|name|Name of the Service|<BALLERINA_SERVICE_NAME>-service|
|labels|Labels for service|{ app: <OUTPUT_FILE_NAME> }|
|port|Service port|Port of the ballerina service|
|targetPort|Target pod(s) port|Port of the ballerina service|
|sessionAffinity|Pod session affinity|None|
|serviceType|Service type of the service|ClusterIP|

### @kubernetes:Ingress{}
- Supported with ballerina listeners.

|**Annotation Name**|**Description**|**Default value**|
|--|--|--|
|name|Name of the Ingress|<BALLERINA_SERVICE_NAME>-ingress|
|labels|Labels for service|{ app: <OUTPUT_FILE_NAME> }|
|annotations|Map of additional annotations|null|
|hostname|Host name of the ingress|<BALLERINA_SERVICE_NAME>.com or <BALLERINA_SERVICE_LISTENER_NAME>.com|
|path|Resource path.|/|
|targetPath|This will use for URL rewrite.|null|
|ingressClass|Ingress class|nginx|
|enableTLS|Enable ingress TLS|false|

### @kubernetes:HPA{}
- Supported with ballerina services.

|**Annotation Name**|**Description**|**Default value**|
|--|--|--|
|name|Name of the Horizontal Pod Autoscaler|<BALLERINA_SERVICE_NAME>-hpa|
|labels|Labels for service|{ app: <OUTPUT_FILE_NAME> }|
|annotations|Map of annotations|null|
|minReplicas|Minimum number of replicas|Number of replicas in deployment|
|maxReplicas|Maximum number of replicas|minReplicas + 1|
|cpuPrecentage|CPU percentage to start scaling|50|

### @kubernetes:Secret{}
- Supported with ballerina service.

|**Annotation Name**|**Description**|**Default value**|
|--|--|--|
|name|Name secret mount|<BALLERINA_SERVICE_NAME>-secret|
|labels|Labels for service|{ app: <OUTPUT_FILE_NAME> }|
|annotations|Map of annotations|null|
|mountPath|Path to mount on container|null|
|readOnly|Is mount read only|true|
|data|Paths to data files|null|

### @kubernetes:ConfigMap{}
- Supported with ballerina services.

|**Annotation Name**|**Description**|**Default value**|
|--|--|--|
|name|Name config map mount|<BALLERINA_SERVICE_NAME>-config-map|
|mountPath|Path to mount on container|null|
|readOnly|Is mount read only|true|
|ballerinaConf|Ballerina conf file location|null|
|data|Paths to data files|null|

### @kubernetes:PersistentVolumeClaim{}
- Supported with ballerina services.

|**Annotation Name**|**Description**|**Default value**|
|--|--|--|
|name|Name secret mount|null|
|labels|Labels for service|{ app: <OUTPUT_FILE_NAME> }|
|annotations|Metadata Annotations map|null|
|mountPath|Path to mount on container|null|
|accessMode|Access mode|ReadWriteOnce|
|volumeClaimSize|Size of the volume claim|null|
|readOnly|Is mount read only|false|

### @kubernetes:ResourceQuota{}
- Support with ballerina services, listeners and functions.

|**Annotation Name**|**Description**|**Default value**|
|--|--|--|
|name| Name of the resource quota|<OUTPUT_FILE_NAME>_resource_quota|
|labels|Labels for resource quota|{ app: <OUTPUT_FILE_NAME> }|
|annotations|Metadata Annotations map|null|
|hard|Hard rules|{}|
|scopes|Scopes to which the resource quota will be applied to|[]|

### @kubernetes:Job{}
- Supported with ballerina main function.

|**Annotation Name**|**Description**|**Default value**|
|--|--|--|
|name|Name of the job|<OUTPUT_FILE_NAME>-job|
|labels|Labels for job|{ app: <OUTPUT_FILE_NAME> }|
|annotations|Metadata Annotations map|{}|
|dockerHost|Docker host IP and docker PORT.(e.g "tcp://192.168.99.100:2376")|DOCKER_HOST environment variable. If DOCKER_HOST is unavailable, use "unix:///var/run/docker.sock" for Unix or use "npipe:////./pipe/docker_engine" for Windows 10 or use "localhost:2375"|
|dockerCertPath|Docker cert path|DOCKER_CERT_PATH environment variable|
|registry|Docker registry url|null|
|username|Username for the docker registry|null|
|password|Password for the docker registry|null|
|baseImage|Base image to create the docker image|ballerina/ballerina-runtime:<BALLERINA_VERSION>|
|image|Docker image with tag|<OUTPUT_FILE_NAME>:latest|
|buildImage|Building docker image|true|
|push|Push docker image to registry. This will be effective if image buildImage field is true|false|
|copyFiles|Copy external files for Docker image|null|
|singleYAML|Generate a single yaml file for all k8s resources|true|
|namespace|Namespace for the Job|default|
|imagePullPolicy|Docker image pull policy|IfNotPresent|
|env|List of environment variables|null|
|restartPolicy|Restart policy|Never|
|backoffLimit|Backoff limit|3|
|activeDeadlineSeconds|Active deadline seconds|20|
|schedule|Schedule for cron jobs|none|
|imagePullSecrets|Image pull secrets value|null|

## How to build

1. Download and install JDK 8 or later
2. Install Docker
3. Get a clone or download the source from this repository (https://github.com/ballerinax/kubernetes)
4. Run the Maven command ``mvn clean install`` from within the ``kubernetes`` directory.

## Deploy ballerina service directly using kubectl command.

### Annotation Usage Sample:

```ballerina
import ballerina/http;
import ballerina/log;
import ballerinax/kubernetes;

@kubernetes:Ingress{
    hostname: "abc.com"
}
@kubernetes:Service {
    name:"hello"
}
listener http:Listener helloEP = new(9090);

@kubernetes:Deployment {
    livenessProbe: true
}
@http:ServiceConfig {
    basePath: "/helloWorld"
}
service helloWorld on helloEP {
    resource function sayHello(http:Caller caller, http:Request request) {
        http:Response response = new;
        response.setTextPayload("Hello, World from service helloWorld ! ");
        var responseResult = caller->respond(response);
        if (responseResult is error) {
            log:printError("error responding back to client.", err = responseResult);
        }
    }
}
```

The kubernetes artifacts will be created in following structure.
```bash
kubernetes
├── deployment.yaml
├── ingress.yaml
├── secret.yaml
├── config_map.yaml
├── volume_claim.yaml
├── svc.yaml
└── docker
 └── Dockerfile
    	
```
