## Sample5: Ballerina service with http and https endpoint

- This sample deploy ballerina program with http and https urls.
- The ingress is configured so that two APIs can be accessed as following.
    http://internal.pizzashack.com/customer
    https://pizzashack.com/customer
- Following artifacts will be generated from this sample.
    ``` 
    $> docker image
    ballerina.com/pizzashack:2.1.0 
    
    $> tree
    ├── README.md
    ├── docker
        └── Dockerfile
    ├── security
        └── ballerinaKeystore.p12
    ├── kubernetes
    │   ├── pizzashack-deployment
    │   │   ├── Chart.yaml
    │   │   └── templates
    │   │       └── pizzashack.yaml
    │   └── pizzashack.yaml
    ├── pizzashack.bal
    └── pizzashack.jar
    ```
### How to run:

1. Compile the  pizzashack.bal file. Command to run kubernetes artifacts will be printed on success:
```bash
$> ballerina build pizzashack.bal
Compiling source
        pizzashack.bal

Generating executables
        pizzashack.jar

Generating artifacts...

        @kubernetes:Service                      - complete 1/2
        @kubernetes:Service                      - complete 2/2
        @kubernetes:Ingress                      - complete 2/2
        @kubernetes:Secret                       - complete 1/1
        @kubernetes:Deployment                   - complete 1/1
        @kubernetes:HPA                          - complete 1/1
        @kubernetes:Docker                       - complete 2/2 
        @kubernetes:Helm                         - complete 1/1

        Run the following command to deploy the Kubernetes artifacts: 
        kubectl apply -f /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample5/kubernetes

        Run the following command to install the application using Helm: 
        helm install --name pizzashack-deployment /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample5/kubernetes/pizzashack-deployment

```

2. pizzashack.jar, Dockerfile, docker image and kubernetes artifacts will be generated: 
```bash
$> tree
.
├── README.md
├── docker
    └── Dockerfile
├── security
    └── ballerinaKeystore.p12
├── kubernetes
│   ├── pizzashack-deployment
│   │   ├── Chart.yaml
│   │   └── templates
│   │       └── pizzashack.yaml
│   └── pizzashack.yaml
├── pizzashack.bal
└── pizzashack.jar
```

3. Verify the docker image is created:
```bash
$> docker images
REPOSITORY                   TAG                 IMAGE ID            CREATED             SIZE
ballerina.com/pizzashack     2.1.0              df83ae43f69b        2 minutes ago        102MB

```

4. Run kubectl command to deploy artifacts (Use the command printed on screen in step 1):
```bash
$> kubectl apply -f /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample5/kubernetes
service/pizzaepsecured-svc created
service/pizzaep-svc created
ingress.extensions/pizzaep-ingress created
ingress.extensions/pizzaepsecured-ingress created
secret/pizzaepsecured-keystore created
deployment.apps/pizzashack-deployment created
horizontalpodautoscaler.autoscaling/pizzashack-hpa created

```

5. Verify kubernetes deployment,service and ingress is running:
```bash
$> kubectl get pods
NAME                                    READY     STATUS    RESTARTS   AGE
pizzashack-deployment-d6747b8b9-64n7d   1/1       Running   0          39m


$> kubectl get svc
NAME                 TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)    AGE
pizzaep-svc          ClusterIP   10.108.63.96    <none>        9090/TCP   59s
pizzaepsecured-svc   ClusterIP   10.101.253.24   <none>        9095/TCP   59s



$> kubectl get ingress
NAME                     HOSTS                     ADDRESS   PORTS     AGE
pizzaep-ingress          internal.pizzashack.com             80        1m
pizzaepsecured-ingress   pizzashack.com                      80, 443   1m
```

6. Access the hello world service with curl command:

- **Using ingress**

Add /etc/hosts entry to match hostname. 
_(127.0.0.1 is only applicable to docker for mac users. Other users should map the hostname with correct ip address 
from `kubectl get ingress` command.)_
 ```
 127.0.0.1 internal.pizzashack.com
 127.0.0.1 pizzashack.com
 ```
Use curl command with hostname to access the service.
```bash
$> curl http://internal.pizzashack.com/customer
Get Customer resource !!!!

$> curl https://pizzashack.com/customer -k
Get Customer resource !!!!
```

7. Undeploy sample:
```bash
$> kubectl delete -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample5/kubernetes/
$> docker rmi ballerina.com/pizzashack:2.1.0
```