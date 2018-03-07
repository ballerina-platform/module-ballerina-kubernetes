## Sample5: Kubernetes Hello World in Google Cloud Environment

- This sample runs  ballerina hello world service in GCE kubernetes cluster with liveness probe and  hostname
 mapping for ingress. 
- This sample will build the docker image and push it to docker-hub. 
- Kubernetes cluster will pull the image from docker registry.
- Following files will be generated from this sample.
    ``` 
    $> docker image
    anuruddhal/gce-sample:1.0
    
    $> tree
        .
        ├── hello-world-gce.balx
        └── target
            └── hello-world-gce
                └── kubernetes
                    ├── docker
                    │   └── Dockerfile
                    ├── hello-world-gce-deployment.yaml
                    ├── helloWorld-hpa.yaml
                    ├── helloWorld-ingress.yaml
                    └── helloWorld-svc.yaml
    ```
### How to run:

1. Configure [kubernetes cluster and ingress controller](https://cloud.google.com/community/tutorials/nginx-ingress-gke) in google cloud.

2. Open hello-world-gce.bal and change username and password with docker hub credentials.
```bash
@kubernetes:deployment{
    liveness:"enable",
    push:true,
    image:"index.docker.io/<username>/gce-sample:1.0",
    username:"<username>",
    password:"<password>"
}
```

3. Compile the  hello-world-gce.bal file. Command to run kubernetes artifacts will be printed on success:
```bash
$> ballerina build hello-world-gce.bal

info: Processing svc{} annotation for:helloWorld
success: Service yaml generated.
success: Ingress yaml generated.
success: Horizontal pod autoscaler yaml generated.
info: Creating Dockerfile ...
success: Dockerfile generated.
info: Creating docker image ...
success: Docker image index.docker.io/anuruddhal/gce-sample:1.0 generated.
info: Pushing docker image ...
info: The push refers to repository [docker.io/anuruddhal/gce-sample]
info: 1.0: digest: sha256:b28b52eaccad80c69f16f311601a4632f46505d785f6585ed3829c55fc5d83f4 size: 1368
success: Done.
success: Deployment yaml generated.

Run following command to deploy kubernetes artifacts:
kubectl create -f /Users/anuruddha/Repos/ballerinax/kubernetes/samples/sample5/target/hello-world-gce/kubernetes
```

4. hello-world-gce.balx, Dockerfile, docker image and kubernetes artifacts will be generated: 
```bash
$> tree
.
├── hello-world-gce.balx
└── target
    └── hello-world-gce
        └── kubernetes
            ├── docker
            │   └── Dockerfile
            ├── hello-world-gce-deployment.yaml
            ├── helloWorld-hpa.yaml
            ├── helloWorld-ingress.yaml
            └── helloWorld-svc.yaml
```

5. Verify the docker image is created:
```bash
$> docker images
REPOSITORY                   TAG                 IMAGE ID            CREATED             SIZE
anuruddhal/gce-sample        1.0                 88cabd203149        4 minutes ago       102MB

```

6. Run kubectl command to deploy artifacts (Use the command printed on screen in step 1):
```bash
$> kubectl create -f target/hello-world-gce/kubernetes
deployment "hello-world-gce-deployment" created
horizontalpodautoscaler "helloworld" created
ingress "helloworld" created
service "helloworld" created
```

7. Verify kubernetes deployment,service and ingress is running:
```bash
$> kubectl get pods
NAME                                         READY     STATUS    RESTARTS   AGE
hello-world-gce-deployment-8477c9c446-h4dnr   1/1       Running   0          8s


$> kubectl get svc
NAME         TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)          AGE
helloworld   NodePort    10.11.255.30   <none>        9090:30949/TCP   2m


$> kubectl get ingress
NAME         HOSTS     ADDRESS          PORTS     AGE
helloworld   abc.com   35.188.183.218   80, 443   1m

$> kubectl get hpa
NAME         REFERENCE                               TARGETS    MINPODS   MAXPODS   REPLICAS   AGE
helloworld   Deployment/hello-world-gce-deployment   1% / 50%   1         2         1          2m
```

8. Access the hello world service with curl command:

- **Using ingress**

Add /etc/host entry to match hostname.
_(35.188.183.218 is the external ip address from `kubectl get ingress` command.)_
 ```
 35.188.183.218 abc.com
 ```
Use curl command with hostname to access the service.
```bash
$> curl http://abc.com/HelloWorld/sayHello
Hello, World from service helloWorld !
```

9. Undeploy sample:
```bash
$> kubectl delete -f ./target/hello-world-gce/kubernetes
```