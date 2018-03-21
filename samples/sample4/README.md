## Sample4: Ballerina program with multiple services running in multiple ports

- This sample creates 2 ballerina services in kubernetes.
- This program has two services with two endpoints
- The ingress is configured so that two APIs can be accessed as following.
    http://pizza.com/pizzastore/pizza/menu
    http://burger.com/menu
- Following artifacts will be generated from this sample.
    ``` 
    $> docker image
    foodstore:latest 
    
    $> tree
        ├── README.md
        ├── foodstore.bal
        ├── foodstore.balx
        └── kubernetes
            ├── docker
            │   └── Dockerfile
            ├── foodstore_deployment.yaml
            ├── foodstore_ingress.yaml
            └── foodstore_svc.yaml
    ```
### How to run:

1. Compile the  foodstore.bal file. Command to run kubernetes artifacts will be printed on success:
```bash
$> ballerina build foodstore.bal
@docker 			 - complete 3/3 
@kubernetes:Deployment 		 - complete 1/1
@kubernetes:Service 		 - complete 2/2
@kubernetes:Ingress 		 - complete 2/2
Run following command to deploy kubernetes artifacts: 
kubectl apply -f /Users/lakmal/ballerina/kubernetes/samples/sample4/kubernetes/
```

2. foodstore.balx, Dockerfile, docker image and kubernetes artifacts will be generated: 
```bash
$> tree
    ├── README.md
    ├── foodstore.bal
    ├── foodstore.balx
    └── kubernetes
        ├── docker
        │   └── Dockerfile
        ├── foodstore_deployment.yaml
        ├── foodstore_ingress.yaml
        └── foodstore_svc.yaml
```

3. Verify the docker image is created:
```bash
$> docker images
REPOSITORY                   TAG                 IMAGE ID            CREATED             SIZE
foodstore                   latest              df83ae43f69b        2 minutes ago        102MB

```

4. Run kubectl command to deploy artifacts (Use the command printed on screen in step 1):
```bash
$> kubectl apply -f /Users/lakmal/ballerina/kubernetes/samples/sample4/kubernetes/
deployment "foodstore" created
ingress "burgerapi-ingress" created
ingress "pizzaapi-ingress" created
service "pizzaep-svc" created
service "burgerep-svc" created
```

5. Verify kubernetes deployment,service and ingress is running (3 pods create as replica count is 3):
```bash
$> kubectl get pods
NAME                                    READY     STATUS    RESTARTS   AGE
foodstore-7d45cf99bd-8jw75   1/1       Running   0          22m
foodstore-7d45cf99bd-t9nnc   1/1       Running   0          22m
foodstore-7d45cf99bd-x8fp6   1/1       Running   0          22m



$> kubectl get svc
NAME                                              TYPE           CLUSTER-IP       EXTERNAL-IP   PORT(S)                      AGE
burgerep-svc                                      ClusterIP      10.96.62.142     <none>        9096/TCP                     27s
pizzaep-svc                                       ClusterIP      10.100.27.253    <none>        9099/TCP                     27s




$> kubectl get ingress
NAME                HOSTS        ADDRESS   PORTS     AGE
burgerapi-ingress   burger.com             80, 443   1m
pizzaapi-ingress    pizza.com              80, 443   1m
```

6. Access the hello world service with curl command:

- **Using ingress**

Add /etc/host entry to match hostname. 
_(127.0.0.1 is only applicable to docker for mac users. Other users should map the hostname with correct ip address 
from `kubectl get ingress` command.)_
 ```
 127.0.0.1 burger.com
 127.0.0.1 pizza.com
 ```
Use curl command with hostname to access the service.
```bash
$> curl http://pizza.com/pizzastore/pizza/menu
Get pizza menu

$>curl http://burger.com/menu
Get burger menu
```

7. Undeploy sample:
```bash
$> kubectl delete -f /Users/lakmal/ballerina/kubernetes/samples/sample4/kubernetes/
```
