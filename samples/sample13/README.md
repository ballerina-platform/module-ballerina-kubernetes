## Sample13: Ballerina packages with dependencies

- This sample runs foodstore with dependsOn annotation.   
- The foodstore application will wait until pizza_api & burger_api gets deployed.
- Following files will be generated from this sample.
    ``` 
    $> docker images
    pizza        :latest 
    burger       :latest 
    foodstore    :latest
    
    $> tree
      target/
      ├── Ballerina.lock
      ├── burger.balx
      ├── foodstore.balx
      ├── kubernetes
      │   ├── burger
      │   │   ├── burger_deployment.yaml
      │   │   ├── burger_svc.yaml
      │   │   └── docker
      │   │       └── Dockerfile
      │   ├── foodstore
      │   │   ├── docker
      │   │   │   └── Dockerfile
      │   │   ├── foodstore_deployment.yaml
      │   │   ├── foodstore_ingress.yaml
      │   │   └── foodstore_svc.yaml
      │   └── pizza
      │       ├── docker
      │       │   └── Dockerfile
      │       ├── pizza_deployment.yaml
      │       └── pizza_svc.yaml
      └── pizza.balx
  
    ```
### How to run:

1. Initialize ballerina project.
```bash
sample13$> ballerina init
Ballerina project initialized
```

1. Compile the project. Commands to run kubernetes artifacts will be printed on success:
```bash
$> ballerina build 
Compiling source
    anuruddha/foodstore:0.0.1
    anuruddha/burger:0.0.1
    anuruddha/pizza:0.0.1

Compiling tests
    anuruddha/foodstore:0.0.1
    anuruddha/burger:0.0.1
    anuruddha/pizza:0.0.1

Running tests
    anuruddha/burger:0.0.1
	No tests found

    anuruddha/foodstore:0.0.1
	No tests found

    anuruddha/pizza:0.0.1
	No tests found

Generating executables
    ./target/foodstore.balx
	@kubernetes:Service 			 - complete 1/1
	@kubernetes:Ingress 			 - complete 1/1
	@kubernetes:Deployment 			 - complete 1/1
	@kubernetes:Docker 			 - complete 3/3

	Run following command to deploy kubernetes artifacts:
	kubectl apply -f /Users/anuruddha/workspace/ballerinax/kubernetes/samples/sample14/target/kubernetes/foodstore

    ./target/burger.balx
	@kubernetes:Service 			 - complete 1/1
	@kubernetes:Deployment 			 - complete 1/1
	@kubernetes:Docker 			 - complete 3/3

	Run following command to deploy kubernetes artifacts:
	kubectl apply -f /Users/anuruddha/workspace/ballerinax/kubernetes/samples/sample14/target/kubernetes/burger

    ./target/pizza.balx
	@kubernetes:Service 			 - complete 1/1
	@kubernetes:Deployment 			 - complete 1/1
	@kubernetes:Docker 			 - complete 3/3

	Run following command to deploy kubernetes artifacts:
	kubectl apply -f /Users/anuruddha/workspace/ballerinax/kubernetes/samples/sample14/target/kubernetes/pizza
```

2. food_api_pkg.balx, Dockerfile, docker image and kubernetes artifacts will be generated: 
```bash
$> tree
target/
      ├── Ballerina.lock
      ├── burger.balx
      ├── foodstore.balx
      ├── kubernetes
      │   ├── burger
      │   │   ├── burger_deployment.yaml
      │   │   ├── burger_svc.yaml
      │   │   └── docker
      │   │       └── Dockerfile
      │   ├── foodstore
      │   │   ├── docker
      │   │   │   └── Dockerfile
      │   │   ├── foodstore_deployment.yaml
      │   │   ├── foodstore_ingress.yaml
      │   │   └── foodstore_svc.yaml
      │   └── pizza
      │       ├── docker
      │       │   └── Dockerfile
      │       ├── pizza_deployment.yaml
      │       └── pizza_svc.yaml
      └── pizza.balx
```

3. Verify the docker image is created:
```bash
$> docker images
REPOSITORY       TAG                 IMAGE ID            CREATED             SIZE
pizza           latest              62792bafcd4c        About a minute ago   127MB
burger          latest              20099ea12ff5        About a minute ago   127MB
foodstore       latest              31d58eaa27fa        About a minute ago   127MB
```

5. Run kubectl command to deploy the artifacts.
```bash
$ kubectl apply -Rf ./target/kubernetes
deployment.extensions "burger-deployment" created
service "buger-backend" created
deployment.extensions "foodstore-deployment" created
ingress.extensions "pizzaep-ingress" created
service "pizzaep-svc" created
deployment.extensions "pizza-deployment" created
service "pizza-backend" created
```

6. Verify kubernetes deployment,service,secrets and ingress is deployed:
```bash
$> kubectl get pods
NAME                                    READY     STATUS    RESTARTS   AGE
burger-deployment-5464d4d896-44xbn      1/1       Running   0          2m
foodstore-deployment-5674f659c5-8vk2k   1/1       Running   0          2m
pizza-deployment-8487d4dc6-4hsww        1/1       Running   0          2m
pizza-deployment-8487d4dc6-ckgf7        1/1       Running   0          2m
pizza-deployment-8487d4dc6-vhsbz        1/1       Running   0          2m


$> kubectl get svc
NAME              TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
buger-backend     ClusterIP   10.104.40.52    <none>        9090/TCP         22s
foodstoreep-svc   NodePort    10.111.120.8    <none>        9090:31184/TCP   22s
pizza-backend     ClusterIP   10.108.25.125   <none>        9090/TCP         22s

$> kubectl get ingress
NAME                  HOSTS           ADDRESS   PORTS     AGE
foodstoreep-ingress   foodstore.com             80        40s
```

7. Access the food store with curl command:

- **Using ingress:**
Add /etc/hosts entry to match hostname. 
_(127.0.0.1 is only applicable to docker for mac users. Other users should map the hostname with correct ip address 
from `kubectl get ingress` command.)_
```bash
 127.0.0.1 foodstore.com
```
```bash
$> curl http://foodstore.com/pizza/menu
Pizza menu

$> curl http://foodstore.com/burger/menu
Burger menu
```

8. Undeploy sample:
```bash
$> kubectl delete -Rf target/kubernetes/pizza
```
