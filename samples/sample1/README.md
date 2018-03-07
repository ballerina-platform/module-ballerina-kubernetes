## Sample1: Kubernetes Hello World

- This sample runs simple ballerina hello world service in kubernetes cluster with minimal configurations. 
- The service is annotated with @kubernetes:svc{}, @kubernetes:ingress{} and without passing any parameters. 
- Note that the @kubernetes:deployment{} is optional.
- Default values for kubernetes annotation attributes will be used to create artifacts.
- Following files will be generated from this sample.
    ``` 
    $> docker image
    hello-world-k8s:latest
    
    $> tree
       .
       ├── hello-world-k8s.balx
       └── target
           └── hello-world-k8s
               └── kubernetes
                   ├── docker
                   │   └── Dockerfile
                   ├── hello-world-k8s-deployment.yaml
                   ├── helloWorld-ingress.yaml
                   └── helloWorld-svc.yaml

    ```
### How to run:

1. Compile the  hello-world-k8s.bal file. Command to run kubernetes artifacts will be printed on success:
```bash
$> ballerina build hello-world-k8s.bal

Run following command to deploy kubernetes artifacts: 
kubectl create -f ./target/hello-world-k8s/kubernetes
```

2. hello-world-k8s.balx, Dockerfile, docker image and kubernetes artifacts will be generated: 
```bash
$> tree
.
├── hello-world-k8s.balx
└── target
    └── hello-world-k8s
        └── kubernetes
            ├── docker
            │   └── Dockerfile
            ├── hello-world-k8s-deployment.yaml
            ├── helloWorld-ingress.yaml
            └── helloWorld-svc.yaml
```

3. Verify the docker image is created:
```bash
$> docker images
REPOSITORY             TAG                 IMAGE ID            CREATED             SIZE
hello-world-k8s       latest              df83ae43f69b        2 minutes ago        102MB

```

4. Run kubectl command to deploy artifacts (Use the command printed on screen in step 1):
```bash
$> kubectl create -f /Users/anuruddha/Repos/ballerina-k8s-demo/kubernetes/sample1/target/hello-world-k8s/kubernetes
horizontalpodautoscaler "helloworld" created
ingress "helloworld" created
service "helloworld" created
```

5. Verify kubernetes deployment,service and ingress is running:
```bash
$> kubectl get pods
NAME                                         READY     STATUS    RESTARTS   AGE
hello-world-k8s-deployment-bf8f98c7c-twwf9   1/1       Running   0          0s

$> kubectl get svc
NAME         TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
helloworld   NodePort    10.96.118.214   <none>        9090:32045/TCP   1m

$> kubectl get ingress
NAME         HOSTS            ADDRESS   PORTS     AGE
helloworld   helloworld.com             80, 443   1m
```

6. Access the hello world service with curl command:

- **Using node port:**

Note that the node port is derived from `kubectl get svc` output.
```bash
$> curl http://localhost:32045/HelloWorld/sayHello
Hello, World from service helloWorld !
```

- **Using ingress:**

Add /etc/host entry to match hostname.
 ```
 127.0.0.1 helloworld.com
 ```
Use curl command with hostname to access the service.
```bash
$> curl http://helloworld.com/HelloWorld/sayHello
Hello, World from service helloWorld !
```

7. Undeploy sample:
```bash
$> kubectl delete -f ./target/hello-world-k8s/kubernetes
deployment "hello-world-k8s-deployment" deleted
ingress "helloworld" deleted
service "helloworld" deleted

```
