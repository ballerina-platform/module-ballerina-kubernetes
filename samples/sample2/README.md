## Sample2: Kubernetes Hello World with livenessProbe and hostname mapping

- This sample runs simple ballerina hello world service in kubernetes cluster with livenessProbe probe and  hostname
 mapping for ingress. 
- Following files will be generated from this sample.
    ``` 
    $> docker image
    hello_world_k8s_config:latest
    
    $> tree
    ├── README.md
    ├── hello_world_k8s_config.bal
    ├── hello_world_k8s_config.jar
    ├── docker
        └── Dockerfile
    └── kubernetes
        ├── hello-world-k8s-config-deployment
        │   ├── Chart.yaml
        │   └── templates
        │       └── hello_world_k8s_config.yaml
        └── hello_world_k8s_config.yaml
    ```
### How to run:

1. Compile the  hello_world_k8s_config.bal file. Command to deploy kubernetes artifacts will be printed on build success.
```bash
$> ballerina build hello_world_k8s_config.bal
Compiling source
        hello_world_k8s_config.bal

Generating executables
        hello_world_k8s_config.jar

Generating artifacts...

        @kubernetes:Service                      - complete 1/1
        @kubernetes:Ingress                      - complete 1/1
        @kubernetes:Deployment                   - complete 1/1
        @kubernetes:Docker                       - complete 2/2 
        @kubernetes:Helm                         - complete 1/1

        Execute the below command to deploy the Kubernetes artifacts: 
        kubectl apply -f /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample2/kubernetes

        Execute the below command to install the application using Helm: 
        helm install --name hello-world-k8s-config-deployment /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample2/kubernetes/hello-world-k8s-config-deployment
```

2. hello_world_k8s_config.jar, Dockerfile, docker image and kubernetes artifacts will be generated: 
```bash
$> tree
.
├── README.md
├── hello_world_k8s_config.bal
├── hello_world_k8s_config.jar
├── docker
    └── Dockerfile
└── kubernetes
    ├── hello-world-k8s-config-deployment
    │   ├── Chart.yaml
    │   └── templates
    │       └── hello_world_k8s_config.yaml
    └── hello_world_k8s_config.yaml
```

3. Verify the docker image is created:
```bash
$> docker images
REPOSITORY                   TAG                 IMAGE ID            CREATED             SIZE
hello_world_k8s_config      latest              df83ae43f69b        2 minutes ago        102MB

```

4. Run kubectl command to deploy artifacts (Use the command printed on screen in step 1):
```bash
$> kubectl apply -f /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample2/kubernetes
service/hello created
ingress.extensions/helloep-ingress created
deployment.apps/hello-world-k8s-config-deployment created
```

5. Verify kubernetes deployment, service and ingress is running:
```bash
$> kubectl get pods
NAME                                                READY     STATUS    RESTARTS   AGE
hello-world-k8s-config-deployment-54768647ff-m64v9   1/1       Running   0          4s


$> kubectl get svc
NAME                                              TYPE           CLUSTER-IP       EXTERNAL-IP   PORT(S)                      AGE
helloep-svc                                       ClusterIP      10.110.199.222   <none>        9090/TCP                     3m

$> kubectl get ingress
NAME                 HOSTS     ADDRESS   PORTS     AGE
helloworld-ingress   abc.com             80, 443   4m
```

6. Access the hello world service with curl command:

- **Using ingress**

Add /etc/hosts entry to match hostname.
_(127.0.0.1 is only applicable to docker for mac users. Other users should map the hostname with correct ip address 
from `kubectl get ingress` command.)_
 ```
 127.0.0.1 abc.com
 ```
Use curl command with hostname to access the service.
```bash
$> curl http://abc.com/helloWorld/sayHello
Hello, World from service helloWorld !
```
7. Undeploy sample:
```bash
$> kubectl delete -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample2/kubernetes/
$> docker rmi hello_world_k8s_config
```