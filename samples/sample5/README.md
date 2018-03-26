## Sample5: Kubernetes Hello World in Google Cloud Environment

- This sample runs  ballerina hello world service in GCE kubernetes cluster with enableLiveness probe and  hostname
 mapping for ingress. 
- This sample will build the docker image and push it to docker-hub. 
- Kubernetes cluster will pull the image from docker registry.
- Following files will be generated from this sample.
    ``` 
    $> docker image
    anuruddhal/gce-sample:1.0
    
    $> tree
        .
        ├── hello_world_gce.balx
        └── target
            └── hello_world_gce
                └── kubernetes
                    ├── docker
                    │   └── Dockerfile
                    ├── hello_world_gce-deployment.yaml
                    ├── helloWorld-hpa.yaml
                    ├── helloWorld-ingress.yaml
                    └── helloWorld-svc.yaml
    ```
### How to run:

1. Configure [kubernetes cluster and ingress controller](https://cloud.google.com/community/tutorials/nginx-ingress-gke) in google cloud.

2. Export docker registry username and password as envrionment variable.
```bash
export DOCKER_USERNAME=<username>
export DOCKER_PASSWORD=<password>
```

3. Compile the  hello_world_gce.bal file. Command to run kubernetes artifacts will be printed on success:
```bash
$> ballerina build hello_world_gce.bal
@kubernetes:Docker 				    - complete 3/3
@kubernetes:HPA 			- complete 1/1
@kubernetes:Deployment 	    - complete 1/1
@kubernetes:Ingress 		- complete 1/1

Run following command to deploy kubernetes artifacts:
kubectl apply -f /Users/anuruddha/Repos/ballerinax/kubernetes/samples/sample5/kubernetes/
```

4. hello_world_gce.balx, Dockerfile, docker image and kubernetes artifacts will be generated: 
```bash
$> tree
.
├── hello_world_gce.balx
└── target
    └── hello_world_gce
        └── kubernetes
            ├── docker
            │   └── Dockerfile
            ├── hello_world_gce-deployment.yaml
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
$> kubectl create -f target/hello_world_gce/kubernetes
deployment "hello_world_gce-deployment" created
horizontalpodautoscaler "helloworld" created
ingress "helloworld" created
service "helloworld" created
```

7. Verify kubernetes deployment,service and ingress is running:
```bash
$> kubectl get pods
NAME                                         READY     STATUS    RESTARTS   AGE
hello_world_gce-deployment-8477c9c446-h4dnr   1/1       Running   0          8s


$> kubectl get svc
NAME         TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)          AGE
helloworld   NodePort    10.11.255.30   <none>        9090:30949/TCP   2m


$> kubectl get ingress
NAME         HOSTS     ADDRESS          PORTS     AGE
helloworld   abc.com   35.188.183.218   80, 443   1m

$> kubectl get hpa
NAME         REFERENCE                               TARGETS    MINPODS   MAXPODS   REPLICAS   AGE
helloworld   Deployment/hello_world_gce-deployment   1% / 50%   1         2         1          2m
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
$> curl http://abc.com/helloWorld/sayHello
Hello, World from service helloWorld !
```

9. Undeploy sample:
```bash
$> kubectl delete -f ./target/hello_world_gce/kubernetes
```
## Troubleshooting
- Run following commands to deploy ingress backend and controller
```bash
$> helm init
$> kubectl create clusterrolebinding permissive-binding --clusterrole=cluster-admin --user=admin --user=kubelet --group=system:serviceaccounts;
$> helm install --name nginx-ingress stable/nginx-ingress --set rbac.create=true
```