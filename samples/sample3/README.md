## Sample3: Ballerina program with multiple services with different ports

- This sample deploy ballerina program 2 services with two base paths. (/bureger,/pizza)
- The ingress is configured so that two APIs can be accessed as following.
    http://pizzashack.com/customer
    http://order.com/orders
- Following artifacts will be generated from this sample.
    ``` 
    $> docker image
    ballerina.com/pizzashack:2.1.0 
    
    $> tree
        .
        ├── pizzashack.balx
        └── target
            └── pizzashack
                └── kubernetes
                    ├── Customer-ingress.yaml
                    ├── Customer-svc.yaml
                    ├── Order-ingress.yaml
                    ├── Order-svc.yaml
                    ├── docker
                    │   └── Dockerfile
                    └── pizzashack-deployment.yaml


    ```
### How to run:

1. Compile the  pizzashack.bal file. Command to run kubernetes artifacts will be printed on success:
```bash
$> ballerina build pizzashack.bal

Run following command to deploy kubernetes artifacts:  
kubectl create -f ./target/pizzashack/kubernetes

```

2. pizzashack.balx, Dockerfile, docker image and kubernetes artifacts will be generated: 
```bash
$> tree
.
├── pizzashack.balx
└── target
    └── pizzashack
        └── kubernetes
            ├── Customer-ingress.yaml
            ├── Customer-svc.yaml
            ├── Order-ingress.yaml
            ├── Order-svc.yaml
            ├── docker
            │   └── Dockerfile
            └── pizzashack-deployment.yaml

```

3. Verify the docker image is created:
```bash
$> docker images
REPOSITORY                   TAG                 IMAGE ID            CREATED             SIZE
ballerina.com/pizzashack     2.1.0              df83ae43f69b        2 minutes ago        102MB

```

4. Run kubectl command to deploy artifacts (Use the command printed on screen in step 1):
```bash
$> kubectl create -f target/pizzashack/kubernetes
ingress "customer" created
service "customer" created
ingress "order" created
service "order" created
deployment "pizzashack-deployment" created

```

5. Verify kubernetes deployment,service and ingress is running:
```bash
$> kubectl get pods
NAME                                    READY     STATUS    RESTARTS   AGE
pizzashack-deployment-d6747b8b9-64n7d   1/1       Running   0          39m


$> kubectl get svc
NAME         TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)          AGE
customer     NodePort    10.103.235.191   <none>        9090:32376/TCP   39m
order        NodePort    10.98.245.0      <none>        9090:31491/TCP   39m



$> kubectl get ingress
NAME       HOSTS            ADDRESS   PORTS     AGE
customer   pizzashack.com             80, 443   39m
order      order.com                  80, 443   39m
```

6. Access the hello world service with curl command:

- **Using ingress**

Add /etc/host entry to match hostname. 
_(127.0.0.1 is only applicable to docker for mac users. Other users should map the hostname with correct ip address 
from `kubectl get ingress` command.)_
 ```
 127.0.0.1 order.com
 127.0.0.1 pizzashack.com
 ```
Use curl command with hostname to access the service.
```bash
$> curl curl http://order.com/orders
Get order resource

$>curl http://pizzashack.com/customer
Get Customer resource
```

7. Undeploy sample:
```bash
$> kubectl delete -f ./target/pizzashack/kubernetes
```
