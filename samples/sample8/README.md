## Sample8: Mount config map volumes to deployment 

- This sample runs simple ballerina hello world service with config map mounts.
- K8S config maps are intended to hold config information.
- Putting this information in a config map is safer and more flexible than putting it verbatim in a pod definition or in a docker image.
- @kubernetes:ConfigMap{} annotation will create k8s config maps. See [hello_world_config_map_k8s.bal](
./hello_world_config_map_k8s.bal)  
- Following files will be generated from this sample.
    ``` 
    $> docker image
    hello_world_config_map_k8s:latest
    
    $> tree
    ├── README.md
    ├── conf
    │   ├── ballerina.conf
    │   └── data.txt
    ├── security
    │   ├── ballerinaKeystore.p12
    │   └── ballerinaTruststore.p12
    ├── hello_world_config_map_k8s.bal
    ├── hello_world_config_map_k8s.jar
    ├── docker
        └── Dockerfile
    └── kubernetes
        ├── hello-world-config-map-k8s-deployment
        │   ├── Chart.yaml
        │   └── templates
        │       └── hello_world_config_map_k8s.yaml
        └── hello_world_config_map_k8s.yaml

    ```
### How to run:

1. Compile the hello_world_config_map_k8s.bal file. Command to deploy kubernetes artifacts will be printed on build success.
```bash
$> ballerina build hello_world_config_map_k8s.bal
Compiling source
        hello_world_config_map_k8s.bal

Generating executables
        hello_world_config_map_k8s.jar

Generating artifacts...

        @kubernetes:Service                      - complete 1/1
        @kubernetes:Ingress                      - complete 1/1
        @kubernetes:Secret                       - complete 1/1
        @kubernetes:ConfigMap                    - complete 2/2
        @kubernetes:Deployment                   - complete 1/1
        @kubernetes:Docker                       - complete 2/2 
        @kubernetes:Helm                         - complete 1/1

        Execute the below command to deploy the Kubernetes artifacts: 
        kubectl apply -f /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample8/kubernetes

        Execute the below command to install the application using Helm: 
        helm install --name hello-world-config-map-k8s-deployment /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample8/kubernetes/hello-world-config-map-k8s-deployment```

2. hello_world_config_map_k8s.jar, Dockerfile, docker image and kubernetes artifacts will be generated: 
```bash
$> tree
.
├── README.md
├── conf
│   ├── ballerina.conf
│   └── data.txt
├── security
│   ├── ballerinaKeystore.p12
│   └── ballerinaTruststore.p12
├── hello_world_config_map_k8s.bal
├── hello_world_config_map_k8s.jar
├── docker
    └── Dockerfile
└── kubernetes
    ├── hello-world-config-map-k8s-deployment
    │   ├── Chart.yaml
    │   └── templates
    │       └── hello_world_config_map_k8s.yaml
    └── hello_world_config_map_k8s.yaml

```

3. Verify the docker image is created:
```bash
$> docker images
REPOSITORY                      TAG                 IMAGE ID            CREATED             SIZE
hello_world_config_map_k8s     latest              53559c0cd4f4        55 seconds ago      194MB
```

4. Run kubectl command to deploy artifacts (Use the command printed on screen in step 1):
```bash
$> kubectl apply -f /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample8/kubernetes
service/helloworldep-svc created
ingress.extensions/helloworldep-ingress created
secret/helloworldep-secure-socket created
configmap/helloworld-config-map created
configmap/helloworld-ballerina-conf-config-map created
deployment.apps/hello-world-config-map-k8s-deployment created
```

5. Verify kubernetes deployment,service,secrets and ingress is deployed:
```bash
$> kubectl get pods
NAME                                                       READY      STATUS    RESTARTS   AGE
hello-world-config-map-k8s-deployment-6744b97dc5-2z5k4      1/1       Running   0          5m

$> kubectl get svc
NAME               TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)    AGE
helloworldep-svc   ClusterIP   10.100.232.242   <none>        9090/TCP   6m

$> kubectl get ingress
NAME                   HOSTS     ADDRESS   PORTS     AGE
helloworldep-ingress   abc.com             80, 443   25s

$> kubectl get secrets
NAME                         TYPE                                  DATA      AGE
helloworldep-secure-socket   Opaque                                2         1m

$> kubectl get configmaps
NAME                                   DATA      AGE
helloworld-ballerina-conf-config-map   1         4s
helloworld-config-map                  1         4s
```

6. Access the hello world service with curl command:

- **Using ingress:**
Add /etc/hosts entry to match hostname. 
_(127.0.0.1 is only applicable to docker for mac users. Other users should map the hostname with correct ip address 
from `kubectl get ingress` command.)_

```bash
$> curl https://abc.com/helloWorld/config/john -k
{userId: john@ballerina.com, groups: apim,esb}
$> curl https://abc.com/helloWorld/config/jane -k
{userId: jane3@ballerina.com, groups: esb}
$> curl https://abc.com/helloWorld/data -k
Data: Lorem ipsum dolor sit amet.
```

7. Undeploy sample:
```bash
$> kubectl delete -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample8/kubernetes/
$> docker rmi hello_world_config_map_k8s

```
