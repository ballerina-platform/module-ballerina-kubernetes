## Sample16: Istio Gateway and Virtual Service generation

- This sample uses 3 microservices implementing a book shop.
- Traffic is managed through an istio gateway and virtual service which gets generated through @kubernetes:IstioGateway 
and @kubernetes:IstioVirtualService annotations.
- Following are the microservices:  
    - `book.details` module has a service which gets details of a book. These details include the author and the price 
    of a book.
    - `book.reviews` module has a service which gets reviews of a book.
    - `book.shop` module has a service that communicates to `book.details` and `book.reviews` service to retrieve 
    information of a book and responses back to the client collectively.
- `book.shop` service depends on `book.details` and `book.reviews`.
- Make sure that istio is installed correctly and that all pods and services of the istio-system are up and running. 
- Remove the nginx artifacts added in setting up of the tutorial. Run `kubectl delete -f 
nginx-ingress/namespaces/nginx-ingress.yaml -Rf nginx-ingress` from the `samples` folder.
See [here](https://istio.io/docs/setup/kubernetes/quick-start/) on how to install istio on kubernetes.
- Following files will be generated from building this sample.
    ``` 
    $> docker images
    book.reviews:latest
    book.shop:latest 
    book.details:latest
    
    $> tree
    target/
    ├── Ballerina.lock
    ├── book.details.balx
    ├── book.reviews.balx
    ├── book.shop.balx
    └── kubernetes
        ├── book.details
        │   ├── book-details-deployment
        │   │   ├── Chart.yaml
        │   │   └── templates
        │   │       └── book.details.yaml
        │   ├── book.details.yaml
        │   └── docker
        │       └── Dockerfile
        ├── book.reviews
        │   ├── book-reviews-deployment
        │   │   ├── Chart.yaml
        │   │   └── templates
        │   │       └── book.reviews.yaml
        │   ├── book.reviews.yaml
        │   └── docker
        │       └── Dockerfile
        └── book.shop
            ├── book-shop-deployment
            │   ├── Chart.yaml
            │   └── templates
            │       ├── book.shop_deployment.yaml
            │       └── book.shop_svc.yaml
            ├── book.shop_deployment.yaml
            ├── book.shop_istio_gateway.yaml
            ├── book.shop_istio_virtual_service.yaml
            ├── book.shop_svc.yaml
            └── docker
                └── Dockerfile
  
    ```
### How to run:

1. Compile the ballerina project from `sample16` folder. Command to run kubernetes artifacts will be printed on success:
```bash
$> ballerina build
Compiling source
    foo/book.details:1.0.0
    foo/book.shop:1.0.0
    foo/book.reviews:1.0.0

Running tests
    foo/book.details:1.0.0
	No tests found

    foo/book.reviews:1.0.0
	No tests found

    foo/book.shop:1.0.0
	No tests found

Generating executables
    ./target/book.details.balx
	@kubernetes:Service 			 - complete 1/1
	@kubernetes:Deployment 			 - complete 1/1
	@kubernetes:Docker 			 - complete 3/3
	@kubernetes:Helm 			 - complete 1/1

	Run the following command to deploy the Kubernetes artifacts:
	kubectl apply -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample16/target/kubernetes/book.details

	Run the following command to install the application using Helm:
	helm install --name book-details-deployment /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample16/target/kubernetes/book.details/book-details-deployment

    ./target/book.shop.balx
	@kubernetes:Service 			 - complete 1/1
	@kubernetes:Deployment 			 - complete 1/1
	@kubernetes:Docker 			 - complete 3/3
	@kubernetes:Helm 			 - complete 1/1
	@kubernetes:IstioGatewayModel 		 - complete 1/1
	@kubernetes:IstioVirtualService 	 - complete 1/1

	Run the following command to deploy the Kubernetes artifacts:
	kubectl apply -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample16/target/kubernetes/book.shop

	Run the following command to install the application using Helm:
	helm install --name book-shop-deployment /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample16/target/kubernetes/book.shop/book-shop-deployment

    ./target/book.reviews.balx
	@kubernetes:Service 			 - complete 1/1
	@kubernetes:Deployment 			 - complete 1/1
	@kubernetes:Docker 			 - complete 3/3
	@kubernetes:Helm 			 - complete 1/1

	Run the following command to deploy the Kubernetes artifacts:
	kubectl apply -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample16/target/kubernetes/book.reviews

	Run the following command to install the application using Helm:
	helm install --name book-reviews-deployment /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample16/target/kubernetes/book.reviews/book-reviews-deployment
```

2. book.details.balx, book.reviews.balx, book.shop.balx, Dockerfile, docker image, kubernetes and istio artifacts will be generated: 
```bash
$> tree target
target/
├── Ballerina.lock
├── book.details.balx
├── book.reviews.balx
├── book.shop.balx
└── kubernetes
    ├── book.details
    │   ├── book-details-deployment
    │   │   ├── Chart.yaml
    │   │   └── templates
    │   │       └── book.details.yaml
    │   ├── book.details.yaml
    │   └── docker
    │       └── Dockerfile
    ├── book.reviews
    │   ├── book-reviews-deployment
    │   │   ├── Chart.yaml
    │   │   └── templates
    │   │       └── book.reviews.yaml
    │   ├── book.reviews.yaml
    │   └── docker
    │       └── Dockerfile
    └── book.shop
        ├── book-shop-deployment
        │   ├── Chart.yaml
        │   └── templates
        │       ├── book.shop_deployment.yaml
        │       └── book.shop_svc.yaml
        ├── book.shop_deployment.yaml
        ├── book.shop_istio_gateway.yaml
        ├── book.shop_istio_virtual_service.yaml
        ├── book.shop_svc.yaml
        └── docker
            └── Dockerfile

```

3. Verify the docker images are created:
```bash
$> docker images
REPOSITORY                                                       TAG                               IMAGE ID            CREATED             SIZE
book.reviews                                                     latest                            36b31684f47b        5 seconds ago       128MB
book.shop                                                        latest                            cf5ac9d57651        6 seconds ago       128MB
book.details                                                     latest                            4d3c92f36683        9 seconds ago       128MB

```

4. Enable istio-sidecar-injection for the default namespace if its not already set:
```bash
$> kubectl label namespace default istio-injection=enabled
namespace "default" labeled
```

5. Run the kubectl commands to deploy artifacts (Use the command printed on screen in step 1):
```bash
$> kubectl apply -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample16/target/kubernetes/book.details
service "book-detail" created
deployment.extensions "book-details-deployment" created

$> kubectl apply -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample16/target/kubernetes/book.shop
deployment.extensions "book-shop-deployment" created
gateway.networking.istio.io "bookshopep-istio-gw" created
virtualservice.networking.istio.io "bookshopep-istio-vs" created
service "bookshopep-svc" created

$> kubectl apply -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample16/target/kubernetes/book.reviews
service "book-review" created
deployment.extensions "book-reviews-deployment" created

```

6. Verify kubernetes deployments, services and istio gateway, virtual service are deployed. Each pod created by
the deployment would have 2 instances running in them. One is the service that was implement by ballerina while the 
other being the sidecar injected by istio: 
```bash
$> kubectl get pods
NAME                                       READY     STATUS    RESTARTS   AGE
book-details-deployment-5d689db7fc-vcvg8   2/2       Running   0          3m
book-reviews-deployment-d4b7f84dd-vkws4    2/2       Running   0          1m
book-shop-deployment-696b97d9d6-2xcmv      2/2       Running   0          2m

$> kubectl get svc
NAME             TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)    AGE
book-detail      ClusterIP   10.101.171.22   <none>        8080/TCP   5m
book-review      ClusterIP   10.102.1.162    <none>        7070/TCP   3m
bookshopep-svc   ClusterIP   10.109.214.55   <none>        9080/TCP   4m
kubernetes       ClusterIP   10.96.0.1       <none>        443/TCP    4d

$> kubectl get gateway
NAME                  AGE
bookshopep-istio-gw   5m

$> kubectl get virtualservice
NAME                  AGE
bookshopep-istio-vs   6m

```

7. Find the IP and port of the istio ingress gateway. See [here](https://istio.io/docs/tasks/traffic-management/ingress/#determining-the-ingress-ip-and-ports)
on determining the gateway URL. Once found, Execute the following commands to export the URL.
```bash
$> echo $GATEWAY
export GATEWAY_URL=$INGRESS_HOST:$INGRESS_PORT

```

8. Access the books by their IDs with curl commands:
```bash
$> curl http://${GATEWAY_URL}/book/B1
{"id":"B1", "details":{"author":"John Jonathan", "cost":10.0}, "reviews":"Review of book1"}

$> curl http://${GATEWAY_URL}/book/B2
{"id":"B2", "details":{"author":"Anne Anakin", "cost":15.0}, "reviews":"Review of book2"}

$> curl http://${GATEWAY_URL}/book/B3
{"id":"B3", "details":{"author":"Greg George", "cost":20.0}, "reviews":"(no reviews found)"}

$> curl http://${GATEWAY_URL}/book/B4
{"message":"book not found: B4"}
```

9. Undeploy sample:
```bash
kubectl delete -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample16/target/kubernetes/book.details
kubectl delete -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample16/target/kubernetes/book.shop
kubectl delete -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample16/target/kubernetes/book.reviews

```
