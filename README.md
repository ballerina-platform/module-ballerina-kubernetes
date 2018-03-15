# Ballerina Kubernetes Extension
 
Annotation based kubernetes extension implementation for ballerina. 

## Features:
- Kubernetes deployment support. 
- Kubernetes service support.
- Kubernetes ingress support.
- Kubernetes horizontal pod autoscaler support.
- Docker file generation. 
- Docker image generation. 
- Docker push support with docker registry.
 

## Supported Annotations:

### @kubernetes:deployment{}
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
|namespace|Kubernetes namespace|default|
|image|Docker image with tag|<output file name>:latest|
|env|List of environment variables|null|
|buildImage|Building docker image|true|
|push|Push docker image to registry. This can only be true if image build is true.|false|
|username|Username for the docker registry|null|
|password|Password for the docker registry|null|
|baseImage|Base image to create the docker image|ballerina/ballerina:latest|

### @kubernetes:svc{}
|**Annotation Name**|**Description**|**Default value**|
|--|--|--|
|name|Name of the Service|\<ballerina service name\>-service|
|labels|Labels for service|"app: \<outputfilename\>"|
|serviceType|Service type of the service|NodePort|
|port|Service port|Port of the ballerina service|

### @kubernetes:ingress{}
|**Annotation Name**|**Description**|**Default value**|
|--|--|--|
|name|Name of the Ingress|\<ballerina service name\>-ingress
|labels|Labels for service|"app: \<outputfilename\>"
|hostname|Host name of the ingress|\<ballerina service name\>.com
|path|Resource path.|/
|targetPath|This will use for URL rewrite.|null
|ingressClass|Ingress class|nginx
|enableTLS|Enable ingress TLS|false

### @kubernetes:hpa{}
|**Annotation Name**|**Description**|**Default value**|
|--|--|--|
|name|Name of the Horizontal Pod Autoscaler|\<ballerina service name\>-hpa|
|labels|Labels for service|"app: \<outputfilename\>"|
|minReplicas|Minimum number of replicas|No of replicas in deployment|
|maxReplicas|Maximum number of replicas|minReplicas+1|
|cpuPrecentage|CPU percentage to start scaling|50|

## How to run

1. Download and install JDK 8 or later
2. Get a clone or download the source from this repository (https://github.com/ballerinax/docker)
3. Run the Maven command ``mvn clean  install`` from within the ``kubernetes`` directory.
4. Copy ``target/kubernetes-extenstion-0.962.0.jar`` file to ``<BALLERINA_HOME>/bre/lib`` directory.
5. Run ``ballerina build <.bal filename>`` to generate artifacts.

The docker artifacts will be created in a folder called target with following structure.
```bash
target/
├── outputfilename
│   └── kubernetes
│      		├── deployment.yaml
│       		├── ingress.yaml
│       		├── job.yaml
│       		├── service.yaml
│        		└── docker
│	      		    └── Dockerfile
│  	
└── outputfilename.balx	
```

### Annotation Usage Sample:
```ballerina
import ballerina.net.http;
import ballerinax.kubernetes;


@kubernetes:deployment{
    enableLiveness:"enable"
}
@kubernetes:svc{}
@kubernetes:ingress{
    hostname:"abc.com"
}
@http:configuration {
    basePath:"/helloWorld"
}
service<http> helloWorld {
    resource sayHello (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        res.setStringPayload("Hello, World from service helloWorld! ");
        _ = conn.respond(res);
    }
}
```
**Refer [samples](samples) for more info.**
