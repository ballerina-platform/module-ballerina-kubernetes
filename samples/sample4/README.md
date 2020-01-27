## Sample4: Kubernetes SSL

- This sample runs simple ballerina hello world service in kubernetes cluster with https. Keystore will 
  automatically passed into relevant POD by using k8s secret volume mount.
- The endpoint is annotated with @kubernetes:Service{} and without passing serviceType as NodePort. 
- Note that the @kubernetes:Deployment{} is optional.
- Default values for kubernetes annotation attributes will be used to create artifacts.
- Following files will be generated from this sample.
    ``` 
    $> docker image
    hello_world_ssl_k8s:latest
    
    $> tree
    ├── README.md
    ├── hello_world_ssl_k8s.bal
    ├── hello_world_ssl_k8s.jar
    ├── security
        └── ballerinaKeystore.p12
    ├── docker
        └── Dockerfile
    └── kubernetes
        ├── hello-world-ssl-k8s-deployment
        │   ├── Chart.yaml
        │   └── templates
        │       └── hello_world_ssl_k8s.yaml
        └── hello_world_ssl_k8s.yaml
    ```
### How to run:

1. Compile the  hello_world_ssl_k8s.bal file. Command to deploy kubernetes artifacts will be printed on build success.
```bash
$> ballerina build hello_world_ssl_k8s.bal
Compiling source
        hello_world_ssl_k8s.bal

Generating executables
        hello_world_ssl_k8s.jar

Generating artifacts...

        @kubernetes:Service                      - complete 1/1
        @kubernetes:Ingress                      - complete 1/1
        @kubernetes:Secret                       - complete 1/1
        @kubernetes:Deployment                   - complete 1/1
        @kubernetes:Docker                       - complete 2/2 
        @kubernetes:Helm                         - complete 1/1

        Run the following command to deploy the Kubernetes artifacts: 
        kubectl apply -f /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample4/kubernetes

        Run the following command to install the application using Helm: 
        helm install --name hello-world-ssl-k8s-deployment /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample4/kubernetes/hello-world-ssl-k8s-deployment
```

2. hello_world_ssl_k8s.jar, Dockerfile, docker image and kubernetes artifacts will be generated: 
```bash
$> tree
.
├── README.md
    ├── hello_world_ssl_k8s.bal
    ├── hello_world_ssl_k8s.jar
    ├── security
        └── ballerinaKeystore.p12
    ├── docker
        └── Dockerfile
    └── kubernetes
        ├── hello-world-ssl-k8s-deployment
        │   ├── Chart.yaml
        │   └── templates
        │       └── hello_world_ssl_k8s.yaml
        └── hello_world_ssl_k8s.yaml
```

3. Verify the docker image is created:
```bash
$> docker images
REPOSITORY             TAG                 IMAGE ID            CREATED             SIZE
hello_world_ssl_k8s       latest              df83ae43f69b        2 minutes ago        103MB

```

4. Run kubectl command to deploy artifacts (Use the command printed on screen in step 1):
```bash
$> kubectl apply -f /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample4/kubernetes
service/helloworldsecuredep-svc created
ingress.extensions/helloworldsecuredep-ingress created
secret/helloworldsecuredep-keystore created
deployment.apps/hello-world-ssl-k8s-deployment created
```

5. Verify kubernetes deployment,service,secret and ingress is deployed:
```bash
$> kubectl get pods
NAME                                         READY     STATUS    RESTARTS   AGE
hello-world-k8s-deployment-bf8f98c7c-twwf9   1/1       Running   0          0s

$> kubectl get svc
NAME                    TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
helloworldep           ClusterIP    10.96.118.214    <none>        9090/TCP         1m

$> kubectl get secret
NAME                           TYPE                                  DATA      AGE
helloworldsecuredep-keystore   Opaque                                1         21m
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
$> curl https://abc.com/helloWorld/sayHello -k
Hello, World from secured service !
```

7. Undeploy sample:
```bash
$> kubectl delete -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample4/kubernetes/
$> docker rmi hello_world_ssl_k8s

```
