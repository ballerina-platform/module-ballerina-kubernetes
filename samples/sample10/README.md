## Sample10: Ballerina package with kubernetes annotations

- This sample runs [foodstore](../sample4) as a package.   
- Following files will be generated from this sample.
    ``` 
    $> docker image
    food_api_pkg:latest
    
    $> tree
      └── target
          ├── food_api_pkg
          │   └── kubernetes
          │       ├── docker
          │       │   └── Dockerfile
          │       ├── food_api_pkg_deployment.yaml
          │       ├── food_api_pkg_ingress.yaml
          │       └── food_api_pkg_svc.yaml
          └── food_api_pkg.balx
  
    ```
### How to run:

1. Compile the  food_api_pkg file. Command to run kubernetes artifacts will be printed on success:
```bash
$> ballerina build food_api_pkg
@kubernetes:Service 			 - complete 2/2
@kubernetes:Ingress 			 - complete 2/2
@kubernetes:Docker 			     - complete 3/3
@kubernetes:Deployment 			 - complete 1/1


Run following command to deploy kubernetes artifacts:
kubectl apply -f /Users/anuruddha/workspace/ballerinax/kubernetes/samples/sample10/target/food_api_pkg/kubernetes/
```

2. food_api_pkg.balx, Dockerfile, docker image and kubernetes artifacts will be generated: 
```bash
$> tree
.
├── Ballerina.toml
├── README.md
├── food_api_pkg
│   ├── burger_api.bal
│   └── pizza_api.bal
└── target
    ├── food_api_pkg
    │   └── kubernetes
    │       ├── docker
    │       │   └── Dockerfile
    │       ├── food_api_pkg_deployment.yaml
    │       ├── food_api_pkg_ingress.yaml
    │       └── food_api_pkg_svc.yaml
    └── food_api_pkg.balx

```

3. Verify the docker image is created:
```bash
$> docker images
REPOSITORY                    TAG                       IMAGE ID            CREATED              SIZE
food_api_pkg                 latest                    dacc0a8cff85        About a minute ago   122MB
```

4. Create sample kubernetes volume using following command.
 ```bash
kubectl create -f ./volumes/persistent-volume.yaml
```

5. Run kubectl command to deploy artifacts (Use the command printed on screen in step 1):
```bash
$>  kubectl apply -f /Users/anuruddha/workspace/ballerinax/kubernetes/samples/sample10/target/food_api_pkg/kubernetes/
deployment "foodstore" created
ingress "pizzaapi-ingress" created
ingress "burgerapi-ingress" created
service "pizzaep-svc" created
service "burgerep-svc" created
```

6. Verify kubernetes deployment,service,secrets and ingress is deployed:
```bash
$> kubectl get pods
NAME                       READY     STATUS    RESTARTS   AGE
foodstore-5fd78b97-6jw5b   1/1       Running   0          29s
foodstore-5fd78b97-9dwp6   1/1       Running   0          29s
foodstore-5fd78b97-knkh8   1/1       Running   0          29s

$> kubectl get svc
NAME           TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)    AGE
burgerep-svc   ClusterIP   10.107.127.86   <none>        9096/TCP   45s
pizzaep-svc    ClusterIP   10.96.214.133   <none>        9099/TCP   45s

$> kubectl get ingress
NAME                HOSTS        ADDRESS   PORTS     AGE
burgerapi-ingress   burger.com             80, 443   1m
pizzaapi-ingress    pizza.com              80, 443   1m

```

7. Access the hello world service with curl command:

- **Using ingress:**
Add /etc/host entry to match hostname. 
_(127.0.0.1 is only applicable to docker for mac users. Other users should map the hostname with correct ip address 
from `kubectl get ingress` command.)_

```bash
$> curl https://pizza.com/menu -k
Pizza menu

$>curl https://burger.com/menu -k
Burger menu
```

8. Undeploy sample:
```bash
$> kubectl delete -f /Users/anuruddha/Repos/ballerinax/kubernetes/samples/sample9/kubernetes/

```
