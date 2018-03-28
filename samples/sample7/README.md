## Sample7: Mount secret volumes to deployment 

- This sample runs simple ballerina hello world service with secret volume mounts.
- K8S secret are intended to hold sensitive information, such as passwords, OAuth tokens, and ssh keys.
- Putting this information in a secret is safer and more flexible than putting it verbatim in a pod definition or in a
  docker image.
- @kubernetes:Secret{} annotation will create k8s secrets. See [hello_world_secret_mount_k8s.bal](./hello_world_secret_mount_k8s.bal)  
- Following files will be generated from this sample.
    ``` 
    $> docker image
    hello_world_secret_mount_k8s:latest
    
    $> tree
    ├── README.md
    ├── hello_world_secret_mount_k8s.bal
    ├── hello_world_secret_mount_k8s.balx
    ├── kubernetes
    │   ├── docker
    │   │   └── Dockerfile
    │   ├── hello_world_secret_mount_k8s_deployment.yaml
    │   ├── hello_world_secret_mount_k8s_ingress.yaml
    │   ├── hello_world_secret_mount_k8s_secret.yaml
    │   └── hello_world_secret_mount_k8s_svc.yaml
    └── secrets
        ├── MySecret1.txt
        ├── MySecret2.txt
        └── MySecret3.txt

    ```
### How to run:

1. Compile the  hello_world_secret_mount_k8s.bal file. Command to run kubernetes artifacts will be printed on success:
```bash
$> ballerina build hello_world_secret_mount_k8s.bal
@kubernetes:Docker 			 - complete 3/3
@kubernetes:Deployment 		 - complete 1/1
@kubernetes:Service 		 - complete 1/1
@kubernetes:Secret  		 - complete 3/3

Run following command to deploy kubernetes artifacts:
kubectl apply -f /Users/anuruddha/Repos/ballerinax/kubernetes/samples/sample7/kubernetes/
```

2. hello_world_secret_mount_k8s.balx, Dockerfile, docker image and kubernetes artifacts will be generated: 
```bash
$> tree
    ├── README.md
    ├── hello_world_secret_mount_k8s.bal
    ├── hello_world_secret_mount_k8s.balx
    ├── kubernetes
    │   ├── docker
    │   │   └── Dockerfile
    │   ├── hello_world_secret_mount_k8s_deployment.yaml
    │   ├── hello_world_secret_mount_k8s_ingress.yaml
    │   ├── hello_world_secret_mount_k8s_secret.yaml
    │   └── hello_world_secret_mount_k8s_svc.yaml
    └── secrets
        ├── MySecret1.txt
        ├── MySecret2.txt
        └── MySecret3.txt

```

3. Verify the docker image is created:
```bash
$> docker images
REPOSITORY                      TAG                 IMAGE ID            CREATED             SIZE
hello_world_secret_mount_k8s   latest              53559c0cd4f4        55 seconds ago      194MB

```

4. Run kubectl command to deploy artifacts (Use the command printed on screen in step 1):
```bash
$> kubectl apply -f /Users/anuruddha/Repos/ballerinax/kubernetes/samples/sample7/kubernetes/
deployment "hello-world-secret-mount-k8s-deployment" created
ingress "helloworld-ingress" created
secret "public" created
secret "private" created
service "helloworldep-svc" created
```

5. Verify kubernetes deployment,service,secrets and ingress is deployed:
```bash
$> kubectl get pods
NAME                                                       READY     STATUS    RESTARTS   AGE
hello-world-secret-mount-k8s-deployment-7fb6b6f7f8-blwm2   1/1       Running   0          34s

$> kubectl get svc
NAME               TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
helloworldep-svc   ClusterIP    10.105.238.19   <none>        9090/TCP         47s

$> kubectl get ingress
NAME                 HOSTS     ADDRESS   PORTS     AGE
helloworld-ingress   abc.com             80, 443   1m

$> kubectl get secrets
NAME                    TYPE                                 DATA      AGE
private                 Opaque                                1         1m
public                  Opaque                                2         1m
helloworldep-keystore   Opaque                                1         1m

```

6. Access the hello world service with curl command:

- **Using ingress:**
Add /etc/host entry to match hostname. 
_(127.0.0.1 is only applicable to docker for mac users. Other users should map the hostname with correct ip address 
from `kubectl get ingress` command.)_

```bash
$>curl https://abc.com/helloWorld/secret1 -k
Secret1 resource: Secret1

$>curl https://abc.com/helloWorld/secret2 -k
Secret2 resource: Secret2

$>curl https://abc.com/helloWorld/secret3 -k
Secret3 resource: Secret3
```

7. Undeploy sample:
```bash
$> kubectl delete -f /Users/anuruddha/Repos/ballerinax/kubernetes/samples/sample7/kubernetes/

```
