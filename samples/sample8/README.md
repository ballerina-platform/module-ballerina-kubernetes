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
    ├── hello_world_config_map_k8s.bal
    ├── hello_world_config_map_k8s.balx
    └── kubernetes
        ├── docker
        │   └── Dockerfile
        ├── hello-world-config-map-k8s-deployment
        │   ├── Chart.yaml
        │   └── templates
        │       ├── hello_world_config_map_k8s_config_map.yaml
        │       ├── hello_world_config_map_k8s_deployment.yaml
        │       ├── hello_world_config_map_k8s_ingress.yaml
        │       ├── hello_world_config_map_k8s_secret.yaml
        │       └── hello_world_config_map_k8s_svc.yaml
        ├── hello_world_config_map_k8s_config_map.yaml
        ├── hello_world_config_map_k8s_deployment.yaml
        ├── hello_world_config_map_k8s_ingress.yaml
        ├── hello_world_config_map_k8s_secret.yaml
        └── hello_world_config_map_k8s_svc.yaml

    ```
### How to run:

1. Compile the hello_world_config_map_k8s.bal file. Command to run kubernetes artifacts will be printed on success:
```bash
$> ballerina build hello_world_config_map_k8s.bal
Compiling source
    hello_world_config_map_k8s.bal
Generating executable
    hello_world_config_map_k8s.balx
	@kubernetes:Service 			 - complete 1/1
	@kubernetes:Ingress 			 - complete 1/1
	@kubernetes:Secret 			 - complete 1/1
	@kubernetes:ConfigMap 			 - complete 2/2
	@kubernetes:Deployment 			 - complete 1/1
	@kubernetes:Docker 			 - complete 3/3
	@kubernetes:Helm 			 - complete 1/1

	Run the following command to deploy the Kubernetes artifacts:
	kubectl apply -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample8/kubernetes/

	Run the following command to install the application using Helm:
	helm install --name hello-world-config-map-k8s-deployment /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample8/kubernetes/hello-world-config-map-k8s-deployment
```

2. hello_world_config_map_k8s.balx, Dockerfile, docker image and kubernetes artifacts will be generated: 
```bash
$> tree
.
├── README.md
├── conf
│   ├── ballerina.conf
│   └── data.txt
├── hello_world_config_map_k8s.bal
├── hello_world_config_map_k8s.balx
└── kubernetes
    ├── docker
    │   └── Dockerfile
    ├── hello-world-config-map-k8s-deployment
    │   ├── Chart.yaml
    │   └── templates
    │       ├── hello_world_config_map_k8s_config_map.yaml
    │       ├── hello_world_config_map_k8s_deployment.yaml
    │       ├── hello_world_config_map_k8s_ingress.yaml
    │       ├── hello_world_config_map_k8s_secret.yaml
    │       └── hello_world_config_map_k8s_svc.yaml
    ├── hello_world_config_map_k8s_config_map.yaml
    ├── hello_world_config_map_k8s_deployment.yaml
    ├── hello_world_config_map_k8s_ingress.yaml
    ├── hello_world_config_map_k8s_secret.yaml
    └── hello_world_config_map_k8s_svc.yaml

```

3. Verify the docker image is created:
```bash
$> docker images
REPOSITORY                      TAG                 IMAGE ID            CREATED             SIZE
hello_world_config_map_k8s     latest              53559c0cd4f4        55 seconds ago      194MB
```

4. Run kubectl command to deploy artifacts (Use the command printed on screen in step 1):
```bash
$> kubectl apply -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample8/kubernetes/
configmap "helloworld-config-map" configured
configmap "helloworld-ballerina-conf-config-map" configured
deployment.extensions "hello-world-config-map-k8s-deployment" created
ingress.extensions "helloworldep-ingress" created
secret "helloworldep-secure-socket" created
service "helloworldep-svc" created
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
