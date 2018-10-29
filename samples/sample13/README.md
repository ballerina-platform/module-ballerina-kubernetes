## Sample13: Ballerina modules with dependencies

- This sample runs foodstore with dependsOn annotation.   
- The foodstore application will wait until pizza_api & burger_api gets deployed.
- Following files will be generated from this sample.
    ``` 
    $> docker images
    hot_drink        :latest 
    cool_drink       :latest 
    drink_store    :latest
    
    $> tree
    target
    ├── Ballerina.lock
    ├── cool_drink.balx
    ├── drink_store.balx
    ├── hot_drink.balx
    └── kubernetes
        ├── cool_drink
        │   ├── cool_drink.yaml
        │   └── docker
        │       ├── Dockerfile
        │       └── mysql-connector-java-8.0.11.jar
        ├── drink_store
        │   ├── docker
        │   │   └── Dockerfile
        │   └── drink_store.yaml
        └── hot_drink
            ├── docker
            │   ├── Dockerfile
            │   └── mysql-connector-java-8.0.11.jar
            └── hot_drink.yaml
  
    ```
### How to run:
1. Go to `resource/docker` folder and run the build.sh file. This will generate two databases deployment in mysql namespace.
Verify pods and services are created. 

```bash
$> ./build.sh
Sending build context to Docker daemon  3.584kB
Step 1/2 : FROM mysql:5.7.22
 ---> 66bc0f66b7af
Step 2/2 : COPY script.sql /docker-entrypoint-initdb.d/
 ---> Using cache
 ---> 08789a90af68
Successfully built 08789a90af68
Successfully tagged hotdrink_mysql_db:1.0.0
Sending build context to Docker daemon  3.584kB
Step 1/2 : FROM mysql:5.7.22
 ---> 66bc0f66b7af
Step 2/2 : COPY script.sql /docker-entrypoint-initdb.d/
 ---> Using cache
 ---> ca5db463028b
Successfully built ca5db463028b
Successfully tagged cooldrink_mysql_db:1.0.0
namespace "mysql" created
deployment.extensions "hotdrink-mysql-deployment" created
service "hotdrink-mysql" created
deployment.extensions "cooldrink-mysql-deployment" created
service "cooldrink-mysql" created

$> kubectl get pods -n mysql
NAME                                          READY     STATUS    RESTARTS   AGE
cooldrink-mysql-deployment-587c466765-zs9ll   1/1       Running   0          39s
hotdrink-mysql-deployment-77869db489-c8lcf    1/1       Running   0          39s
```

1. Navigate to sample13 folder and Initialize ballerina project.
```bash
sample13$> ballerina init
Ballerina project initialized
```

1. Compile the project. Commands to run kubernetes artifacts will be printed on success:
```bash
$> ballerina build 
Compiling source
    anuruddha/cool_drink:0.0.1
    anuruddha/drink_store:0.0.1
    anuruddha/hot_drink:0.0.1

Compiling tests
    anuruddha/cool_drink:0.0.1
    anuruddha/drink_store:0.0.1
    anuruddha/hot_drink:0.0.1

Running tests
    anuruddha/cool_drink:0.0.1
	No tests found

    anuruddha/drink_store:0.0.1
	No tests found

    anuruddha/hot_drink:0.0.1
	No tests found

Generating executables
    ./target/cool_drink.balx
	@kubernetes:Service 			 - complete 1/1
	@kubernetes:ConfigMap 			 - complete 1/1
	@kubernetes:Deployment 			 - complete 1/1
	@kubernetes:Docker 			 - complete 3/3

	Run following command to deploy kubernetes artifacts:
	kubectl apply -f /Users/anuruddha/workspace/ballerinax/kubernetes/samples/sample13/target/kubernetes/cool_drink

    ./target/drink_store.balx
	@kubernetes:Service 			 - complete 1/1
	@kubernetes:Ingress 			 - complete 1/1
	@kubernetes:Secret 			 - complete 1/1
	@kubernetes:Deployment 			 - complete 1/1
	@kubernetes:HPA 			 - complete 1/1
	@kubernetes:Docker 			 - complete 3/3

	Run following command to deploy kubernetes artifacts:
	kubectl apply -f /Users/anuruddha/workspace/ballerinax/kubernetes/samples/sample13/target/kubernetes/drink_store

    ./target/hot_drink.balx
	@kubernetes:Service 			 - complete 1/1
	@kubernetes:ConfigMap 			 - complete 1/1
	@kubernetes:Deployment 			 - complete 1/1
	@kubernetes:Docker 			 - complete 3/3

	Run following command to deploy kubernetes artifacts:
	kubectl apply -f /Users/anuruddha/workspace/ballerinax/kubernetes/samples/sample13/target/kubernetes/hot_drink
```

2. .balx files, Dockerfile, docker image and kubernetes artifacts will be generated: 
```bash
$> tree
target/
     ├── cool_drink.balx
     ├── drink_store.balx
     ├── hot_drink.balx
     └── kubernetes
         ├── cool_drink
         │   ├── cool_drink.yaml
         │   └── docker
         │       ├── Dockerfile
         │       └── mysql-connector-java-8.0.11.jar
         ├── drink_store
         │   ├── docker
         │   │   └── Dockerfile
         │   └── drink_store.yaml
         └── hot_drink
             ├── docker
             │   ├── Dockerfile
             │   └── mysql-connector-java-8.0.11.jar
             └── hot_drink.yaml
```

3. Verify the docker image is created:
```bash
$> docker images
REPOSITORY       TAG                 IMAGE ID            CREATED             SIZE
hot_drink        latest              62792bafcd4c        About a minute ago   127MB
drink_store      latest              20099ea12ff5        About a minute ago   127MB
cool_drink       latest              31d58eaa27fa        About a minute ago   127MB
```

5. Run kubectl command to deploy the artifacts.
```bash
$ kubectl apply -Rf ./target/kubernetes
service "cooldrink-backend" created
configmap "cooldrinkapi-ballerina-conf-config-map" created
deployment.extensions "cool-drink-deployment" created
service "drinkstoreep-svc" created
ingress.extensions "drinkstoreep-ingress" created
secret "drinkstoreep-keystore" created
deployment.extensions "drink-store-deployment" created
horizontalpodautoscaler.autoscaling "drink-store-hpa" created
service "hotdrink-backend" created
configmap "hotdrinksapi-ballerina-conf-config-map" created
deployment.extensions "hot-drink-deployment" created
```

6. Verify kubernetes deployment,service,secrets and ingress is deployed:
```bash
$> kubectl get pods
NAME                                      READY     STATUS    RESTARTS   AGE
cool-drink-deployment-57f78bb594-cshs8    1/1       Running   0          30s
cool-drink-deployment-57f78bb594-rs24q    1/1       Running   0          30s
drink-store-deployment-7867cd9556-kshdj   1/1       Running   0          29s
hot-drink-deployment-59d5cf9b8-6qhp5      1/1       Running   0          29s


$> kubectl get svc
NAME                TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)          AGE
cooldrink-backend   ClusterIP   10.96.56.143     <none>        9090/TCP         47s
drinkstoreep-svc    NodePort    10.98.3.100      <none>        9091:30718/TCP   47s
hotdrink-backend    ClusterIP   10.105.105.217   <none>        9090/TCP         46s
kubernetes          ClusterIP   10.96.0.1        <none>        443/TCP          3d

$> kubectl get ingress
NAME                  HOSTS           ADDRESS   PORTS     AGE
foodstoreep-ingress   drinkstore.com             80        40s
```

7. Access the food store with curl command:

- **Using ingress:**
Add /etc/hosts entry to match hostname. 
_(127.0.0.1 is only applicable to docker for mac users. Other users should map the hostname with correct ip address 
from `kubectl get ingress` command.)_
```bash
 127.0.0.1 drinkstore.com
```
```bash

$>curl https://drinkstore.com/store/getTempreature -k
Tempreture in San Francisco: 17.56 clecius. Sunny.

$>curl https://drinkstore.com/store/coolDrink -k
[{"id":1,"name":"Lime Soda","description":"Sparkling Soda with Lime","price":11.76,"diff":"+1.76"},{"id":2,"name":"Mango Juice","description":"Fresh Mango Juice with milk","price":17.63,"diff":"+2.63"},{"id":3,"name":"Mojito","description":"White rum, sugar, lime juice, soda water, and mint. ","price":23.51,"diff":"+3.51"}]


$> curl https://drinkstore.com/store/hotDrink -k
[{"id":1,"name":"Espresso","description":"1 Shot of espresso in an espresso cup","price":4.12,"diff":"-0.88"},{"id":2,"name":"Cappuccino","description":"Steamed milk, micro-foam & Sprinkle chocolate on top of the coffee","price":4.95,"diff":"-1.05"},{"id":3,"name":"Flat White","description":"espresso & steamed milk","price":2.47,"diff":"-0.53"}]
```

8. Undeploy sample:
```bash
$> kubectl delete -Rf target/kubernetes
$> kubectl delete namespace mysql
```
