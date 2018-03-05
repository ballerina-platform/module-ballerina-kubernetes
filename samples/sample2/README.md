## Sample2: Kubernetes Hello World with liveness and hostname mapping

- This sample runs simple ballerina hello world service in kubernetes cluster with liveness probe and  hostname
 mapping for ingress. 
- Following files will be generated from this sample.
    ``` 
    $> docker image
    hello-world-k8s:latest
    
    $> tree
        .
        ├── hello-world-k8s-config.balx
        └── target
            └── hello-world-k8s-config
                └── kubernetes
                    ├── docker
                    │   └── Dockerfile
                    ├── hello-world-k8s-config-deployment.yaml
                    ├── helloWorld-ingress.yaml
                    └── helloWorld-svc.yaml

    ```
### How to run:

1. Compile the  hello-world-k8s-config.bal file. Command to run kubernetes artifacts will be printed on success:
```bash
$> ballerina build hello-world-k8s.bal

Run following command to deploy kubernetes artifacts:  
kubectl create -f ./target/hello-world-k8s-config/kubernetes

```

2. hello-world-k8s-config.balx, Dockerfile, docker image and kubernetes artifacts will be generated: 
```bash
$> tree
.
├── hello-world-k8s-config.balx
└── target
    └── hello-world-k8s-config
        └── kubernetes
            ├── docker
            │   └── Dockerfile
            ├── hello-world-k8s-config-deployment.yaml
            ├── helloWorld-ingress.yaml
            └── helloWorld-svc.yaml
```

3. Verify the docker image is created:
```bash
$> docker images
REPOSITORY                   TAG                 IMAGE ID            CREATED             SIZE
hello-world-k8s-config      latest              df83ae43f69b        2 minutes ago        102MB

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
NAME                                                READY     STATUS    RESTARTS   AGE
hello-world-k8s-config-deployment-684965455d-5622z   1/1       Running   0          4s


$> kubectl get svc
NAME         TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
helloworld   NodePort    10.104.207.65   <none>        9090:30778/TCP   23s


$> kubectl get ingress
NAME         HOSTS     ADDRESS   PORTS     AGE
helloworld   abc.com             80, 443   41s
```

6. Access the hello world service with curl command:

- **Using node port:**

Note that the node port(30778) is derived from `kubectl get svc` output.
```bash
$> curl http://localhost:30778/HelloWorld/sayHello
Hello, World from service helloWorld !
```

- **Using ingress**

Add /etc/host entry to match hostname.
_(127.0.0.1 is only applicable to docker for mac users. Other users should map the hostname with correct ip address 
from `kubectl get ingress` command.)_
 ```
 127.0.0.1 abc.com
 ```
Use curl command with hostname to access the service.
```bash
$> curl http://abc.com/HelloWorld/sayHello
Hello, World from service helloWorld !
```
7. Undeploy sample:
```bash
$> kubectl delete -f ./target/hello-world-k8s/kubernetes
```