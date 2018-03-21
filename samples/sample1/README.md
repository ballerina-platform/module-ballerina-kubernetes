## Sample1: Kubernetes Hello World

- This sample runs simple ballerina hello world service in kubernetes cluster with minimal configurations. 
- The endpoint is annotated with @kubernetes:SVC{} and without passing serviceType as NodePort. 
- Note that the @kubernetes:Deployment{} is optional.
- Default values for kubernetes annotation attributes will be used to create artifacts.
- Following files will be generated from this sample.
    ``` 
    $> docker image
    hello_world_k8s:latest
    
    $> tree
    ├── README.md
    ├── hello_world_k8s.bal
    ├── hello_world_k8s.balx
    └── kubernetes
        ├── docker
        │   └── Dockerfile
        ├── hello_world_k8s_svc.yaml
        └── hello_world_k8s_deployment.yaml
    ```
### How to run:

1. Compile the  hello_world_k8s.bal file. Command to run kubernetes artifacts will be printed on success:
```bash
$> ballerina build hello_world_k8s.bal

@docker                     - complete 3/3
@kubernetes:Deployment      - complete 1/1
@kubernetes:Service         - complete 1/1

Run following command to deploy kubernetes artifacts: 
kubectl apply -f /Users/lakmal/ballerina/kubernetes/samples/sample1/kubernetes/
```

2. hello_world_k8s.balx, Dockerfile, docker image and kubernetes artifacts will be generated: 
```bash
$> tree
.
├── README.md
├── hello_world_k8s.bal
├── hello_world_k8s.balx
└── kubernetes
    ├── docker
    │   └── Dockerfile
    ├── hello_world_k8s_svc.yaml
    └── hello_world_k8s_deployment.yaml
```

3. Verify the docker image is created:
```bash
$> docker images
REPOSITORY             TAG                 IMAGE ID            CREATED             SIZE
hello_world_k8s       latest              df83ae43f69b        2 minutes ago        103MB

```

4. Run kubectl command to deploy artifacts (Use the command printed on screen in step 1):
```bash
$> kubectl apply -f /Users/lakmal/ballerina/kubernetes/samples/sample1/kubernetes/
service "helloworldep" created
deployment "hello-world-k8s-deployment" created
```

5. Verify kubernetes deployment,service and ingress is running:
```bash
$> kubectl get pods
NAME                                         READY     STATUS    RESTARTS   AGE
hello-world-k8s-deployment-bf8f98c7c-twwf9   1/1       Running   0          0s

$> kubectl get svc
NAME                    TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
helloworldep           NodePort    10.96.118.214    <none>        9090:32045/TCP   1m

```

6. Access the hello world service with curl command:

- **Using node port:**

Note that the node port is derived from `kubectl get svc` output.
```bash
$> curl http://localhost:32045/HelloWorld/sayHello
Hello, World from service helloWorld !
```


7. Undeploy sample:
```bash
$> kubectl delete -f /Users/lakmal/ballerina/kubernetes/samples/sample1/kubernetes/
deployment "hello-world-k8s-deployment" deleted
service "helloworldep" deleted

```
