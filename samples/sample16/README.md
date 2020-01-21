## Sample16: Istio Gateway and Virtual Service generation

- This sample contains 4 microservices implementing a travel agency.
- Traffic is managed through an istio gateway and virtual service which gets generated through @istio:Gateway 
and @istio:VirtualService annotations. See the `travel_agency` module implementation for these annotations.
- Following are the microservices:  
    - `airline_reservation` module has a service performs airline reservations. 
    - `hotel_reservation` module has a service for making reservations at hotels.
    - `car_rental` module has a service that rents out cars.
    - `travel_agency` module has a service that communicates to `airline_reservation`, `hotel_reservation` and 
    `car_rental` service to make reservations and booking upon customer requests.
- Make sure that istio is installed correctly and that all pods and services of the istio-system are up and running. 
- Remove the nginx artifacts added in setting up of the tutorial. Run `kubectl delete -f nginx-ingress/namespaces/nginx-ingress.yaml -Rf nginx-ingress` 
from the `samples` folder.
See [here](https://istio.io/docs/setup/kubernetes/quick-start/) on how to install istio on kubernetes.
- Following files will be generated from building this sample.
    ``` 
    $> docker images
    airline_reservation:latest
    hotel_reservation:latest 
    car_rental:latest 
    travel_agency:latest
    
    $> tree target/
    target/
    ├── docker                                             
    │   ├── airline_reservation
    │   │       └── Dockerfile
    │   ├── car_rental
    │   │       └── Dockerfile
    │   ├── hotel_reservation
    │   │   ├── docker
    │   │   │   └── Dockerfile
    │   └── travel_agency
    │       ├── docker
    │       │   └── Dockerfile
    ├── kubernetes                                         
    │   ├── airline_reservation
    │   │   ├── airline-reservation-deployment
    │   │   │   ├── Chart.yaml
    │   │   │   └── templates
    │   │   │       └── airline_reservation.yaml
    │   │   ├── airline_reservation.yaml
    │   ├── car_rental
    │   │   ├── car-rental-deployment
    │   │   │   ├── Chart.yaml
    │   │   │   └── templates
    │   │   │       └── car_rental.yaml
    │   │   ├── car_rental.yaml
    │   ├── hotel_reservation
    │   │   ├── hotel-reservation-deployment
    │   │   │   ├── Chart.yaml
    │   │   │   └── templates
    │   │   │       └── hotel_reservation.yaml
    │   │   └── hotel_reservation.yaml
    │   └── travel_agency
    │       ├── travel-agency-deployment
    │       │   ├── Chart.yaml
    │       │   └── templates
    │       │       └── travel_agency.yaml
    │       └── travel_agency.yaml                               
    ├── balo                                               
        ├── airline_reservation-2019r3-any-1.0.0.balo    
        ├── car_rental-2019r3-any-1.0.0.balo
        ├── hotel_reservation-2019r3-any-1.0.0.balo          
        └── travel_agency-2019r3-any-1.0.0.balo                   
    ├── bin                                                
        ├── airline_reservation.jar  
        ├── car_rental.jar
        ├── hotel_reservation.jar 
        └── travel_agency.jar                                   
    └── caches                                             
        ├── bir_cache                                      
        │   ├── gogo                                       
        │   │   ├── airline_reservation                              
        │   │   |   └── 1.0.0 
        |   │   │   |   ├── airline_reservation.bir                        
        │   │   |   |   └── airline_reservation-testable.bir                     
        │   │   └── car_rental                                  
        │   │   |   └── 1.0.0 
        |   │   │   |   ├── car_rental.bir                        
        │   │   |   |   └── car_rental-testable.bir 
        │   │   ├── hotel_reservation                              
        │   │   |   └── 1.0.0 
        |   │   │   |   ├── hotel_reservation.bir                        
        │   │   |   |   └── hotel_reservation-testable.bir                     
        │   │   └── travel_agency                                  
        │   │   |   └── 1.0.0 
        |   │   │   |   ├── travel_agency.bir                        
        │   │   |   |   └── travel_agency-testable.bir                           
        └── jar_cache                                      
            ├── ballerina                                       
            |   |          
            |   |            
            |   └──                                
            |   |        
            |   |   
            ├── ballerinax
            |   |        
            |   |   
            └── gogo
                |        
                └──
    ```
### How to run:

1. Compile the ballerina project from `sample16` folder. Command to deploy kubernetes artifacts will be printed on build success.
```bash
$> ballerina build -a
Compiling source
        gogo/car_rental:1.0.0
        gogo/travel_agency:1.0.0
        gogo/hotel_reservation:1.0.0
        gogo/airline_reservation:1.0.0

Creating balos
        target/balo/car_rental-2019r3-any-1.0.0.balo
        target/balo/travel_agency-2019r3-any-1.0.0.balo
        target/balo/hotel_reservation-2019r3-any-1.0.0.balo
        target/balo/airline_reservation-2019r3-any-1.0.0.balo

Running tests
        gogo/airline_reservation:1.0.0
[ballerina/http] started HTTP/WS listener 0.0.0.0:8080
[ballerina/http] stopped HTTP/WS listener 0.0.0.0:8080
        1 passing
        0 failing
        0 skipped
        gogo/car_rental:1.0.0
[ballerina/http] started HTTP/WS listener 0.0.0.0:6060
[ballerina/http] stopped HTTP/WS listener 0.0.0.0:6060
        1 passing
        0 failing
        0 skipped
        gogo/hotel_reservation:1.0.0
[ballerina/http] started HTTP/WS listener 0.0.0.0:7070
[ballerina/http] stopped HTTP/WS listener 0.0.0.0:7070
        1 passing
        0 failing
        0 skipped
        gogo/travel_agency:1.0.0
[ballerina/http] started HTTP/WS listener 0.0.0.0:9090
[ballerina/http] started HTTP/WS listener 0.0.0.0:8080
[ballerina/http] started HTTP/WS listener 0.0.0.0:7070
[ballerina/http] started HTTP/WS listener 0.0.0.0:6060
[ballerina/http] stopped HTTP/WS listener 0.0.0.0:8080
[ballerina/http] stopped HTTP/WS listener 0.0.0.0:7070
[ballerina/http] stopped HTTP/WS listener 0.0.0.0:6060
[ballerina/http] stopped HTTP/WS listener 0.0.0.0:9090
        1 passing
        0 failing
        0 skipped

Generating executables
        target/bin/car_rental.jar
        target/bin/travel_agency.jar
        target/bin/hotel_reservation.jar
        target/bin/airline_reservation.jar

Generating artifacts...

        @kubernetes:Service                      - complete 1/1
        @kubernetes:Deployment                   - complete 1/1
        @kubernetes:Docker                       - complete 2/2 
        @kubernetes:Helm                         - complete 1/1

        Run the following command to deploy the Kubernetes artifacts: 
        kubectl apply -f /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample16/target/kubernetes/car_rental

        Run the following command to install the application using Helm: 
        helm install --name car-rental-deployment /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample16/target/kubernetes/car_rental/car-rental-deployment


Generating artifacts...

        @kubernetes:Service                      - complete 1/1
        @kubernetes:Deployment                   - complete 1/1
        @kubernetes:Docker                       - complete 2/2 
        @kubernetes:Helm                         - complete 1/1
        @istio:Gateway                           - complete 1/1
        @istio:VirtualService                    - complete 1/1

        Run the following command to deploy the Kubernetes artifacts: 
        kubectl apply -f /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample16/target/kubernetes/travel_agency

        Run the following command to install the application using Helm: 
        helm install --name travel-agency-deployment /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample16/target/kubernetes/travel_agency/travel-agency-deployment


Generating artifacts...

        @kubernetes:Service                      - complete 1/1
        @kubernetes:Deployment                   - complete 1/1
        @kubernetes:Docker                       - complete 2/2 
        @kubernetes:Helm                         - complete 1/1

        Run the following command to deploy the Kubernetes artifacts: 
        kubectl apply -f /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample16/target/kubernetes/hotel_reservation

        Run the following command to install the application using Helm: 
        helm install --name hotel-reservation-deployment /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample16/target/kubernetes/hotel_reservation/hotel-reservation-deployment


Generating artifacts...

        @kubernetes:Service                      - complete 1/1
        @kubernetes:Deployment                   - complete 1/1
        @kubernetes:Docker                       - complete 2/2 
        @kubernetes:Helm                         - complete 1/1

        Run the following command to deploy the Kubernetes artifacts: 
        kubectl apply -f /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample16/target/kubernetes/airline_reservation

        Run the following command to install the application using Helm: 
        helm install --name airline-reservation-deployment /Users/parkavi/Documents/Parkavi/BalKube/kubernetes/samples/sample16/target/kubernetes/airline_reservation/airline-reservation-deployment
```

2. Relevant jar files, Dockerfile, docker image, kubernetes and istio artifacts will be generated: 
```bash
$> tree target
target/
├── docker                                             
│   ├── airline_reservation
│   │       └── Dockerfile
│   ├── car_rental
│   │       └── Dockerfile
│   ├── hotel_reservation
│   │   ├── docker
│   │   │   └── Dockerfile
│   └── travel_agency
│       ├── docker
│       │   └── Dockerfile
├── kubernetes                                         
│   ├── airline_reservation
│   │   ├── airline-reservation-deployment
│   │   │   ├── Chart.yaml
│   │   │   └── templates
│   │   │       └── airline_reservation.yaml
│   │   ├── airline_reservation.yaml
│   ├── car_rental
│   │   ├── car-rental-deployment
│   │   │   ├── Chart.yaml
│   │   │   └── templates
│   │   │       └── car_rental.yaml
│   │   ├── car_rental.yaml
│   ├── hotel_reservation
│   │   ├── hotel-reservation-deployment
│   │   │   ├── Chart.yaml
│   │   │   └── templates
│   │   │       └── hotel_reservation.yaml
│   │   └── hotel_reservation.yaml
│   └── travel_agency
│       ├── travel-agency-deployment
│       │   ├── Chart.yaml
│       │   └── templates
│       │       └── travel_agency.yaml
│       └── travel_agency.yaml                               
├── balo                                               
    ├── airline_reservation-2019r3-any-1.0.0.balo    
    ├── car_rental-2019r3-any-1.0.0.balo
    ├── hotel_reservation-2019r3-any-1.0.0.balo          
    └── travel_agency-2019r3-any-1.0.0.balo                   
├── bin                                                
    ├── airline_reservation.jar  
    ├── car_rental.jar
    ├── hotel_reservation.jar 
    └── travel_agency.jar                                   
└── caches                                             
    ├── bir_cache                                      
    │   ├── gogo                                       
    │   │   ├── airline_reservation                              
    │   │   |   └── 1.0.0 
    |   │   │   |   ├── airline_reservation.bir                        
    │   │   |   |   └── airline_reservation-testable.bir                     
    │   │   └── car_rental                                  
    │   │   |   └── 1.0.0 
    |   │   │   |   ├── car_rental.bir                        
    │   │   |   |   └── car_rental-testable.bir 
    │   │   ├── hotel_reservation                              
    │   │   |   └── 1.0.0 
    |   │   │   |   ├── hotel_reservation.bir                        
    │   │   |   |   └── hotel_reservation-testable.bir                     
    │   │   └── travel_agency                                  
    │   │   |   └── 1.0.0 
    |   │   │   |   ├── travel_agency.bir                        
    │   │   |   |   └── travel_agency-testable.bir                           
    └── jar_cache                                      
        ├── ballerina                                       
        |   |          
        |   |            
        |   └──                                
        |   |        
        |   |   
        ├── ballerinax
        |   |        
        |   |   
        └── gogo
            |        
            └──

```

3. Verify the docker images are created:
```bash
$> docker images
REPOSITORY                                                       TAG                                        IMAGE ID            CREATED             SIZE
airline_reservation                                              latest                                     968622b45938        11 minutes ago      180MB
hotel_reservation                                                latest                                     7e6522d5e713        11 minutes ago      180MB
travel_agency                                                    latest                                     ff8888d83e86        11 minutes ago      180MB
car_rental                                                       latest                                     0b3b013df707        11 minutes ago      180MB
```

4. Enable istio-sidecar-injection for the default namespace if its not already set:
```bash
$> kubectl label namespace default istio-injection=enabled
namespace "default" labeled
```

5. Run the kubectl commands to deploy artifacts (Use the command printed on screen in step 1):
```bash
$> kubectl apply -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample16/target/kubernetes/airline_reservation
service/airline-reservation created
deployment.apps/airline-reservation-deployment created

$> kubectl apply -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample16/target/kubernetes/hotel_reservation
service/hotel-reservation created
deployment.apps/hotel-reservation-deployment created

$> kubectl apply -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample16/target/kubernetes/car_rental
service/car-rental created
deployment.apps/car-rental-deployment created

$> kubectl apply -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample16/target/kubernetes/travel_agency
service/travelagencyep-svc created
deployment.apps/travel-agency-deployment created
gateway.networking.istio.io/travelagencyep-istio-gw created
virtualservice.networking.istio.io/travelagencyep-istio-vs created

```

6. Verify kubernetes deployments, services and istio gateway, virtual service are deployed. Each pod created by
the deployment would have 2 instances running in them. One is the service that was implement by ballerina while the 
other being the sidecar injected by istio: 
```bash
$> kubectl get pods
NAME                                             READY   STATUS    RESTARTS   AGE
airline-reservation-deployment-987c5847b-hv5zs   1/1     Running   0          16m
car-rental-deployment-79444847f8-5zlhc           1/1     Running   0          16m
hotel-reservation-deployment-8445d9d5d8-nm2dn    1/1     Running   0          16m
travel-agency-deployment-5f4bcd6f8f-6zvgc        1/1     Running   0          16m

$> kubectl get svc
NAME                  TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)    AGE
airline-reservation   ClusterIP   10.98.141.251    <none>        8080/TCP   16m
car-rental            ClusterIP   10.99.129.188    <none>        6060/TCP   16m
hotel-reservation     ClusterIP   10.111.42.120    <none>        7070/TCP   16m
travelagencyep-svc    ClusterIP   10.100.172.128   <none>        9090/TCP   16m

$> kubectl get gateway
NAME                      CREATED AT
travelagencyep-istio-gw   17m

$> kubectl get virtualservice
NAME                      CREATED AT
travelagencyep-istio-vs   17m

```

7. Find the IP and port of the istio ingress gateway. See [here](https://istio.io/docs/tasks/traffic-management/ingress/#determining-the-ingress-ip-and-ports)
on determining the gateway URL. Once found, Execute the following commands to export the URL.
```bash
$> export GATEWAY_URL=$INGRESS_HOST:$INGRESS_PORT

```

8. Access the books by their IDs with curl commands:
```bash
$> curl -v -X POST -d '{"Name":"Bob", "ArrivalDate":"12-03-2018", "DepartureDate":"13-04-2018", "Preference":{"Airline":"Business", "Accommodation":"Air Conditioned", "Car":"Air Conditioned"}}' \
      -H "Content-Type:application/json" \
      "http://${GATEWAY_URL}/travel/arrange"
      
{"Message":"Congratulations! Your journey is ready!!"}
```

9. Undeploy sample:
```bash
$> kubectl delete -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample16/target/kubernetes/travel_agency
$> kubectl delete -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample16/target/kubernetes/car_rental
$> kubectl delete -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample16/target/kubernetes/hotel_reservation
$> kubectl delete -f /Users/hemikak/ballerina/dev/ballerinax/kubernetes/samples/sample16/target/kubernetes/airline_reservation
$> docker rmi airline_reservation hotel_reservation travel_agency car_rental

```
