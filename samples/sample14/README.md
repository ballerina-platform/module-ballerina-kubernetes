## Sample15: Resource quotas for namespaces

- This sample creates kubernetes resource quotas.
- Following files will be generated from this sample.
    ``` 
    $> docker image
    hello_world_k8s:latest
    
    $> tree
    ├── README.md
    ├── hello_world_k8s.bal
    ├── hello_world_k8s.balx
    └── kubernetes
        ├── docker
        │   └── Dockerfile
        ├── hello-world-k8s-deployment
        │   ├── Chart.yaml
        │   └── templates
        │       ├── hello_world_k8s_deployment.yaml
        │       ├── hello_world_k8s_ingress.yaml
        │       ├── hello_world_k8s_resource_quota.yaml
        │       └── hello_world_k8s_svc.yaml
        ├── hello_world_k8s_deployment.yaml
        ├── hello_world_k8s_ingress.yaml
        ├── hello_world_k8s_resource_quota.yaml
        └── hello_world_k8s_svc.yaml
    ```
### How to run:

1. Compile the hello_world_k8s_resource-quota.bal file. Command to run kubernetes artifacts will be printed on success:
```bash
$> ballerina build hello_world_k8s.bal
Compiling source
    hello_world_k8s.bal
Generating executable
    hello_world_k8s.balx
	@kubernetes:Service 			 - complete 1/1
	@kubernetes:Ingress 			 - complete 1/1
	@kubernetes:ResourceQuota 		 - complete 1/1
	@kubernetes:Deployment 			 - complete 1/1
	@kubernetes:Docker 			 - complete 3/3
	@kubernetes:Helm 			 - complete 1/1

	Run the following command to deploy the Kubernetes artifacts:
	kubectl apply -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample15/kubernetes/

	Run the following command to install the application using Helm:
	helm install --name hello-world-k8s-deployment /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample15/kubernetes/hello-world-k8s-deployment
```

2. hello_world_k8s.balx, Dockerfile, docker image and kubernetes artifacts will be generated: 
```bash
$> tree
├── README.md
├── hello_world_k8s.bal
├── hello_world_k8s.balx
└── kubernetes
    ├── docker
    │   └── Dockerfile
    ├── hello-world-k8s-deployment
    │   ├── Chart.yaml
    │   └── templates
    │       ├── hello_world_k8s_deployment.yaml
    │       ├── hello_world_k8s_ingress.yaml
    │       ├── hello_world_k8s_resource_quota.yaml
    │       └── hello_world_k8s_svc.yaml
    ├── hello_world_k8s_deployment.yaml
    ├── hello_world_k8s_ingress.yaml
    ├── hello_world_k8s_resource_quota.yaml
    └── hello_world_k8s_svc.yaml
```

3. Verify the docker image is created:
```bash
$> docker images
REPOSITORY                          TAG         IMAGE ID            CREATED             SIZE
hello_world_k8s                     latest      d88fa54de116        32 seconds ago      128MB

```

4. Create a namespace as `ballerina` in Kubernetes.
```bash
$> kubectl create namespace ballerina
namespace "ballerina" created
```

5. Run kubectl command to deploy artifacts (Use the command printed on screen in step 1):
```bash
$> kubectl apply -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample15/kubernetes/ --namespace=ballerina
deployment.extensions "hello-world-k8s-deployment" created
ingress.extensions "helloep-ingress" created
resourcequota "pod-limit" created
service "hello" created
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
$> kubectl scale --replicas=5 deployment/hello-world-k8s-deployment -n ballerina
deployment.extensions "hello-world-k8s-deployment" scaled

```

9. Verify if the number of pods limit has been applied by the resource quota.
```bash
$> kubectl get deployment hello-world-k8s-deployment --namespace=ballerina
NAME                         DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
hello-world-k8s-deployment   5         2         2            2           8m

```

9. Undeploy sample:
```bash
$> kubectl delete -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample15/kubernetes/ --namespace=ballerina
deployment.extensions "hello-world-k8s-deployment" deleted
ingress.extensions "helloep-ingress" deleted
resourcequota "pod-limit" deleted
service "hello" deleted
```