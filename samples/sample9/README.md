## Sample9: Mount PersistentVolumeClaim to deployment 

- This sample runs simple ballerina hello world service with persistence volume claim mounts.
- @kubernetes:persistentVolumeClaim{} annotation will create k8s config maps. See [hello_world_config_map_k8s.bal](
./hello_world_persistence_volume_k8s.bal)  
- Following files will be generated from this sample.
    ``` 
    $> docker image
    hello_world_persistence_volume_k8s:latest
    
    $> tree
      ├── hello_world_persistence_volume_k8s.balx
      ├── kubernetes
         ├── docker
         │   └── Dockerfile
         ├── hello_world_persistence_volume_k8s_deployment.yaml
         ├── hello_world_persistence_volume_k8s_ingress.yaml
         ├── hello_world_persistence_volume_k8s_secret.yaml
         ├── hello_world_persistence_volume_k8s_svc.yaml
         └── hello_world_persistence_volume_k8s_volume_claim.yaml
  
    ```
### How to run:

1. Compile the  hello_world_config_map_k8s.bal file. Command to run kubernetes artifacts will be printed on success:
```bash
$> ballerina build hello_world_persistence_volume_k8s.bal
@docker 				 - complete 3/3
@kubernetes:deployment 			 - complete 1/1
@kubernetes:ingress 			 - complete 1/1
@kubernetes:secret 			 - complete 1/1
@kubernetes:volumeClaim 		 - complete 1/1

Run following command to deploy kubernetes artifacts:
kubectl apply -f /Users/anuruddha/Repos/ballerinax/kubernetes/samples/sample9/kubernetes/
```

2. hello_world_secret_mount_k8s.balx, Dockerfile, docker image and kubernetes artifacts will be generated: 
```bash
$> tree
.
├── README.md
├── hello_world_persistence_volume_k8s.bal
├── hello_world_persistence_volume_k8s.balx
├── kubernetes
│   ├── docker
│   │   └── Dockerfile
│   ├── hello_world_persistence_volume_k8s_deployment.yaml
│   ├── hello_world_persistence_volume_k8s_ingress.yaml
│   ├── hello_world_persistence_volume_k8s_secret.yaml
│   ├── hello_world_persistence_volume_k8s_svc.yaml
│   └── hello_world_persistence_volume_k8s_volume_claim.yaml
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
kubectl create -f ./volumes/persistent-volume.yaml
```

5. Run kubectl command to deploy artifacts (Use the command printed on screen in step 1):
```bash
$> kubectl apply -f /Users/anuruddha/Repos/ballerinax/kubernetes/samples/sample9/kubernetes/
deployment "hello-world-persistence-volume-k8s-deployment" created
ingress "helloworld-ingress" created
secret "helloworldep-keystore" created
service "helloworldep-svc" created
persistentvolumeclaim "local-pv-2" created
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
```bash
$>curl https://abc.com/HelloWorld/sayHello -k
Hello World
```

8. Undeploy sample:
```bash
$> kubectl delete -f /Users/anuruddha/Repos/ballerinax/kubernetes/samples/sample9/kubernetes/

```
