## Sample15: Resource quotas for namespaces

- This sample creates kubernetes resource quotas.
- Following files will be generated from this sample.
    ``` 
    $> docker image
    hello_world_k8s:latest
    
    $> tree
    ├── README.md
    ├── hello_world_k8s_rq.bal
    ├── hello_world_k8s_rq.jar
    ├── docker
        └── Dockerfile
    └── kubernetes
        ├── hello-world-k8s-rq-deployment
        │   ├── Chart.yaml
        │   └── templates
        │       └── hello_world_k8s_rq.yaml
        └── hello_world_k8s_rq.yaml
    ```
### How to run:

1. Compile the hello_world_k8s_rq.bal file. Command to run kubernetes artifacts will be printed on success:
```bash
$> ballerina build hello_world_k8s_rq.bal
Compiling source
        hello_world_k8s_rq.bal

Generating executables
        hello_world_k8s_rq.jar

Generating artifacts...

        @kubernetes:Service                      - complete 1/1
        @kubernetes:Ingress                      - complete 1/1
        @kubernetes:ResourceQuota                - complete 1/1
        @kubernetes:Deployment                   - complete 1/1
        @kubernetes:Docker                       - complete 2/2 
        @kubernetes:Helm                         - complete 1/1

        Run the following command to deploy the Kubernetes artifacts: 
        kubectl apply -f /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample15/kubernetes

        Run the following command to install the application using Helm: 
        helm install --name hello-world-k8s-rq-deployment /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample15/kubernetes/hello-world-k8s-rq-deployment
```

2. hello_world_k8s_rq.jar, Dockerfile, docker image and kubernetes artifacts will be generated: 
```bash
$> tree
├── README.md
├── hello_world_k8s_rq.bal
├── hello_world_k8s_rq.jar
├── docker
    └── Dockerfile
└── kubernetes
    ├── hello-world-k8s-rq-deployment
    │   ├── Chart.yaml
    │   └── templates
    │       └── hello_world_k8s_rq.yaml
    └── hello_world_k8s_rq.yaml
```

3. Verify the docker image is created:
```bash
$> docker images
REPOSITORY                                                       TAG                               IMAGE ID            CREATED              SIZE
hello_world_k8s_rq                                               latest                            6cb9c74b1d2c        About a minute ago   125MB

```

4. Create a namespace as `ballerina` in Kubernetes.
```bash
$> kubectl create namespace ballerina
namespace/ballerina created
```

5. Run kubectl command to deploy artifacts (Use the command printed on screen in step 1):
```bash
$> kubectl apply -f /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample15/kubernetes -n ballerina
service/hello created
ingress.extensions/helloep-ingress created
resourcequota/pod-limit created
deployment.apps/hello-world-k8s-rq-deployment created
deployment.apps/hello-world-k8s-rq-deployment created
```

6. Verify kubernetes deployment is running:
```bash
$> kubectl get pods -n ballerina
NAME                                          READY     STATUS    RESTARTS   AGE
hello-world-k8s-deployment-57bd5c5d8b-lkxmn   1/1       Running   0          14s
hello-world-k8s-deployment-57bd5c5d8b-vc7w8   1/1       Running   0          14s

```

7. Verify kubernetes resource quota is created for `ballerina` namespace.
```bash
$> kubectl get resourcequota pod-limit --namespace=ballerina --output=yaml
apiVersion: v1
kind: ResourceQuota
metadata:
  annotations:
    kubectl.kubernetes.io/last-applied-configuration: |
      {"apiVersion":"v1","kind":"ResourceQuota","metadata":{"annotations":{},"finalizers":[],"labels":{},"name":"pod-limit","namespace":"ballerina","ownerReferences":[]},"spec":{"hard":{"limits.cpu":"2","limits.memory":"2Gi","pods":"2","requests.cpu":"1","requests.memory":"1Gi"},"scopes":[]}}
  creationTimestamp: 2018-10-22T08:25:48Z
  name: pod-limit
  namespace: ballerina
  resourceVersion: "622100"
  selfLink: /api/v1/namespaces/ballerina/resourcequotas/pod-limit
  uid: 1468f1a9-d5d4-11e8-b9e5-025000000001
spec:
  hard:
    limits.cpu: "2"
    limits.memory: 2Gi
    pods: "2"
    requests.cpu: "1"
    requests.memory: 1Gi
status:
  hard:
    limits.cpu: "2"
    limits.memory: 2Gi
    pods: "2"
    requests.cpu: "1"
    requests.memory: 1Gi
  used:
    limits.cpu: "0"
    limits.memory: "0"
    pods: "2"
    requests.cpu: "0"
    requests.memory: "0"

```

8. Increase the replication of the deployment to 5.
```bash
$> kubectl scale --replicas=5 deployment/hello-world-k8s-rq-deployment -n ballerina
deployment.extensions/hello-world-k8s-rq-deployment scaled

```

9. Verify if the number of pods limit has been applied by the resource quota.
```bash
$> kubectl get deployment hello-world-k8s-rq-deployment -n ballerina
NAME                            DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
hello-world-k8s-rq-deployment   5         2         2            2           7m

```

9. Undeploy sample:
```bash
$> kubectl delete -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample15/kubernetes/ -n ballerina
deployment.extensions "hello-world-k8s-deployment" deleted
ingress.extensions "helloep-ingress" deleted
resourcequota "pod-limit" deleted
service "hello" deleted

$> kubectl delete namespace ballerina
$> docker rmi hello_world_k8s_rq 

```