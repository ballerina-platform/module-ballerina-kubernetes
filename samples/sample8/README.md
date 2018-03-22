## Sample8: Mount config map volumes to deployment 

- This sample runs simple ballerina hello world service with config map mounts.
- K8S config maps are intended to hold config information.
- Putting this information in a config map is safer and more flexible than putting it verbatim in a pod definition or in a docker image.
- @kubernetes:ConfigMap{} annotation will create k8s config maps. See [hello_world_config_map_k8s.bal](
./hello_world_config_map_k8s.bal)  
- Following files will be generated from this sample.
    ``` 
    $> docker image
    hello_world_config_map_k8s:latest
    
    $> tree
    ├── hello_world_config_map_k8s.balx
    └── kubernetes
        ├── docker
        │   └── Dockerfile
        ├── hello_world_config_map_k8s_config_map.yaml
        ├── hello_world_config_map_k8s_deployment.yaml
        ├── hello_world_config_map_k8s_ingress.yaml
        ├── hello_world_config_map_k8s_secret.yaml
        └── hello_world_config_map_k8s_svc.yaml

    ```
### How to run:

1. Compile the  hello_world_config_map_k8s.bal file. Command to run kubernetes artifacts will be printed on success:
```bash
$> ballerina build hello_world_config_map_k8s.bal
@docker 			         - complete 3/3
@kubernetes:Deployment 		 - complete 1/1
@kubernetes:Service 		 - complete 1/1
@kubernetes:Secret  		 - complete 1/1
@kubernetes:ConfigMap 		 - complete 1/1

Run following command to deploy kubernetes artifacts:
kubectl apply -f /Users/anuruddha/Repos/ballerinax/kubernetes/samples/sample8/kubernetes/
```

2. hello_world_secret_mount_k8s.balx, Dockerfile, docker image and kubernetes artifacts will be generated: 
```bash
$> tree
    ├── README.md
    ├── conf
    │   └── ballerina.conf
    ├── hello_world_config_map_k8s.bal
    ├── hello_world_config_map_k8s.balx
    ├── hello_world_secret_mount_k8s.balx
    └── kubernetes
        ├── docker
        │   └── Dockerfile
        ├── hello_world_config_map_k8s_config_map.yaml
        ├── hello_world_config_map_k8s_deployment.yaml
        ├── hello_world_config_map_k8s_ingress.yaml
        ├── hello_world_config_map_k8s_secret.yaml
        └── hello_world_config_map_k8s_svc.yaml

```

3. Verify the docker image is created:
```bash
$> docker images
REPOSITORY                      TAG                 IMAGE ID            CREATED             SIZE
hello_world_config_map_k8s     latest              53559c0cd4f4        55 seconds ago      194MB
```

4. Run kubectl command to deploy artifacts (Use the command printed on screen in step 1):
```bash
$> kubectl apply -f /Users/anuruddha/Repos/ballerinax/kubernetes/samples/sample8/kubernetes/
configmap "ballerina-config" created
deployment "hello-world-config-map-k8s-deployment" created
ingress "helloworld-ingress" created
secret "helloworldep-keystore" created
service "helloworldep-svc" created
```

5. Verify kubernetes deployment,service,secrets and ingress is deployed:
```bash
$> kubectl get pods
NAME                                                       READY      STATUS    RESTARTS   AGE
hello-world-config-map-k8s-deployment-6744b97dc5-2z5k4      1/1       Running   0          5m

$> kubectl get svc
NAME               TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)    AGE
helloworldep-svc   ClusterIP   10.100.232.242   <none>        9090/TCP   6m

$> kubectl get ingress
NAME                 HOSTS     ADDRESS   PORTS     AGE
helloworld-ingress   abc.com             80, 443   6m

$> kubectl get secrets
NAME                    TYPE                                 DATA      AGE
helloworldep-keystore   Opaque                                1         1m

$> kubectl get configmaps
NAME               DATA      AGE
ballerina-config   1         7m
```

6. Access the hello world service with curl command:


- **Using ingress:**
```bash
$>curl https://abc.com/HelloWorld/conf -k
conf resource: enableDebug=true;
```

7. Undeploy sample:
```bash
$> kubectl delete -f /Users/anuruddha/Repos/ballerinax/kubernetes/samples/sample8/kubernetes/

```
