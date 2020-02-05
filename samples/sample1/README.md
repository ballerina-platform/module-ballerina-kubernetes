## Sample1: Kubernetes Hello World

- This sample runs simple ballerina hello world service in kubernetes cluster with minimal configurations. 
- The endpoint is annotated with @kubernetes:Service{} and passing serviceType as NodePort. 
- Note that the @kubernetes:Deployment{} is optional. See [hello_world_k8s.bal](./hello_world_k8s.bal)
- Default values for kubernetes annotation attributes will be used to create artifacts.
- Following files will be generated from this sample.
    ``` 
    $> docker image
    hello_world_k8s:latest
    
    $> tree
    ├── README.md
    ├── hello_world_k8s.bal
    ├── hello_world_k8s.jar
    ├── docker
        └── Dockerfile
    └── kubernetes
        ├── hello-world-k8s-deployment
        │   ├── Chart.yaml
        │   └── templates
        │       └── hello_world_k8s_deployment.yaml
        └── hello_world_k8s.yaml
    ```
### How to run:

1. Compile the  hello_world_k8s.bal file. Command to deploy kubernetes artifacts will be printed on build success.
```bash
$> ballerina build hello_world_k8s.bal
Compiling source
        hello_world_k8s.bal

Generating executables
        hello_world_k8s.jar

Generating artifacts...

        @kubernetes:Service                      - complete 1/1
        @kubernetes:Deployment                   - complete 1/1
        @kubernetes:Docker                       - complete 2/2 
        @kubernetes:Helm                         - complete 1/1

        Run the following command to deploy the Kubernetes artifacts: 
        kubectl apply -f /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample1/kubernetes

        Run the following command to install the application using Helm: 
        helm install --name hello-world-k8s-deployment /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample1/kubernetes/hello-world-k8s-deployment
```

2. hello_world_k8s.jar, Dockerfile, docker image and kubernetes artifacts will be generated: 
```bash
$> tree
.
├── README.md
├── hello_world_k8s.bal
├── hello_world_k8s.jar
├── docker
    └── Dockerfile
└── kubernetes
    ├── hello-world-k8s-deployment
    │   ├── Chart.yaml
    │   └── templates
    │       └── hello_world_k8s_deployment.yaml
    └── hello_world_k8s.yaml
```

3. Verify the docker image is created:
```bash
$> docker images
REPOSITORY             TAG                 IMAGE ID            CREATED             SIZE
hello_world_k8s       latest              df83ae43f69b        2 minutes ago        103MB

```

4. Run kubectl command to deploy artifacts (Use the command printed on screen in step 1):
```bash
$> kubectl apply -f /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample1/kubernetes
service/helloworld-svc created
deployment.apps/hello-world-k8s-deployment created
```

5. Verify kubernetes deployment,service and ingress is running:
```bash
$> kubectl get pods
NAME                                         READY     STATUS    RESTARTS   AGE
hello-world-k8s-deployment-bf8f98c7c-twwf9   1/1       Running   0          0s

$> kubectl get svc
NAME                    TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
helloworldep           NodePort    10.96.118.214    <none>        9090:32001/TCP   1m

```

6. Access the hello world service with curl command:

- **Using node port:**

Note that the node port is derived from `kubectl get svc` output.
```bash
$> curl http://localhost:32001/helloWorld/sayHello
Hello, World from service helloWorld !
```


7. Undeploy sample:
```bash
$> kubectl delete -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample1/kubernetes/
deployment "hello-world-k8s-deployment" deleted
service "helloworldep" deleted

$> docker rmi hello_world_k8s
```
