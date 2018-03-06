## Sample4: Ballerina program with multiple services running in multiple ports

- This sample creates 2 ballerina services in kubernetes.
- This program has two services with two base paths running in two ports. (/burger,/pizza)
- The ingress is configured so that two APIs can be accessed as following.
    http://pizza.com/pizzastore/pizza/menu
    http://burger.com/menu
- Following artifacts will be generated from this sample.
    ``` 
    $> docker image
    foodstore:latest 
    
    $> tree
        ├── foodstore.balx
        └── target
            └── foodstore
                └── kubernetes
                    ├── BurgerAPI-ingress.yaml
                    ├── BurgerAPI-svc.yaml
                    ├── PizzaAPI-ingress.yaml
                    ├── PizzaAPI-svc.yaml
                    ├── docker
                    │   └── Dockerfile
                    └── foodstore-deployment.yaml
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
├── foodstore.balx
└── target
    └── foodstore
        └── kubernetes
            ├── BurgerAPI-ingress.yaml
            ├── BurgerAPI-svc.yaml
            ├── PizzaAPI-ingress.yaml
            ├── PizzaAPI-svc.yaml
            ├── docker
            │   └── Dockerfile
            └── foodstore-deployment.yaml


```

3. Verify the docker image is created:
```bash
$> docker images
REPOSITORY                   TAG                 IMAGE ID            CREATED             SIZE
foodstore                   latest              df83ae43f69b        2 minutes ago        102MB

```

4. Run kubectl command to deploy artifacts (Use the command printed on screen in step 1):
```bash
$> kubectl create -f target/foodstore/kubernetes
ingress "burgerapi" created
service "burgerapi" created
ingress "pizzaapi" created
service "pizzaapi" created
deployment "foodstore" created


```

5. Verify kubernetes deployment,service and ingress is running (3 pods create as replica count is 3):
```bash
$> kubectl get pods
NAME                                    READY     STATUS    RESTARTS   AGE
foodstore-7d45cf99bd-8jw75   1/1       Running   0          22m
foodstore-7d45cf99bd-t9nnc   1/1       Running   0          22m
foodstore-7d45cf99bd-x8fp6   1/1       Running   0          22m



$> kubectl get svc
NAME         TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)          AGE
burgerapi    NodePort    10.100.131.153   <none>        9096:30036/TCP   28m
pizzaapi     NodePort    10.100.52.163    <none>        9099:30848/TCP   28m




$> kubectl get ingress
NAME        HOSTS        ADDRESS   PORTS     AGE
burgerapi   burger.com             80, 443   28m
pizzaapi    pizza.com              80, 443   28m
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
$> kubectl delete -f ./target/foodstore/kubernetes
```
