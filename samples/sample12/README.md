## Sample12: Copy External files to Docker Image 

- This sample runs simple ballerina hello world service with external file.
- @kubernetes:Deployment{copyfiles:[]} attribute will copy external files to docker image. See [hello_world_copy_file.bal](
hello_world_copy_file.bal)  
- Following files will be generated from this sample.
    ``` 
    $> docker image
    hello_world_copy_file:latest
    
    $> tree
    ├── README.md
    ├── data
    │   └── data.txt
    ├── hello_world_copy_file.bal
    ├── hello_world_copy_file.balx
    └── kubernetes
        ├── docker
        │   ├── Dockerfile
        │   └── data.txt
        ├── hello-world-copy-file-deployment
        │   ├── Chart.yaml
        │   └── templates
        │       ├── hello_world_copy_file_deployment.yaml
        │       ├── hello_world_copy_file_ingress.yaml
        │       ├── hello_world_copy_file_secret.yaml
        │       └── hello_world_copy_file_svc.yaml
        ├── hello_world_copy_file_deployment.yaml
        ├── hello_world_copy_file_ingress.yaml
        ├── hello_world_copy_file_secret.yaml
        └── hello_world_copy_file_svc.yaml

    ```
### How to run:

1. Compile the  hello_world_copy_file.bal file. Command to run kubernetes artifacts will be printed on success:
```bash
$> ballerina build hello_world_copy_file.bal
@kubernetes:Service 			 - complete 1/1
@kubernetes:Ingress 			 - complete 1/1
@kubernetes:Secret 			 - complete 1/1
@kubernetes:Deployment 			 - complete 1/1
@kubernetes:Docker 			 - complete 3/3
@kubernetes:Helm 			 - complete 1/1

Run following command to deploy kubernetes artifacts:
kubectl apply -f /Users/anuruddha/Repos/ballerinax/kubernetes/samples/sample12/kubernetes/
```

2. hello_world_copy_file.balx, Dockerfile, docker image and kubernetes artifacts will be generated: 
```bash
$> tree
    .
    ├── README.md
    ├── data
    │   └── data.txt
    ├── hello_world_copy_file.bal
    ├── hello_world_copy_file.balx
    └── kubernetes
        ├── docker
        │   ├── Dockerfile
        │   └── data.txt
        ├── hello-world-copy-file-deployment
        │   ├── Chart.yaml
        │   └── templates
        │       ├── hello_world_copy_file_deployment.yaml
        │       ├── hello_world_copy_file_ingress.yaml
        │       ├── hello_world_copy_file_secret.yaml
        │       └── hello_world_copy_file_svc.yaml
        ├── hello_world_copy_file_deployment.yaml
        ├── hello_world_copy_file_ingress.yaml
        ├── hello_world_copy_file_secret.yaml
        └── hello_world_copy_file_svc.yaml

```

3. Verify the docker image is created:
```bash
$> docker images
REPOSITORY                 TAG                 IMAGE ID            CREATED             SIZE
hello_world_copy_file     latest              53559c0cd4f4        55 seconds ago      194MB
```

4. Run kubectl command to deploy artifacts (Use the command printed on screen in step 1):
```bash
$> kubectl apply -f /Users/anuruddha/Repos/ballerinax/kubernetes/samples/sample12/kubernetes/
deployment.extensions "hello-world-copy-file-deployment" created
ingress.extensions "helloworldep-ingress" created
secret "helloworldep-secure-socket" created
service "helloworldep-svc" created
```

5. Verify kubernetes deployment,service,secrets and ingress is deployed:
```bash
$> kubectl get pods
NAME                                                READY     STATUS    RESTARTS   AGE
hello-world-copy-file-deployment-7b85f8b5c4-8nfzv   1/1       Running   0          0s

$> kubectl get svc
NAME               TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)    AGE
helloworldep-svc   ClusterIP   10.104.89.233   <none>        9090/TCP   14s

$> kubectl get ingress
NAME                 HOSTS     ADDRESS   PORTS     AGE
helloworld-ingress   abc.com             80, 443   6m

$> kubectl get secrets
NAME                         TYPE                                  DATA      AGE
helloworldep-secure-socket   Opaque                                2         36s

```

6. Access the hello world service with curl command:

- **Using ingress:**
Add /etc/hosts entry to match hostname. 
_(127.0.0.1 is only applicable to docker for mac users. Other users should map the hostname with correct ip address 
from `kubectl get ingress` command.)_

```bash
$> curl https://abc.com/helloWorld/data -k
Data: Lorem ipsum dolor sit amet.
```

7. Undeploy sample:
```bash
$> kubectl delete -f /Users/anuruddha/Repos/ballerinax/kubernetes/samples/sample12/kubernetes/

```
