## Sample9: Mount PersistentVolumeClaim to deployment 

- This sample runs simple ballerina hello world service with persistence volume claim mounts.
- @kubernetes:PersistentVolumeClaim{} annotation will create k8s persistent volume claim mounts. See 
[hello_world_persistence_volume_k8s.bal](./hello_world_persistence_volume_k8s.bal)  
- Following files will be generated from this sample.
    ``` 
    $> docker image
    hello_world_persistence_volume_k8s:latest
    
    $> tree
   ├── README.md
   ├── hello_world_persistence_volume_k8s.bal
   ├── hello_world_persistence_volume_k8s.jar
   ├── docker
       └── Dockerfile
   ├── security
   │   └── ballerinaKeystore.p12
   ├── kubernetes
   │   ├── hello-world-persistence-volume-k8s-deployment
   │   │   ├── Chart.yaml
   │   │   └── templates
   │   │       └── hello_world_persistence_volume_k8s.yaml
   │   └── hello_world_persistence_volume_k8s.yaml
   └── volumes
       └── persistent-volume.yaml
  
    ```
### How to run:

1. Compile the  hello_world_persistence_volume_k8s.bal file. Command to deploy kubernetes artifacts will be printed on build success.
```bash
$> ballerina build hello_world_persistence_volume_k8s.bal
Compiling source
        hello_world_persistence_volume_k8s.bal

Generating executables
        hello_world_persistence_volume_k8s.jar

Generating artifacts...

        @kubernetes:Service                      - complete 1/1
        @kubernetes:Ingress                      - complete 1/1
        @kubernetes:Secret                       - complete 1/1
        @kubernetes:VolumeClaim                  - complete 1/1
        @kubernetes:Deployment                   - complete 1/1
        @kubernetes:Docker                       - complete 2/2 
        @kubernetes:Helm                         - complete 1/1

        Execute the below command to deploy the Kubernetes artifacts: 
        kubectl apply -f /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample9/kubernetes

        Execute the below command to install the application using Helm: 
        helm install --name hello-world-persistence-volume-k8s-deployment /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample9/kubernetes/hello-world-persistence-volume-k8s-deployment
```

2. hello_world_persistence_volume_k8s.jar, Dockerfile, docker image and kubernetes artifacts will be generated: 
```bash
$> tree
.
├── README.md
├── hello_world_persistence_volume_k8s.bal
├── hello_world_persistence_volume_k8s.jar
├── docker
    └── Dockerfile
├── security
│   └── ballerinaKeystore.p12
├── kubernetes
│   ├── hello-world-persistence-volume-k8s-deployment
│   │   ├── Chart.yaml
│   │   └── templates
│   │       └── hello_world_persistence_volume_k8s.yaml
│   └── hello_world_persistence_volume_k8s.yaml
└── volumes
    └── persistent-volume.yaml

```

3. Verify the docker image is created:
```bash
$> docker images
REPOSITORY                              TAG                 IMAGE ID            CREATED             SIZE
hello_world_persistence_volume_k8s     latest              53559c0cd4f4        55 seconds ago      194MB
```

4. Create sample kubernetes volume using following command.
 ```bash
$> kubectl create -f ./volumes/persistent-volume.yaml
persistentvolume "local-pv-2" created
```

5. Run kubectl command to deploy artifacts (Use the command printed on screen in step 1):
```bash
$> kubectl apply -f /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample9/kubernetes
service/helloworldep-svc created
ingress.extensions/helloworldep-ingress created
secret/helloworldep-keystore created
persistentvolumeclaim/local-pv-2 created
deployment.apps/hello-world-persistence-volume-k8s-deployment created
```

6. Verify kubernetes deployment,service,secrets and ingress is deployed:
```bash
$> kubectl get pods
NAME                                                             READY     STATUS    RESTARTS   AGE
hello-world-persistence-volume-k8s-deployment-6ff8d6b94b-fqwmw   1/1       Running   0          1m

$> kubectl get svc
NAME               TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)    AGE
helloworldep-svc   ClusterIP   10.102.241.188   <none>        9090/TCP   2m

$> kubectl get ingress
NAME                 HOSTS     ADDRESS   PORTS     AGE
helloworld-ingress   abc.com             80, 443   2m

$> kubectl get secrets
NAME                    TYPE                                 DATA      AGE
helloworldep-keystore   Opaque                                1         1m

$> kubectl get pv
NAME                                       CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS      CLAIM                STORAGECLASS   REASON    AGE
local-pv-2                                 2Gi        RWO            Delete           Available                                                 3h

$> kubectl get pvc
NAME         STATUS    VOLUME                                     CAPACITY   ACCESS MODES   STORAGECLASS   AGE
local-pv-2   Bound     pvc-d26dd46d-2c46-11e8-b313-025000000001   1Gi        RWO            hostpath       3m
```

7. Access the hello world service with curl command:

- **Using ingress:**
Add /etc/hosts entry to match hostname. 
_(127.0.0.1 is only applicable to docker for mac users. Other users should map the hostname with correct ip address 
from `kubectl get ingress` command.)_

```bash
$> curl https://abc.com/helloWorld/sayHello -k
Hello World
```

8. Undeploy sample:
```bash
$> kubectl delete -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample9/kubernetes/
$> kubectl delete -f ./volumes/persistent-volume.yaml
$> docker rmi hello_world_persistence_volume_k8s

```
