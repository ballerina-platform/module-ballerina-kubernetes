## Sample13: Ballerina modules with dependencies

- This sample runs foodstore with dependsOn annotation.   
- The foodstore application will wait until pizza_api & burger_api gets deployed.
- Following files will be generated from this sample.
    ``` 
    $> docker images
    hot_drink:latest 
    cool_drink:latest 
    drink_store:latest
    
    $> tree
    target
    ├── docker                                             
        |   ├── cool_drink 
        |   |   ├── Dockerfile
        │   |   └── mysql-connector-java-8.0.11.jar
        |   ├── drink_store
        |   |   ├── Dockerfile
        |   └── hot_drink
        |   |   ├── Dockerfile
        │   |   └── mysql-connector-java-8.0.11.jar                                
        ├── kubernetes                                         
        |   ├── cool_drink 
        |   |   ├── cool_drink.yaml
        │   |   └── cool-drink-deployment
        │   │   │   ├── Chart.yaml
        │   │   │   └── templates                              
        │   │   │       └── cool_drink.yaml 
        |   ├── drink_store
        |   |   ├── drink_store.yaml
        │   |   └── drink-store-deployment
        │   │   │   ├── Chart.yaml
        │   │   │   └── templates                              
        │   │   │       └── drink_store.yaml 
        |   └── hot_drink
        |   |   ├── hot_drink.yaml
        │   |   └── hot-drink-deployment
        │   │   │   ├── Chart.yaml
        │   │   │   └── templates                              
        │   │   │       └── hot_drink.yaml 
        ├── balo                                               
            ├── cool_drink-2019r3-java8-0.0.1.balo
            ├── drink_store-2019r3-java8-0.0.1.balo                   
            └── hot_drink-2019r3-java8-0.0.1.balo                    
        ├── bin                                                
            ├── cool_drink.jar     
            ├── drink_store.jar                                       
            └── hot_drink.jar                                      
        └── caches                                             
            ├── bir_cache                                      
            │   ├── kathy                                       
            │   │   ├── cool_drink                                 
            │   │   |   └── 0.0.1                              
            │   │   |   |   └── cool_drink.bir
            │   │   ├── drink_store                                 
            │   │   |   └── 0.0.1                              
            │   │   |   |   └── drink_store.bir                     
            │   │   └── hot_drink                                  
            │   │   |   └── 0.0.1                              
            │   │   |   |   └── hot_drink.bir                      
            └── jar_cache                                      
                └── kathy                                       
            │   │   ├── cool_drink                                 
            │   │   |   └── 0.0.1                              
            │   │   |   |   └── kathy-cool_drink-0.0.1.jar
            │   │   ├── drink_store                                 
            │   │   |   └── 0.0.1                              
            │   │   |   |   └── kathy-drink_store-0.0.1.jar                     
            │   │   └── hot_drink                                  
            │   │   |   └── 0.0.1                              
            │   │   |   |   └── kathy-hot_drink-0.0.1.jar
  
    ```
### How to run:
1. Download [mysql-connector-java-8.0.11.jar](https://jar-download.com/artifacts/mysql/mysql-connector-java/8.0.11/source-code) and add it to a new folder named `libs` inside sample13 directory.
2. Go to `resource/docker` folder and run the build.sh file. This will generate two databases deployment in mysql namespace.
Verify pods and services are created. 

```bash
$> ./build.sh
Sending build context to Docker daemon  3.584kB
Step 1/2 : FROM mysql:5.7.22
 ---> 6bb891430fb6
Step 2/2 : COPY script.sql /docker-entrypoint-initdb.d/
 ---> Using cache
 ---> 6114e58c6b53
Successfully built 6114e58c6b53
Successfully tagged hotdrink_mysql_db:1.0.0
Sending build context to Docker daemon  3.584kB
Step 1/2 : FROM mysql:5.7.22
 ---> 6bb891430fb6
Step 2/2 : COPY script.sql /docker-entrypoint-initdb.d/
 ---> Using cache
 ---> f67a133230f3
Successfully built f67a133230f3
Successfully tagged cooldrink_mysql_db:1.0.0
namespace/mysql created
deployment.apps/hotdrink-mysql-deployment created
service/hotdrink-mysql created
deployment.apps/cooldrink-mysql-deployment created
service/cooldrink-mysql created

$> kubectl get pods -n mysql
NAME                                          READY     STATUS    RESTARTS   AGE
cooldrink-mysql-deployment-587c466765-zs9ll   1/1       Running   0          39s
hotdrink-mysql-deployment-77869db489-c8lcf    1/1       Running   0          39s
```

1. Navigate to sample13 folder and build the project. Commands to deploy kubernetes artifacts will be printed on build success.
```bash
$> ballerina build -a
Compiling source
        kathy/cool_drink:0.0.1
        kathy/drink_store:0.0.1
        kathy/hot_drink:0.0.1

Creating balos
        target/balo/cool_drink-2019r3-java8-0.0.1.balo
        target/balo/drink_store-2019r3-java8-0.0.1.balo
        target/balo/hot_drink-2019r3-java8-0.0.1.balo

Running tests
        kathy/cool_drink:0.0.1
        No tests found

        kathy/drink_store:0.0.1
        No tests found

        kathy/hot_drink:0.0.1
        No tests found


Generating executables
        target/bin/cool_drink.jar
        target/bin/drink_store.jar
        target/bin/hot_drink.jar

Generating artifacts...

        @kubernetes:Service                      - complete 1/1
        @kubernetes:ConfigMap                    - complete 1/1
        @kubernetes:Deployment                   - complete 1/1
        @kubernetes:Docker                       - complete 2/2 
        @kubernetes:Helm                         - complete 1/1

        Execute the below command to deploy the Kubernetes artifacts: 
        kubectl apply -f /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample13/target/kubernetes/cool_drink

        Execute the below command to install the application using Helm: 
        helm install --name cool-drink-deployment /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample13/target/kubernetes/cool_drink/cool-drink-deployment


Generating artifacts...

        @kubernetes:Service                      - complete 1/1
        @kubernetes:Ingress                      - complete 1/1
        @kubernetes:Secret                       - complete 1/1
        @kubernetes:Deployment                   - complete 1/1
        @kubernetes:HPA                          - complete 1/1
        @kubernetes:Docker                       - complete 2/2 
        @kubernetes:Helm                         - complete 1/1

        Execute the below command to deploy the Kubernetes artifacts: 
        kubectl apply -f /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample13/target/kubernetes/drink_store

        Execute the below command to install the application using Helm: 
        helm install --name drink-store-deployment /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample13/target/kubernetes/drink_store/drink-store-deployment


Generating artifacts...

        @kubernetes:Service                      - complete 1/1
        @kubernetes:ConfigMap                    - complete 1/1
        @kubernetes:Deployment                   - complete 1/1
        @kubernetes:Docker                       - complete 2/2 
        @kubernetes:Helm                         - complete 1/1

        Execute the below command to deploy the Kubernetes artifacts: 
        kubectl apply -f /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample13/target/kubernetes/hot_drink

        Execute the below command to install the application using Helm: 
        helm install --name hot-drink-deployment /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample13/target/kubernetes/hot_drink/hot-drink-deployment```
```

2. .jar files, Dockerfile, docker image and kubernetes artifacts will be generated: 
```bash
$> tree
target/
    ├── docker                                             
    |   ├── cool_drink 
    |   |   ├── Dockerfile
    │   |   └── mysql-connector-java-8.0.11.jar
    |   ├── drink_store
    |   |   ├── Dockerfile
    |   └── hot_drink
    |   |   ├── Dockerfile
    │   |   └── mysql-connector-java-8.0.11.jar                                
    ├── kubernetes                                         
    |   ├── cool_drink 
    |   |   ├── cool_drink.yaml
    │   |   └── cool-drink-deployment
    │   │   │   ├── Chart.yaml
    │   │   │   └── templates                              
    │   │   │       └── cool_drink.yaml 
    |   ├── drink_store
    |   |   ├── drink_store.yaml
    │   |   └── drink-store-deployment
    │   │   │   ├── Chart.yaml
    │   │   │   └── templates                              
    │   │   │       └── drink_store.yaml 
    |   └── hot_drink
    |   |   ├── hot_drink.yaml
    │   |   └── hot-drink-deployment
    │   │   │   ├── Chart.yaml
    │   │   │   └── templates                              
    │   │   │       └── hot_drink.yaml 
    ├── balo                                               
        ├── cool_drink-2019r3-java8-0.0.1.balo
        ├── drink_store-2019r3-java8-0.0.1.balo                   
        └── hot_drink-2019r3-java8-0.0.1.balo                    
    ├── bin                                                
        ├── cool_drink.jar     
        ├── drink_store.jar                                       
        └── hot_drink.jar                                      
    └── caches                                             
        ├── bir_cache                                      
        │   ├── kathy                                       
        │   │   ├── cool_drink                                 
        │   │   |   └── 0.0.1                              
        │   │   |   |   └── cool_drink.bir
        │   │   ├── drink_store                                 
        │   │   |   └── 0.0.1                              
        │   │   |   |   └── drink_store.bir                     
        │   │   └── hot_drink                                  
        │   │   |   └── 0.0.1                              
        │   │   |   |   └── hot_drink.bir                      
        └── jar_cache                                      
            └── kathy                                       
        │   │   ├── cool_drink                                 
        │   │   |   └── 0.0.1                              
        │   │   |   |   └── kathy-cool_drink-0.0.1.jar
        │   │   ├── drink_store                                 
        │   │   |   └── 0.0.1                              
        │   │   |   |   └── kathy-drink_store-0.0.1.jar                     
        │   │   └── hot_drink                                  
        │   │   |   └── 0.0.1                              
        │   │   |   |   └── kathy-hot_drink-0.0.1.jar
```

3. Verify the docker image is created:
```bash
$> docker images
REPOSITORY       TAG                 IMAGE ID            CREATED             SIZE
hot_drink        latest              62792bafcd4c        About a minute ago   127MB
drink_store      latest              20099ea12ff5        About a minute ago   127MB
cool_drink       latest              31d58eaa27fa        About a minute ago   127MB
```

4. Run kubectl command to deploy the artifacts.
```bash
$ kubectl apply -f /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample13/target/kubernetes/hot_drink
service/hotdrink-backend created
configmap/hotdrinksapi-ballerina-conf-config-map created
deployment.apps/hot-drink-deployment created

$ kubectl apply -f /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample13/target/kubernetes/cool_drink
service/cooldrink-backend created
configmap/cooldrinkapi-ballerina-conf-config-map created
deployment.apps/cool-drink-deployment created

$ kubectl apply -f /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample13/target/kubernetes/drink_store
service/drinkstoreep-svc created
ingress.extensions/drinkstoreep-ingress created
secret/drinkstoreep-keystore created
deployment.apps/drink-store-deployment created
horizontalpodautoscaler.autoscaling/drink-store-hpa created
```

5. Verify kubernetes deployment,service,secrets and ingress is deployed:
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

6. Access the food store with curl command:

- **Using ingress:**
Add /etc/hosts entry to match hostname. 
_(127.0.0.1 is only applicable to docker for mac users. Other users should map the hostname with correct ip address 
from `kubectl get ingress` command.)_
```bash
 127.0.0.1 drinkstore.com
```
```bash

$> curl https://drinkstore.com/store/getTempreature -k
Tempreture in San Francisco: 17.56 clecius. Sunny.

$> curl https://drinkstore.com/store/coolDrink -k
[{"id":1,"name":"Lime Soda","description":"Sparkling Soda with Lime","price":11.76,"diff":"+1.76"},{"id":2,"name":"Mango Juice","description":"Fresh Mango Juice with milk","price":17.63,"diff":"+2.63"},{"id":3,"name":"Mojito","description":"White rum, sugar, lime juice, soda water, and mint. ","price":23.51,"diff":"+3.51"}]


$> curl https://drinkstore.com/store/hotDrink -k
[{"id":1,"name":"Espresso","description":"1 Shot of espresso in an espresso cup","price":4.12,"diff":"-0.88"},{"id":2,"name":"Cappuccino","description":"Steamed milk, micro-foam & Sprinkle chocolate on top of the coffee","price":4.95,"diff":"-1.05"},{"id":3,"name":"Flat White","description":"espresso & steamed milk","price":2.47,"diff":"-0.53"}]
```

7. Undeploy sample:
```bash
$> kubectl delete -f /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample13/target/kubernetes/cool_drink
$> kubectl delete -f /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample13/target/kubernetes/hot_drink
$> kubectl delete -f /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample13/target/kubernetes/drink_store
$> kubectl delete namespace mysql
```
